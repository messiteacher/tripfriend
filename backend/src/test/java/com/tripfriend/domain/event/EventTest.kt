package com.tripfriend.domain.event

import com.tripfriend.domain.event.entity.Event
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class EventTest {

    @Test
    fun `Event 생성 시 createdAt과 updatedAt은 null이어야 한다`() {
        val event = Event(
            title = "테스트 제목",
            description = "설명입니다",
            eventDate = LocalDate.of(2025, 4, 7)
        )

        assertThat(event.createdAt).isNull()
        assertThat(event.updatedAt).isNull()
    }

    @Test
    fun `onCreate 호출 시 createdAt과 updatedAt이 현재 시간으로 설정된다`() {
        val event = Event(
            title = "테스트 제목",
            description = "설명입니다",
            eventDate = LocalDate.of(2025, 4, 7)
        )

        event.onCreate()

        assertThat(event.createdAt).isNotNull()
        assertThat(event.updatedAt).isNotNull()
        assertThat(event.createdAt).isEqualTo(event.updatedAt)
    }

    @Test
    fun `onUpdate 호출 시 updatedAt이 갱신된다`() {
        val event = Event(
            title = "테스트 제목",
            description = "설명입니다",
            eventDate = LocalDate.of(2025, 4, 7),
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now().minusDays(1)
        )

        val before = event.updatedAt
        Thread.sleep(5) // 약간의 시간차 확보
        event.onUpdate()

        assertThat(event.updatedAt).isAfter(before)
    }
}
