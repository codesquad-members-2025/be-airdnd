package com.team3.airdnd.accommodation.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AccommodationListConditionDto {

	@FutureOrPresent(message = "체크인 날짜는 오늘 이후여야 합니다.")
	protected LocalDate checkIn;

	@Future(message = "체크아웃 날짜는 미래여야 합니다.")
	protected LocalDate checkOut;

	@Min(value = 1, message = "인원 수는 1명 이상이어야 합니다.")
	@Max(value = 20, message = "인원 수는 20명 이하여야 합니다.")
	protected Integer guests;

	@Min(value = 1000, message = "최소 요금은 1,000원 이상이어야 합니다.")
	@Max(value = 10000000, message = "최소 요금은 10,000,000원 이하여야 합니다.")
	private Integer minPrice;

	@Min(value = 1000, message = "최대 요금은 1,000원 이상이어야 합니다.")
	@Max(value = 10000000, message = "최대 요금은 10,000,000원 이하여야 합니다.")
	private Integer maxPrice;

	@Min(value = -90, message = "북동 위도는 -90 이상이어야 합니다.")
	@Max(value = 90, message = "북동 위도는 90 이하여야 합니다.")
	private Double northEastLat;

	@Min(value = -180, message = "북동 경도는 -180 이상이어야 합니다.")
	@Max(value = 180, message = "북동 경도는 180 이하여야 합니다.")
	private Double northEastLng;

	@Min(value = -90, message = "남서 위도는 -90 이상이어야 합니다.")
	@Max(value = 90, message = "남서 위도는 90 이하여야 합니다.")
	private Double southWestLat;

	@Min(value = -180, message = "남서 경도는 -180 이상이어야 합니다.")
	@Max(value = 180, message = "남서 경도는 180 이하여야 합니다.")
	private Double southWestLng;

	@AssertTrue(message = "체크인 날짜는 체크아웃 날짜보다 이전이어야 합니다.")
	public boolean isValidDateRange() {
		return checkIn != null && checkOut != null && checkIn.isBefore(checkOut);
	}

	public boolean isGuestsValid() {
		return guests != null && guests > 0;
	}

	public boolean isValidPriceRange() {
		return minPrice != null && maxPrice != null && minPrice <= maxPrice;
	}
}
