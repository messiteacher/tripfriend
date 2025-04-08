package com.tripfriend.domain.trip.schedule.dto;


import com.tripfriend.domain.trip.information.dto.TripInformationUpdateReqDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripUpdateReqDto {

    @NotNull
    private Long tripScheduleId;  // 여행 일정 ID

    @Valid
    private TripScheduleUpdateReqDto scheduleUpdate; // 여행 일정 수정 정보

    @Valid
    private List<TripInformationUpdateReqDto> tripInformationUpdates; // 여행 정보 수정 리스트
}
