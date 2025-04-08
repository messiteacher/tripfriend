package com.tripfriend.domain.review.entity

import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.place.place.entity.Place
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
open class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var reviewId: Long? = null

    @Column(nullable = false)
    var title: String = ""

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String = ""

    @Column(nullable = false)
    var rating: Double = 0.0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    var place: Place? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member? = null

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    protected constructor()

    constructor(title: String, content: String, rating: Double, member: Member, place: Place) {
        this.title = title
        this.content = content
        this.rating = rating
        this.member = member
        this.place = place
        this.createdAt = LocalDateTime.now()
        this.updatedAt = this.createdAt
    }

    fun update(title: String, content: String, rating: Double) {
        this.title = title
        this.content = content
        this.rating = rating
        this.updatedAt = LocalDateTime.now()
    }

    @PrePersist
    fun prePersist() {
        this.createdAt = LocalDateTime.now()
        this.updatedAt = this.createdAt
    }

    @PreUpdate
    fun preUpdate() {
        this.updatedAt = LocalDateTime.now()
    }
}