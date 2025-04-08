package com.tripfriend.domain.place.place.service;

import com.tripfriend.domain.place.place.dto.PlaceCreateReqDto;
import com.tripfriend.domain.place.place.dto.PlaceUpdateReqDto;
import com.tripfriend.domain.place.place.entity.Place;
import com.tripfriend.domain.place.place.repository.PlaceRepository;
import com.tripfriend.global.annotation.CheckPermission;
import com.tripfriend.global.exception.ServiceException;
import com.tripfriend.global.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final PlaceRepository placeRepository;
    private final ImageUtil imageUtil;

    // 여행 장소 등록
    @Transactional
    public Place createPlace(PlaceCreateReqDto req) {

        String imageUrl = null;
        MultipartFile imageFile = req.getImageUrl();

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imageUrl = uploadPlaceImage(imageFile);
            } catch (IOException e) {
                throw new ServiceException("404-2", "이미지 업로드에 실패했습니다.");
            }
        }

        Place place = new Place();
        place.setCityName(req.getCityName());
        place.setPlaceName(req.getPlaceName());
        place.setDescription(req.getDescription());
        place.setCategory(req.getCategory());
        place.setImageUrl(imageUrl);

        placeRepository.save(place);
        return place;
    }

    // 여행 장소 전체 리스트 조회
    public List<Place> getAllPlaces() {
        List<Place> places = placeRepository.findAll();
        return places;
    }

    // 특정 도시의 여행 장소 리스트 조회
    public List<Place> getPlacesByCity(String cityName) {
        return placeRepository.findByCityName(cityName);
    }

    // 도시 목록 중복 제거
    public List<String> getDistinctCities() {
        return placeRepository.findDistinctCityNames();
    }

    // 여행 장소 단건 조회
    public Place getPlace(Long id) {
        return placeRepository.findById(id)
                .orElseThrow(() -> new ServiceException("404-1", "해당 장소가 존재하지 않습니다."));
    }

    // 여행 장소 삭제
    @CheckPermission("ADMIN")
    @Transactional
    public void deletePlace(Place place) {
        placeRepository.delete(place);
    }

    // 여행 장소 수정
    @Transactional
    public Place updatePlace(Place place, PlaceUpdateReqDto req) {
        place.setCityName(req.getCityName());
        place.setPlaceName(req.getPlaceName());
        place.setDescription(req.getDescription());
        place.setCategory(req.getCategory());

        return placeRepository.save(place);
    }

    public List<Place> searchPlace(String name, String city) {

        // 두 파라미터 모두 제공된 경우
        if (name != null && !name.isEmpty() && city != null && !city.isEmpty()) {
            return placeRepository.findByPlaceNameContainingIgnoreCaseAndCityNameContainingIgnoreCase(name, city);
        }
        // name만 제공된 경우
        else if (name != null && !name.isEmpty()) {
            return placeRepository.findByPlaceNameContainingIgnoreCase(name);
        }
        // city만 제공된 경우
        else if (city != null && !city.isEmpty()) {
            return placeRepository.findByCityNameContainingIgnoreCase(city);
        }
        // 둘 다 없으면 전체 조회 또는 빈 리스트 반환
        return new ArrayList<>();

    }

    // 여행지 이미지 등록
    public String uploadPlaceImage(MultipartFile imageFile) throws IOException {

        String imageUrl = imageUtil.saveImage(imageFile);

        return imageUrl;
    }

}
