package com.tripfriend.domain.recruit.recruit.entity

import com.fasterxml.jackson.annotation.JsonProperty
import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.place.place.entity.Place
import com.tripfriend.domain.recruit.apply.entity.Apply
import com.tripfriend.domain.recruit.recruit.dto.RecruitRequestDto
import com.tripfriend.global.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "Recruit")
class Recruit(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruit_id")
    val recruitId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,

    @OneToMany(mappedBy = "recruit", cascade = [CascadeType.REMOVE])
    @OrderBy("applyId asc")
    var applies: MutableList<Apply> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    var place: Place,

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    var content: String,

    @JsonProperty("isClosed")
    @Column(name = "is_closed", nullable = false)
    var isClosed: Boolean = false,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate,

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "travel_style", nullable = false)
    var travelStyle: TravelStyle,

    @Column(name = "same_gender", nullable = false)
    var sameGender: Boolean,

    @Column(name = "same_age", nullable = false)
    var sameAge: Boolean,

    @Column(name = "budget", nullable = false)
    var budget: Int = 0,

    @Column(name = "group_size", nullable = false)
    var groupSize: Int = 2

) : BaseEntity() {

    fun update(requestDto: RecruitRequestDto, place: Place): Recruit {
        this.place = place
        this.title = requestDto.title
        this.content = requestDto.content
        this.isClosed = requestDto.isClosed
        this.startDate = requestDto.startDate
        this.endDate = requestDto.endDate
        this.travelStyle = requestDto.travelStyle
        this.sameGender = requestDto.sameGender
        this.sameAge = requestDto.sameAge
        this.budget = requestDto.budget
        this.groupSize = requestDto.groupSize
        return this
    }
}
