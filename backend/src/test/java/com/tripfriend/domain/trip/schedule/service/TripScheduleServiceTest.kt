package com.tripfriend.domain.trip.schedule.service

import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.repository.MemberRepository
import com.tripfriend.domain.member.member.service.MemberService
import com.tripfriend.domain.trip.information.dto.TripInformationReqDto
import com.tripfriend.domain.trip.information.dto.TripInformationUpdateReqDto
import com.tripfriend.domain.trip.information.entity.Transportation
import com.tripfriend.domain.trip.schedule.dto.TripScheduleReqDto
import com.tripfriend.domain.trip.schedule.dto.TripScheduleUpdateReqDto
import com.tripfriend.domain.trip.schedule.dto.TripUpdateReqDto
import com.tripfriend.global.exception.ServiceException
import com.tripfriend.global.util.JwtUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TripScheduleServiceTest {

    @Autowired
    private lateinit var tripScheduleService: TripScheduleService

    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    private lateinit var loggedInMember: Member
    private lateinit var token: String

    @BeforeEach
    fun setup() {
        // test 데이터베이스에 user1이 존재한다고 가정
        loggedInMember = memberRepository.findByUsername("user1").orElseThrow()
        token = jwtUtil.generateAccessToken(loggedInMember.username, loggedInMember.authority, loggedInMember.verified)
    }

    @Test
    @DisplayName("여행 일정 생성 성공")
    fun createScheduleSuccess() {
        val reqDto = TripScheduleReqDto().apply {
            memberId = loggedInMember.id
            title = "서울 여행"
            description = "서울 여행 설명"
            cityName = "서울"
            startDate = LocalDate.of(2023, 9, 1)
            endDate = LocalDate.of(2023, 9, 10)
            tripInformations = listOf(
                TripInformationReqDto().apply {
                    placeId = 1L // 사용 가능한 장소 ID로 변경 필요
                    visitTime = LocalDateTime.of(2025, 4, 10, 9, 0)
                    duration = 2
                    transportation = Transportation.SUBWAY
                    cost = 3000
                    notes = "경복궁 방문"
                }
            )
        }

        val result = tripScheduleService.createSchedule(reqDto, token)
        assertThat(result).isNotNull
        assertThat(result.id).isNotNull
        assertThat(result.title).isEqualTo("서울 여행")
    }

    @Test
    @DisplayName("여행 일정 생성 실패 - 빈 도시명")
    fun createScheduleFailEmptyCity() {
        val reqDto = TripScheduleReqDto().apply {
            title = "빈 도시 테스트"
            description = "설명"
            cityName = "" // 빈 도시명
            startDate = LocalDate.of(2023, 9, 1)
            endDate = LocalDate.of(2023, 9, 10)
            tripInformations = listOf()
        }

        val ex = assertThrows<ServiceException> {
            tripScheduleService.createSchedule(reqDto, token)
        }
        assertThat(ex.code).isEqualTo("400-2")
    }

    @Test
    @DisplayName("여행 일정 생성 실패 - 여행 정보 도시 불일치")
    fun createScheduleFailTripInfoCityMismatch() {
        val reqDto = TripScheduleReqDto().apply {
            memberId = loggedInMember.id
            title = "서울 여행"
            description = "서울 여행 설명"
            cityName = "서울" // 선택 도시: 서울
            startDate = LocalDate.of(2023, 9, 1)
            endDate = LocalDate.of(2023, 9, 10)
            tripInformations = listOf(
                TripInformationReqDto().apply {
                    placeId = 6L // 6L는 부산의 도시 장소 ID
                    visitTime = LocalDateTime.of(2025, 4, 10, 9, 0)
                    duration = 2
                    transportation = Transportation.SUBWAY
                    cost = 3000
                    notes = "부산 명소 방문"
                }
            )
        }

        val ex = assertThrows<ServiceException> {
            tripScheduleService.createSchedule(reqDto, token)
        }
        assertThat(ex.code).isEqualTo("400-1")
    }

    @Test
    @DisplayName("여행 일정 삭제 성공")
    fun deleteScheduleSuccess() {
        val scheduleId = 1L
        // 삭제 수행
        tripScheduleService.deleteSchedule(scheduleId, token)
        // 삭제 후 조회 시 ServiceException 또는 null 반환 등의 방식으로 검증할 수 있음
    }

    @Test
    @DisplayName("여행 일정 삭제 실패 - 소유자가 아님")
    fun deleteScheduleFailNotOwner() {
        val scheduleId = 1L
        // user2의 토큰을 사용하여 삭제 시도
        val otherMember = memberRepository.findByUsername("user2").orElseThrow()
        val otherToken = jwtUtil.generateAccessToken(otherMember.username, otherMember.authority, otherMember.verified)

        val ex = assertThrows<ServiceException> {
            tripScheduleService.deleteSchedule(scheduleId, otherToken)
        }
        assertThat(ex.code).isEqualTo("403-1")
    }

    @Test
    @DisplayName("여행 일정 수정 성공")
    fun updateScheduleSuccess() {
        val scheduleId = 1L
        val tripInfoId = 1L

        val updateSchedule = TripScheduleUpdateReqDto().apply {
            // 생성자의 파라미터 요구사항이 없다면, 프로퍼티를 설정하는 방식으로 변경
            title = "수정된 제목"
            description = "수정된 설명"
            // 필요한 경우 startDate, endDate 등 추가 프로퍼티 설정
        }

        val updateTripInfo = TripInformationUpdateReqDto().apply {
            tripInformationId = tripInfoId
            cost = 5000 // 수정된 비용
            notes = "수정된 노트" // 수정된 노트
            duration = 1    // 수정된 일정
        }

        val updateReq = TripUpdateReqDto().apply {
            tripScheduleId = scheduleId
            scheduleUpdate = updateSchedule
            tripInformationUpdates = listOf(updateTripInfo)
        }

        val updateRes = tripScheduleService.updateTrip(updateReq, token)

        assertThat(updateRes.updatedSchedule.title).isEqualTo("수정된 제목")
        assertThat(updateRes.updatedSchedule.description).isEqualTo("수정된 설명")
        assertThat(updateRes.updatedTripInformations.first().cost).isEqualTo(5000)
        assertThat(updateRes.updatedTripInformations.first().notes).isEqualTo("수정된 노트")
        assertThat(updateRes.updatedTripInformations.first().duration).isEqualTo(1)
    }

    @Test
    @DisplayName("여행 일정 수정 실패 - 일정 생성자가 아님")
    fun updateTripFailNotOwner() {
        val scheduleId = 1L
        val tripInfoId = 1L

        val updateSchedule = TripScheduleUpdateReqDto().apply {
            title = "수정된 제목"
            description = "수정된 설명"
        }

        val updateTripInfo = TripInformationUpdateReqDto().apply {
            tripInformationId = tripInfoId
            cost = 5000
            notes = "수정된 노트"
            duration = 1
        }

        val updateReq = TripUpdateReqDto().apply {
            tripScheduleId = scheduleId
            scheduleUpdate = updateSchedule
            tripInformationUpdates = listOf(updateTripInfo)
        }

        // user2의 토큰 생성 (user1이 생성한 일정을 수정할 수 없음)
        val otherMember = memberRepository.findByUsername("user2").orElseThrow()
        val otherToken = jwtUtil.generateAccessToken(otherMember.username, otherMember.authority, otherMember.verified)

        val ex = assertThrows<ServiceException> {
            tripScheduleService.updateTrip(updateReq, otherToken)
        }
        // 일정 생성자가 아니므로 "403-1" 에러 코드가 발생해야 함
        assertThat(ex.code).isEqualTo("403-1")
    }

    @Test
    @DisplayName("내 여행 일정 조회 성공")
    fun schedulesByCreatorSuccess() {
        val schedules = tripScheduleService.getSchedulesByCreator(token)
        assertThat(schedules).isNotEmpty
    }

    @Test
    @DisplayName("여행 일정 세부 정보 조회 성공")
    fun tripInfoSuccess() {
        val scheduleId = 1L
        val tripInfoList = tripScheduleService.getTripInfo(token, scheduleId)
        assertThat(tripInfoList).isNotEmpty
    }

    @Test
    @DisplayName("여행 일정 세부 정보 조회 실패 - 소유자가 아님")
    fun tripInfoFailNotOwner() {
        val scheduleId = 1L
        val otherMember = memberRepository.findByUsername("user2").orElseThrow()
        val otherToken = jwtUtil.generateAccessToken(otherMember.username, otherMember.authority, otherMember.verified)

        val ex = assertThrows<ServiceException> {
            tripScheduleService.getTripInfo(otherToken, scheduleId)
        }
        assertThat(ex.code).isEqualTo("403-1")
    }

    @Test
    @DisplayName("특정 회원의 여행 일정 조회 실패 - 회원 미존재")
    fun schedulesByMemberIdFailNotFound() {
        // 존재하지 않는 회원 ID (예: 9999L)
        val nonExistentMemberId = 9999L
        val ex = assertThrows<ServiceException> {
            tripScheduleService.getSchedulesByMemberId(nonExistentMemberId)
        }
        assertThat(ex.code).isEqualTo("404-1")
    }
}