package com.tripfriend.global.exception

import com.tripfriend.global.dto.RsData
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.util.stream.Collectors

@RestControllerAdvice
class GlobalExceptionHandler {

    /*
      MethodArgumentNotValidException 예외 처리
      -> 요청 데이터 검증 할 때, 유효성 검사 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<RsData<Void>> {

        // 검증 실패한 필드의 오류 메시지를 정리하여 하나의 문자열 반환
        val message = e.bindingResult.fieldErrors
            .stream()
            .map { fe: FieldError -> fe.field + " : " + fe.code + " : " + fe.defaultMessage }
            .sorted()
            .collect(Collectors.joining("\n"))

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST) // 400 Bad Request
            .body(
                RsData(
                    "400-1",
                    message
                )
            )
    }

    /*
        서비스 계층에서 발생하는 예외 처리
        예외 내부에서 정의된 HTTP 상태 코드와 코드 및 메시지를 포함한 응답 반환
     */
    @ExceptionHandler(ServiceException::class)
    fun ServiceExceptionHandle(ex: ServiceException): ResponseEntity<RsData<Void>> {
        return ResponseEntity
            .status(ex.statusCode)
            .body(
                RsData(
                    ex.code,
                    ex.msg
                )
            )
    }

    /*
        클라이언트에서 전달된 요청 파라미터의 타입이 맞지 않을 때 발생하는 예외를 처리
        HTTP 상태 코드 400 (Bad Request)와 함께 "잘못된 요청입니다."라는 메시지를 반환
    */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun MethodArgumentTypeMismatchExceptionHandle(ex: MethodArgumentTypeMismatchException): ResponseEntity<RsData<Void>> {

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                RsData(
                    "400-1",
                    "잘못된 요청입니다."
                )
            )
    }

    /*
        처리되지 않은 런타임 예외가 발생할 경우 이를 처리
        HTTP 상태 코드 500 (Internal Server Error)와 함께 "에러 발생" 메시지를 반환
     */
    @ExceptionHandler(RuntimeException::class)
    fun RuntimeException(ex: RuntimeException): ResponseEntity<RsData<Void>> {

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                RsData(
                    "500",
                    "에러 발생"
                )
            )
    }
}
