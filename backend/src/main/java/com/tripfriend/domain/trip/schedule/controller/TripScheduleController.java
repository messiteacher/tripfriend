package com.tripfriend.domain.trip.schedule.controller;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.trip.schedule.dto.*;
import com.tripfriend.domain.trip.schedule.entity.TripSchedule;
import com.tripfriend.domain.trip.schedule.service.TripScheduleService;
import com.tripfriend.global.annotation.CheckPermission;
import com.tripfriend.global.dto.RsData;
import com.tripfriend.global.exception.ServiceException;
import com.tripfriend.global.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/trip/schedule")
@Tag(name = "TripSchedule API", description = "여행일정 관련 기능을 제공합니다.")
public class TripScheduleController {

    private final TripScheduleService scheduleService;

    // 개인 일정 생성
    @PostMapping
    @Operation(summary = "나의 여행 일정 등록", description = "로그인된 회원의 token 값을 기반으로 새로운 일정을 생성합니다.")
    public RsData<TripScheduleInfoResDto> createSchedule(
            @RequestBody TripScheduleReqDto reqBody,
            @RequestHeader(value = "Authorization", required = false) String token) {

        // 일정 생성
        TripScheduleInfoResDto schedule = scheduleService.createSchedule(reqBody, token);
        return new RsData<>(
                "200-1",
                "일정이 성공적으로 생성되었습니다.",
                schedule
        );
    }

    // 전체 일정 조회 - 관리자 권한
    @GetMapping
    @CheckPermission("ADMIN")
    @Operation(hidden = true)
    public RsData<List<TripScheduleResDto>> getAllSchedules() {
        List<TripScheduleResDto> schedules = scheduleService.getAllSchedules();
        return new RsData<>(
                "200-2",
                "전체 일정을 성공적으로 조회했습니다.",
                schedules
        );

    }

    // 로그인한 회원이 자신의 여행 일정 전체 조회
    @GetMapping("/my-schedules")
    @Operation(summary = "나의 여행 일정 전체 조회")
    public RsData<List<TripScheduleResDto>> getMySchedules(@RequestHeader(value = "Authorization", required = false) String token) {

        List<TripScheduleResDto> schedules = scheduleService.getSchedulesByCreator(token);
        return new RsData<>(
                "200-3",
                "'%s'님이 생성한 일정 조회가 완료되었습니다.".formatted(schedules.get(0).getMemberName()),
                schedules
        );
    }

    // 로그인한 회원이 자신의 여행 단건 정보 조회
    @GetMapping("/my-schedules/{id}")
    @Operation(summary = "나의 여행 일정 세부 조회")
    public RsData<List<TripScheduleInfoResDto>> getMyTripInfo(@RequestHeader(value = "Authorization", required = false) String token,
                                                              @PathVariable Long id) {

        List<TripScheduleInfoResDto> schedules = scheduleService.getTripInfo(token, id);
        return new RsData<>(
                "200-4",
                "'%s'님이 생성한 일정 조회가 완료되었습니다.".formatted(schedules.get(0).getMemberName()),
                schedules
        );
    }

    // 특정 회원의 여행 일정 조회 - 관리자 권한 필요
    @GetMapping("/member/{memberId}")
    @CheckPermission("ADMIN")
    @Operation(hidden = true)
    public RsData<List<TripScheduleResDto>> getSchedulesByMember(@PathVariable Long memberId) {

        // 회원 이름 찾기
        String memberName = scheduleService.getMemberName(memberId);

        List<TripScheduleResDto> schedules = scheduleService.getSchedulesByMemberId(memberId);
        return new RsData<>(
                "200-3",
                "'%s'님의 일정을 성공적으로 조회했습니다.".formatted(memberName),
                schedules
        );
    }

    // 개인 여행 일정 삭제
    @DeleteMapping("/my-schedules/{scheduleId}")
    @Operation(summary = "나의 여행 일정 삭제", description = "나의 일정을 삭제할 수 있다.")
    public RsData<Void> deleteSchedule(@PathVariable Long scheduleId,
                                       @RequestHeader(value = "Authorization", required = false) String token) {

        scheduleService.deleteSchedule(scheduleId, token);
        return new RsData<>(
                "200-5",
                "일정이 성공적으로 삭제되었습니다."
        );
    }


    // 특정 여행 일정을 수정 - 관리자 권한 필요
    @PutMapping("/{scheduleId}")
    @CheckPermission("ADMIN")
    @Operation(hidden = true)
    public RsData<TripScheduleResDto> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody @Valid TripScheduleUpdateReqDto req) {

        TripSchedule updatedSchedule = scheduleService.updateSchedule(scheduleId, req);
        TripScheduleResDto resDto = new TripScheduleResDto(updatedSchedule); // 반환 DTO
        return new RsData<>(
                "200-5",
                "일정이 성공적으로 수정되었습니다.",
                resDto
        );
    }

    @PutMapping("/update")
    @Operation(summary = "나의 여행 일정 및 여행 정보 통합 수정", hidden = true)
    public RsData<TripUpdateResDto> updateTrip(@RequestBody @Valid TripUpdateReqDto reqDto,
                                               @RequestHeader(value = "Authorization", required = false) String token) {
        TripUpdateResDto resTrip = scheduleService.updateTrip(reqDto, token);
        return new RsData<>(
                "200-1",
                "여행 일정 및 여행 정보가 성공적으로 수정되었습니다.",
                resTrip
        );
    }

}
