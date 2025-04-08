package com.tripfriend.domain.member.member.service;

import com.tripfriend.domain.member.member.dto.JoinRequestDto;
import com.tripfriend.domain.member.member.dto.MemberResponseDto;
import com.tripfriend.domain.member.member.dto.MemberUpdateRequestDto;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import com.tripfriend.global.util.ImageUtil;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthService authService;
    private final MailService mailService;
    private final ImageUtil imageUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponseDto join(JoinRequestDto joinRequestDto) throws MessagingException {

        String email = joinRequestDto.getEmail();

        // 중복 검사
        if (memberRepository.existsByUsername(joinRequestDto.getUsername())) {
            throw new RuntimeException("이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByEmail(joinRequestDto.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        if (memberRepository.existsByNickname(joinRequestDto.getNickname())) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }

        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(joinRequestDto.getPassword());

        // DTO를 엔티티로 변환하고 암호화된 비밀번호를 설정
        Member member = joinRequestDto.toEntity();
        member.setPassword(encryptedPassword);
        Member savedMember = memberRepository.save(member);

        return MemberResponseDto.fromEntity(savedMember);
    }

    @Transactional
    public MemberResponseDto updateMember(Long id, MemberUpdateRequestDto memberUpdateRequestDto) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));

        // 이메일 중복 검사 (변경된 경우에만)
        if (memberUpdateRequestDto.getEmail() != null && !memberUpdateRequestDto.getEmail().equals(member.getEmail())) {
            if (memberRepository.existsByEmail(memberUpdateRequestDto.getEmail())) {
                throw new RuntimeException("이미 사용 중인 이메일입니다.");
            }
            member.setEmail(memberUpdateRequestDto.getEmail());
        }

        // 닉네임 중복 검사 (변경된 경우에만)
        if (memberUpdateRequestDto.getNickname() != null && !memberUpdateRequestDto.getNickname().equals(member.getNickname())) {
            if (memberRepository.existsByNickname(memberUpdateRequestDto.getNickname())) {
                throw new RuntimeException("이미 사용 중인 닉네임입니다.");
            }
            member.setNickname(memberUpdateRequestDto.getNickname());
        }

        // 비밀번호 변경 (값이 있는 경우만)
        if (memberUpdateRequestDto.getPassword() != null && !memberUpdateRequestDto.getPassword().isEmpty()) {
            member.setPassword(memberUpdateRequestDto.getPassword());
        }

        // 나머지 필드 업데이트 (null이 아닌 경우만)
        if (memberUpdateRequestDto.getProfileImage() != null) {
            member.setProfileImage(memberUpdateRequestDto.getProfileImage());
        }
        if (memberUpdateRequestDto.getGender() != null) {
            member.setGender(memberUpdateRequestDto.getGender());
        }
        if (memberUpdateRequestDto.getAgeRange() != null) {
            member.setAgeRange(memberUpdateRequestDto.getAgeRange());
        }
        if (memberUpdateRequestDto.getTravelStyle() != null) {
            member.setTravelStyle(memberUpdateRequestDto.getTravelStyle());
        }
        if (memberUpdateRequestDto.getAboutMe() != null) {
            member.setAboutMe(memberUpdateRequestDto.getAboutMe());
        }

        member.setUpdatedAt(LocalDateTime.now());
        Member updatedMember = memberRepository.save(member);

        return MemberResponseDto.fromEntity(updatedMember);
    }

    @Transactional
    public void deleteMember(Long id, HttpServletRequest request, HttpServletResponse response) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));

        authService.logout(request, response);

        member.setDeleted(true);
        member.setDeletedAt(LocalDateTime.now());
        memberRepository.save(member);
    }

    @Transactional
    public void restoreMember(Long id) {
        Member member = memberRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않거나 이미 활성화된 회원입니다."));

        // 복구 가능 기간 확인
        if (!member.canBeRestored()) {
            throw new RuntimeException("계정 복구 기간이 만료되었습니다.");
        }

        member.setDeleted(false);
        member.setDeletedAt(null);
        memberRepository.save(member);
    }

    public MemberResponseDto getMyPage(Long id, String username) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

        // 보안 강화: 본인의 정보만 조회할 수 있도록 체크
        if (!member.getUsername().equals(username)) {
            throw new AccessDeniedException("본인만 확인할 수 있습니다.");
        }

        return MemberResponseDto.fromEntity(member);
    }
    //회원 조회

    public List<MemberResponseDto> getAllMembers() {
        List<Member> members = memberRepository.findAll();
        return members.stream()
                .map(MemberResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void purgeExpiredDeletedMembers() {

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<Member> expiredMembers = memberRepository.findByDeletedTrueAndDeletedAtBefore(cutoffDate);
        memberRepository.deleteAll(expiredMembers); // 실제 DB에서 삭제
    }

    public boolean isSoftDeleted(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        // deleted 필드가 있다고 가정
        return member.getDeleted();
    }

    @Transactional
    public String uploadProfileImage(Long memberId, MultipartFile profileImage) throws IOException {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        // 기존 프로필 이미지 삭제 (선택 사항)
        if (member.getProfileImage() != null) {
            imageUtil.deleteImage(member.getProfileImage());
        }

        // 새 이미지 저장
        String profileImageUrl = imageUtil.saveImage(profileImage);
        member.setProfileImage(profileImageUrl);
        memberRepository.save(member);

        return profileImageUrl; // 저장된 이미지 경로 반환
    }

    @Transactional
    public void deleteProfileImage(Long memberId) throws IOException {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        if (member.getProfileImage() != null) {
            imageUtil.deleteImage(member.getProfileImage());
            member.setProfileImage(null);
            memberRepository.save(member);
        }
    }
}
