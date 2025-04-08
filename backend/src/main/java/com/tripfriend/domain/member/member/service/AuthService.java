package com.tripfriend.domain.member.member.service;

import com.tripfriend.domain.member.member.dto.AuthResponseDto;
import com.tripfriend.domain.member.member.dto.LoginRequestDto;
import com.tripfriend.domain.member.member.dto.TokenInfoDto;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import com.tripfriend.global.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    // 로그인 처리
    public AuthResponseDto login(LoginRequestDto loginRequestDto, HttpServletResponse response) {

        // 회원 인증 처리
        Member member = memberRepository.findByUsername(loginRequestDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호를 확인하세요.");
        }

        // 계정이 삭제된 상태인 경우
        if (member.getDeleted()) {
            // 복구 가능한 경우
            if (member.canBeRestored()) {
                // 복구 가능한 경우에만 특별한 토큰 발급
                String accessToken = jwtUtil.generateAccessToken(member.getUsername(), member.getAuthority(), member.getVerified(), true);
                String refreshToken = jwtUtil.generateRefreshToken(member.getUsername(), member.getAuthority(), member.getVerified(), true);

                // 액세스 토큰과 리프레시 토큰을 쿠키에 저장
                addCookie(response, "accessToken", accessToken, 10 * 60); // 10분
                addCookie(response, "refreshToken", refreshToken, 60 * 60 * 24); // 1일

                // 토큰 정보도 응답에 포함
                return new AuthResponseDto(accessToken, refreshToken, true);
            } else {
                throw new RuntimeException("영구 삭제된 계정입니다. 새로운 계정으로 가입해주세요.");
            }
        }

        // 토큰 생성 (Redis에 저장됨)
        String accessToken = jwtUtil.generateAccessToken(member.getUsername(), member.getAuthority(), member.getVerified());
        String refreshToken = jwtUtil.generateRefreshToken(member.getUsername(), member.getAuthority(), member.getVerified());

        // 액세스 토큰과 리프레시 토큰을 쿠키에 저장
        addCookie(response, "accessToken", accessToken, 30 * 60); // 30분
        addCookie(response, "refreshToken", refreshToken, 60 * 60 * 24 * 7); // 7일

        // 토큰 정보도 응답에 포함
        return new AuthResponseDto(accessToken, refreshToken, false);
    }


    // 로그아웃 처리 - Redis 블랙리스트 활용
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 액세스 토큰 추출
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    // 액세스 토큰을 블랙리스트에 추가
                    String accessToken = cookie.getValue();
                    if (accessToken != null && !accessToken.isEmpty()) {
                        String username = jwtUtil.extractUsername(accessToken);

                        // 액세스 토큰 블랙리스트에 추가
                        jwtUtil.addToBlacklist(accessToken);

                        // Redis에서 리프레시 토큰 삭제
                        redisTemplate.delete("refresh:" + username);
                    }
                }
            }
        }

        // 쿠키에서 액세스 토큰 삭제
        addCookie(response, "accessToken", null, 0);
    }

    // 리프레시 토큰으로 새로운 액세스 토큰 발급 - Redis 검증 추가
    public AuthResponseDto refreshToken(String accessToken, HttpServletResponse response) {
        // 만료된 액세스 토큰에서 사용자명 추출
        String username = jwtUtil.extractUsername(accessToken);

        // Redis에서 해당 사용자의 리프레시 토큰 확인
        String storedRefreshToken = jwtUtil.getStoredRefreshToken(username);

        if (storedRefreshToken == null) {
            throw new RuntimeException("저장된 리프레시 토큰이 없습니다");
        }

        // 리프레시 토큰 검증
        if (jwtUtil.isTokenExpired(storedRefreshToken)) {
            throw new RuntimeException("리프레시 토큰이 만료되었습니다");
        }

        // 액세스 토큰에서 필요한 정보 추출
        String authority = jwtUtil.extractAuthority(accessToken);
        boolean isVerified = jwtUtil.extractVerified(accessToken);
        boolean isDeleted = jwtUtil.isDeletedAccount(accessToken);

        // 새로운 액세스 토큰 생성
        String newAccessToken;
        if (isDeleted) {
            newAccessToken = jwtUtil.generateAccessToken(username, authority, isVerified, true);
            addCookie(response, "accessToken", newAccessToken, 10 * 60); // 10분
        } else {
            newAccessToken = jwtUtil.generateAccessToken(username, authority, isVerified);
            addCookie(response, "accessToken", newAccessToken, 30 * 60); // 30분
        }

        String newRefreshToken = storedRefreshToken;

        // 리프레시 토큰 재발급 필요성 확인
        if (isRefreshTokenNeedsRenewal(storedRefreshToken)) {
            if (isDeleted) {
                newRefreshToken = jwtUtil.generateRefreshToken(username, authority, isVerified, true);
                addCookie(response, "refreshToken", newRefreshToken, 60 * 60 * 24); // 1일
            } else {
                newRefreshToken = jwtUtil.generateRefreshToken(username, authority, isVerified);
                addCookie(response, "refreshToken", newRefreshToken, 60 * 60 * 24 * 7); // 7일
            }
        }

        // 토큰 정보도 응답에 포함
        return new AuthResponseDto(newAccessToken, newRefreshToken, isDeleted);
    }

    // 리프레시 토큰 갱신 필요 여부 확인
    private boolean isRefreshTokenNeedsRenewal(String refreshToken) {
        try {
            Claims claims = jwtUtil.getClaims(refreshToken);
            Date expiration = claims.getExpiration();

            // 만료까지 남은 시간 계산 (밀리초)
            long timeToExpire = expiration.getTime() - System.currentTimeMillis();

            // 만료 기간의 30% 이하로 남았으면 갱신
            return timeToExpire < (jwtUtil.getRefreshTokenExpiration() * 0.3);
        } catch (Exception e) {
            return true;
        }
    }

    // 로그인된 사용자의 정보를 반환하는 메서드
    public Member getLoggedInMember(String token) {

        // 토큰에서 "Bearer "를 제거
        String extractedToken = token.replace("Bearer ", "");

        // 토큰이 블랙리스트에 있는지 확인
        if (jwtUtil.isTokenBlacklisted(extractedToken)) {
            throw new RuntimeException("로그아웃된 토큰입니다.");
        }

        // Redis에 저장된 토큰과 일치하는지 확인
        String username = jwtUtil.extractUsername(extractedToken);
        if (!jwtUtil.validateAccessTokenInRedis(username, extractedToken)) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        // 토큰에서 사용자 정보 추출
        TokenInfoDto tokenInfo = extractTokenInfo(extractedToken);
        return memberRepository.findByUsername(tokenInfo.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // 토큰에서 사용자 정보를 추출하는 메서드
    public TokenInfoDto extractTokenInfo(String token) {

        if (jwtUtil.isTokenExpired(token)) {
            throw new RuntimeException("만료된 토큰입니다.");
        }

        String username = jwtUtil.extractUsername(token);
        String authority = jwtUtil.extractAuthority(token);
        boolean isVerified = jwtUtil.extractVerified(token);

        return new TokenInfoDto(username, authority, isVerified);
    }

    // 쿠키 생성 메서드 (만료 시간 설정)
    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge); // 만료 시간 설정
        cookie.setHttpOnly(true); // 자바스크립트에서 접근 불가
        cookie.setSecure(true); // HTTPS 환경에서만 사용 가능
        response.addCookie(cookie);
    }
}
