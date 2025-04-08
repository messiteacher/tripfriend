package com.tripfriend.domain.recruit.recruit.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.place.place.entity.Place
import com.tripfriend.domain.recruit.recruit.entity.Recruit
import com.tripfriend.domain.recruit.recruit.entity.TravelStyle
import java.time.LocalDate

data class
RecruitRequestDto(
    val placeId: Long,
    val title: String,
    val content: String,
    @JsonProperty("isClosed")
    val isClosed: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val travelStyle: TravelStyle,
    val sameGender: Boolean,
    val sameAge: Boolean,
    val budget: Int = 0,
    val groupSize: Int = 2
) {
    fun toEntity(member: Member, place: Place): Recruit {
        return Recruit(
            member = member,
            place = place,
            title = title,
            content = content,
            isClosed = isClosed,
            startDate = startDate,
            endDate = endDate,
            travelStyle = travelStyle,
            sameGender = sameGender,
            sameAge = sameAge,
            budget = budget,
            groupSize = groupSize
        )
    }
}
