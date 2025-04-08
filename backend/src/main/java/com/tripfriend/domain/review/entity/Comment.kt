package com.tripfriend.domain.review.entity

import com.tripfriend.domain.member.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
open class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var commentId: Long? = null

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String = ""

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    var review: Review? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member? = null

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    protected constructor()

    constructor(content: String, review: Review, member: Member) {
        this.content = content
        this.review = review
        this.member = member
        this.createdAt = LocalDateTime.now()
        this.updatedAt = this.createdAt
    }

    fun update(content: String) {
        this.content = content
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