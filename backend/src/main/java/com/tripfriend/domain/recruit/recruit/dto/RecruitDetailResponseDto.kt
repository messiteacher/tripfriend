package com.tripfriend.domain.recruit.recruit.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.tripfriend.domain.member.member.entity.AgeRange
import com.tripfriend.domain.member.member.entity.Gender
import com.tripfriend.domain.recruit.apply.dto.ApplyResponseDto
import com.tripfriend.domain.recruit.recruit.entity.Recruit
import java.time.LocalDate
import java.time.LocalDateTime

data class RecruitDetailResponseDto(
    val recruitId: Long,
    val memberId: Long?,
    val memberProfileImage: String?,
    val memberNickname: String,
    val genderRestriction: String,
    val ageRestriction: String,
    val placeId: Long,
    val placeCityName: String?,
    val placePlaceName: String?,
    val title: String,
    val content: String,
    @JsonProperty("isClosed")
    val isClosed: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val travelStyle: String,
    val sameGender: Boolean,
    val sameAge: Boolean,
    val budget: Int = 0,
    val groupSize: Int = 2,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val applies: List<ApplyResponseDto> = emptyList()
) {
        companion object {
            fun from(recruit: Recruit): RecruitDetailResponseDto = RecruitDetailResponseDto(
            recruitId = recruit.recruitId!!,
            memberId = recruit.member.id,
            memberProfileImage = recruit.member.profileImage,
            memberNickname = recruit.member.nickname,
            genderRestriction = if (recruit.sameGender && recruit.member.gender != Gender.UNKNOWN) {
                if (recruit.member.gender == Gender.MALE) "남자만" else "여자만"
            } else {
                if (recruit.member.gender == Gender.UNKNOWN) "알 수 없음" else "모든 성별"
            },
            ageRestriction = if (recruit.sameAge) {
                when (recruit.member.ageRange) {
                    AgeRange.TEENS -> "10대만"
                    AgeRange.TWENTIES -> "20대만"
                    AgeRange.THIRTIES -> "30대만"
                    AgeRange.FORTIES_PLUS -> "40대 이상만"
                    else -> "알 수 없음"
                }
            } else {
                "모든 연령대"
            },
            placeId = recruit.place.id!!,
            placeCityName = recruit.place.cityName,
            placePlaceName = recruit.place.placeName,
            title = recruit.title,
            content = recruit.content,
            isClosed = recruit.isClosed,
            startDate = recruit.startDate,
            endDate = recruit.endDate,
            travelStyle = recruit.travelStyle.koreanName,
            sameGender = recruit.sameGender,
            sameAge = recruit.sameAge,
            budget = recruit.budget,
            groupSize = recruit.groupSize,
            createdAt = recruit.createdAt,
            updatedAt = recruit.updatedAt
        )
            fun fromWithApplies(recruit: Recruit, applies: List<ApplyResponseDto>): RecruitDetailResponseDto =
                from(recruit).copy(applies = applies)

        }

}
