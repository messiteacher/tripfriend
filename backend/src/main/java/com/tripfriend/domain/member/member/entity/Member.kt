package com.tripfriend.domain.member.member.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Member(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    val id: Long? = null,

    @Column(name = "username", nullable = false, length = 100, unique = true)
    var username: String,

    @Column(name = "email", nullable = false)
    var email: String,

    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "nickname", nullable = false)
    var nickname: String,

    @Column(name = "profile_image")
    var profileImage: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    var gender: Gender,

    @Enumerated(EnumType.STRING)
    @Column(name = "age_range", nullable = false)
    var ageRange: AgeRange,

    @Enumerated(EnumType.STRING)
    @Column(name = "travel_style", nullable = false)
    var travelStyle: TravelStyle,

    @Lob
    @Column(name = "about_me")
    var aboutMe: String? = null,

    @Column(name = "rating", nullable = false)
    var rating: Double,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "authority", nullable = false)
    var authority: String,

    @Column(name = "verified", nullable = false)
    var verified: Boolean, // 이메일 인증 여부

    // 소셜 로그인 식별
    @Column(name = "provider")
    var provider: String? = null,

    @Column(name = "provider_id")
    var providerId: String? = null,

    // soft delete 여부
    @Column(name = "deleted")
    var deleted: Boolean = false,

    // soft delete 된 날짜
    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
) {

    @PrePersist
    private fun onCreate() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    private fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    // 삭제 취소 가능 여부 확인
    fun canBeRestored(): Boolean {
        // 삭제되지 않았거나 삭제 시간이 기록되지 않은 경우
        if (!deleted || deletedAt == null) {
            return false
        }

        // 삭제 후 30일 이내인지 확인
        val restoreDeadline = deletedAt!!.plusDays(30)
        return LocalDateTime.now().isBefore(restoreDeadline)
    }
}
