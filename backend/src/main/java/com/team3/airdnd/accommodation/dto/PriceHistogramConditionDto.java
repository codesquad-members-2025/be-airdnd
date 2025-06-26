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
public class PriceHistogramConditionDto {

	@FutureOrPresent(message = "체크인 날짜는 오늘 이후여야 합니다.")
	protected LocalDate checkIn;

	@Future(message = "체크아웃 날짜는 미래여야 합니다.")
	protected LocalDate checkOut;

	@Min(value = 1, message = "인원 수는 1명 이상이어야 합니다.")
	@Max(value = 20, message = "인원 수는 20명 이하여야 합니다.")
	protected Integer guests;

	@AssertTrue(message = "체크인 날짜는 체크아웃 날짜보다 이전이어야 합니다.")
	public boolean isValidDateRange() {
		return checkIn != null && checkOut != null && checkIn.isBefore(checkOut);
	}

	public boolean isGuestsValid() {
		return guests != null && guests > 0;
	}
}
