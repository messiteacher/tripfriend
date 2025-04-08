package com.tripfriend.domain.trip.schedule.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.trip.information.entity.TripInformation
import com.tripfriend.domain.trip.schedule.dto.TripScheduleUpdateReqDto
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "trip_schedule")
open class TripSchedule(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_schedule_id")
    var id: Long? = null, // 여행 일정 id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member? = null, // 여행 일정을 생성한 회원

    @Column(name = "title", nullable = false)
    var title: String? = null, // 여행 일정 제목

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null, // 여행 일정 상세 설명

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate? = null, // 시작일

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate? = null, // 종료일

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null, // 일정 생성일

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null // 일정 수정일

) {
    @OneToMany(mappedBy = "tripSchedule", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val tripInformations: MutableList<TripInformation> = mutableListOf() // 여행 세부 일정 리스트

    // 여행 세부 일정 추가(단일)
    fun addTripInformation(tripInformation: TripInformation) {
        tripInformations.add(tripInformation)
        tripInformation.tripSchedule = this
    }

    // 여행 세부 일정 추가(여러개)
    fun addTripInformations(tripInformations: List<TripInformation>?) {
        tripInformations?.forEach { addTripInformation(it) }
    }

    // 여행 일정 수정
    fun updateSchedule(scheduleUpdate: TripScheduleUpdateReqDto) {
        title = scheduleUpdate.title
        description = scheduleUpdate.description
        startDate = scheduleUpdate.startDate
        endDate = scheduleUpdate.endDate
        updatedAt = LocalDateTime.now()
    }
}