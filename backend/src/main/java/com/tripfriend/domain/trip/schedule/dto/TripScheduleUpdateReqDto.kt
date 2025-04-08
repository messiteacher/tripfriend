package com.tripfriend.domain.trip.schedule.dto

import java.time.LocalDate

class TripScheduleUpdateReqDto @JvmOverloads constructor(
    // 테스트 코드 코틀린 전환 후 변경
    var title: String? = null,

    var description: String? = null,

    var startDate: LocalDate? = null,

    var endDate: LocalDate? = null,
) {}