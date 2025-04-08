package com.tripfriend.domain.recruit.recruit.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.tripfriend.domain.member.member.entity.Gender
import com.tripfriend.domain.recruit.recruit.entity.Recruit
import java.time.LocalDate
import java.time.LocalDateTime

data class RecruitListResponseDto(
    val recruitId: Long,
    val memberProfileImage: String?,
    val memberNickname: String?,
    val genderRestriction: String,
    val ageRestriction: String,
    val placeCityName: String?,
    val placePlaceName: String?,
    val title: String,
    @JsonProperty("isClosed")
    val isClosed: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val travelStyle: String,
    val budget: Int = 0,
    val groupSize: Int = 2,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    constructor(recruit: Recruit) : this(
        recruitId = recruit.recruitId!!,
        memberProfileImage = recruit.member.profileImage,
        memberNickname = recruit.member.nickname,
        genderRestriction = if (recruit.sameGender && recruit.member.gender != Gender.UNKNOWN) {
            if (recruit.member.gender == Gender.MALE) "남자만" else "여자만"
        } else {
            if (recruit.member.gender == Gender.UNKNOWN) "알 수 없음" else "모든 성별"
        },
        ageRestriction = if (recruit.sameAge) {
            when (recruit.member.ageRange) {
                com.tripfriend.domain.member.member.entity.AgeRange.TEENS -> "10대만"
                com.tripfriend.domain.member.member.entity.AgeRange.TWENTIES -> "20대만"
                com.tripfriend.domain.member.member.entity.AgeRange.THIRTIES -> "30대만"
                com.tripfriend.domain.member.member.entity.AgeRange.FORTIES_PLUS -> "40대 이상만"
                else -> "알 수 없음"
            }
        } else {
            "모든 연령대"
        },
        placeCityName = recruit.place.cityName,
        placePlaceName = recruit.place.placeName,
        title = recruit.title,
        isClosed = recruit.isClosed,
        startDate = recruit.startDate,
        endDate = recruit.endDate,
        travelStyle = recruit.travelStyle.koreanName,
        budget = recruit.budget,
        groupSize = recruit.groupSize,
        createdAt = recruit.createdAt,
        updatedAt = recruit.updatedAt
    )
}
