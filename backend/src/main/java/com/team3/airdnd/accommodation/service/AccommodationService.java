package com.team3.airdnd.accommodation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team3.airdnd.accommodation.domain.Accommodation;
import com.team3.airdnd.accommodation.domain.AccommodationAmenity;
import com.team3.airdnd.accommodation.domain.Address;
import com.team3.airdnd.accommodation.domain.Amenity;
import com.team3.airdnd.accommodation.domain.AmenityType;
import com.team3.airdnd.accommodation.dto.AccommodationListConditionDto;
import com.team3.airdnd.accommodation.dto.AccommodationRequestDto;
import com.team3.airdnd.accommodation.dto.AccommodationResponseDto;
import com.team3.airdnd.accommodation.dto.HostAccommodationQueryDto;
import com.team3.airdnd.accommodation.dto.PriceHistogramConditionDto;
import com.team3.airdnd.accommodation.dto.PriceHistogramResponseDto;
import com.team3.airdnd.accommodation.dto.ReviewDto;
import com.team3.airdnd.accommodation.query.AccommodationQueryRepository;
import com.team3.airdnd.accommodation.repository.AccommodationAmenityRepository;
import com.team3.airdnd.accommodation.repository.AccommodationRepository;
import com.team3.airdnd.accommodation.repository.AddressRepository;
import com.team3.airdnd.accommodation.repository.AmenityRepository;
import com.team3.airdnd.global.exception.CommonException;
import com.team3.airdnd.global.exception.ErrorCode;
import com.team3.airdnd.reservation.repository.ReservationRepository;
import com.team3.airdnd.review.repository.ReviewRepository;
import com.team3.airdnd.storedFile.StoredFileService;
import com.team3.airdnd.storedFile.domain.StoredFile;
import com.team3.airdnd.storedFile.repository.StoredFileRepository;
import com.team3.airdnd.user.domain.User;
import com.team3.airdnd.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccommodationService {

	private final AccommodationRepository accommodationRepository;
	private final StoredFileRepository storedFileRepository;
	private final AccommodationAmenityRepository accommodationAmenityRepository;
	private final ReviewRepository reviewRepository;
	private final AddressRepository addressRepository;
	private final UserRepository userRepository;
	private final AmenityRepository amenityRepository;
	private final ReservationRepository reservationRepository;
	private final AccommodationQueryRepository accommodationQueryRepository;

	private final GeometryFactory geometryFactory;
	private final JPAQueryFactory queryFactory;
	private final StoredFileService storedFileService;
	// 에어비엔비 기준으로 범위를 50으로 정했습니다.
	private static final int DEFAULT_BIN_COUNT = 50;

	public AccommodationResponseDto.AccommodationDetailDto getAccommodationDetail(Long id) {
		Accommodation accommodation = findAccommodationOrThrow(id);
		List<String> imageUrls = findAllImageUrlsByAccommodationId(id);
		List<String> amenities = findAmenityNamesByAccommodationId(id);
		AccommodationResponseDto.ReviewListDto reviewLists = buildReviewLists(id);
		AccommodationResponseDto.AddressInfoDto address = buildAddress(accommodation.getAddress());

		return AccommodationResponseDto.AccommodationDetailDto.builder()
			.name(accommodation.getName())
			.imageUrls(imageUrls)
			.amenities(amenities)
			.hostId(accommodation.getHost().getId())
			.description(accommodation.getDescription())
			.pricePerNight(accommodation.getPricePerNight())
			.maxGuests(accommodation.getMaxGuests())
			.bedCount(accommodation.getBedCount())
			.roomCount(accommodation.getRoomCount())
			.address(address)
			.reviews(reviewLists)
			.build();
	}

	private Accommodation findAccommodationOrThrow(Long id) {
		return accommodationRepository.findDetailById(id)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE));
	}

	private List<String> findAllImageUrlsByAccommodationId(Long id) {
		return storedFileRepository.findByTargetTypeAndTargetIdOrderByFileOrderAsc(
			StoredFile.TargetType.ACCOMMODATION, id);
	}

	private List<String> findAmenityNamesByAccommodationId(Long id) {
		return accommodationAmenityRepository.findAmenityByAccommodationId(id);
	}

	private AccommodationResponseDto.ReviewListDto buildReviewLists(Long id) {
		List<ReviewDto> reviews = reviewRepository.findReviewByAccommodationId(id);

		double avg = reviews.stream()
			.mapToDouble(ReviewDto::rating)
			.average()
			.orElse(0.0);

		return AccommodationResponseDto.ReviewListDto.builder()
			.avgRating(avg)
			.reviewSize(reviews.size())
			.comments(reviews)
			.build();
	}

	private AccommodationResponseDto.AddressInfoDto buildAddress(Address address) {
		return new AccommodationResponseDto.AddressInfoDto(
			address.getCity(),
			address.getDistrict(),
			address.getStreetAddress(),
			address.getLatitude(),
			address.getLongitude()
		);
	}

	@Transactional
	public void createAccommodation(AccommodationRequestDto.CreateAccommodationDto request,
		List<MultipartFile> files, Long hostId) {
		User host = validateHostUser(hostId);
		Address address = saveAdderss(request);
		Accommodation accommodation = saveAccommodation(request, address, host);
		saveAmenities(request.getAmenityTypes(), accommodation);
		storedFileService.saveFiles(files, accommodation.getId());
	}

	private Address saveAdderss(AccommodationRequestDto.CreateAccommodationDto request) {

		Address address = Address.builder()
			.city(request.getCity())
			.district(request.getDistrict())
			.streetAddress(request.getStreetAddress())
			.detailAddress(request.getDetailAddress())
			.latitude(request.getLatitude())
			.longitude(request.getLongitude())
			.build();
		return addressRepository.save(address);
	}

	private Accommodation saveAccommodation(AccommodationRequestDto.CreateAccommodationDto request, Address address,
		User host) {
		Accommodation accommodation = Accommodation.builder()
			.name(request.getName())
			.pricePerNight(request.getPricePerNight())
			.description(request.getDescription())
			.maxGuests(request.getMaxGuests())
			.bedCount(request.getBedCount())
			.roomCount(request.getRoomCount())
			.address(address)
			.host(host)
			.build();

		return accommodationRepository.save(accommodation);
	}

	private void saveAmenities(List<AmenityType> amenityTypes, Accommodation accommodation) {
		List<Amenity> amenities = amenityRepository.findByNameIn(amenityTypes);
		for (Amenity amenity : amenities) {
			AccommodationAmenity mapping = AccommodationAmenity.builder()
				.accommodation(accommodation)
				.amenity(amenity)
				.build();

			accommodationAmenityRepository.save(mapping);
		}
	}

	@Transactional
	public void updateAccommodation(Long accommodationId, AccommodationRequestDto.UpdateAccommodationDto request,
		Long hostId, List<MultipartFile> files) {
		Accommodation accommodation = getAccommodation(accommodationId);
		User host = validateHostUser(hostId);
		validateOwnership(accommodation, host); //본인 소유 숙소인지 확인

		Address updatedAddress = updateAddress(accommodation.getAddress(), request);
		Accommodation updatedAccommodation = updateAccommodationFields(accommodation, updatedAddress, request);

		updateAmenities(updatedAccommodation, request.getAmenityTypes());

		if (files != null) {
			storedFileService.deleteFilesByAccommodationId(accommodationId);
			storedFileService.saveFiles(files, updatedAccommodation.getId());
		}
	}

	private Accommodation getAccommodation(Long accommodationId) {
		return accommodationRepository.findById(accommodationId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE));
	}

	private Address updateAddress(Address oldAddress, AccommodationRequestDto.UpdateAccommodationDto dto) {
		Address updated = oldAddress.toBuilder()
			.city(dto.getCity() != null ? dto.getCity() : oldAddress.getCity())
			.district(dto.getDistrict() != null ? dto.getDistrict() : oldAddress.getDistrict())
			.streetAddress(dto.getStreetAddress() != null ? dto.getStreetAddress() : oldAddress.getStreetAddress())
			.detailAddress(dto.getDetailAddress() != null ? dto.getDetailAddress() : oldAddress.getDetailAddress())
			.latitude(dto.getLatitude() != null ? dto.getLatitude() : oldAddress.getLatitude())
			.longitude(dto.getLongitude() != null ? dto.getLongitude() : oldAddress.getLongitude())
			.build();

		return addressRepository.save(updated);
	}

	private Accommodation updateAccommodationFields(Accommodation old, Address address,
		AccommodationRequestDto.UpdateAccommodationDto dto) {
		Accommodation updated = old.toBuilder()
			.name(dto.getName() != null ? dto.getName() : old.getName())
			.pricePerNight(dto.getPricePerNight() != null ? dto.getPricePerNight() : old.getPricePerNight())
			.description(dto.getDescription() != null ? dto.getDescription() : old.getDescription())
			.maxGuests(dto.getMaxGuests() != null ? dto.getMaxGuests() : old.getMaxGuests())
			.bedCount(dto.getBedCount() != null ? dto.getBedCount() : old.getBedCount())
			.roomCount(dto.getRoomCount() != null ? dto.getRoomCount() : old.getRoomCount())
			.address(address)
			.host(old.getHost()) // host는 수정 불가
			.build();

		return accommodationRepository.save(updated);
	}

	private void updateAmenities(Accommodation accommodation, List<AmenityType> types) {
		if (types == null)
			return;

		accommodationAmenityRepository.deleteByAccommodationId(accommodation.getId());

		List<Amenity> amenities = amenityRepository.findByNameIn(types);

		for (Amenity amenity : amenities) {
			AccommodationAmenity mapping = AccommodationAmenity.builder()
				.accommodation(accommodation)
				.amenity(amenity)
				.build();
			accommodationAmenityRepository.save(mapping);
		}
	}

	@Transactional
	public void deleteAccommodation(Long accommodationId, Long hostId) {
		Accommodation accommodation = getAccommodation(accommodationId);
		User host = validateHostUser(hostId);
		validateOwnership(accommodation, host); //본인 소유 숙소인지 확인

		//예약 존재하는지 확인
		boolean hasReservation = reservationRepository.existsByAccommodationId(accommodationId);
		if (hasReservation) {
			throw new CommonException(ErrorCode.ACCOMMODATION_HAS_RESERVATIONS);
		}

		storedFileService.deleteFilesByAccommodationId(accommodationId);

		accommodationAmenityRepository.deleteByAccommodationId(accommodationId);
		addressRepository.delete(accommodation.getAddress());
		accommodationRepository.delete(accommodation);
	}

	//User 확인 + Host 권한 확인
	private User validateHostUser(Long hostId) {
		User user = userRepository.findById(hostId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

		if (!user.isHost()) {
			throw new CommonException(ErrorCode.ACCESS_DENIED);
		}
		return user;
	}

	//본인 소유 숙소인지 확인
	private void validateOwnership(Accommodation accommodation, User host) {
		if (!accommodation.getHost().getId().equals(host.getId())) {
			throw new CommonException(ErrorCode.NOT_AUTHORIZED_TO_DELETE);
		}
	}

	@Transactional(readOnly = true)
	public PriceHistogramResponseDto getPriceHistogram(PriceHistogramConditionDto request) {
		request = applyDefaultFilterCondition(request);

		List<Integer> prices = accommodationQueryRepository.findAvailableAccommodationPrices(request);

		if (prices.isEmpty()) {
			return new PriceHistogramResponseDto(0, 0, Collections.nCopies(DEFAULT_BIN_COUNT, 0));
		}

		return calculatePriceHistogram(prices);
	}

	private PriceHistogramResponseDto calculatePriceHistogram(List<Integer> prices) {
		int min = Collections.min(prices);
		int max = Collections.max(prices);

		List<Integer> histogram = new ArrayList<>(Collections.nCopies(DEFAULT_BIN_COUNT, 0));

		if (min == max) {
			int centerBin = DEFAULT_BIN_COUNT / 2;
			histogram.set(centerBin, prices.size());
		} else {
			double binWidth = (max - min) / (double)DEFAULT_BIN_COUNT;

			for (Integer price : prices) {
				int binIndex = (int)Math.floor((price - min) / binWidth);
				binIndex = Math.min(binIndex, DEFAULT_BIN_COUNT - 1);
				histogram.set(binIndex, histogram.get(binIndex) + 1);
			}
		}

		return new PriceHistogramResponseDto(min, max, histogram);
	}

	public List<AccommodationResponseDto.HostAccommodationDto> getMyAccommodations(Long hostId) {
		List<HostAccommodationQueryDto> accommodations = accommodationRepository.findAccommodationListByHostId(
			hostId);

		List<Long> accommodationIds = accommodations.stream()
			.map(HostAccommodationQueryDto::id)
			.toList();

		// 대표 이미지 URL을 한 번에 가져옴
		Map<Long, String> imageUrlMap = storedFileRepository.findFirstImageUrlsForAccommodationIds(accommodationIds);

		return accommodations.stream()
			.map(acc -> {
				String imageUrl = imageUrlMap.get(acc.id());

				return AccommodationResponseDto.HostAccommodationDto.builder()
					.id(acc.id())
					.name(acc.name())
					.city(acc.city())
					.district(acc.district())
					.streetAddress(acc.streetAddress())
					.imageUrl(imageUrl)
					.build();
			})
			.toList();
	}

	@Transactional(readOnly = true)
	public AccommodationResponseDto.AccommodationListDto getAccommodations(
		AccommodationListConditionDto request, int page, int size) {

		applyDefaultMapBounds(request);
		return accommodationQueryRepository.findAccommodationListWithinBounds(request, page, size);
	}

	private PriceHistogramConditionDto applyDefaultFilterCondition(PriceHistogramConditionDto request) {
		LocalDate checkIn = request.getCheckIn() != null ? request.getCheckIn() : LocalDate.now();
		LocalDate checkOut = request.getCheckOut() != null ? request.getCheckOut() : checkIn.plusDays(1);
		Integer guests = request.getGuests() != null ? request.getGuests() : 1;

		request.setCheckIn(checkIn);
		request.setCheckOut(checkOut);
		request.setGuests(guests);
		return request;
	}

	private AccommodationListConditionDto applyDefaultMapBounds(AccommodationListConditionDto request) {
		LocalDate checkIn = request.getCheckIn() != null ? request.getCheckIn() : LocalDate.now();
		LocalDate checkOut = request.getCheckOut() != null ? request.getCheckOut() : checkIn.plusDays(1);
		Integer guests = request.getGuests() != null ? request.getGuests() : 1;

		request.setCheckIn(checkIn);
		request.setCheckOut(checkOut);
		request.setGuests(guests);

		Integer minPrice = request.getMinPrice() != null ? request.getMinPrice() : 1000;
		Integer maxPrice = request.getMaxPrice() != null ? request.getMaxPrice() : 10_000_000;

		request.setMinPrice(minPrice);
		request.setMaxPrice(maxPrice);

		if (request.getNorthEastLat() == null || request.getNorthEastLng() == null
			|| request.getSouthWestLat() == null || request.getSouthWestLng() == null) {
			request.setNorthEastLat(37.701);
			request.setNorthEastLng(127.183);
			request.setSouthWestLat(37.413);
			request.setSouthWestLng(126.734);
		}

		return request;
	}
}
