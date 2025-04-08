package com.tripfriend.domain.event.dto

import lombok.Getter
import lombok.Setter
import java.time.LocalDate


class EventRequest (
     val title: String,
     val description: String,
     val eventDate: LocalDate
)
