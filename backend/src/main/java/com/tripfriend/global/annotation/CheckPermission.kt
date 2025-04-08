package com.tripfriend.global.annotation

@Retention(AnnotationRetention.RUNTIME) // 런타임까지 유지
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER) // 메서드에만 적용 가능
annotation class CheckPermission(
    val value: String // 권한 (ex: "ADMIN", "USER")
)
