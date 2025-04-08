package com.tripfriend.domain.event.service

import com.tripfriend.domain.event.dto.EventRequest
import com.tripfriend.domain.event.entity.Event
import com.tripfriend.domain.event.repository.EventRepository
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class EventServiceTest {

    private lateinit var eventRepository: EventRepository
    private lateinit var eventService: EventService

    @BeforeEach
    fun setUp() {
        eventRepository = mockk()
        eventService = EventService(eventRepository)
    }

    @Test
    @DisplayName("이벤트 생성 테스트")
    fun createEventTest() {
        // given
        val request = EventRequest(
            title = "이벤트 제목",
            description = "이벤트 설명",
            eventDate = LocalDate.of(2025, 5, 1)
        )

        val eventSlot = slot<Event>()
        every { eventRepository.save(capture(eventSlot)) } answers { eventSlot.captured }

        // when
        val response = eventService.create(request)

        // then
        assertThat(eventSlot.captured.title).isEqualTo("이벤트 제목")
        assertThat(response.title).isEqualTo("이벤트 제목")
        verify { eventRepository.save(any()) }
    }

    @Test
    @DisplayName("이벤트 전체 조회 테스트")
    fun findAllEventsTest() {
        // given
        val list = listOf(
            Event(1L, "제목", "설명", LocalDate.now(), LocalDateTime.now(), LocalDateTime.now())
        )
        every { eventRepository.findAll() } returns list

        // when
        val result = eventService.findAll()

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("제목")
    }

    @Test
    @DisplayName("이벤트 삭제 테스트")
    fun deleteEventTest() {
        // given
        val eventId = 1L
        every { eventRepository.deleteById(eventId) } just Runs

        // when
        eventService.delete(eventId)

        // then
        verify(exactly = 1) { eventRepository.deleteById(eventId) }
    }

    @Test
    @DisplayName("이벤트 수정 테스트")
    fun updateEventTest() {
        // given
        val id = 1L
        val original = Event(
            id, "old title", "old desc",
            LocalDate.of(2025, 1, 1),
            LocalDateTime.now(), LocalDateTime.now()
        )

        val request = EventRequest(
            title = "new title",
            description = "new desc",
            eventDate = LocalDate.of(2025, 5, 5)
        )

        every { eventRepository.findById(id) } returns Optional.of(original)
        every { eventRepository.save(any()) } answers { firstArg() }

        // when
        val result = eventService.update(id, request)

        // then
        assertThat(result.title).isEqualTo("new title")
        assertThat(result.description).isEqualTo("new desc")
        verify { eventRepository.save(any()) }
    }
}
