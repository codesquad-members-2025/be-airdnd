package com.team3.airdnd.accommodation;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.team3.airdnd.AbstractIntegrationTest;
import com.team3.airdnd.AccommodationTestFactory;
import com.team3.airdnd.accommodation.domain.Accommodation;
import com.team3.airdnd.accommodation.domain.AccommodationAmenity;
import com.team3.airdnd.accommodation.domain.AmenityType;
import com.team3.airdnd.accommodation.dto.AccommodationListConditionDto;
import com.team3.airdnd.accommodation.dto.AccommodationRequestDto;
import com.team3.airdnd.accommodation.dto.AccommodationResponseDto;
import com.team3.airdnd.accommodation.query.AccommodationQueryRepository;
import com.team3.airdnd.accommodation.repository.AccommodationAmenityRepository;
import com.team3.airdnd.accommodation.repository.AccommodationRepository;
import com.team3.airdnd.accommodation.service.AccommodationService;
import com.team3.airdnd.config.MockAwsConfig;
import com.team3.airdnd.global.exception.CommonException;
import com.team3.airdnd.storedFile.StoredFileService;
import com.team3.airdnd.storedFile.domain.StoredFile;
import com.team3.airdnd.storedFile.repository.StoredFileRepository;
import com.team3.airdnd.user.domain.User;
import com.team3.airdnd.user.repository.UserRepository;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@Import(MockAwsConfig.class)
public class AccommodationServiceTest extends AbstractIntegrationTest {
	@Autowired
	private AccommodationService accommodationService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AccommodationRepository accommodationRepository;

	@Autowired
	private AccommodationAmenityRepository accommodationAmenityRepository;

	@Autowired
	private StoredFileRepository storedFileRepository;

	@Autowired
	private StoredFileService storedFileService;
	@Autowired
	private AccommodationQueryRepository accommodationQueryRepository;

	@Test
	@DisplayName("숙소 ID로 상세 정보를 조회할 수 있다")
	void getAccommodationDetail_success() {
		// given
		Long accommodationId = 1L;

		// when
		AccommodationResponseDto.AccommodationDetailDto result =
			accommodationService.getAccommodationDetail(accommodationId);

		// then
		assertThat(result.getName()).isEqualTo("숙소 1");
		assertThat(result.getAmenities()).containsExactlyInAnyOrder(
			AmenityType.AIR_CONDITIONER.name(),
			AmenityType.TV.name(),
			AmenityType.HEATER.name()
		);
		assertThat(result.getHostId()).isEqualTo(2L);
		assertThat(result.getAddress().getCity()).isEqualTo("전라북도");
		assertThat(result.getReviews().getComments()).hasSize(2);
		assertThat(result.getReviews().getAvgRating()).isEqualTo(4.5);
	}

	@DisplayName("숙소가 존재하지 않으면 예외가 발생한다")
	@Test
	void getAccommodationDetail_notFound() {
		// given
		Long invalidId = 999L;

		// when & then
		assertThatThrownBy(() -> accommodationService.getAccommodationDetail(invalidId))
			.isInstanceOf(CommonException.class)
			.hasMessageContaining("해당 리소스가 존재하지 않습니다");
	}

	@DisplayName("숙소에 리뷰가 없으면 빈 리스트와 평균 0.0을 반환한다")
	@Test
	void getAccommodationDetail_noReviews() {
		// given
		Long accommodationId = 2L; //리뷰 없는 숙소

		// when
		var dto = accommodationService.getAccommodationDetail(accommodationId);

		// then
		assertThat(dto.getReviews().getComments()).isEmpty();
		assertThat(dto.getReviews().getAvgRating()).isEqualTo(0.0);
	}

	@Test
	@DisplayName("숙소를 생성하면 주소, 숙소, 편의시설, 이미지가 모두 저장된다")
	void createAccommodation_success() {
		// given
		User host = userRepository.findByLoginId("user06")
			.orElseThrow(() -> new RuntimeException("기존 호스트가 없습니다"));

		AccommodationRequestDto.CreateAccommodationDto request = AccommodationTestFactory.createDto(host.getId());

		List<MultipartFile> mockImages = List.of(
			new MockMultipartFile("images", "test1.jpg", "image/jpeg", "file1".getBytes()),
			new MockMultipartFile("images", "test2.jpg", "image/jpeg", "file2".getBytes())
		);

		// when
		accommodationService.createAccommodation(request, mockImages, host.getId());

		// then
		Accommodation saved = accommodationRepository.findByName("테스트 숙소")
			.orElseThrow(() -> new AssertionError("생성된 숙소가 없습니다"));

		assertThat(saved.getDescription()).isEqualTo("설명입니다");
		assertThat(saved.getMaxGuests()).isEqualTo(2);
		assertThat(saved.getAddress().getCity()).isEqualTo("서울");

		List<AccommodationAmenity> amenities = accommodationAmenityRepository.findAll()
			.stream()
			.filter(a -> a.getAccommodation().getId().equals(saved.getId()))
			.toList();
		assertThat(amenities).hasSize(2);

		List<StoredFile> images = storedFileRepository.findAll()
			.stream()
			.filter(i -> i.getTargetId().equals(saved.getId()))
			.toList();
		assertThat(images).hasSize(2);
	}

