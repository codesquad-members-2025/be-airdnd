package com.team3.airdnd.accommodation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.team3.airdnd.accommodation.dto.AccommodationListConditionDto;
import com.team3.airdnd.accommodation.dto.AccommodationRequestDto;
import com.team3.airdnd.accommodation.dto.AccommodationResponseDto;
import com.team3.airdnd.accommodation.dto.PriceHistogramConditionDto;
import com.team3.airdnd.accommodation.dto.PriceHistogramResponseDto;
import com.team3.airdnd.accommodation.service.AccommodationService;
import com.team3.airdnd.global.dto.ResponseDto;
import com.team3.airdnd.storedFile.validation.ImageValidator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodations")
public class AccommodationController {

	private final AccommodationService accommodationService;
	private final ImageValidator imageValidator;

	@GetMapping("/{accommodationId}")
	public ResponseEntity<ResponseDto<AccommodationResponseDto.AccommodationDetailDto>> getAccommodationDetail(
		@PathVariable Long accommodationId) {
		AccommodationResponseDto.AccommodationDetailDto detailDto = accommodationService.getAccommodationDetail(
			accommodationId);
		return ResponseDto.ok(detailDto);
	}

	@PostMapping("/create")
	public ResponseEntity<ResponseDto<Void>> createAccommodation(
		@RequestPart @Valid AccommodationRequestDto.CreateAccommodationDto request,
		@RequestPart("files") List<MultipartFile> files,
		@RequestParam Long hostId) {

		imageValidator.validate(files);
		accommodationService.createAccommodation(request, files, hostId);
		return ResponseDto.created();
	}

	@PatchMapping("/{accommodationId}")
	public ResponseEntity<ResponseDto<Void>> updateAccommodation(
		@PathVariable Long accommodationId,
		@RequestPart AccommodationRequestDto.UpdateAccommodationDto request,
		@RequestParam Long hostId,
		@RequestPart(value = "files", required = false) List<MultipartFile> files
	) {
		imageValidator.validate(files);
		accommodationService.updateAccommodation(accommodationId, request, hostId, files);
		return ResponseDto.ok(null);
	}

	@DeleteMapping("/{accommodationId}")
	public ResponseEntity<ResponseDto<Void>> deleteAccommodation(
		@PathVariable Long accommodationId,
		@RequestParam Long hostId
	) {
		accommodationService.deleteAccommodation(accommodationId, hostId);
		return ResponseDto.noContent();
	}

	@GetMapping("/price-range")
	public ResponseEntity<ResponseDto<PriceHistogramResponseDto>> getAccommodationPriceRange(
		@Valid @ModelAttribute PriceHistogramConditionDto request
	) {
		return ResponseDto.ok(accommodationService.getPriceHistogram(request));
	}

	@GetMapping("")
	public ResponseEntity<ResponseDto<AccommodationResponseDto.AccommodationListDto>> getAccommodationListByMap(
		@Valid @ModelAttribute AccommodationListConditionDto request,
		@RequestParam(required = false, defaultValue = "1") int page,
		@RequestParam(required = false, defaultValue = "10") int size
	) {
		AccommodationResponseDto.AccommodationListDto accommodations =
			accommodationService.getAccommodations(request, page, size);

		return ResponseDto.ok(accommodations);
	}

	@GetMapping("/host")
	public ResponseEntity<ResponseDto<Map<String, Object>>> getMyAccommodations(@RequestParam Long hostId) {
		List<AccommodationResponseDto.HostAccommodationDto> accommodations = accommodationService.getMyAccommodations(
			hostId);

		Map<String, Object> data = Map.of("accommodations", accommodations);
		return ResponseDto.ok(data);
	}
}
