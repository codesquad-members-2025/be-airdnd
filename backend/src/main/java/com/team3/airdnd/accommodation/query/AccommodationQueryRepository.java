package com.team3.airdnd.accommodation.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team3.airdnd.accommodation.domain.Accommodation;
import com.team3.airdnd.accommodation.domain.QAccommodation;
import com.team3.airdnd.accommodation.domain.QAccommodationAmenity;
import com.team3.airdnd.accommodation.domain.QAddress;
import com.team3.airdnd.accommodation.domain.QAmenity;
import com.team3.airdnd.accommodation.dto.AccommodationListConditionDto;
import com.team3.airdnd.accommodation.dto.AccommodationResponseDto;
import com.team3.airdnd.accommodation.dto.AmenityDto;
import com.team3.airdnd.accommodation.dto.PriceHistogramConditionDto;
import com.team3.airdnd.reservation.domain.QReservation;
import com.team3.airdnd.reservation.domain.Reservation;
import com.team3.airdnd.storedFile.domain.QStoredFile;
import com.team3.airdnd.storedFile.domain.StoredFile;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AccommodationQueryRepository {

	private final JPAQueryFactory queryFactory;

	private final QAccommodation accommodation = QAccommodation.accommodation;
	private final QAccommodationAmenity accommodationAmenity = QAccommodationAmenity.accommodationAmenity;
	private final QAmenity amenity = QAmenity.amenity;
	private final QStoredFile storedFile = QStoredFile.storedFile;
	private final QReservation reservation = QReservation.reservation;
	private final QAddress address = QAddress.address;

	public List<Integer> findAvailableAccommodationPrices(PriceHistogramConditionDto request) {
		BooleanBuilder condition = buildHistogramCondition(request);

		return queryFactory
			.select(accommodation.pricePerNight)
			.from(accommodation)
			.join(accommodation.address, address)
			.where(condition)
			.fetch();
	}

	public AccommodationResponseDto.AccommodationListDto findAccommodationListWithinBounds(
		AccommodationListConditionDto request, int page, int size) {

		BooleanBuilder condition = buildListCondition(request);

		return buildAccommodationListResponse(condition, page, size);
	}

	private BooleanBuilder buildHistogramCondition(PriceHistogramConditionDto request) {
		BooleanBuilder condition = new BooleanBuilder();

		if (request.getGuests() != null && request.getGuests() > 0) {
			condition.and(accommodation.maxGuests.goe(request.getGuests()));
		}

		if (request.getCheckIn() != null && request.getCheckOut() != null) {
			BooleanExpression noOverlap = JPAExpressions
				.selectOne()
				.from(reservation)
				.where(
					reservation.accommodation.eq(accommodation)
						.and(reservation.status.in(Reservation.Status.CONFIRMED, Reservation.Status.PENDING))
						.and(reservation.checkOut.gt(request.getCheckIn()))
						.and(reservation.checkIn.lt(request.getCheckOut()))
				)
				.notExists();

			condition.and(noOverlap);
		}

		return condition;
	}

	private BooleanBuilder buildListCondition(AccommodationListConditionDto request) {
		BooleanBuilder condition = new BooleanBuilder();

		// 1. 지도 범위
		BooleanExpression bounds = createBoundsCondition(
			request.getNorthEastLat(), request.getNorthEastLng(),
			request.getSouthWestLat(), request.getSouthWestLng()
		);

		if (bounds != null)
			condition.and(bounds);

		// 2. 게스트 수
		if (request.isGuestsValid()) {
			condition.and(accommodation.maxGuests.goe(request.getGuests()));
		}

		// 3. 가격 범위
		if (request.isValidPriceRange()) {
			condition.and(accommodation.pricePerNight.between(request.getMinPrice(), request.getMaxPrice()));
		}

		// 4. 예약 겹침 방지
		BooleanExpression noOverlap = JPAExpressions
			.selectOne()
			.from(reservation)
			.where(
				reservation.accommodation.eq(accommodation)
					.and(reservation.status.in(Reservation.Status.CONFIRMED, Reservation.Status.PENDING))
					.and(reservation.checkOut.gt(request.getCheckIn()))
					.and(reservation.checkIn.lt(request.getCheckOut()))
			)
			.notExists();

		condition.and(noOverlap);

		return condition;
	}

	private AccommodationResponseDto.AccommodationListDto buildAccommodationListResponse(
		BooleanBuilder condition, int page, int size
	) {
		PageRequest pageRequest = PageRequest.of(page - 1, size);

		List<Accommodation> accommodations = queryFactory
			.selectFrom(accommodation)
			.join(accommodation.address, address).fetchJoin()
			.where(condition)
			.offset(pageRequest.getOffset())
			.limit(pageRequest.getPageSize())
			.fetch();

		Long count = queryFactory
			.select(accommodation.count())
			.from(accommodation)
			.join(accommodation.address, address)
			.where(condition)
			.fetchOne();

		long total = count != null ? count : 0L;

		List<Long> accommodationIds = accommodations.stream().map(Accommodation::getId).toList();
		Map<Long, String> imageMap = fetchImageMap(accommodationIds);
		Map<Long, List<AmenityDto>> amenityMap = fetchAmenityMap(accommodationIds);

		List<AccommodationResponseDto.AccommodationInfo> accommodationInfos = accommodations.stream()
			.map(acc -> {
				String imageUrl = imageMap.getOrDefault(acc.getId(), null);
				List<AmenityDto> amenities = amenityMap.getOrDefault(acc.getId(), Collections.emptyList());
				return AccommodationResponseDto.AccommodationInfo.builder()
					.id(acc.getId())
					.name(acc.getName())
					.imageUrl(imageUrl)
					.pricePerNight(acc.getPricePerNight())
					.description(acc.getDescription())
					.maxGuests(acc.getMaxGuests())
					.bedCount(Optional.ofNullable(acc.getBedCount()).orElse(0))
					.address(acc.getAddress().getCity() + " "
						+ acc.getAddress().getDistrict() + " "
						+ acc.getAddress().getStreetAddress())
					.amenity(amenities)
					.latitude(acc.getAddress().getLatitude())
					.longitude(acc.getAddress().getLongitude())
					.build();
			})
			.toList();

		return AccommodationResponseDto.AccommodationListDto.builder()
			.page(page)
			.size(size)
			.totalPages((int)Math.ceil((double)total / size))
			.totalElements((int)total)
			.accommodations(accommodationInfos)
			.build();
	}

	private Map<Long, String> fetchImageMap(List<Long> accommodationIds) {
		List<Tuple> imageTuples = queryFactory
			.select(storedFile.targetId, storedFile.fileUrl)
			.from(storedFile)
			.where(
				storedFile.targetType.eq(StoredFile.TargetType.ACCOMMODATION)
					.and(storedFile.targetId.in(accommodationIds))
					.and(storedFile.fileOrder.eq(1))
			)
			.fetch();

		return imageTuples.stream()
			.collect(Collectors.toMap(
				t -> t.get(storedFile.targetId),
				t -> t.get(storedFile.fileUrl)
			));
	}

	private Map<Long, List<AmenityDto>> fetchAmenityMap(List<Long> accommodationIds) {
		List<Tuple> amenityTuples = queryFactory
			.select(accommodationAmenity.accommodation.id, amenity.id, amenity.name)
			.from(accommodationAmenity)
			.join(accommodationAmenity.amenity, amenity)
			.where(accommodationAmenity.accommodation.id.in(accommodationIds))
			.fetch();

		Map<Long, List<AmenityDto>> amenityMap = new HashMap<>();
		for (Tuple tuple : amenityTuples) {
			Long accId = tuple.get(accommodationAmenity.accommodation.id);
			AmenityDto dto = new AmenityDto(tuple.get(amenity.id), tuple.get(amenity.name));
			amenityMap.computeIfAbsent(accId, k -> new ArrayList<>()).add(dto);
		}
		return amenityMap;
	}

	private BooleanExpression createBoundsCondition(Double neLat, Double neLng, Double swLat, Double swLng) {
		if (neLat == null || neLng == null || swLat == null || swLng == null) {
			return null;
		}

		return address.latitude.between(swLat, neLat)
			.and(address.longitude.between(swLng, neLng));
	}
}
