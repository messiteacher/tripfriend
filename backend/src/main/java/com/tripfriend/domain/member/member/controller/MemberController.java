package com.tripfriend.domain.member.member.controller;

import com.tripfriend.domain.member.member.dto.*;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.member.member.service.MailService;
import com.tripfriend.domain.member.member.service.MemberService;
import com.tripfriend.global.annotation.CheckPermission;
import com.tripfriend.global.dto.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Member API", description = "회원관련 기능을 제공합니다.")
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;
    private final MailService mailService;

    //회원정보 조회
    @Operation(summary = "회원정보 조회")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String token) {
        Member member = authService.getLoggedInMember(token);

        Map<String, Object> response = new HashMap<>();
        response.put("id", member.getId());
        response.put("username", member.getUsername());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원가입")
    @PostMapping("/join")
    public RsData<MemberResponseDto> join(@Valid @RequestBody JoinRequestDto joinRequestDto) throws MessagingException {

        MemberResponseDto savedMember = memberService.join(joinRequestDto);
        return new RsData<>("201-1", "회원가입이 완료되었습니다.", savedMember);
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public RsData<AuthResponseDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {

        AuthResponseDto authResponse = authService.login(loginRequestDto, response);
        return new RsData<>("200-1", "로그인 성공", authResponse);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public RsData<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        authService.logout(request, response);
        return new RsData<>("200-1", "로그아웃이 완료되었습니다.", null);
    }

    @Operation(summary = "액세스 토큰 재발급")
    @PostMapping("/refresh")
    public RsData<AuthResponseDto> refresh(@CookieValue(name = "accessToken", required = false) String accessToken, HttpServletResponse response) {

        try {
            if (accessToken == null) {
                return new RsData<>("401-2", "액세스 토큰이 없습니다.", null);
            }

            AuthResponseDto authResponseDto = authService.refreshToken(accessToken, response);
            return new RsData<>("200-1", "토큰이 재발급되었습니다.", authResponseDto);
        } catch (Exception e) {
            return new RsData<>("401-1", "토큰 재발급에 실패했습니다: " + e.getMessage(), null);
        }
    }

    @Operation(summary = "회원정보 수정")
    @PutMapping("/update")
    public RsData<MemberResponseDto> updateMember(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody MemberUpdateRequestDto memberUpdateRequestDto) {

        Member loggedInMember = authService.getLoggedInMember(token);

        MemberResponseDto response = memberService.updateMember(loggedInMember.getId(), memberUpdateRequestDto);
        return new RsData<>("200-1", "회원 정보가 수정되었습니다.", response);
    }

    @Operation(summary = "회원 삭제")
    @DeleteMapping("/delete")
    public RsData<Void> deleteMember(@RequestHeader(value = "Authorization", required = false) String token, HttpServletRequest request,
                                     HttpServletResponse response) {

        Member loggedInMember = authService.getLoggedInMember(token);

        memberService.deleteMember(loggedInMember.getId(), request, response);
        return new RsData<>("204-1", "회원이 삭제되었습니다.", null);
    }

    @Operation(summary = "회원 복구")
    @PostMapping("/restore")
    public RsData<Void> restoreMember(@RequestHeader(value = "Authorization", required = false) String token) {

        Member loggedInMember = authService.getLoggedInMember(token);
        memberService.restoreMember(loggedInMember.getId());

        return new RsData<>("200-1", "계정이 성공적으로 복구되었습니다.", null);
    }

    @Operation(summary = "이메일 인증 코드 전송")
    @GetMapping("/auth/verify-email")
    public RsData<Void> requestAuthCode(String email) throws MessagingException {

        boolean isSend = mailService.sendAuthCode(email);
        return isSend
                ? new RsData<>("200-1", "인증 코드가 전송되었습니다.", null)
                : new RsData<>("500-1", "인증 코드 전송이 실패하였습니다.", null);
    }

    @Operation(summary = "이메일 인증")
    @PostMapping("/auth/email")
    public RsData<Void> validateAuthCode(@RequestBody @Valid EmailVerificationRequestDto emailVerificationRequestDto) {

        boolean isSuccess = mailService.validationAuthCode(emailVerificationRequestDto);
        return isSuccess
                ? new RsData<>("200-1", "이메일 인증에 성공하였습니다.", null)
                : new RsData<>("400-1", "이메일 인증에 실패하였습니다.", null);
    }

    @Operation(summary = "마이페이지")
    @GetMapping("/mypage")
    public RsData<MemberResponseDto> getMyPage(@RequestHeader(value = "Authorization", required = false) String token) {

        Member loggedInMember = authService.getLoggedInMember(token);

        MemberResponseDto response = memberService.getMyPage(loggedInMember.getId(), loggedInMember.getUsername());
        return new RsData<>("200-1", "마이페이지 정보 조회 성공", response);
    }

    //관리자 회원 조회
    @Operation(summary = "전체 회원 목록 조회 (관리자 전용)")
    @GetMapping("/all")
    @CheckPermission("ADMIN") //관리자
    public ResponseEntity<List<MemberResponseDto>> getAllMembers() {
        List<MemberResponseDto> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    @Operation(summary = "프로필 이미지 등록")
    @PostMapping(value = "/profile-image/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RsData<String> uploadProfileImage(@RequestHeader(value = "Authorization", required = false) String token,
                                             @RequestPart("image") MultipartFile imageFile) throws IOException {

        Member loggedInMember = authService.getLoggedInMember(token);
        String imageUrl = memberService.uploadProfileImage(loggedInMember.getId(), imageFile);

        return new RsData<>("200-1", "이미지 업로드 성공", imageUrl);
    }

    @Operation(summary = "프로필 이미지 삭제")
    @DeleteMapping("/profile-image/delete")
    public RsData<String> deleteProfileImage(@RequestHeader(value = "Authorization", required = false) String token) throws IOException {

        Member loggedInMember = authService.getLoggedInMember(token);

        if (loggedInMember.getProfileImage() == null) {
            return new RsData<>("400-1", "삭제할 이미지가 없습니다.", null);
        }

        memberService.deleteProfileImage(loggedInMember.getId());

        return new RsData<>("200-1", "이미지 삭제 성공", null);
    }
}