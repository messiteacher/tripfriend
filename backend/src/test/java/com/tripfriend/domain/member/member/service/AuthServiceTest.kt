package com.tripfriend.domain.member.member.service

import com.tripfriend.domain.member.member.dto.LoginRequestDto
import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.repository.MemberRepository
import com.tripfriend.global.util.JwtUtil
import io.jsonwebtoken.Claims
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private lateinit var jwtUtil: JwtUtil

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Mock
    private lateinit var valueOperations: ValueOperations<String, String>

    @Mock
    private lateinit var httpServletResponse: HttpServletResponse

    @Mock
    private lateinit var httpServletRequest: HttpServletRequest

    @InjectMocks
    private lateinit var authService: AuthService

    private val username = "testuser"
    private val password = "password123"
    private val encodedPassword = "encodedPassword123"
    private val accessToken = "access.token.value"
    private val refreshToken = "refresh.token.value"

    @Test
    fun `로그인 성공 테스트`() {
        // Given
        val testMember = mock(Member::class.java)
        `when`(testMember.username).thenReturn(username)
        `when`(testMember.password).thenReturn(encodedPassword)
        `when`(testMember.authority).thenReturn("ROLE_USER")
        `when`(testMember.verified).thenReturn(true)
        `when`(testMember.deleted).thenReturn(false)

        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)

        val loginRequestDto = LoginRequestDto(username, password)
        `when`(memberRepository.findByUsername(username)).thenReturn(Optional.of(testMember))
        `when`(passwordEncoder.matches(password, encodedPassword)).thenReturn(true)
        `when`(jwtUtil.generateAccessToken(eq(username), eq("ROLE_USER"), eq(true))).thenReturn(accessToken)
        `when`(jwtUtil.generateRefreshToken(eq(username), eq("ROLE_USER"), eq(true))).thenReturn(refreshToken)

        // When
        val result = authService.login(loginRequestDto, httpServletResponse)

        // Then
        assertNotNull(result)
        assertEquals(accessToken, result.accessToken)
        assertEquals(refreshToken, result.refreshToken)
        assertFalse(result.isDeletedAccount)
        verify(httpServletResponse, times(2)).addCookie(any(Cookie::class.java))
    }

    @Test
    fun `존재하지 않는 회원 로그인 시 예외 발생 테스트`() {
        // Given
        val loginRequestDto = LoginRequestDto(username, password)
        `when`(memberRepository.findByUsername(username)).thenReturn(Optional.empty())

        // Then
        assertThrows<UsernameNotFoundException> {
            // When
            authService.login(loginRequestDto, httpServletResponse)
        }
    }

    @Test
    fun `비밀번호 불일치 로그인 시 예외 발생 테스트`() {
        // Given
        val testMember = mock(Member::class.java)
        `when`(testMember.password).thenReturn(encodedPassword)

        val loginRequestDto = LoginRequestDto(username, "wrongPassword")
        `when`(memberRepository.findByUsername(username)).thenReturn(Optional.of(testMember))
        `when`(passwordEncoder.matches("wrongPassword", encodedPassword)).thenReturn(false)

        // Then
        assertThrows<RuntimeException> {
            // When
            authService.login(loginRequestDto, httpServletResponse)
        }
    }

    @Test
    fun `삭제된 계정 복구 가능할 경우 로그인 성공 테스트`() {
        // Given
        val testMember = mock(Member::class.java)
        `when`(testMember.username).thenReturn(username)
        `when`(testMember.password).thenReturn(encodedPassword)
        `when`(testMember.authority).thenReturn("ROLE_USER")
        `when`(testMember.verified).thenReturn(true)
        `when`(testMember.deleted).thenReturn(true)
        `when`(testMember.canBeRestored()).thenReturn(true)

        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)

        val loginRequestDto = LoginRequestDto(username, password)
        `when`(memberRepository.findByUsername(username)).thenReturn(Optional.of(testMember))
        `when`(passwordEncoder.matches(password, encodedPassword)).thenReturn(true)
        `when`(jwtUtil.generateAccessToken(eq(username), eq("ROLE_USER"), eq(true), eq(true))).thenReturn(accessToken)
        `when`(jwtUtil.generateRefreshToken(eq(username), eq("ROLE_USER"), eq(true), eq(true))).thenReturn(refreshToken)

        // When
        val result = authService.login(loginRequestDto, httpServletResponse)

        // Then
        assertNotNull(result)
        assertEquals(accessToken, result.accessToken)
        assertEquals(refreshToken, result.refreshToken)
        assertTrue(result.isDeletedAccount)
        verify(httpServletResponse, times(2)).addCookie(any(Cookie::class.java))
    }

    @Test
    fun `삭제된 계정 복구 불가능할 경우 예외 발생 테스트`() {
        // Given
        val testMember = mock(Member::class.java)
        `when`(testMember.username).thenReturn(username)
        `when`(testMember.password).thenReturn(encodedPassword)
        `when`(testMember.deleted).thenReturn(true)
        `when`(testMember.canBeRestored()).thenReturn(false)

        val loginRequestDto = LoginRequestDto(username, password)
        `when`(memberRepository.findByUsername(username)).thenReturn(Optional.of(testMember))
        `when`(passwordEncoder.matches(password, encodedPassword)).thenReturn(true)

        // Then
        assertThrows<RuntimeException> {
            // When
            authService.login(loginRequestDto, httpServletResponse)
        }
    }

    @Test
    fun `로그아웃 성공 테스트`() {
        // Given
        val cookies = arrayOf(Cookie("accessToken", accessToken))
        `when`(httpServletRequest.cookies).thenReturn(cookies)
        `when`(jwtUtil.extractUsername(accessToken)).thenReturn(username)
        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)

        // When
        authService.logout(httpServletRequest, httpServletResponse)

        // Then
        verify(jwtUtil).addToBlacklist(accessToken)
        verify(redisTemplate).delete("refresh:$username")
        verify(httpServletResponse).addCookie(any(Cookie::class.java))
    }

    @Test
    fun `토큰 갱신 성공 테스트`() {
        // Given
        val newAccessToken = "new.access.token"
        `when`(jwtUtil.extractUsername(accessToken)).thenReturn(username)
        `when`(jwtUtil.getStoredRefreshToken(username)).thenReturn(refreshToken)
        `when`(jwtUtil.isTokenExpired(refreshToken)).thenReturn(false)
        `when`(jwtUtil.extractAuthority(accessToken)).thenReturn("ROLE_USER")
        `when`(jwtUtil.extractVerified(accessToken)).thenReturn(true)
        `when`(jwtUtil.isDeletedAccount(accessToken)).thenReturn(false)
        `when`(jwtUtil.generateAccessToken(eq(username), eq("ROLE_USER"), eq(true))).thenReturn(newAccessToken)

        // Mock claims, expiration 확인
        val mockClaims = mock(Claims::class.java)
        val expirationDate = Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 6)
        `when`(mockClaims.expiration).thenReturn(expirationDate)
        `when`(jwtUtil.getClaims(refreshToken)).thenReturn(mockClaims)
        `when`(jwtUtil.getRefreshTokenExpiration()).thenReturn(1000L * 60 * 60 * 24 * 7)

        // When
        val result = authService.refreshToken(accessToken, httpServletResponse)

        // Then
        assertNotNull(result)
        assertEquals(newAccessToken, result.accessToken)
        assertEquals(refreshToken, result.refreshToken)
        assertFalse(result.isDeletedAccount)
        verify(httpServletResponse).addCookie(any(Cookie::class.java))
    }

    @Test
    fun `리프레시 토큰 없을 경우 예외 발생 테스트`() {
        // Given
        `when`(jwtUtil.extractUsername(accessToken)).thenReturn(username)
        `when`(jwtUtil.getStoredRefreshToken(username)).thenReturn(null)

        // Then
        assertThrows<RuntimeException> {
            // When
            authService.refreshToken(accessToken, httpServletResponse)
        }
    }

    @Test
    fun `만료된 리프레시 토큰으로 갱신 시 예외 발생 테스트`() {
        // Given
        `when`(jwtUtil.extractUsername(accessToken)).thenReturn(username)
        `when`(jwtUtil.getStoredRefreshToken(username)).thenReturn(refreshToken)
        `when`(jwtUtil.isTokenExpired(refreshToken)).thenReturn(true)

        // Then
        assertThrows<RuntimeException> {
            // When
            authService.refreshToken(accessToken, httpServletResponse)
        }
    }

    @Test
    fun `토큰에서 로그인된 사용자 정보 추출 성공 테스트`() {
        // Given
        val testMember = mock(Member::class.java)
        val token = "Bearer $accessToken"
        `when`(jwtUtil.isTokenBlacklisted(accessToken)).thenReturn(false)
        `when`(jwtUtil.extractUsername(accessToken)).thenReturn(username)
        `when`(jwtUtil.validateAccessTokenInRedis(username, accessToken)).thenReturn(true)
        `when`(jwtUtil.isTokenExpired(accessToken)).thenReturn(false)
        `when`(jwtUtil.extractAuthority(accessToken)).thenReturn("ROLE_USER")
        `when`(jwtUtil.extractVerified(accessToken)).thenReturn(true)
        `when`(memberRepository.findByUsername(username)).thenReturn(Optional.of(testMember))
        `when`(testMember.username).thenReturn(username)

        // When
        val result = authService.getLoggedInMember(token)

        // Then
        assertNotNull(result)
        assertEquals(username, result.username)
    }

    @Test
    fun `블랙리스트 토큰으로 사용자 정보 추출 시 예외 발생 테스트`() {
        // Given
        val token = "Bearer $accessToken"
        `when`(jwtUtil.isTokenBlacklisted(accessToken)).thenReturn(true)

        // Then
        assertThrows<RuntimeException> {
            // When
            authService.getLoggedInMember(token)
        }
    }

    @Test
    fun `유효하지 않은 토큰으로 사용자 정보 추출 시 예외 발생 테스트`() {
        // Given
        val token = "Bearer $accessToken"
        `when`(jwtUtil.isTokenBlacklisted(accessToken)).thenReturn(false)
        `when`(jwtUtil.extractUsername(accessToken)).thenReturn(username)
        `when`(jwtUtil.validateAccessTokenInRedis(username, accessToken)).thenReturn(false)

        // Then
        org.junit.jupiter.api.assertThrows<RuntimeException> {
            // When
            authService.getLoggedInMember(token)
        }
    }

    @Test
    fun `만료된 토큰으로 정보 추출 시 예외 발생 테스트`() {
        // Given
        `when`(jwtUtil.isTokenExpired(accessToken)).thenReturn(true)

        // Then
        assertThrows<RuntimeException> {
            // When
            authService.extractTokenInfo(accessToken)
        }
    }

    @Test
    fun `토큰에서 정보 추출 성공 테스트`() {
        // Given
        `when`(jwtUtil.isTokenExpired(accessToken)).thenReturn(false)
        `when`(jwtUtil.extractUsername(accessToken)).thenReturn(username)
        `when`(jwtUtil.extractAuthority(accessToken)).thenReturn("ROLE_USER")
        `when`(jwtUtil.extractVerified(accessToken)).thenReturn(true)

        // When
        val result = authService.extractTokenInfo(accessToken)

        // Then
        assertNotNull(result)
        assertEquals(username, result.username)
        assertEquals("ROLE_USER", result.authority)
        assertTrue(result.isVerified)
    }
}