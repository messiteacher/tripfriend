package com.tripfriend.domain.place.place.entity

enum class Category(
    val koreanName: String,
) {
    PLACE("장소"),
    STAY("숙박"),
    RESTAURANT("식당"),
    CAFE("카페"),
    ETC("기타"),
}
