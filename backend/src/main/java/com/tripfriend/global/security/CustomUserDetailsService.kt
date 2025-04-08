package com.tripfriend.global.security

import com.tripfriend.domain.member.member.repository.MemberRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val memberRepository: MemberRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val member = memberRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("사용자를 찾을 수 없습니다.") }

        // 인증 시점에서 검증 상태 확인 (선택적)
        if (!member.verified) {
            throw UsernameNotFoundException("검증되지 않은 사용자입니다.")
        }

        return PrincipalDetails(member, null)  // OAuth2 인증에 필요한 attributes는 null로 설정
    }

    fun loadUserByOAuth2User(oAuth2User: OAuth2User): UserDetails {
        val member = memberRepository.findByUsername(oAuth2User.name)
            .orElseThrow { OAuth2AuthenticationException("Member not found") }

        // 검증 상태 확인 (선택적)
        if (!member.verified) {
            throw OAuth2AuthenticationException("검증되지 않은 사용자입니다.")
        }

        // OAuth2User의 attributes를 PrincipalDetails로 전달
        return PrincipalDetails(member, oAuth2User.attributes)
    }
}
