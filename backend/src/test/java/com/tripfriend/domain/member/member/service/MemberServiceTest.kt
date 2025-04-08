package com.tripfriend.domain.member.member.service

import com.tripfriend.domain.member.member.dto.JoinRequestDto
import com.tripfriend.domain.member.member.dto.MemberUpdateRequestDto
import com.tripfriend.domain.member.member.entity.AgeRange
import com.tripfriend.domain.member.member.entity.Gender
import com.tripfriend.domain.member.member.entity.Member
import com.tripfriend.domain.member.member.entity.TravelStyle
import com.tripfriend.domain.member.member.repository.MemberRepository
import com.tripfriend.global.util.ImageUtil
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.*

/**
 * 회원 서비스 테스트 클래스
 * MockK 확장을 사용하여 테스트 의존성을 관리함
 */
@ExtendWith(MockKExtension::class)
class MemberServiceTest {

    @MockK
    private lateinit var memberRepository: MemberRepository

    @MockK
    private lateinit var authService: AuthService

    @MockK
    private lateinit var mailService: MailService

    @MockK
    private lateinit var imageUtil: ImageUtil

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var memberService: MemberService

    /**
     * 각 테스트 전에 실행되는 초기화 메서드
     * 필요한 모든 모킹 객체와 서비스를 설정함
     */
    @BeforeEach
    fun setUp() {

        MockKAnnotations.init(this)

        memberService = MemberService(
            memberRepository,
            authService,
            mailService,
            imageUtil,
            passwordEncoder
        )
    }

    /**
     * 회원 가입 성공 시나리오 테스트
     * 새로운 사용자가 유효한 정보로 가입하는 경우를 검증
     */
    @Test
    @DisplayName("회원 가입 성공 테스트")
    fun joinSuccess() {

        // Given
        val joinRequestDto = mockk<JoinRequestDto>()
        val encryptedPassword = "encrypted_password"

        // relaxed = true 설정으로 모든 setter 메서드를 처리하는 Member 모킹 객체 생성
        val member = mockk<Member>(relaxed = true)

        // joinRequestDto 설정
        every { joinRequestDto.username } returns "testuser"
        every { joinRequestDto.email } returns "test@example.com"
        every { joinRequestDto.password } returns "password123"
        every { joinRequestDto.nickname } returns "테스트유저"
        every { joinRequestDto.toEntity() } returns member

        // member 객체의 동작 정의
        every { member.id } returns 1L
        every { member.username } returns "testuser"
        every { member.email } returns "test@example.com"
        every { member.nickname } returns "테스트유저"

        // When
        every { memberRepository.existsByUsername("testuser") } returns false
        every { memberRepository.existsByEmail("test@example.com") } returns false
        every { memberRepository.existsByNickname("테스트유저") } returns false
        every { passwordEncoder.encode("password123") } returns encryptedPassword
        every { memberRepository.save(any()) } returns member

        // Then
        val result = memberService.join(joinRequestDto)

        // Verify
        verify { memberRepository.existsByUsername("testuser") }
        verify { memberRepository.existsByEmail("test@example.com") }
        verify { memberRepository.existsByNickname("테스트유저") }
        verify { passwordEncoder.encode("password123") }
        verify { member.password = encryptedPassword }  // 비밀번호가 설정되었는지 확인
        verify { memberRepository.save(any()) }

        assertEquals(member.id, result.id)
        assertEquals(member.username, result.username)
        assertEquals(member.email, result.email)
        assertEquals(member.nickname, result.nickname)
    }

    /**
     * 회원 가입 실패 테스트 - 이미 사용 중인 사용자명으로 가입 시도
     * 중복된 사용자명으로 가입 시 예외 발생을 검증
     */
    @Test
    @DisplayName("회원 가입 실패 - 이미 존재하는 사용자명")
    fun joinFailUsername() {

        // Given
        val joinRequestDto = JoinRequestDto().apply {
            username = "existinguser"
            email = "test@example.com"
            password = "password123"
            nickname = "테스트유저"
        }

        // When
        every { memberRepository.existsByUsername(joinRequestDto.username) } returns true

        // Then
        val exception = assertThrows(RuntimeException::class.java) {
            memberService.join(joinRequestDto)
        }

        // Verify
        verify { memberRepository.existsByUsername(joinRequestDto.username) }
        assertEquals("이미 사용 중인 아이디입니다.", exception.message)
    }

