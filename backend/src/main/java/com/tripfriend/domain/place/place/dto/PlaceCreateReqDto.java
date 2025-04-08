package com.tripfriend.domain.place.place.dto;

import com.tripfriend.domain.place.place.entity.Category;
import com.tripfriend.domain.place.place.entity.Place;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceCreateReqDto {

    @NotBlank(message = "도시명을 입력해주세요.")
    private String cityName;

    @NotBlank(message = "장소명을 입력해주세요.")
    private String placeName;

    private String description;

    @NotNull(message = "카테고리를 선택해주세요")
    private Category category;

    private MultipartFile imageUrl; // 단일 이미지

    // DTO -> Entity 변환
    public Place toEntity(){
        Place place = new Place();
        place.setCityName(cityName);
        place.setPlaceName(placeName);
        place.setDescription(description);
        place.setCategory(category);
        return place;
    }
}