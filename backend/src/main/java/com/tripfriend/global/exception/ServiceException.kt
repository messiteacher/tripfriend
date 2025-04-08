package com.tripfriend.global.exception

import com.tripfriend.global.dto.Empty
import com.tripfriend.global.dto.RsData

class ServiceException(
    private val _code: String,
    override val message: String
) : RuntimeException(message) {
    private val rsData: RsData<Empty> = RsData(_code, message)

    val code: String
        get() = rsData.code

    val msg: String
        get() = rsData.msg

    val statusCode: Int
        get() = rsData.statusCode
}