    /**
     * 회원 정보 업데이트 성공 테스트
     * 기존 회원 정보를 새로운 정보로 업데이트하는 기능 검증
     */
    @Test
    @DisplayName("회원 정보 업데이트 성공 테스트")
    fun updateMemberSuccess() {

        // Given
        val memberId = 1L
        val updateRequestDto = MemberUpdateRequestDto.builder()
            .email("updated@example.com")
            .nickname("업데이트유저")
            .password("newpassword123")
            .gender(Gender.FEMALE)
            .ageRange(AgeRange.THIRTIES)
            .travelStyle(TravelStyle.SHOPPING)
            .aboutMe("프로필 업데이트")
            .build()

        val originalMember = Member(
            id = memberId,
            username = "testuser",
            email = "test@example.com",
            password = "encrypted_password",
            nickname = "테스트유저",
            gender = Gender.MALE,
            ageRange = AgeRange.TWENTIES,
            travelStyle = TravelStyle.TOURISM,
            aboutMe = "안녕하세요",
            rating = 0.0,
            authority = "ROLE_USER",
            verified = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val updatedMember = originalMember.copy(
            email = updateRequestDto.email,
            password = updateRequestDto.password,
            nickname = updateRequestDto.nickname,
            gender = updateRequestDto.gender,
            ageRange = updateRequestDto.ageRange,
            travelStyle = updateRequestDto.travelStyle,
            aboutMe = updateRequestDto.aboutMe,
            updatedAt = LocalDateTime.now()
        )

        // When
        every { memberRepository.findById(memberId) } returns Optional.of(originalMember)
        every { memberRepository.existsByEmail(updateRequestDto.email!!) } returns false
        every { memberRepository.existsByNickname(updateRequestDto.nickname!!) } returns false
        every { memberRepository.save(any()) } returns updatedMember

        // Then
        val result = memberService.updateMember(memberId, updateRequestDto)

        // Verify
        verify { memberRepository.findById(memberId) }
        verify { memberRepository.existsByEmail(updateRequestDto.email!!) }
        verify { memberRepository.existsByNickname(updateRequestDto.nickname!!) }
        verify { memberRepository.save(any()) }

        assertEquals(updateRequestDto.email, result.email)
        assertEquals(updateRequestDto.nickname, result.nickname)
        assertEquals(updateRequestDto.gender, result.gender)
        assertEquals(updateRequestDto.ageRange, result.ageRange)
        assertEquals(updateRequestDto.travelStyle, result.travelStyle)
        assertEquals(updateRequestDto.aboutMe, result.aboutMe)
    }

    /**
     * 회원 정보 업데이트 실패 테스트 - 존재하지 않는 회원
     * 존재하지 않는 회원 ID로 업데이트를 시도할 때 예외 발생을 검증
     */
    @Test
    @DisplayName("회원 정보 업데이트 실패 - 존재하지 않는 회원")
    fun updateMemberFailNotFound() {
        // Given
        val memberId = 1L
        val updateRequestDto = MemberUpdateRequestDto.builder()
            .email("updated@example.com")
            .build()

        // When
        every { memberRepository.findById(memberId) } returns Optional.empty()

        // Then
        val exception = assertThrows(RuntimeException::class.java) {
            memberService.updateMember(memberId, updateRequestDto)
        }

        // Verify
        verify { memberRepository.findById(memberId) }
        assertEquals("존재하지 않는 회원입니다.", exception.message)
    }

    /**
     * 회원 삭제(소프트 삭제) 성공 테스트
     * 회원 정보를 실제로 삭제하지 않고 삭제 표시만 하는 소프트 삭제 기능 검증
     */
    @Test
    @DisplayName("회원 삭제 성공 테스트")
    fun deleteMemberSuccess() {
        // Given
        val memberId = 1L
        val member = Member(
            id = memberId,
            username = "testuser",
            email = "test@example.com",
            password = "password",
            nickname = "테스트유저",
            gender = Gender.MALE,
            ageRange = AgeRange.TWENTIES,
            travelStyle = TravelStyle.TOURISM,
            rating = 0.0,
            authority = "ROLE_USER",
            verified = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val request = mockk<HttpServletRequest>()
        val response = mockk<HttpServletResponse>()

        // 삭제된 회원 정보를 모킹
        val deletedMember = member.copy(
            updatedAt = LocalDateTime.now()
        )

        // deletedAt과 deleted 상태를 확인
        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { authService.logout(request, response) } just runs
        every { memberRepository.save(any()) } answers {
            // 저장할 때 deleted와 deletedAt이 설정되었음을 검증
            val savedMember = firstArg<Member>()
            assertTrue(savedMember.deleted)  // getter 사용
            assertNotNull(savedMember.deletedAt)  // getter 사용
            deletedMember
        }

        // Then
        memberService.deleteMember(memberId, request, response)

        // Verify
        verify { memberRepository.findById(memberId) }
        verify { authService.logout(request, response) }
        verify { memberRepository.save(any()) }
    }

    /**
     * 삭제된 회원 복구 성공 테스트
     * 소프트 삭제된 회원 정보를 복구하는 기능 검증
     */
    @Test
    @DisplayName("회원 복구 성공 테스트")
    fun restoreMemberSuccess() {
        // Given
        val memberId = 1L
        val deletedMember = mockk<Member>(relaxed = true)  // 모든 setter를 처리하기 위해 relaxed 모킹 사용

        // 삭제된 회원 동작 정의
        every { deletedMember.deleted } returns true
        every { deletedMember.deletedAt } returns LocalDateTime.now().minusDays(5)
        every { deletedMember.canBeRestored() } returns true

        // 레포지토리 메서드 모킹
        every { memberRepository.findByIdAndDeletedTrue(memberId) } returns Optional.of(deletedMember)
        every { memberRepository.save(any()) } returns deletedMember

        // Then
        memberService.restoreMember(memberId)

        // Verify
        verify { memberRepository.findByIdAndDeletedTrue(memberId) }
        verify { deletedMember.deleted = false }  // isDeleted setter가 false로 호출되었는지 확인
        verify { deletedMember.deletedAt = null }   // deletedAt setter가 null로 호출되었는지 확인
        verify { memberRepository.save(any()) }
    }

    /**
     * 회원 복구 실패 테스트 - 복구 기간 만료
     * 삭제 후 복구 가능 기간이 지난 경우 복구 실패를 검증
     */
    @Test
    @DisplayName("회원 복구 실패 - 복구 기간 만료")
    fun restoreMemberFailExpired() {
        // Given
        val memberId = 1L
        val member = mockk<Member>()

        // When
        every { memberRepository.findByIdAndDeletedTrue(memberId) } returns Optional.of(member)
        every { member.canBeRestored() } returns false

        // Then
        val exception = assertThrows(RuntimeException::class.java) {
            memberService.restoreMember(memberId)
        }

        // Verify
        verify { memberRepository.findByIdAndDeletedTrue(memberId) }
        assertEquals("계정 복구 기간이 만료되었습니다.", exception.message)
    }

    /**
     * 마이페이지 조회 성공 테스트
     * 사용자 본인이 마이페이지를 조회하는 기능 검증
     */
    @Test
    @DisplayName("마이페이지 조회 성공 테스트")
    fun getMyPageSuccess() {
        // Given
        val memberId = 1L
        val username = "testuser"
        val member = Member(
            id = memberId,
            username = "testuser",
            email = "test@example.com",
            password = "password",
            nickname = "테스트유저",
            gender = Gender.MALE,
            ageRange = AgeRange.TWENTIES,
            travelStyle = TravelStyle.TOURISM,
            rating = 0.0,
            authority = "ROLE_USER",
            verified = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // When
        every { memberRepository.findById(memberId) } returns Optional.of(member)

        // Then
        val result = memberService.getMyPage(memberId, username)

        // Verify
        verify { memberRepository.findById(memberId) }
        assertEquals(memberId, result.id)
        assertEquals(username, result.username)
    }

    /**
     * 마이페이지 조회 실패 테스트 - 권한 없음
     * 다른 사용자의 마이페이지를 조회하려 할 때 권한 거부 예외 발생 검증
     */
    @Test
    @DisplayName("마이페이지 조회 실패 - 권한 없음")
    fun getMyPageFailNoPermission() {
        // Given
        val memberId = 1L
        val username = "testuser"
        val otherUsername = "otheruser"
        val member = Member(
            id = memberId,
            username = "testuser",
            email = "test@example.com",
            password = "password",
            nickname = "테스트유저",
            gender = Gender.MALE,
            ageRange = AgeRange.TWENTIES,
            travelStyle = TravelStyle.TOURISM,
            rating = 0.0,
            authority = "ROLE_USER",
            verified = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // When
        every { memberRepository.findById(memberId) } returns Optional.of(member)

        // Then
        val exception = assertThrows(AccessDeniedException::class.java) {
            memberService.getMyPage(memberId, otherUsername)
        }

        // Verify
        verify { memberRepository.findById(memberId) }
        assertEquals("본인만 확인할 수 있습니다.", exception.message)
    }

    /**
     * 모든 회원 조회 테스트
     * 시스템에 등록된 모든 회원 정보를 조회하는 기능 검증
     */
    @Test
    @DisplayName("모든 회원 조회 테스트")
    fun getAllMembersTest() {
        // Given
        val members = listOf(
            Member(
                id = 1L,
                username = "user1",
                password = "password1",
                email = "user1@test.com",
                nickname = "유저1",
                gender = Gender.MALE,
                ageRange = AgeRange.TWENTIES,
                travelStyle = TravelStyle.TOURISM,
                rating = 0.0,
                authority = "ROLE_USER",
                verified = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            Member(
                id = 2L,
                username = "user2",
                password = "password2",
                email = "user2@test.com",
                nickname = "유저2",
                gender = Gender.FEMALE,
                ageRange = AgeRange.THIRTIES,
                travelStyle = TravelStyle.SHOPPING,
                rating = 0.0,
                authority = "ROLE_USER",
                verified = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            Member(
                id = 3L,
                username = "user3",
                password = "password2",
                email = "user3@test.com",
                nickname = "유저3",
                gender = Gender.MALE,
                ageRange = AgeRange.FORTIES_PLUS,
                travelStyle = TravelStyle.RELAXATION,
                rating = 0.0,
                authority = "ROLE_USER",
                verified = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        // When
        every { memberRepository.findAll() } returns members

        // Then
        val result = memberService.getAllMembers()

        // Verify
        verify { memberRepository.findAll() }
        assertEquals(3, result.size)
        assertEquals("user1", result[0].username)
        assertEquals("user2", result[1].username)
        assertEquals("user3", result[2].username)
    }

    /**
     * 만료된 삭제 회원 정리 테스트
     * 복구 기간이 지난 소프트 삭제된 회원 정보를 영구 삭제하는 기능 검증
     */
    @Test
    @DisplayName("만료된 삭제 회원 정리 테스트")
    fun purgeExpiredDeletedMembersTest() {
        // Given
        val expiredMembers = listOf(
            Member(
                id = 1L,
                username = "expired1",
                password = "password1",
                email = "",
                nickname = "",
                gender = Gender.MALE, // 또는 기본값에 맞게 적절히 설정
                ageRange = AgeRange.TWENTIES, // 또는 적절한 기본값
                travelStyle = TravelStyle.TOURISM, // 기본값 필요
                rating = 0.0,
                authority = "ROLE_USER",
                verified = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            Member(
                id = 2L,
                username = "expired2",
                password = "password2",
                email = "",
                nickname = "",
                gender = Gender.MALE,
                ageRange = AgeRange.TWENTIES,
                travelStyle = TravelStyle.TOURISM,
                rating = 0.0,
                authority = "ROLE_USER",
                verified = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        // When
        every { memberRepository.findByDeletedTrueAndDeletedAtBefore(any()) } returns expiredMembers
        every { memberRepository.deleteAll(expiredMembers) } just runs

        // Then
        memberService.purgeExpiredDeletedMembers()

        // Verify
        verify { memberRepository.findByDeletedTrueAndDeletedAtBefore(any()) }
        verify { memberRepository.deleteAll(expiredMembers) }
    }

    /**
     * 소프트 삭제 상태 확인 테스트
     * 특정 회원이 소프트 삭제된 상태인지 확인하는 기능 검증
     */
    @Test
    @DisplayName("소프트 삭제 상태 확인 테스트")
    fun isSoftDeletedTest() {
        // Given
        val memberId = 1L
        val member = mockk<Member>()

        // When
        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { member.deleted } returns true

        // Then
        val result = memberService.isSoftDeleted(memberId)

        // Verify
        verify { memberRepository.findById(memberId) }
        assertTrue(result)
    }

    /**
     * 프로필 이미지 업로드 테스트
     * 회원의 프로필 이미지를 새로 업로드하는 기능 검증
     */
    @Test
    @DisplayName("프로필 이미지 업로드 테스트")
    fun uploadProfileImageTest() {
        // Given
        val memberId = 1L
        val member = mockk<Member>(relaxed = true)  // setter를 처리하기 위한 relaxed 모킹
        val profileImage = mockk<MultipartFile>()
        val imageUrl = "http://example.com/images/profile.jpg"

        // When
        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { member.profileImage } returns null
        every { imageUtil.saveImage(profileImage) } returns imageUrl
        every { memberRepository.save(any()) } returns member

        // Then
        val result = memberService.uploadProfileImage(memberId, profileImage)

        // Verify
        verify { memberRepository.findById(memberId) }
        verify { imageUtil.saveImage(profileImage) }
        verify { member.profileImage = imageUrl }  // setter가 호출되었는지 확인
        verify { memberRepository.save(any()) }
        assertEquals(imageUrl, result)
    }

    /**
     * 기존 프로필 이미지가 있는 경우 프로필 이미지 업로드 테스트
     * 이미 프로필 이미지가 있는 회원이 새 이미지를 업로드할 때 이전 이미지 삭제 처리 검증
     */
    @Test
    @DisplayName("기존 프로필 이미지가 있는 경우 프로필 이미지 업로드 테스트")
    fun uploadProfileImageWithExistingImageTest() {
        // Given
        val memberId = 1L
        val oldImageUrl = "http://example.com/images/old-profile.jpg"
        val member = mockk<Member>(relaxed = true)  // setter를 처리하기 위한 relaxed 모킹
        val profileImage = mockk<MultipartFile>()
        val newImageUrl = "http://example.com/images/new-profile.jpg"

        // When
        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { member.profileImage } returns oldImageUrl
        every { imageUtil.deleteImage(oldImageUrl) } just runs
        every { imageUtil.saveImage(profileImage) } returns newImageUrl
        every { memberRepository.save(any()) } returns member

        // Then
        val result = memberService.uploadProfileImage(memberId, profileImage)

        // Verify
        verify { memberRepository.findById(memberId) }
        verify { imageUtil.deleteImage(oldImageUrl) }
        verify { imageUtil.saveImage(profileImage) }
        verify { member.profileImage = newImageUrl }  // setter가 호출되었는지 확인
        verify { memberRepository.save(any()) }
        assertEquals(newImageUrl, result)
    }

    /**
     * 프로필 이미지 삭제 테스트
     * 회원의 프로필 이미지를 삭제하는 기능 검증
     */
    @Test
    @DisplayName("프로필 이미지 삭제 테스트")
    fun deleteProfileImageTest() {
        // Given
        val memberId = 1L
        val imageUrl = "http://example.com/images/profile.jpg"
        val member = mockk<Member>(relaxed = true)  // setter를 처리하기 위한 relaxed 모킹

        // When
        every { memberRepository.findById(memberId) } returns Optional.of(member)
        every { member.profileImage } returns imageUrl
        every { imageUtil.deleteImage(imageUrl) } just runs
        every { memberRepository.save(any()) } returns member

        // Then
        memberService.deleteProfileImage(memberId)

        // Verify
        verify { memberRepository.findById(memberId) }
        verify { imageUtil.deleteImage(imageUrl) }
        verify { member.profileImage = null }  // setter가 null로 호출되었는지 확인
        verify { memberRepository.save(any()) }
    }
}