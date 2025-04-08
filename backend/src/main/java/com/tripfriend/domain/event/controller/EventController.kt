package com.tripfriend.domain.event.controller

import com.tripfriend.domain.event.dto.EventRequest
import com.tripfriend.domain.event.dto.EventResponse
import com.tripfriend.domain.event.service.EventService
import com.tripfriend.global.annotation.CheckPermission
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import lombok.RequiredArgsConstructor
import org.springframework.web.bind.annotation.*

@Tag(name = "Event API", description = "이벤트 관리 기능을 제공합니다.")
@RestController
@RequestMapping("/admin/event")
class EventController(
    private val eventService: EventService
) {

    @GetMapping
    @Operation(summary = "이벤트 목록 조회", description = "등록된 모든 이벤트를 조회합니다.")
    fun getAll(): List<EventResponse> = eventService.findAll()


    @PostMapping
    @CheckPermission("ADMIN")
    @Operation(summary = "이벤트 생성", description = "새로운 이벤트를 등록합니다.")
    fun create(@RequestBody request: EventRequest): EventResponse =
        eventService.create(request)

    @DeleteMapping("/{id}")
    @CheckPermission("ADMIN")
    @Operation(summary = "이벤트 삭제", description = "ID를 통해 특정 이벤트를 삭제합니다.")
    fun delete(@PathVariable id: Long) {
        eventService.delete(id)
    }

    @PutMapping("/{id}")
    @CheckPermission("ADMIN")
    @Operation(summary = "이벤트 수정", description = "ID를 통해 특정 이벤트 정보를 수정합니다.")
    fun update(@PathVariable id: Long, @RequestBody request: EventRequest): EventResponse =
        eventService.update(id, request)
}

