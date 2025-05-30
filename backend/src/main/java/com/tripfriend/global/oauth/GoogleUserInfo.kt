package com.tripfriend.global.oauth

class GoogleUserInfo(private val attributes: Map<String, Any>) : OAuth2UserInfo {

    override val providerId: String
        get() = attributes["sub"] as String

    override val provider: String
        get() = "google"

    override val email: String
        get() = attributes["email"] as String

    override val name: String
        get() = attributes["name"] as String
}