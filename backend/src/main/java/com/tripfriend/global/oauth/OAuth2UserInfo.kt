package com.tripfriend.global.oauth

interface OAuth2UserInfo {
    val providerId: String?
    val provider: String?
    val email: String?
    val name: String?
}
