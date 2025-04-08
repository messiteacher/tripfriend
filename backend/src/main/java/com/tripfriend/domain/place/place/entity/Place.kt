package com.tripfriend.domain.place.place.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.tripfriend.domain.recruit.recruit.entity.Recruit
import com.tripfriend.domain.review.entity.Review
import com.tripfriend.domain.trip.information.entity.TripInformation
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "place")
open class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    var id: Long? = null // 여행지 Id

    // 여행 스케줄 연결 테이블 리스트
    @OneToMany(mappedBy = "place", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val tripInformations: MutableList<TripInformation> = mutableListOf()

    // 이미지 저장 URL
    @Column(name = "image_url")
    var imageUrl: String? = null

    // 동행 게시글 1:N 연결 (null 대신 빈 컬렉션으로 초기화)
    @OneToMany(mappedBy = "place", cascade = [CascadeType.REMOVE])
    @JsonIgnore
    var recruits: MutableList<Recruit> = mutableListOf()

    // 후기 게시글 1:N 연결 (null 대신 빈 컬렉션으로 초기화)
    @OneToMany(mappedBy = "place", cascade = [CascadeType.REMOVE])
    @JsonIgnore
    var reviews: MutableList<Review> = mutableListOf()

    @Column(name = "city_name", nullable = false)
    var cityName: String? = null // 도시명

    @Column(name = "place_name", nullable = false)
    var placeName: String? = null // 장소명

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null // 설명

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    var category: Category? = null // 카테고리

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null // 생성일

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null // 수정일

    fun addTripInformation(tripInformation: TripInformation) {
        tripInformations.add(tripInformation)
        tripInformation.place = this // 연관관계 설정
    }
}
