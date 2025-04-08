package com.tripfriend.domain.event.service

import com.tripfriend.domain.event.dto.EventRequest
import com.tripfriend.domain.event.dto.EventResponse
import com.tripfriend.domain.event.entity.Event
import com.tripfriend.domain.event.repository.EventRepository
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import java.util.stream.Collectors
import org.springframework.transaction.annotation.Transactional


@Service
class EventService(
    private val eventRepository: EventRepository
) {

    @Transactional(readOnly = true)
    fun findAll(): List<EventResponse> {
        return eventRepository.findAll()
            .map { event -> event.toResponse() }
    }

    @Transactional
    fun create(request: EventRequest): EventResponse {
        val event = Event(
            title = request.title,
            description = request.description,
            eventDate = request.eventDate
        )
        return eventRepository.save(event).toResponse()
    }


    @Transactional
    fun delete(id: Long) {
        eventRepository.deleteById(id)
    }

    @Transactional
    fun update(id: Long, request: EventRequest): EventResponse {
        val event = eventRepository.findById(id)
            .orElseThrow { IllegalArgumentException("이벤트를 찾을 수 없습니다.") }

        event.title = request.title
        event.description = request.description
        event.eventDate = request.eventDate

        return eventRepository.save(event).toResponse()
    }

    private fun Event.toResponse(): EventResponse =
        EventResponse(
            this.id,
            this.title,
            this.description,
            this.eventDate,
            this.createdAt
        )

}
