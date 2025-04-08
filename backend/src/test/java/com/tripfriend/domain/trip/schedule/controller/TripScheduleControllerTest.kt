package com.tripfriend.domain.trip.schedule.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.tripfriend.domain.trip.schedule.dto.TripScheduleInfoResDto
import com.tripfriend.domain.trip.schedule.dto.TripScheduleResDto
import com.tripfriend.domain.trip.schedule.service.TripScheduleService
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doReturn
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ActiveProfiles("test")
@ExtendWith(MockKExtension::class)
class TripScheduleControllerTest {
    @MockK
    private lateinit var mockMvc: MockMvc

    @MockK
    private lateinit var tripScheduleService: TripScheduleService

    @MockK
    private lateinit var objectMapper: ObjectMapper

    @Test
    @DisplayName("여행 스케줄 등록 - 성공 케이스")
    fun createSchedule() {
        // 요청 JSON (TripScheduleReqDto 형식)
        val requestJson =
            """
            {
              "title": "여름 휴가 일정",
              "description": "친구들과 함께 즐기는 여름 휴가",
              "startDate": "2025-07-01",
              "endDate": "2025-07-10",
              "cityName": "서울",
              "tripInformations": [
                {
                  "placeId": 1,
                  "visitTime": "2025-07-02T10:00:00",
                  "duration": 120,
                  "transportation": "BUS",
                  "cost": 15000,
                  "notes": "카메라 꼭 챙기기",
                  "priority": 1,
                  "isVisited": false
                },
                {
                  "placeId": 2,
                  "visitTime": "2025-07-03T14:00:00",
                  "duration": 90,
                  "transportation": "SUBWAY",
                  "cost": 8000,
                  "notes": "근처에서 점심",
                  "priority": 2,
                  "isVisited": false
                }
              ]
            }
            """.trimIndent()

        // dummy 데이터 (TripScheduleInfoResDto 형식, 실제 DTO 구조에 맞게 수정 필요)
        val dummyData =
            mapOf(
                "id" to 1,
                "memberName" to "user1",
                "title" to "여름 휴가 일정",
                "cityName" to "서울",
                "description" to "친구들과 함께 즐기는 여름 휴가",
                "startDate" to "2025-07-01",
                "endDate" to "2025-07-10",
                "tripInformations" to listOf<Map<String, Any>>(),
            )

        // objectMapper를 통해 DTO 객체로 변환 (실제 생성자나 필드에 맞게 변환해야 함)
        val dummyResponseDto = objectMapper.convertValue(dummyData, TripScheduleInfoResDto::class.java)

        doReturn(dummyResponseDto).whenever(tripScheduleService).createSchedule(any(), any())

        val token = "Bearer dummy.token"

        mockMvc
            .perform(
                post("/trip/schedule")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("200-1"))
            .andExpect(jsonPath("$.msg").value("일정이 성공적으로 생성되었습니다."))
            .andExpect(jsonPath("$.data.title").value("여름 휴가 일정"))
    }

    @Test
    @DisplayName("내 일정 전체 조회 성공")
    fun getMySchedules() {
        // dummy 리스트 데이터 (TripScheduleResDto 형식)
        val dummyList =
            listOf(
                mapOf(
                    "id" to 1,
                    "memberName" to "user1",
                    "title" to "서울 힐링 여행",
                    "cityName" to "서울",
                    "description" to "서울 여행",
                    "startDate" to "2025-04-10",
                    "endDate" to "2025-04-12",
                ),
                mapOf(
                    "id" to 2,
                    "memberName" to "user1",
                    "title" to "부산 바다 여행",
                    "cityName" to "부산",
                    "description" to "부산 여행",
                    "startDate" to "2025-05-15",
                    "endDate" to "2025-05-17",
                ),
            )
        // dummy 데이터들을 실제 DTO 리스트로 변환
        val dummyResponseList =
            dummyList.map {
                objectMapper.convertValue(it, TripScheduleResDto::class.java)
            }

        whenever(tripScheduleService.getSchedulesByCreator(any())).thenReturn(dummyResponseList)

        val token = "Bearer dummy.token"
        mockMvc
            .perform(
                get("/trip/schedule/my-schedules")
                    .header("Authorization", token),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("200-3"))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].title").value("서울 힐링 여행"))
    }

    @Test
    @DisplayName("내 일정 상세 조회 성공")
    fun getMyTripInfo() {
        // dummy 상세 데이터 (TripScheduleInfoResDto 형식)
        val dummyData =
            mapOf(
                "id" to 2,
                "memberName" to "user1",
                "title" to "부산 바다 여행",
                "cityName" to "부산",
                "description" to "부산 여행",
                "startDate" to "2025-05-15",
                "endDate" to "2025-05-17",
                "tripInformations" to
                    listOf(
                        mapOf(
                            "tripInformationId" to 4,
                            "placeId" to 5,
                            "cityName" to "부산",
                            "placeName" to "해운대 해수욕장",
                            "visitTime" to "2025-05-15T10:00:00",
                            "duration" to 2,
                            "transportation" to "WALK",
                            "cost" to 0,
                            "notes" to "해운대 해수욕장에서 바다 산책",
                            "visited" to false,
                        ),
                    ),
            )
        val dummyResponseDto = objectMapper.convertValue(dummyData, TripScheduleInfoResDto::class.java)

        whenever(tripScheduleService.getTripInfo(any(), any())).thenReturn(listOf(dummyResponseDto))

        val token = "Bearer dummy.token"
        mockMvc
            .perform(
                get("/trip/schedule/my-schedules/2")
                    .header("Authorization", token),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("200-4"))
            .andExpect(jsonPath("$.data[0].title").value("부산 바다 여행"))
    }

    @Test
    @DisplayName("일정 삭제 성공")
    fun deleteSchedule() {
        doNothing().whenever(tripScheduleService).deleteSchedule(eq(1L), any())

        val token = "Bearer dummy.token"
        mockMvc
            .perform(
                delete("/trip/schedule/my-schedules/1")
                    .header("Authorization", token),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("200-5"))
            .andExpect(jsonPath("$.msg").value("일정이 성공적으로 삭제되었습니다."))
    }
}
