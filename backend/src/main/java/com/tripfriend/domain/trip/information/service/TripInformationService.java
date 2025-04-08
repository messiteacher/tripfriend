package com.tripfriend.domain.trip.information.service;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.place.place.entity.Place;
import com.tripfriend.domain.place.place.repository.PlaceRepository;
import com.tripfriend.domain.trip.information.dto.TripInformationReqDto;
import com.tripfriend.domain.trip.information.dto.TripInformationResDto;
import com.tripfriend.domain.trip.information.dto.TripInformationUpdateReqDto;
import com.tripfriend.domain.trip.information.dto.VisitedReqDto;
import com.tripfriend.domain.trip.information.entity.TripInformation;
import com.tripfriend.domain.trip.information.repository.TripInformationRepository;
import com.tripfriend.domain.trip.schedule.entity.TripSchedule;
import com.tripfriend.domain.trip.schedule.repository.TripScheduleRepository;
import com.tripfriend.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripInformationService {

    private final TripInformationRepository tripInformationRepository;
    private final TripScheduleRepository tripScheduleRepository;
    private final PlaceRepository placeRepository;
    private final AuthService authService;

    /**
     * 현재 로그인한 회원객체를 반환하는 메서드
     *
     * @param token JWT 토큰
     * @return 로그인한 회원 객체
     * @throws ServiceException 로그인하지 않은 경우 예외 발생
     */
    public Member getLoggedInMember(String token) {

        // 로그인 여부 확인 및 회원 정보 가져오기
        Member member = authService.getLoggedInMember(token);

        if (member == null) {
            throw new ServiceException("401-2", "로그인이 필요합니다.");
        }

        return member;
    }

    // 장소 검증
    public Place getPlcae(TripInformationReqDto reqDto) {
        if (reqDto.getPlaceId() == null) {
            throw new ServiceException("400-2", "장소 ID가 누락되었습니다.");
        }

        // 장소 검증
        return placeRepository.findById(reqDto.getPlaceId()).orElseThrow(
                () -> new ServiceException("404-2", "해당 장소가 존재하지 않습니다.")
        );
    }

    // 세부일정 검증
    public TripInformation checkInfo(Long tripInfoId, String token){
        // 회원 확인
        Member member = getLoggedInMember(token);

        // 여행 정보 확인
        TripInformation tripInformation = tripInformationRepository.findById(tripInfoId)
                .orElseThrow(() -> new ServiceException("404-2", "해당 여행 정보가 존재하지 않습니다."));

        // 본인 확인
        if (!tripInformation.getTripSchedule().getMember().getId().equals(member.getId())) {
            throw new ServiceException("403-1", "본인이 생성한 일정의 여행 정보만 수정할 수 있습니다.");
        }
        return tripInformation;
    }

    // 여행 정보 등록
    public TripInformationResDto addTripInformation(TripInformationReqDto reqDto, String token) {

        // 회원 확인
        Member member = getLoggedInMember(token);

        // 상위 일정 조회
        TripSchedule schedule = tripScheduleRepository.findById(reqDto.getTripScheduleId())
                .orElseThrow(() -> new ServiceException("404", "여행 일정이 존재하지 않습니다."));
        if (!schedule.getMember().getId().equals(member.getId())) {
            throw new ServiceException("403", "본인이 생성한 일정만 수정할 수 있습니다.");
        }

        // 장소 검증
        Place place = getPlcae(reqDto);

        TripInformation information = new TripInformation();
        information.setTripSchedule(schedule);
        information.setPlace(place);
        information.setVisitTime(reqDto.getVisitTime());
        information.setDuration(reqDto.getDuration());
        information.setCost(reqDto.getCost());
        information.setNotes(reqDto.getNotes());
        information.setTransportation(reqDto.getTransportation());
        tripInformationRepository.save(information);
        return new TripInformationResDto(information);
    }

    // 여행 정보 리스트 등록
    @Transactional
    public void addTripInformations(TripSchedule schedule, List<TripInformationReqDto> tripInfoReqs) {

        // 요청된 여행지 정보가 없을 경우 처리 x
        if (tripInfoReqs == null || tripInfoReqs.isEmpty()) {
            return;
        }

        // TripInformation 리스트 생성 및 매핑
        List<TripInformation> tripInformations = tripInfoReqs.stream()
                .map(infoReq -> {

                    // 장소 검증
                    Place place = getPlcae(infoReq);

                    // 객체 생성 및 저장
                    TripInformation tripInformation = new TripInformation();
                    tripInformation.setTripSchedule(schedule);
                    tripInformation.setPlace(place);
                    tripInformation.setVisitTime(infoReq.getVisitTime());
                    tripInformation.setDuration(infoReq.getDuration());
                    tripInformation.setTransportation(infoReq.getTransportation());
                    tripInformation.setCost(infoReq.getCost());
                    tripInformation.setNotes(infoReq.getNotes());

                    return tripInformation;

                }).collect(Collectors.toList());

        // 생성된 TripInformation 목록 저장
        tripInformationRepository.saveAll(tripInformations);

        // 여행 일정에 TripInformation 추가
        schedule.addTripInformations(tripInformations);
    }

    /**
     * 특정 여행 정보를 수정하는 메서드
     *
     * @param tripInfoId 수정할 여행 정보 ID
     * @param req        여행 정보 수정 요청 DTO
     * @return 수정된 TripInformation 객체
     */
    @Transactional
    public TripInformation updateTripInformation(Long tripInfoId, TripInformationUpdateReqDto req) {
        // 여행 정보가 존재하는지 확인
        TripInformation tripInformation = tripInformationRepository.findById(tripInfoId)
                .orElseThrow(() -> new ServiceException("404-3", "해당 여행 정보가 존재하지 않습니다."));

        // 여행 정보 업데이트, DTO로 전달
        tripInformation.updateTripInformation(req);

        return tripInformation;
    }

    // 여행 정보 삭제
    @Transactional
    public void deleteTripInformation(Long tripInformationId, String token) {
        TripInformation tripInformation = checkInfo(tripInformationId, token);
        tripInformationRepository.delete(tripInformation);
    }

    // 방문 여부 업데이트
    @Transactional
    public void updateVisited(VisitedReqDto req, String token) {
        TripInformation tripInformation = checkInfo(req.getTripInformationId(), token);
        tripInformation.setVisited(req.getIsVisited());
        tripInformationRepository.save(tripInformation);
    }

    // 세부 일정 조회
    @Transactional
    public TripInformation getTripInformation(Long id, String token) {
        TripInformation tripInformation = checkInfo(id, token);
        return tripInformation;
    }
}
