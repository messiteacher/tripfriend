package com.tripfriend.domain.event.dto

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import java.time.LocalDate
import java.time.LocalDateTime

@Getter
@AllArgsConstructor
@Builder
class EventResponse (
     val id: Long?,
     val title: String,
     val description: String,
     val eventDate: LocalDate,
     val createdAt: LocalDateTime?
    )