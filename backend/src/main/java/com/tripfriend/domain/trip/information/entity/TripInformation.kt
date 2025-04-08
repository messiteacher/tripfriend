package com.tripfriend.domain.trip.information.entity

import com.tripfriend.domain.place.place.entity.Place
import com.tripfriend.domain.trip.information.dto.TripInformationUpdateReqDto
import com.tripfriend.domain.trip.schedule.entity.TripSchedule
import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "trip_information")
open class TripInformation(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_Information_id")
    var id: Long, // 개별 Id 추가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_schedule_id", nullable = false)
    var tripSchedule: TripSchedule, // 여행일정Id - FK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    var place: Place? = null, // 여행지Id - FK

    @Column(name = "visit_time", nullable = false)
    var visitTime: LocalDateTime? = null, // 방문시간

    @Column(name = "duration", nullable = false)
    var duration: Int? = null, // 방문기간(날짜 단위)

    @Enumerated(EnumType.STRING)
    @Column(name = "transportation", nullable = false)
    var transportation: Transportation? = null, // 교통 수단

    @Column(name = "cost")
    var cost: Int = 0, // 여행 경비

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null, // 메모

    @Column(name = "is_visited", nullable = false)
    @ColumnDefault("false")
    var isVisited: Boolean = false // 방문여부
) {

    // 여행 정보 수정 메서드
    fun updateTripInformation(updateDto: TripInformationUpdateReqDto) {
        visitTime = updateDto.visitTime
        duration = updateDto.duration
        transportation = updateDto.transportation
        cost = updateDto.cost
        notes = updateDto.notes
    }
}