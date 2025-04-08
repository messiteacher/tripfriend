package com.tripfriend.domain.recruit.apply.dto

import com.tripfriend.domain.recruit.apply.entity.Apply
import java.time.LocalDateTime

data class ApplyResponseDto(
    val applyId: Long,
    val memberId: Long,
    val memberProfileImage: String,
    val memberNickname: String,
    val content: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    constructor(apply: Apply) : this(
        applyId = apply.applyId!!,
        memberId = apply.member.id!!,
        memberProfileImage = apply.member.profileImage ?: "",
        memberNickname = apply.member.nickname,
        content = apply.content,
        createdAt = apply.createdAt,
        updatedAt = apply.updatedAt
    )
}
