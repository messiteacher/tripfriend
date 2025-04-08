package com.tripfriend.domain.place.place.service

import com.tripfriend.domain.place.place.dto.PlaceCreateReqDto
import com.tripfriend.domain.place.place.entity.Category
import com.tripfriend.domain.place.place.entity.Place
import com.tripfriend.domain.place.place.repository.PlaceRepository
import com.tripfriend.global.exception.ServiceException
import com.tripfriend.global.util.ImageUtil
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.util.*

@ExtendWith(MockKExtension::class)
class PlaceServiceTest {
    @MockK
    private lateinit var placeRepository: PlaceRepository

    @MockK
    private lateinit var imageUtil: ImageUtil

    private lateinit var placeService: PlaceService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        placeService = PlaceService(placeRepository, imageUtil)
    }

    @Test
    @DisplayName("여행 장소 등록 테스트 - 이미지 파일이 없는 경우")
    fun createPlaceWithoutImageTest() {
        // Given
        val req =
            PlaceCreateReqDto().apply {
                cityName = "서울"
                placeName = "남산타워"
                description = "서울의 등대"
                category = Category.PLACE
                imageUrl = null // 이미지 파일 없음
            }

        val savedPlace =
            Place().apply {
                id = 1L
                cityName = req.cityName
                placeName = req.placeName
                description = req.description
                category = req.category
                imageUrl = null
            }

        every { placeRepository.save(any()) } returns savedPlace

        // When
        val result = placeService.createPlace(req)

        // Then
        assertEquals("서울", result.cityName)
        assertEquals("남산타워", result.placeName)
        assertNull(result.imageUrl)
        verify(exactly = 1) { placeRepository.save(any()) }
    }

    @Test
    @DisplayName("여행 장소 등록 테스트 - 이미지 파일이 있는 경우")
    fun createPlaceWithImageTest() {
        // Given
        val mockImageUrl = "http://example.com/image.jpg"
        val mockFile = mockk<MultipartFile>()
        every { mockFile.isEmpty } returns false

        val req =
            PlaceCreateReqDto().apply {
                cityName = "부산"
                placeName = "감천문화마을"
                description = "벽화가 아름다운 마을"
                category = Category.PLACE
                imageUrl = mockFile
            }

        every { imageUtil.saveImage(mockFile) } returns mockImageUrl

        val savedPlace =
            Place().apply {
                id = 2L
                cityName = req.cityName
                placeName = req.placeName
                description = req.description
                category = req.category
                imageUrl = req.imageUrl.toString()
            }
        every { placeRepository.save(any()) } returns savedPlace

        // When
        val result = placeService.createPlace(req)

        // Then
        assertEquals("부산", result.cityName)
        assertEquals(mockImageUrl, result.imageUrl)
        verify(exactly = 1) { imageUtil.saveImage(mockFile) }
        verify(exactly = 1) { placeRepository.save(any()) }
    }

    @Test
    @DisplayName("여행 장소 등록 테스트 - 이미지 업로드 실패 시 ServiceException 발생")
    fun createPlaceImageUploadFailureTest() {
        // Given
        val mockFile = mockk<MultipartFile>()
        every { mockFile.isEmpty } returns false

        val req =
            PlaceCreateReqDto().apply {
                cityName = "제주"
                placeName = "한라산"
                description = "백록담이 아름다운 산"
                category = Category.PLACE
                imageUrl = mockFile
            }

        every { imageUtil.saveImage(mockFile) } throws IOException("Upload error")

        // When, Then
        val exception =
            assertThrows(ServiceException::class.java) {
                placeService.createPlace(req)
            }
        assertEquals("404-2", exception.code)
        verify(exactly = 1) { imageUtil.saveImage(mockFile) }
        verify { placeRepository wasNot Called }
    }

    @Test
    @DisplayName("전체 여행 장소 조회 테스트")
    fun getAllPlacesTest() {
        // Given
        val places =
            listOf(
                Place().apply { id = 1L },
                Place().apply { id = 2L },
            )
        every { placeRepository.findAll() } returns places

        // When
        val result = placeService.getAllPlaces()

        // Then
        assertEquals(2, result.size)
        verify(exactly = 1) { placeRepository.findAll() }
    }

    @Test
    @DisplayName("특정 도시의 여행 장소 조회 테스트")
    fun getPlacesByCityTest() {
        // Given
        val city = "Seoul"
        val places =
            listOf(
                Place().apply { cityName = city },
                Place().apply { cityName = city },
            )
        every { placeRepository.findByCityName(city) } returns places

        // When
        val result = placeService.getPlacesByCity(city)

        // Then
        assertTrue(result.all { it.cityName == city })
        verify(exactly = 1) { placeRepository.findByCityName(city) }
    }

    @Test
    @DisplayName("여행 장소 단건 조회 테스트 - 존재하는 경우")
    fun getPlaceSuccessTest() {
        // Given
        val placeId = 1L
        val place = Place().apply { id = placeId }
        every { placeRepository.findById(placeId) } returns Optional.of(place)

        // When
        val result = placeService.getPlace(placeId)

        // Then
        assertEquals(placeId, result.id)
        verify(exactly = 1) { placeRepository.findById(placeId) }
    }

    @Test
    @DisplayName("여행 장소 단건 조회 테스트 - 존재하지 않는 경우")
    fun getPlaceNotFoundTest() {
        // Given
        val placeId = 9999L
        every { placeRepository.findById(placeId) } returns Optional.empty()

        // When & Then
        val exception =
            assertThrows(ServiceException::class.java) {
                placeService.getPlace(placeId)
            }
        assertEquals("404-1", exception.code)
        verify(exactly = 1) { placeRepository.findById(placeId) }
    }

    @Test
    @DisplayName("여행 장소 삭제 테스트")
    fun deletePlaceTest() {
        // Given
        val place = Place().apply { id = 1L }
        every { placeRepository.delete(place) } just Runs // void 메서드에 대한 stub 설정

        // When
        placeService.deletePlace(place)

        // Then
        verify(exactly = 1) { placeRepository.delete(place) }
    }

    @Test
    @DisplayName("여행 장소 검색 테스트 - 이름과 도시 모두 제공된 경우")
    fun searchPlaceWithNameAndCityTest() {
        // Given
        val name = "숲"
        val city = "서울"
        val places =
            listOf(
                Place().apply {
                    placeName = "서울숲"
                    cityName = "서울"
                },
            )
        every { placeRepository.findByPlaceNameContainingIgnoreCaseAndCityNameContainingIgnoreCase(name, city) } returns places

        // When
        val result = placeService.searchPlace(name, city)

        // Then
        assertEquals(1, result.size)
        verify(exactly = 1) {
            placeRepository.findByPlaceNameContainingIgnoreCaseAndCityNameContainingIgnoreCase(name, city)
        }
    }

    @Test
    @DisplayName("여행 장소 검색 테스트 - 이름만 제공된 경우")
    fun searchPlaceWithNameOnlyTest() {
        // Given
        val name = "숲"
        val places =
            listOf(
                Place().apply { placeName = "서울숲" },
            )
        every { placeRepository.findByPlaceNameContainingIgnoreCase(name) } returns places

        // When
        val result = placeService.searchPlace(name, "")

        // Then
        assertEquals(1, result.size)
        verify(exactly = 1) { placeRepository.findByPlaceNameContainingIgnoreCase(name) }
    }

    @Test
    @DisplayName("여행 장소 검색 테스트 - 도시만 제공된 경우")
    fun searchPlaceWithCityOnlyTest() {
        // Given
        val city = "서울"
        val places =
            listOf(
                Place().apply { cityName = "서울" },
            )
        every { placeRepository.findByCityNameContainingIgnoreCase(city) } returns places

        // When
        val result = placeService.searchPlace("", city)

        // Then
        assertEquals(1, result.size)
        verify(exactly = 1) { placeRepository.findByCityNameContainingIgnoreCase(city) }
    }

    @Test
    @DisplayName("여행 장소 검색 테스트 - 파라미터가 없는 경우 빈 리스트 반환")
    fun searchPlaceWithNoParametersTest() {
        // When
        val result = placeService.searchPlace("", "")

        // Then
        assertTrue(result.isEmpty())
        verify { placeRepository wasNot Called }
    }

    @Test
    @DisplayName("여행지 이미지 등록 테스트")
    fun uploadPlaceImageTest() {
        // Given
        val mockFile = mockk<MultipartFile>()
        val expectedUrl = "http://example.com/uploaded.jpg"
        every { imageUtil.saveImage(mockFile) } returns expectedUrl

        // When
        val result = placeService.uploadPlaceImage(mockFile)

        // Then
        assertEquals(expectedUrl, result)
        verify(exactly = 1) { imageUtil.saveImage(mockFile) }
    }
}
