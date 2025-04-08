package com.tripfriend.global.oauth

class KakaoUserInfo(private val attributes: Map<String, Any>) : OAuth2UserInfo {

    override val providerId: String
        get() = attributes["id"].toString()

    override val provider: String
        get() = "kakao"

    override val email: String?
        get() {
            val `object` = attributes["kakao_account"]
            val accountMap = `object` as LinkedHashMap<*, *>?
            return accountMap!!["email"] as String?
        }

    override val name: String?
        get() = attributes["name"] as String?
}
