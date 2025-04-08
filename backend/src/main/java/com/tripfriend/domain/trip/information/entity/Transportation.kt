package com.tripfriend.domain.trip.information.entity

enum class Transportation(
    val koreanName: String,
) {
    WALK("도보"),
    BUS("버스"),
    SUBWAY("기차"),
    CAR("자가용"),
    TAXI("택시"),
    ETC("기타"),
}
