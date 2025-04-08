package com.tripfriend.domain.recruit.recruit.service;

import com.tripfriend.domain.member.member.entity.AgeRange;
import com.tripfriend.domain.member.member.entity.Gender;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.place.place.entity.Place;
import com.tripfriend.domain.place.place.repository.PlaceRepository;
import com.tripfriend.domain.recruit.recruit.dto.RecruitRequestDto;
import com.tripfriend.domain.recruit.recruit.dto.RecruitListResponseDto;
import com.tripfriend.domain.recruit.recruit.dto.RecruitDetailResponseDto;
import com.tripfriend.domain.recruit.recruit.entity.Recruit;
import com.tripfriend.domain.recruit.recruit.entity.TravelStyle;
import com.tripfriend.domain.recruit.recruit.repository.RecruitRepository;
import com.tripfriend.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RecruitServiceTest {

    @Mock private RecruitRepository recruitRepository;
    @Mock private PlaceRepository placeRepository;
    @Mock private AuthService authService;
    @InjectMocks private RecruitService recruitService;

    private Member fakeMember;
    private Place fakePlace;
    private Recruit fakeRecruit;
    private RecruitRequestDto requestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        fakeMember = Member.builder()
                .id(1L)
                .nickname("tester")
                .profileImage("img")
                .gender(Gender.FEMALE)
                .ageRange(AgeRange.TWENTIES)
                .authority("USER")
                .build();

        fakePlace = Place.builder()
                .id(1L)
                .cityName("서울")
                .placeName("홍대")
                .build();

        requestDto = RecruitRequestDto.builder()
                .placeId(1L)
                .title("제목")
                .content("내용")
                .isClosed(false)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .travelStyle(TravelStyle.RELAXATION)
                .sameGender(true)
                .sameAge(false)
                .budget(100000)
                .groupSize(3)
                .build();

        fakeRecruit = requestDto.toEntity(fakeMember, fakePlace);
    }

    @Test
    void create_ShouldReturnRecruitDetailResponseDto() {
        when(authService.getLoggedInMember("token")).thenReturn(fakeMember);
        when(placeRepository.findById(1L)).thenReturn(Optional.of(fakePlace));
        when(recruitRepository.save(any(Recruit.class))).thenReturn(fakeRecruit);

        var result = recruitService.create(requestDto, "token");

        assertEquals("제목", result.getTitle());
        assertEquals("tester", result.getMemberNickname());
        assertEquals("서울", result.getPlaceCityName());
    }

    @Test
    void create_ShouldThrowException_WhenPlaceNotFound() {
        when(authService.getLoggedInMember("token")).thenReturn(fakeMember);
        when(placeRepository.findById(1L)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class, () ->
                recruitService.create(requestDto, "token")
        );

        assertEquals("해당 장소가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void getLoggedInMember_ShouldThrowException_WhenMemberIsNull() {
        when(authService.getLoggedInMember("token")).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                recruitService.getLoggedInMember("token")
        );

        assertEquals("로그인이 필요합니다.", exception.getMessage());
    }

    @Test
    void findById_ShouldReturnRecruitDetail() {
        fakeRecruit = Recruit.builder()
                .recruitId(1L)
                .member(fakeMember)
                .place(fakePlace)
                .title("제목")
                .content("내용")
                .isClosed(false)
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .travelStyle(requestDto.getTravelStyle())
                .sameGender(requestDto.isSameGender())
                .sameAge(requestDto.isSameAge())
                .budget(requestDto.getBudget())
                .groupSize(requestDto.getGroupSize())
                .applies(Collections.emptyList()) // 💡 여기!
                .build();
        when(recruitRepository.findById(1L)).thenReturn(Optional.of(fakeRecruit));

        RecruitDetailResponseDto result = recruitService.findById(1L);

        assertEquals("제목", result.getTitle());
    }

    @Test
    void findById_ShouldThrow_WhenRecruitNotFound() {
        when(recruitRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class, () -> recruitService.findById(1L));
    }

    @Test
    void findAll_ShouldReturnList() {
        when(recruitRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(fakeRecruit));

        List<RecruitListResponseDto> result = recruitService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void findRecent3_ShouldReturnList() {
        when(recruitRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(List.of(fakeRecruit));

        List<RecruitListResponseDto> result = recruitService.findRecent3();

        assertEquals(1, result.size());
    }

    @Test
    void searchRecruits_ShouldReturnMatchingList() {
        when(recruitRepository.searchByTitleOrContent("test")).thenReturn(List.of(fakeRecruit));

        List<RecruitListResponseDto> result = recruitService.searchRecruits("test");

        assertEquals(1, result.size());
    }

    @Test
    void searchByIsClosed_ShouldReturnFilteredList() {
        when(recruitRepository.findByIsClosed(false)).thenReturn(List.of(fakeRecruit));

        List<RecruitListResponseDto> result = recruitService.searchByIsClosed(false);

        assertEquals(1, result.size());
    }

    @Test
    void searchAndFilter_ShouldWorkForLoggedInUser() {
        when(authService.getLoggedInMember("token")).thenReturn(fakeMember);
        when(recruitRepository.searchFilterSort(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(List.of(fakeRecruit));

        List<RecruitListResponseDto> result = recruitService.searchAndFilter(
                Optional.of("test"), Optional.of("서울"), Optional.of(false), Optional.of(LocalDate.now()), Optional.of(LocalDate.now().plusDays(1)),
                Optional.of("RELAX"), Optional.of(true), Optional.of(false), Optional.of(0), Optional.of(1000000), Optional.of(1), Optional.of(5), Optional.of("createdAt"), "token"
        );

        assertEquals(1, result.size());
    }

    @Test
    void searchAndFilter_ShouldWorkForAnonymousUser() {
        when(recruitRepository.searchFilterSort(any(), any(), any(), any(), any(), any(), eq(Optional.empty()), eq(Optional.empty()), any(), any(), any(), any(), any(), isNull(), isNull())).thenReturn(List.of(fakeRecruit));

        List<RecruitListResponseDto> result = recruitService.searchAndFilter(
                Optional.of("test"), Optional.of("서울"), Optional.of(false), Optional.of(LocalDate.now()), Optional.of(LocalDate.now().plusDays(1)),
                Optional.of("RELAX"), Optional.of(true), Optional.of(false), Optional.of(0), Optional.of(1000000), Optional.of(1), Optional.of(5), Optional.of("createdAt"), null
        );

        assertEquals(1, result.size());
    }

    @Test
    void update_ShouldSucceed_WhenOwner() {
        when(recruitRepository.findById(1L)).thenReturn(Optional.of(fakeRecruit));
        when(placeRepository.findById(1L)).thenReturn(Optional.of(fakePlace));
        when(authService.getLoggedInMember("token")).thenReturn(fakeMember);

        RecruitDetailResponseDto result = recruitService.update(1L, requestDto, "token");

        assertEquals("제목", result.getTitle());
    }

    @Test
    void update_ShouldThrow_WhenNotOwnerOrAdmin() {
        Member otherMember = Member.builder().id(99L).authority("USER").build();
        Recruit otherRecruit = requestDto.toEntity(otherMember, fakePlace);

        when(recruitRepository.findById(1L)).thenReturn(Optional.of(otherRecruit));
        when(placeRepository.findById(1L)).thenReturn(Optional.of(fakePlace));
        when(authService.getLoggedInMember("token")).thenReturn(fakeMember);

        assertThrows(ServiceException.class, () -> recruitService.update(1L, requestDto, "token"));
    }

    @Test
    void delete_ShouldSucceed_WhenOwner() {
        when(recruitRepository.findById(1L)).thenReturn(Optional.of(fakeRecruit));
        when(authService.getLoggedInMember("token")).thenReturn(fakeMember);

        assertDoesNotThrow(() -> recruitService.delete(1L, "token"));

        verify(recruitRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrow_WhenNotOwnerOrAdmin() {
        Member otherMember = Member.builder().id(99L).authority("USER").build();
        Recruit otherRecruit = requestDto.toEntity(otherMember, fakePlace);

        when(recruitRepository.findById(1L)).thenReturn(Optional.of(otherRecruit));
        when(authService.getLoggedInMember("token")).thenReturn(fakeMember);

        assertThrows(ServiceException.class, () -> recruitService.delete(1L, "token"));
    }
}
