package com.tripfriend.domain.event.repository

import com.tripfriend.domain.event.entity.Event
import org.springframework.data.jpa.repository.JpaRepository

interface EventRepository : JpaRepository<Event, Long>
