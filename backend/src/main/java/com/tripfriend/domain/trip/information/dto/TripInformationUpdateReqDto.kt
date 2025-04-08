package com.tripfriend.domain.trip.information.dto

import com.tripfriend.domain.trip.information.entity.Transportation
import java.time.LocalDateTime

data class TripInformationUpdateReqDto(
    var tripInformationId: Long? = null, // 여행 정보 ID

    var placeId: Long? = null,           // 장소 ID

    var visitTime: LocalDateTime? = null,

    var duration: Int? = null,

    var transportation: Transportation? = null,

    var cost: Int = 0,

    var notes: String? = null,
)