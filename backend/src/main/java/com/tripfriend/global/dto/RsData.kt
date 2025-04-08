package com.tripfriend.global.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class RsData<T>(
    val code: String,
    val msg: String,
    val data: T,
) {
    constructor(code: String, msg: String) : this(code, msg, Empty() as T)

    @get:JsonIgnore
    val statusCode: Int
        // StatusCode가 Json 포함 되지 않는다.
        get() {
            val statusCodeStr =
                code.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            return statusCodeStr.toInt() // code값 정수 반환
        }
}
