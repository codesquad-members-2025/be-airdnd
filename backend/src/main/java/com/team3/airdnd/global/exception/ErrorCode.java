package com.team3.airdnd.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	INVALID_REQUEST(40000, HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	NOT_FOUND_RESOURCE(40400, HttpStatus.NOT_FOUND, "해당 리소스가 존재하지 않습니다."),
	NOT_FOUND_USER(40401, HttpStatus.NOT_FOUND, "해당 사용자가 존재하지 않습니다."),

	FAILURE_LOGIN(40100, HttpStatus.UNAUTHORIZED, "잘못된 아이디 또는 비밀번호입니다."),
	ACCESS_DENIED(40300, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	DUPLICATED_LOGIN_ID(40900, HttpStatus.CONFLICT, "이미 존재하는 로그인 아이디입니다."),

	ACCOMMODATION_HAS_RESERVATIONS(40000, HttpStatus.BAD_REQUEST, "예약이 존재하는 숙소는 삭제할 수 없습니다."),
	NOT_AUTHORIZED_TO_DELETE(40301, HttpStatus.BAD_REQUEST, "해당 숙소에 대한 삭제 권한이 없습니다."),
	INVALID_RESERVATION_DATE_RANGE(40002, HttpStatus.BAD_REQUEST, "체크인 날짜는 체크아웃보다 앞서야 합니다."),
	DUPLICATE_RESERVATION_DATE(40003, HttpStatus.BAD_REQUEST, "해당 날짜에는 이미 예약이 존재합니다."),
	NOT_FOUND_RESERVATION(40404, HttpStatus.NOT_FOUND, "예약 정보를 찾을 수 없습니다."),
	CANNOT_CANCEL_AFTER_CHECKOUT(40006, HttpStatus.BAD_REQUEST, "체크아웃이 지난 예약은 취소할 수 없습니다."),
	EXCEEDS_MAX_GUESTS(40007, HttpStatus.BAD_REQUEST, "예약 인원이 최대 허용 인원을 초과했습니다."),

	INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 에러입니다."),
	INVALID_IMAGE(40001, HttpStatus.BAD_REQUEST, "이미지는 최소 1개 이상 최대 5개 이하로 업로드해야 합니다."),

	S3_UPLOAD_FAILED(50001, HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 업로드에 실패했습니다."),
	INVALID_IMAGE_TYPE(40008, HttpStatus.BAD_REQUEST, "이미지만 업로드 가능합니다."),
	IMAGE_TOO_LARGE(40009, HttpStatus.BAD_REQUEST, "업로드 가능한 이미지 용량을 초과했습니다."),

	LOCK_FAILED(40010, HttpStatus.BAD_REQUEST, "다른 사용자가 해당 날짜를 예약 중입니다."),

	ALREADY_WRITTEN_REVIEW(40011, HttpStatus.BAD_REQUEST, "이미 작성된 리뷰가 있습니다."),
	INVALID_RESERVATION_STATUS(40012, HttpStatus.BAD_REQUEST, "예약이 확정되지 않았습니다."),
	CHECKOUT_NOT_PASSED(40013, HttpStatus.BAD_REQUEST, "아직 체크아웃이 지나지 않았습니다."),
	NOT_FOUND_REVIEW(40405, HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),

	NOT_AUTHOR_OF_REVIEW(40405, HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다.");

	private final Integer code;
	private final HttpStatus httpStatus;
	private final String message;
}
