package com.tripfriend.global.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${custom.jwt.secret-key}")
    private String secretKey;

    @Value("${custom.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${custom.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REDIS_ACCESS_TOKEN_PREFIX = "access:";
    private static final String REDIS_REFRESH_TOKEN_PREFIX = "refresh:";
    private static final String REDIS_BLACKLIST_PREFIX = "blacklist:";

    // 액세스 토큰 생성 - Redis에 저장하고 클라이언트에 반환
    public String generateAccessToken(String username, String authority, boolean verified) {
        String token = generateToken(username, authority, verified, accessTokenExpiration);

        // Redis에 액세스 토큰 저장
        redisTemplate.opsForValue().set(
                REDIS_ACCESS_TOKEN_PREFIX + username,
                token,
                accessTokenExpiration,
                TimeUnit.MILLISECONDS
        );

        return token;
    }

    // 삭제된 계정용 액세스 토큰 생성
    public String generateAccessToken(String username, String authority, boolean verified, boolean deleted) {
        String token = generateToken(username, authority, verified, deleted, accessTokenExpiration);

        // Redis에 액세스 토큰 저장 (짧은 유효기간)
        redisTemplate.opsForValue().set(
                REDIS_ACCESS_TOKEN_PREFIX + username,
                token,
                10 * 60 * 1000, // 10분
                TimeUnit.MILLISECONDS
        );

        return token;
    }

    // 리프레시 토큰 생성 - Redis에만 저장
    public String generateRefreshToken(String username, String authority, boolean verified) {
        String refreshToken = generateToken(username, authority, verified, refreshTokenExpiration);

        // Redis에 리프레시 토큰 저장
        redisTemplate.opsForValue().set(
                REDIS_REFRESH_TOKEN_PREFIX + username,
                refreshToken,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS
        );

        return refreshToken;
    }

    // 삭제된 계정용 리프레시 토큰 생성
    public String generateRefreshToken(String username, String authority, boolean verified, boolean deleted) {
        String refreshToken = generateToken(username, authority, verified, deleted, refreshTokenExpiration);

        // 복구 가능한 삭제된 계정용 리프레시 토큰은 짧은 시간만 유효하게 설정 (10분)
        redisTemplate.opsForValue().set(
                REDIS_REFRESH_TOKEN_PREFIX + username,
                refreshToken,
                10 * 60 * 1000, // 10분
                TimeUnit.MILLISECONDS
        );

        return refreshToken;
    }

    // 공통 토큰 생성 메서드
    private String generateToken(String username, String authority, boolean verified, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        Map<String, Object> claims = new HashMap<>();
        claims.put("authority", authority);
        claims.put("verified", verified);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .addClaims(claims)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 소프트딜리트 정보가 포함된 토큰 생성 메서드
    private String generateToken(String username, String authority, boolean verified, boolean deleted, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        Map<String, Object> claims = new HashMap<>();
        claims.put("authority", authority);
        claims.put("verified", verified);
        claims.put("deleted", deleted); // 삭제 여부 추가

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .addClaims(claims)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 토큰 블랙리스트에 추가 (로그아웃 시 사용)
    public void addToBlacklist(String token) {
        // 토큰의 남은 유효 시간 계산
        long expiration = getClaims(token).getExpiration().getTime();
        long now = System.currentTimeMillis();
        long ttl = expiration - now;

        if (ttl > 0) {
            // 블랙리스트에 토큰 추가 (만료 시간까지만 저장)
            redisTemplate.opsForValue().set(
                    REDIS_BLACKLIST_PREFIX + token,
                    "logout",
                    ttl,
                    TimeUnit.MILLISECONDS
            );

            // 사용자의 액세스 토큰도 Redis에서 삭제
            String username = extractUsername(token);
            redisTemplate.delete(REDIS_ACCESS_TOKEN_PREFIX + username);
        }
    }

    // Redis에서 리프레시 토큰 검증
    public boolean validateRefreshTokenInRedis(String username, String refreshToken) {
        String storedToken = redisTemplate.opsForValue().get(REDIS_REFRESH_TOKEN_PREFIX + username);
        return refreshToken.equals(storedToken);
    }

    // Redis에서 액세스 토큰 검증
    public boolean validateAccessTokenInRedis(String username, String accessToken) {
        String storedToken = redisTemplate.opsForValue().get(REDIS_ACCESS_TOKEN_PREFIX + username);
        return accessToken.equals(storedToken);
    }

    // 토큰이 블랙리스트에 있는지 확인
    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(REDIS_BLACKLIST_PREFIX + token));
    }

    // 기존 토큰 관련 메서드들은 그대로 유지
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String extractAuthority(String token) {
        return getClaims(token).get("authority", String.class);
    }

    public boolean extractVerified(String token) {
        return getClaims(token).get("verified", Boolean.class);
    }

    public boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // 토큰 유효성 검증 (블랙리스트 확인 및 Redis 검증 추가)
    public boolean validateToken(String token, String username) {
        return (username.equals(extractUsername(token)) &&
                !isTokenExpired(token) &&
                !isTokenBlacklisted(token) &&
                validateAccessTokenInRedis(username, token));
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS512.getJcaName());
    }

    public boolean isDeletedAccount(String token) {
        try {
            Claims claims = getClaims(token);
            Object deletedClaim = claims.get("deleted");
            return deletedClaim != null && Boolean.TRUE.equals(deletedClaim);
        } catch (Exception e) {
            return false;
        }
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    // 액세스 토큰 키 값 조회를 위한 메서드
    public String getStoredAccessToken(String username) {
        return redisTemplate.opsForValue().get(REDIS_ACCESS_TOKEN_PREFIX + username);
    }

    // 리프레시 토큰 키 값 조회를 위한 메서드
    public String getStoredRefreshToken(String username) {
        return redisTemplate.opsForValue().get(REDIS_REFRESH_TOKEN_PREFIX + username);
    }
}
