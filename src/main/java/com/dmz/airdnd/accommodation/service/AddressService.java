package com.dmz.airdnd.accommodation.service;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.dmz.airdnd.accommodation.adapter.GeocodingClient;
import com.dmz.airdnd.accommodation.domain.Address;
import com.dmz.airdnd.accommodation.dto.response.CoordinatesDto;
import com.dmz.airdnd.accommodation.repository.AddressRepository;
import com.dmz.airdnd.accommodation.util.GeoPointFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AddressService {

	private final AddressRepository addressRepository;
	private final GeocodingClient geocodingClient;

	public Address getOrCreateByFullAddress(String country, String baseAddress, String detailedAddress,
		CoordinatesDto coordinates) {
		String detail = StringUtils.trimToEmpty(detailedAddress);
		Point geometryPoint = findPointFromBaseAddress(coordinates);

		return addressRepository.findByBaseAddressAndDetailedAddress(baseAddress, detail)
			.orElseGet(() -> Address.builder()
				.country(country)
				.baseAddress(baseAddress)
				.detailedAddress(detail)
				.location(geometryPoint)
				.build()
			);
	}

	private Point findPointFromBaseAddress(CoordinatesDto coordinates) {
		double latitude = coordinates.latitude();
		double longitude = coordinates.longitude();
		return GeoPointFactory.createPoint(longitude, latitude);
	}
}
