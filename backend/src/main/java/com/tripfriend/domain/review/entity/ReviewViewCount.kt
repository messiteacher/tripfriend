package com.tripfriend.domain.review.entity

import jakarta.persistence.*

@Entity
@Table(name = "review_view_count")
open class ReviewViewCount {

    @Id
    var reviewId: Long? = null  // Review의 ID를 PK로 사용

    @Column(nullable = false)
    var count: Int = 0  // 조회수

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  // reviewId를 PK로 사용
    @JoinColumn(name = "review_id")
    var review: Review? = null

    protected constructor()

    constructor(review: Review) {
        this.review = review
        this.count = 0
    }

    fun increment() {
        this.count++
    }
}