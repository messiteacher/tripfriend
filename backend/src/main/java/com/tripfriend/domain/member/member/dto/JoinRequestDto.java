package com.tripfriend.domain.member.member.dto;

import com.tripfriend.domain.member.member.entity.AgeRange;
import com.tripfriend.domain.member.member.entity.Gender;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.entity.TravelStyle;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestDto {

    @NotBlank(message = "아이디는 필수 입력값입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "아이디는 영문자, 숫자, 언더스코어만 사용 가능합니다.")
    private String username;

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수 입력값입니다")
    private String nickname;

    private String profileImage;

    @NotNull(message = "성별은 필수 입력값입니다.(남성 / 여성)")
    private Gender gender;

    @NotNull(message = "나이대는 필수 입력값입니다.(10대 / 20대 / 30대 / 40대 이상)")
    private AgeRange ageRange;

    @NotNull(message = "여행 스타일은 필수 입력값입니다.(관광 / 휴양 / 쇼핑)")
    private TravelStyle travelStyle;

    private String aboutMe;

    public Member toEntity() {

        Member member = new Member();

        member.setUsername(username);
        member.setEmail(email);
        member.setPassword(password);
        member.setNickname(nickname);
        member.setProfileImage(profileImage);
        member.setGender(gender);
        member.setAgeRange(ageRange);
        member.setTravelStyle(travelStyle);
        member.setAboutMe(aboutMe);
        member.setRating(0.0);
        member.setAuthority("USER");
        member.setVerified(false);

        return member;
    }
}
