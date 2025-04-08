package com.tripfriend.global.security

import com.tripfriend.domain.member.member.entity.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

class PrincipalDetails(
    private val member: Member, // OAuth2User에서 가져온 추가 정보
    private val attributes: Map<String, Any>?
) : UserDetails, OAuth2User {

    override fun getUsername(): String {
        return member.username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun getPassword(): String {
        return member.password
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return AuthorityUtils.createAuthorityList(member.authority)
    }

    override fun getAttributes(): Map<String, Any>? {
        return attributes
    }

    // OAuth2User 인터페이스 구현
    override fun getName(): String {
        return attributes?.get("name") as? String ?: username
    }

    fun isVerified(): Boolean {
        return member.verified  // 이메일 인증 여부를 체크
    }

    fun getMember(): Member {
        return member
    }
}
