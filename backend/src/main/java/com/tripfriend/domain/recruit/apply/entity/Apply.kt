package com.tripfriend.domain.recruit.apply.entity

import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.recruit.recruit.entity.Recruit
import com.tripfriend.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "Apply")
class Apply(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apply_id")
    val applyId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_id", nullable = false)
    val recruit: Recruit,

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    val content: String

) : BaseEntity()