	@Test
	@DisplayName("HOST 권한이 아닌 유저는 숙소 생성이 불가능하다")
	void createAccommodation_fail_notHost() {
		// given
		User guest = userRepository.save(User.builder()
			.loginId("guest123")
			.password("1234")
			.email("guest@test.com")
			.username("게스트")
			.phone("01011112222")
			.role(User.Role.GUEST)  // GUEST
			.build());

		AccommodationRequestDto.CreateAccommodationDto request = AccommodationTestFactory.createDto(guest.getId());
		List<MultipartFile> images = List.of(
			new MockMultipartFile("images", "test.jpg", "image/jpeg", "file".getBytes()));

		// when & then
		assertThatThrownBy(() -> accommodationService.createAccommodation(request, images, guest.getId()))
			.isInstanceOf(CommonException.class)
			.hasMessageContaining("접근 권한이 없습니다");
	}

	@Test
	@DisplayName("숙소를 수정하면 정보와 이미지가 변경된다")
	void updateAccommodation_success() {
		// given
		User host = userRepository.findByLoginId("user07")
			.orElseThrow(() -> new RuntimeException("호스트 없음"));

		Accommodation original = accommodationRepository.findById(6L).orElseThrow();

		AccommodationRequestDto.UpdateAccommodationDto request = AccommodationRequestDto.UpdateAccommodationDto.builder()
			.name("수정된 숙소 이름")
			.description("수정된 설명")
			.pricePerNight(180000)
			.city("부산")
			.district("해운대구")
			.streetAddress("수정로")
			.detailAddress("101동")
			.latitude(35.16)
			.longitude(129.16)
			.build();

		List<MultipartFile> files = List.of(
			new MockMultipartFile("files", "update1.jpg", "image/jpeg", "file1".getBytes())
		);

		// when
		accommodationService.updateAccommodation(original.getId(), request, host.getId(), files);

		// then
		Accommodation updated = accommodationRepository.findById(original.getId())
			.orElseThrow();

		assertThat(updated.getName()).isEqualTo("수정된 숙소 이름");
		assertThat(updated.getAddress().getCity()).isEqualTo("부산");

		List<StoredFile> images = storedFileRepository.findAll()
			.stream()
			.filter(i -> i.getTargetId().equals(updated.getId()))
			.toList();
		assertThat(images).hasSize(1);
	}

	@Test
	@DisplayName("숙소 수정 시 이미지를 전달하지 않아도 기존 이미지는 유지된다")
	void updateAccommodation_keepExistingImages() {
		// given
		User host = userRepository.findByLoginId("user07").orElseThrow();
		Accommodation accommodation = accommodationRepository.findById(6L).orElseThrow();

		var request = AccommodationRequestDto.UpdateAccommodationDto.builder()
			.name("이미지 없는 수정")
			.description("기존 이미지 유지")
			.build();

		// when
		accommodationService.updateAccommodation(accommodation.getId(), request, host.getId(), null);

		// then
		Accommodation updated = accommodationRepository.findById(accommodation.getId()).orElseThrow();
		assertThat(updated.getName()).isEqualTo("이미지 없는 수정");

		List<StoredFile> images = storedFileRepository.findAll()
			.stream()
			.filter(f -> f.getTargetId().equals(accommodation.getId()))
			.toList();
		assertThat(images).isNotEmpty(); // 기존 이미지 유지됨
	}

	@Test
	@DisplayName("숙소를 삭제하면 관련 데이터도 함께 삭제된다")
	void deleteAccommodation_success() {
		// given
		User host = userRepository.findByLoginId("user03")
			.orElseThrow(() -> new RuntimeException("호스트 없음"));

		Accommodation target = accommodationRepository.findById(2L)
			.orElseThrow();

		Long accommodationId = target.getId();

		// when
		accommodationService.deleteAccommodation(accommodationId, host.getId());

		// then
		assertThat(accommodationRepository.findById(accommodationId)).isEmpty();
		assertThat(accommodationAmenityRepository.findAll()
			.stream()
			.noneMatch(a -> a.getAccommodation().getId().equals(accommodationId)))
			.isTrue();
		assertThat(storedFileRepository.findAll()
			.stream()
			.noneMatch(f -> f.getTargetId().equals(accommodationId)))
			.isTrue();
	}

	@Test
	@DisplayName("호스트는 자신의 숙소 목록을 조회할 수 있다")
	void getMyAccommodations_success() {
		// given
		User host = userRepository.findByLoginId("user07")
			.orElseThrow();

		// when
		List<AccommodationResponseDto.HostAccommodationDto> results = accommodationService.getMyAccommodations(
			host.getId());

		// then
		assertThat(results).isNotEmpty();
		assertThat(results)
			.extracting(AccommodationResponseDto.HostAccommodationDto::getName)
			.contains("숙소 6");
	}

	@Test
	@DisplayName("위치를 기반으로 조회한다.")
	void searchByLocation_masan_shouldReturnTwoAccommodations() {
		// given
		AccommodationListConditionDto request = AccommodationListConditionDto.builder()
			.checkIn(LocalDate.of(2025, 6, 20))
			.checkOut(LocalDate.of(2025, 6, 27))
			.guests(2)
			.northEastLat(35.2375)
			.northEastLng(128.5950)
			.southWestLat(35.1850)
			.southWestLng(128.5700)
			.build();

		// when
		AccommodationResponseDto.AccommodationListDto result = accommodationService.getAccommodations(
			request, 1, 10);

		// then
		assertThat(result.getAccommodations()).hasSize(1);
	}
}
