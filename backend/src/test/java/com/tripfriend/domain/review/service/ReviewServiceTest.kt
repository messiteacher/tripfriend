package com.tripfriend.domain.review.service

import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.place.place.entity.Place
import com.tripfriend.domain.place.place.repository.PlaceRepository
import com.tripfriend.domain.review.dto.ReviewRequestDto
import com.tripfriend.domain.review.dto.ReviewResponseDto
import com.tripfriend.domain.review.entity.Review
import com.tripfriend.domain.review.entity.ReviewViewCount
import com.tripfriend.domain.review.repository.CommentRepository
import com.tripfriend.domain.review.repository.ReviewRepository
import com.tripfriend.domain.review.repository.ReviewViewCountRepository
import com.tripfriend.global.exception.ServiceException
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class ReviewServiceTest {

    // 테스트용 서브클래스 추가
    private class TestPlace : Place() {
        fun apply(block: TestPlace.() -> Unit): TestPlace {
            this.block()
            return this
        }
    }

    private class TestReview : Review() {
        fun apply(block: TestReview.() -> Unit): TestReview {
            this.block()
            return this
        }
    }

    @MockK
    private lateinit var reviewRepository: ReviewRepository

    @MockK
    private lateinit var commentRepository: CommentRepository

    @MockK
    private lateinit var viewCountRepository: ReviewViewCountRepository

    @MockK
    private lateinit var placeRepository: PlaceRepository

    @InjectMockKs
    private lateinit var reviewService: ReviewService

    // 테스트 데이터
    private lateinit var testMember: Member
    private lateinit var testPlace: Place
    private lateinit var testReview: Review
    private lateinit var testViewCount: ReviewViewCount
    private lateinit var testReviewRequest: ReviewRequestDto

    @BeforeEach
    fun setUp() {
        clearAllMocks()

        // 테스트 멤버 설정 - 실제 객체 대신 모킹 사용
        testMember = mockk<Member>()
        every { testMember.id } returns 1L
        every { testMember.username } returns "testUser"
        every { testMember.nickname } returns "테스트 사용자"
        every { testMember.email } returns "test@example.com"
        every { testMember.profileImage } returns "profile.jpg"

        // 테스트 장소 모킹
        testPlace = mockk<Place>()
        every { testPlace.id } returns 1L
        every { testPlace.cityName } returns "서울"
        every { testPlace.placeName } returns "테스트 장소"
        every { testPlace.description } returns "테스트 장소 설명"

        // 테스트 리뷰 모킹
        testReview = mockk<Review>()
        every { testReview.reviewId } returns 1L
        every { testReview.title } returns "테스트 리뷰"
        every { testReview.content } returns "이것은 테스트 리뷰입니다"
        every { testReview.rating } returns 4.5
        every { testReview.member } returns testMember
        every { testReview.place } returns testPlace
        every { testReview.createdAt } returns LocalDateTime.now()
        every { testReview.updatedAt } returns LocalDateTime.now()

        // 테스트 리뷰 조회수 모킹
        testViewCount = mockk<ReviewViewCount>()
        every { testViewCount.count } returns 5
        every { testViewCount.increment() } just runs

        // 테스트 리뷰 요청 DTO 설정
        testReviewRequest = ReviewRequestDto().apply {
            title = "테스트 리뷰"
            content = "이것은 테스트 리뷰입니다"
            rating = 4.5
            placeId = 1L
        }

        // QueryDSL 관련 메서드 모킹 추가
        every { reviewRepository.findAllOrderByCommentCountDesc() } returns listOf(arrayOf(testReview, 3L))
        every { reviewRepository.findAllByOrderByRatingDesc() } returns listOf(testReview)
        every { reviewRepository.findAllByOrderByRatingAsc() } returns listOf(testReview)
        every { reviewRepository.findAllByOrderByCreatedAtDesc() } returns listOf(testReview)
        every { reviewRepository.findAllByOrderByCreatedAtAsc() } returns listOf(testReview)
        every { reviewRepository.findByPlace_IdOrderByRatingDesc(any()) } returns listOf(testReview)
        every { reviewRepository.findByPlace_IdOrderByRatingAsc(any()) } returns listOf(testReview)

        // 추가 모킹 - 인기 리뷰 관련
        every { viewCountRepository.findAll() } returns listOf(testViewCount)
        every { reviewRepository.findAll() } returns listOf(testReview)
    }

    @Nested
    @DisplayName("리뷰 생성 테스트")
    inner class CreateReviewTests {

        @Test
        @DisplayName("리뷰 생성 성공")
        fun createReviewSuccess() {
            // Given
            every { placeRepository.findById(1L) } returns Optional.of(testPlace)
            every { reviewRepository.save(any()) } returns testReview
            every { viewCountRepository.save(any()) } returns testViewCount

            // When
            val result = reviewService.createReview(testReviewRequest, testMember)

            // Then
            assertNotNull(result)
            assertEquals("테스트 리뷰", result.title)
            assertEquals(4.5, result.rating)
            assertEquals("테스트 사용자", result.memberName)

            verify {
                placeRepository.findById(1L)
                reviewRepository.save(any())
                viewCountRepository.save(any())
            }
        }

        @Test
        @DisplayName("장소 ID가 없는 경우 리뷰 생성 실패")
        fun createReviewFailWithNullPlaceId() {
            // Given
            testReviewRequest.placeId = null

            // When & Then
            val exception = assertFailsWith<ServiceException> {
                reviewService.createReview(testReviewRequest, testMember)
            }

            assertEquals("400-1", exception.code)
            assertEquals("여행지 정보는 필수입니다.", exception.msg)

            verify(exactly = 0) {
                placeRepository.findById(any())
                reviewRepository.save(any())
            }
        }

        @Test
        @DisplayName("유효하지 않은 평점으로 리뷰 생성 실패")
        fun createReviewFailWithInvalidRating() {
            // Given
            testReviewRequest.rating = 6.0

            // When & Then
            val exception = assertFailsWith<ServiceException> {
                reviewService.createReview(testReviewRequest, testMember)
            }

            assertEquals("400-2", exception.code)
            assertEquals("평점은 1점에서 5점 사이여야 합니다.", exception.msg)

            verify(exactly = 0) {
                placeRepository.findById(any())
                reviewRepository.save(any())
            }
        }

        @Test
        @DisplayName("존재하지 않는 장소로 리뷰 생성 실패")
        fun createReviewFailWithNonExistentPlace() {
            // Given
            every { placeRepository.findById(1L) } returns Optional.empty()

            // When & Then
            val exception = assertFailsWith<ServiceException> {
                reviewService.createReview(testReviewRequest, testMember)
            }

            assertEquals("404-2", exception.code)
            assertEquals("해당 여행지가 존재하지 않습니다.", exception.msg)

            verify {
                placeRepository.findById(1L)
            }
            verify(exactly = 0) {
                reviewRepository.save(any())
            }
        }
    }

    @Nested
    @DisplayName("리뷰 조회 테스트")
    inner class GetReviewTests {

        @BeforeEach
        fun setUp() {
            every { reviewRepository.findById(1L) } returns Optional.of(testReview)
            every { viewCountRepository.findById(1L) } returns Optional.of(testViewCount)
            every { commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L) } returns emptyList()
        }

        @Test
        @DisplayName("조회수 증가와 함께 리뷰 조회 성공")
        fun getReviewWithIncrementSuccess() {
            // Given
            every { viewCountRepository.save(any()) } returns testViewCount

            // When
            val result = reviewService.getReview(1L, true)

            // Then
            assertNotNull(result)
            assertEquals("테스트 리뷰", result.title)
            assertEquals(4.5, result.rating)
            assertEquals("테스트 사용자", result.memberName)
            assertEquals(5, result.viewCount)

            verify {
                reviewRepository.findById(1L)
                viewCountRepository.findById(1L)
                commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L)
                viewCountRepository.save(any())
                testViewCount.increment()
            }
        }

        @Test
        @DisplayName("조회수 증가 없이 리뷰 조회 성공")
        fun getReviewWithoutIncrementSuccess() {
            // When
            val result = reviewService.getReview(1L, false)

            // Then
            assertNotNull(result)
            assertEquals("테스트 리뷰", result.title)
            assertEquals(4.5, result.rating)
            assertEquals("테스트 사용자", result.memberName)
            assertEquals(5, result.viewCount)

            verify {
                reviewRepository.findById(1L)
                viewCountRepository.findById(1L)
                commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L)
            }
            verify(exactly = 0) {
                viewCountRepository.save(any())
                testViewCount.increment()
            }
        }

        @Test
        @DisplayName("조회수 엔티티가 없을 때 새로 생성")
        fun getReviewWithNoViewCount() {
            // Given
            every { viewCountRepository.findById(1L) } returns Optional.empty()
            val newViewCount = mockk<ReviewViewCount>()
            every { newViewCount.count } returns 0
            every { newViewCount.increment() } just runs
            every { viewCountRepository.save(any()) } returns newViewCount

            // When
            val result = reviewService.getReview(1L, true)

            // Then
            assertNotNull(result)
            assertEquals("테스트 리뷰", result.title)
            assertEquals(0, result.viewCount)

            verify {
                reviewRepository.findById(1L)
                viewCountRepository.findById(1L)
                viewCountRepository.save(any())
                commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L)
            }
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 조회 실패")
        fun getReviewNonExistentFail() {
            // Given
            every { reviewRepository.findById(999L) } returns Optional.empty()

            // When & Then
            val exception = assertFailsWith<ServiceException> {
                reviewService.getReview(999L)
            }

            assertEquals("404-1", exception.code)
            assertEquals("존재하지 않는 리뷰입니다.", exception.msg)

            verify {
                reviewRepository.findById(999L)
            }
            verify(exactly = 0) {
                viewCountRepository.findById(any())
                commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(any())
            }
        }
    }

    @Nested
    @DisplayName("리뷰 수정 테스트")
    inner class UpdateReviewTests {

        private lateinit var updateRequest: ReviewRequestDto

        @BeforeEach
        fun setUp() {
            updateRequest = ReviewRequestDto().apply {
                title = "수정된 리뷰"
                content = "이것은 수정된 리뷰입니다"
                rating = 5.0
            }

            every { reviewRepository.findById(1L) } returns Optional.of(testReview)
            every { viewCountRepository.findById(1L) } returns Optional.of(testViewCount)
            every { commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L) } returns emptyList()
        }

        @Test
        @DisplayName("리뷰 수정 성공")
        fun updateReviewSuccess() {
            // Given
            every { testReview.update(any(), any(), any()) } just runs

            // 수정 후 값을 반환하도록 모킹
            every { testReview.title } returns "수정된 리뷰"
            every { testReview.content } returns "이것은 수정된 리뷰입니다"
            every { testReview.rating } returns 5.0

            // When
            val result = reviewService.updateReview(1L, updateRequest, testMember)

            // Then
            assertNotNull(result)
            assertEquals("수정된 리뷰", result.title)
            assertEquals("이것은 수정된 리뷰입니다", result.content)
            assertEquals(5.0, result.rating)

            verify {
                reviewRepository.findById(1L)
                testReview.update(any(), any(), any())
                viewCountRepository.findById(1L)
                commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L)
            }
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 수정 실패")
        fun updateNonExistentReviewFail() {
            // Given
            every { reviewRepository.findById(999L) } returns Optional.empty()

            // When & Then
            val exception = assertFailsWith<ServiceException> {
                reviewService.updateReview(999L, updateRequest, testMember)
            }

            assertEquals("404-1", exception.code)
            assertEquals("존재하지 않는 리뷰입니다.", exception.msg)

            verify {
                reviewRepository.findById(999L)
            }
        }

        @Test
        @DisplayName("다른 사용자의 리뷰 수정 실패")
        fun updateOtherUserReviewFail() {
            // Given
            val otherMember = mockk<Member>()
            every { otherMember.id } returns 2L
            every { otherMember.username } returns "otherUser"

            // When & Then
            val exception = assertFailsWith<ServiceException> {
                reviewService.updateReview(1L, updateRequest, otherMember)
            }

            assertEquals("403-1", exception.code)
            assertEquals("리뷰 작성자만 수정할 수 있습니다.", exception.msg)

            verify {
                reviewRepository.findById(1L)
            }
        }

        @Test
        @DisplayName("유효하지 않은 평점으로 리뷰 수정 실패")
        fun updateReviewWithInvalidRatingFail() {
            // Given
            updateRequest.rating = 6.0

            // When & Then
            val exception = assertFailsWith<ServiceException> {
                reviewService.updateReview(1L, updateRequest, testMember)
            }

            assertEquals("400-2", exception.code)
            assertEquals("평점은 1점에서 5점 사이여야 합니다.", exception.msg)

            verify {
                reviewRepository.findById(1L)
            }
        }
    }

    @Nested
    @DisplayName("리뷰 삭제 테스트")
    inner class DeleteReviewTests {

        @BeforeEach
        fun setUp() {
            every { reviewRepository.findById(1L) } returns Optional.of(testReview)
            every { viewCountRepository.deleteById(any()) } just runs
            every { reviewRepository.delete(any()) } just runs
        }

        @Test
        @DisplayName("리뷰 삭제 성공")
        fun deleteReviewSuccess() {
            // When
            reviewService.deleteReview(1L, testMember)

            // Then
            verify {
                reviewRepository.findById(1L)
                viewCountRepository.deleteById(1L)
                reviewRepository.delete(testReview)
            }
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 삭제 실패")
        fun deleteNonExistentReviewFail() {
            // Given
            every { reviewRepository.findById(999L) } returns Optional.empty()

            // When & Then
            val exception = assertFailsWith<ServiceException> {
                reviewService.deleteReview(999L, testMember)
            }

            assertEquals("404-1", exception.code)
            assertEquals("존재하지 않는 리뷰입니다.", exception.msg)

            verify {
                reviewRepository.findById(999L)
            }
            verify(exactly = 0) {
                viewCountRepository.deleteById(any())
                reviewRepository.delete(any())
            }
        }

        @Test
        @DisplayName("다른 사용자의 리뷰 삭제 실패")
        fun deleteOtherUserReviewFail() {
            // Given
            val otherMember = mockk<Member>()
            every { otherMember.id } returns 2L
            every { otherMember.username } returns "otherUser"

            // When & Then
            val exception = assertFailsWith<ServiceException> {
                reviewService.deleteReview(1L, otherMember)
            }

            assertEquals("403-1", exception.code)
            assertEquals("리뷰 작성자만 삭제할 수 있습니다.", exception.msg)

            verify {
                reviewRepository.findById(1L)
            }
            verify(exactly = 0) {
                viewCountRepository.deleteById(any())
                reviewRepository.delete(any())
            }
        }
    }

    @Nested
    @DisplayName("특정 장소의 리뷰 목록 조회 테스트")
    inner class GetReviewsByPlaceTests {

        @BeforeEach
        fun setUp() {
            every { reviewRepository.findByPlace_IdOrderByCreatedAtDesc(1L) } returns listOf(testReview)
            every { viewCountRepository.findById(1L) } returns Optional.of(testViewCount)
            every { commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L) } returns emptyList()
        }

        @Test
        @DisplayName("특정 장소의 리뷰 목록 조회 성공")
        fun getReviewsByPlaceSuccess() {
            // When
            val result = reviewService.getReviewsByPlace(1L)

            // Then
            assertNotNull(result)
            assertEquals(1, result.size)
            assertEquals("테스트 리뷰", result[0].title)
            assertEquals(4.5, result[0].rating)
            assertEquals("테스트 사용자", result[0].memberName)
            assertEquals(5, result[0].viewCount)

            verify {
                reviewRepository.findByPlace_IdOrderByCreatedAtDesc(1L)
                viewCountRepository.findById(1L)
                commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L)
            }
        }

        @Test
        @DisplayName("장소 ID가 없는 경우 실패")
        fun getReviewsByPlaceNullIdFail() {
            // When & Then
            val exception = assertFailsWith<ServiceException> {
                reviewService.getReviewsByPlace(null)
            }

            assertEquals("400-3", exception.code)
            assertEquals("여행지 ID는 필수입니다.", exception.msg)

            verify(exactly = 0) {
                reviewRepository.findByPlace_IdOrderByCreatedAtDesc(any())
            }
        }

        @Test
        @DisplayName("리뷰가 없는 장소의 경우 빈 목록 반환")
        fun getReviewsByPlaceEmptyList() {
            // Given
            every { reviewRepository.findByPlace_IdOrderByCreatedAtDesc(2L) } returns emptyList()

            // When
            val result = reviewService.getReviewsByPlace(2L)

            // Then
            assertNotNull(result)
            assertTrue(result.isEmpty())

            verify {
                reviewRepository.findByPlace_IdOrderByCreatedAtDesc(2L)
            }
            verify(exactly = 0) {
                viewCountRepository.findById(any())
                commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(any())
            }
        }
    }

    @Nested
    @DisplayName("리뷰 목록 조회 테스트")
    inner class GetReviewsTests {

        @BeforeEach
        fun setUp() {
            every { reviewRepository.findAllByOrderByCreatedAtDesc() } returns listOf(testReview)
            every { reviewRepository.findByTitleContainingOrderByCreatedAtDesc(any()) } returns listOf(testReview)
            every { reviewRepository.findByPlace_IdOrderByCreatedAtDesc(any()) } returns listOf(testReview)
            every { reviewRepository.findByMemberIdOrderByCreatedAtDesc(any()) } returns listOf(testReview)
            every { viewCountRepository.findById(1L) } returns Optional.of(testViewCount)
            every { commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L) } returns emptyList()
        }

        @Test
        @DisplayName("최신순으로 리뷰 목록 조회 성공")
        fun getReviewsNewestSuccess() {
            // When
            val result = reviewService.getReviews("newest", null, null, null)

            // Then
            assertNotNull(result)
            assertEquals(1, result.size)
            assertEquals("테스트 리뷰", result[0].title)

            verify {
                reviewRepository.findAllByOrderByCreatedAtDesc()
                viewCountRepository.findById(1L)
                commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L)
            }
        }

        @Test
        @DisplayName("키워드로 리뷰 검색 성공")
        fun getReviewsByKeywordSuccess() {
            // When
            val result = reviewService.getReviews("newest", "테스트", null, null)

            // Then
            assertNotNull(result)
            assertEquals(1, result.size)
            assertEquals("테스트 리뷰", result[0].title)

            verify {
                reviewRepository.findByTitleContainingOrderByCreatedAtDesc("테스트")
                viewCountRepository.findById(1L)
                commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L)
            }
        }

        @Test
        @DisplayName("특정 장소의 리뷰 목록 조회 성공")
        fun getReviewsByPlaceIdSuccess() {
            // When
            val result = reviewService.getReviews("newest", null, 1L, null)

            // Then
            assertNotNull(result)
            assertEquals(1, result.size)
            assertEquals("테스트 리뷰", result[0].title)

            verify {
                reviewRepository.findByPlace_IdOrderByCreatedAtDesc(1L)
                viewCountRepository.findById(1L)
                commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L)
            }
        }

        @Test
        @DisplayName("특정 회원의 리뷰 목록 조회 성공")
        fun getReviewsByMemberIdSuccess() {
            // When
            val result = reviewService.getReviews("newest", null, null, 1L)

            // Then
            assertNotNull(result)
            assertEquals(1, result.size)
            assertEquals("테스트 리뷰", result[0].title)

            verify {
                reviewRepository.findByMemberIdOrderByCreatedAtDesc(1L)
                viewCountRepository.findById(1L)
                commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L)
            }
        }

        @Test
        @DisplayName("잘못된 정렬 옵션으로 리뷰 목록 조회 실패")
        fun getReviewsInvalidSortFail() {
            // When & Then
            val exception = assertFailsWith<ServiceException> {
                reviewService.getReviews("invalid_sort", null, null, null)
            }

            assertEquals("400-4", exception.code)
            assertEquals("유효하지 않은 정렬 옵션입니다.", exception.msg)

            verify(exactly = 0) {
                reviewRepository.findAllByOrderByCreatedAtDesc()
            }
        }
    }

    @Nested
    @DisplayName("인기 리뷰 목록 조회 테스트")
    inner class GetPopularReviewsTests {

        @BeforeEach
        fun setUp() {
            every { reviewRepository.findAll() } returns listOf(testReview)
            every { viewCountRepository.findById(1L) } returns Optional.of(testViewCount)
            every { commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L) } returns emptyList()
        }

        @Test
        @DisplayName("인기 리뷰 목록 조회 성공")
        fun getPopularReviewsSuccess() {
            // When
            val result = reviewService.getPopularReviews(10)

            // Then
            assertNotNull(result)
            assertEquals(1, result.size)
            assertEquals("테스트 리뷰", result[0].title)
            assertEquals(5, result[0].viewCount)

            verify {
                reviewRepository.findAll()
                viewCountRepository.findById(1L)
                commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L)
            }
        }

        @Test
        @DisplayName("인기 리뷰가 없는 경우 빈 목록 반환")
        fun getPopularReviewsEmptyList() {
            // Given
            every { reviewRepository.findAll() } returns emptyList()

            // When
            val result = reviewService.getPopularReviews(10)

            // Then
            assertNotNull(result)
            assertTrue(result.isEmpty())

            verify {
                reviewRepository.findAll()
            }
            verify(exactly = 0) {
                viewCountRepository.findById(any())
                commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(any())
            }
        }
    }

    @Nested
    @DisplayName("특정 회원의 리뷰 목록 조회 테스트")
    inner class GetReviewsByMemberTests {

        @BeforeEach
        fun setUp() {
            every { reviewRepository.findByMemberIdOrderByCreatedAtDesc(1L) } returns listOf(testReview)
            every { viewCountRepository.findById(1L) } returns Optional.of(testViewCount)
            every { commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L) } returns emptyList()
        }

        @Test
        @DisplayName("특정 회원의 리뷰 목록 조회 성공")
        fun getReviewsByMemberSuccess() {
            // When
            val result = reviewService.getReviewsByMember(1L)

            // Then
            assertNotNull(result)
            assertEquals(1, result.size)
            assertEquals("테스트 리뷰", result[0].title)
            assertEquals(5, result[0].viewCount)

            verify {
                reviewRepository.findByMemberIdOrderByCreatedAtDesc(1L)
                viewCountRepository.findById(1L)
                commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(1L)
            }
        }

        @Test
        @DisplayName("회원 ID가 없는 경우 실패")
        fun getReviewsByMemberNullIdFail() {
            // When & Then
            val exception = assertFailsWith<ServiceException> {
                reviewService.getReviewsByMember(null)
            }

            assertEquals("400-5", exception.code)
            assertEquals("회원 ID는 필수입니다.", exception.msg)

            verify(exactly = 0) {
                reviewRepository.findByMemberIdOrderByCreatedAtDesc(any())
            }
        }

        @Test
        @DisplayName("리뷰가 없는 회원의 경우 빈 목록 반환")
        fun getReviewsByMemberEmptyList() {
            // Given
            every { reviewRepository.findByMemberIdOrderByCreatedAtDesc(2L) } returns emptyList()

            // When
            val result = reviewService.getReviewsByMember(2L)

            // Then
            assertNotNull(result)
            assertTrue(result.isEmpty())

            verify {
                reviewRepository.findByMemberIdOrderByCreatedAtDesc(2L)
            }
            verify(exactly = 0) {
                viewCountRepository.findById(any())
                commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(any())
            }
        }
    }
}