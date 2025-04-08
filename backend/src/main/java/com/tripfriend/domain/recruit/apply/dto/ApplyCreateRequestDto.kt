package com.tripfriend.domain.recruit.apply.dto

import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.recruit.apply.entity.Apply
import com.tripfriend.domain.recruit.recruit.entity.Recruit

data class ApplyCreateRequestDto(
    val content: String
) {
    fun toEntity(member: Member, recruit: Recruit): Apply {
        return Apply(
            member = member,
            recruit = recruit,
            content = content
        )
    }
}
