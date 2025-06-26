package com.team3.airdnd.accommodation;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.team3.airdnd.AbstractIntegrationTest;
import com.team3.airdnd.accommodation.dto.AccommodationListConditionDto;
import com.team3.airdnd.accommodation.dto.AccommodationResponseDto;
import com.team3.airdnd.accommodation.dto.PriceHistogramConditionDto;
import com.team3.airdnd.accommodation.dto.PriceHistogramResponseDto;
import com.team3.airdnd.accommodation.service.AccommodationService;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("가격 히스토그램 기능 테스트")
class PriceHistogramServiceTest extends AbstractIntegrationTest {

	@Autowired
	private AccommodationService accommodationService;

	@Autowired
	private EntityManager em;

	private Validator validator;

	@BeforeEach
	void setUpValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		this.validator = factory.getValidator();
	}

	@Nested
	@DisplayName("숙소 필터링 기능 테스트")
	class FilteringCondition {

		@Test
		@DisplayName("필터에 해당하는 값은 총 21개가 나온다.")
		void shouldMatchTotalHistogramCountWithAvailableAccommodations() {
			PriceHistogramConditionDto request = PriceHistogramConditionDto.builder()
				.checkIn(LocalDate.of(2025, 6, 20))
				.checkOut(LocalDate.of(2025, 6, 27))
				.guests(2)
				.build();

			PriceHistogramResponseDto result = accommodationService.getPriceHistogram(request);

			int totalCount = result.priceHistogram().stream().mapToInt(Integer::intValue).sum();
			assertThat(totalCount).isEqualTo(21);
		}

		@Test
		@DisplayName("최소 요금이 최대 요금보다 크면 에러가 발생한다")
		void shouldFailWhenMinPriceGreaterThanMaxPrice() {
			// given
			AccommodationListConditionDto request = AccommodationListConditionDto.builder()
				.checkIn(LocalDate.of(2025, 6, 20))
				.checkOut(LocalDate.of(2025, 6, 27))
				.guests(2)
				.minPrice(100000)
				.maxPrice(50000)
				.build();

			// when
			Set<ConstraintViolation<AccommodationListConditionDto>> violations = validator.validate(request);

			// then
			assertThat(violations)
				.extracting(ConstraintViolation::getMessage)
				.contains("최소 요금은 최대 요금보다 클 수 없습니다.");
		}

		@Test
		@DisplayName("체크인 날짜가 체크아웃보다 나중이면 에러 메시지가 발생한다")
		void shouldFailWhenCheckInIsAfterCheckOut() {
			// given
			PriceHistogramConditionDto request = PriceHistogramConditionDto.builder()
				.checkIn(LocalDate.of(2025, 6, 27))
				.checkOut(LocalDate.of(2025, 6, 20))
				.guests(2)
				.build();

			// when
			Set<ConstraintViolation<PriceHistogramConditionDto>> violations = validator.validate(request);

			// then
			assertThat(violations)
				.extracting(ConstraintViolation::getMessage)
				.contains("체크인 날짜는 체크아웃 날짜보다 이전이어야 합니다.");
		}

		@DisplayName("아무 조건도 없이 검색했을 때도 숙소가 반환된다")
		@Test
		void search_with_empty_filter_returns_results() {
			// given
			AccommodationListConditionDto request = AccommodationListConditionDto.builder()
				.build(); // 모든 필드 null

			// when
			AccommodationResponseDto.AccommodationListDto accommodations = accommodationService.getAccommodations(
				request, 1, 10);

			// then
			assertThat(accommodations.getAccommodations()).isNotEmpty();
		}

	}

	@Nested
	@DisplayName("히스토그램 분포 계산 테스트")
	class HistogramCalculation {

		@Test
		@DisplayName("모든 숙소가격이 같다면 히스토그램 중앙에 분포한다.")
		void shouldDistributeSamePriceIntoCenterBinOnly() {

			PriceHistogramConditionDto request = PriceHistogramConditionDto.builder()
				.checkIn(LocalDate.of(2025, 6, 20))
				.checkOut(LocalDate.of(2025, 6, 27))
				.guests(2)
				.build();

			PriceHistogramResponseDto result = accommodationService.getPriceHistogram(request);

			int centerBin = 50 / 2;
			for (int i = 0; i < 50; i++) {
				if (i == centerBin) {
					assertThat(result.priceHistogram().get(i)).isEqualTo(11);
				} else {
					assertThat(result.priceHistogram().get(i)).isEqualTo(0);
				}
			}
		}

		@Test
		@DisplayName("가격이 모두 같다면 최소/최대 값이 동일하다")
		void shouldHaveEqualMinAndMaxWhenAllPricesAreSame() {
			PriceHistogramConditionDto request = PriceHistogramConditionDto.builder()
				.checkIn(LocalDate.of(2025, 6, 20))
				.checkOut(LocalDate.of(2025, 6, 27))
				.guests(2)
				.build();

			PriceHistogramResponseDto result = accommodationService.getPriceHistogram(request);

			assertThat(result.minValue()).isEqualTo(150000);
			assertThat(result.maxValue()).isEqualTo(150000);
		}

		@Test
		@DisplayName("가격 리스트가 비어있을 경우 0으로 채워진 히스토그램을 반환한다")
		void shouldReturnZeroFilledHistogramWhenNoData() {
			// when
			PriceHistogramConditionDto request = PriceHistogramConditionDto.builder()
				.checkIn(LocalDate.of(2025, 6, 20))
				.checkOut(LocalDate.of(2025, 6, 27))
				.guests(99)
				.build();

			PriceHistogramResponseDto result = accommodationService.getPriceHistogram(request);

			// then
			assertThat(result.priceHistogram()).hasSize(50);
			assertThat(result.priceHistogram()).allMatch(bin -> bin == 0);
			assertThat(result.minValue()).isEqualTo(0);
			assertThat(result.maxValue()).isEqualTo(0);
		}
	}
}
