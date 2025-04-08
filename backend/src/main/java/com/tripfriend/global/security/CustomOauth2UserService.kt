package com.tripfriend.global.security

import com.tripfriend.domain.member.member.entity.AgeRange
import com.tripfriend.domain.member.member.entity.Gender
import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.entity.TravelStyle
import com.tripfriend.domain.member.member.repository.MemberRepository
import com.tripfriend.global.oauth.GoogleUserInfo
import com.tripfriend.global.oauth.KakaoUserInfo
import com.tripfriend.global.oauth.NaverUserInfo
import com.tripfriend.global.oauth.OAuth2UserInfo
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.LocalDateTime

@Service
class CustomOauth2UserService(
    private val memberRepository: MemberRepository
) : DefaultOAuth2UserService() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val oAuth2UserInfo: OAuth2UserInfo?

        // 액세스 토큰 얻기
        val accessToken = userRequest.accessToken.tokenValue
        println("Access Token: $accessToken")

        oAuth2UserInfo = when (userRequest.clientRegistration.registrationId) {
            "google" -> GoogleUserInfo(oAuth2User.attributes)
            "naver" -> NaverUserInfo(oAuth2User.attributes["response"] as Map<String, Any>)
            "kakao" -> KakaoUserInfo(oAuth2User.attributes)
            else -> {
                logger.info("error")
                null
            }
        }

        val randomPassword = generateRandomPassword(15)  // 15자리 랜덤 비밀번호 생성

        val provider = oAuth2UserInfo?.provider ?: throw OAuth2AuthenticationException("Provider not found")
        val providerId = oAuth2UserInfo.providerId
        val username = "${provider}_${providerId}"
        val password = bCryptPasswordEncoder.encode(randomPassword)
        val email = oAuth2UserInfo.email ?: "${username}@noemail.com" // Null일 경우 기본값 제공
        val rating = 0.0
        val authority = "USER"
        val verified = true

        // 사용자 조회
        val memberEntity = memberRepository.findByUsername(username)

        if (memberEntity.isEmpty) {
            println("OAuth 로그인이 최초입니다.")

            // 새로운 Member 객체 생성 (기본 정보만 저장)
            val newMember = Member(
                username = username,
                email = email,
                password = password,
                nickname = username,
                gender = Gender.UNKNOWN,
                ageRange = AgeRange.UNKNOWN,
                travelStyle = TravelStyle.UNKNOWN,
                rating = rating,
                authority = authority,
                verified = verified,
                provider = provider,
                providerId = providerId,
                deleted = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            // 새로운 회원 DB에 저장
            memberRepository.save(newMember)

            return PrincipalDetails(newMember, oAuth2User.attributes)
        } else {
            println("로그인을 이미 한 적이 있습니다. 당신은 자동 회원가입이 되어 있습니다.")
            // 이미 존재하는 사용자라면 기본 정보를 기반으로 로그인 처리
        }

        // PrincipalDetails 반환
        return PrincipalDetails(
            memberEntity.orElseThrow { OAuth2AuthenticationException("Member not found") },
            oAuth2User.attributes
        )
    }

    companion object {
        private val bCryptPasswordEncoder = BCryptPasswordEncoder()

        // 랜덤 비밀번호 생성
        private fun generateRandomPassword(length: Int): String {
            val password = StringBuilder(length)
            val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            val random = SecureRandom()

            repeat(length) {
                val index = random.nextInt(characters.length)
                password.append(characters[index])
            }
            return password.toString()
        }
    }
}