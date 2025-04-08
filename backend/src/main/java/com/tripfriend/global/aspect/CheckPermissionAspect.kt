package com.tripfriend.global.aspect

import com.tripfriend.global.annotation.CheckPermission
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Aspect
@Component
class CheckPermissionAspect {
    @Pointcut("@annotation(com.tripfriend.global.annotation.CheckPermission)")
    fun checkPermissionPointcut() {}

    @Before("checkPermissionPointcut()")
    fun before(joinPoint: JoinPoint) {
        // 현재 로그인된 사용자 정보 가져오기
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            throw SecurityException("인증되지 않은 사용자입니다.")
        }

        // 메서드에 붙은 어노테이션 정보 가져오기
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val annotation = method.getAnnotation(CheckPermission::class.java)

        if (annotation != null) {
            val requiredRole = annotation.value // 필요한 권한
            val hasRole = authentication.authorities.any { it.authority == "ROLE_$requiredRole" }

            if (!hasRole) {
                throw SecurityException("접근 권한이 없습니다: $requiredRole")
            }
        }
    }
}
