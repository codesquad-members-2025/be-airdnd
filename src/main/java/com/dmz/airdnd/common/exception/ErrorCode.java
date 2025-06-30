package com.dmz.airdnd.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
	// 유저 관련
	USER_NOT_FOUND(404, "USER_NOT_FOUND", "해당 유저를 찾을 수 없습니다."),
	DUPLICATE_LOGIN_ID(409, "DUPLICATE_LOGIN_ID", "이미 존재하는 로그인 아이디입니다."),
	DUPLICATE_EMAIL(409, "DUPLICATE_EMAIL", "이미 존재하는 이메일입니다."),
	DUPLICATE_PHONE(409, "DUPLICATE_PHONE", "이미 존재하는 전화번호입니다."),

	// 인증 관련
	UNAUTHORIZED(401, "UNAUTHORIZED", "인증이 필요합니다."),
	INVALID_PASSWORD(401, "INVALID_PASSWORD", "잘못된 비밀번호입니다."),

	// JWT 토큰 관련
	INVALID_JWT_SIGNATURE(401, "INVALID_JWT_SIGNATURE", "잘못된 JWT 서명입니다."),
	EXPIRED_JWT_TOKEN(401, "EXPIRED_JWT_TOKEN", "만료된 JWT 토큰입니다."),
	UNSUPPORTED_JWT_TOKEN(401, "UNSUPPORTED_JWT_TOKEN", "지원하지 않는 JWT 토큰입니다."),
	INVALID_JWT_TOKEN(401, "INVALID_JWT_TOKEN", "JWT 토큰이 잘못되었습니다."),
	EMPTY_JWT_TOKEN(401, "EMPTY_JWT_TOKEN", "헤더에 JWT 토큰이 없습니다."),
	// 요청 포맷 관련
	INVALID_REQUEST_FORMAT(400, "INVALID_REQUEST_FORMAT", "잘못된 요청 형식입니다."),
	// 서버 오류
	INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "서버에 오류가 발생했습니다."),
	// 파일 관련
	PAYLOAD_TOO_LARGE(413, "PAYLOAD_TOO_LARGE", "파일크기가 혀용된 최대 크기를 초과하였습니다."),
	// 인가 관련
	FORBIDDEN(403, "FORBIDDEN", "해당 작업을 수행할 권한이 없습니다."),
	// 예약 관련
	DUPLICATE_RESERVATION(409, "DUPLICATE_RESERVATION", "이미 존재하는 예약입니다."),
	RESERVATION_IN_PROGRESS(409, "RESERVATION_IN_PROGRESS", "다른 사용자가 예약 중입니다."),
	LOCK_INTERRUPTED(409, "LOCK_INTERRUPTED", "락 획득 중 인터럽트 발생"),
	// Google API 관련
	GEOCODING_FAILED(502, "GEOCODING_FAILED", "주소 정보를 가져오는 데 실패했습니다."),
	// 필터링 요청 관련
	INVALID_DATE_RANGE(400, "INVALID_DATE_RANGE", "체크인은 체크아웃 이전이어야 합니다."),
	INVALID_MIN_MAX_PRICE(400, "INVALID_MIN_MAX_PRICE", "최소 가격은 최대 가격보다 작아야 합니다."),
	// 숙소 관련
	ACCOMMODATION_NOT_FOUND(404, "ACCOMMODATION_NOT_FOUND", "해당 숙소를 찾을 수 없습니다."),
	LABEL_NOT_FOUND(404, "LABEL_NOT_FOUND", "등록되지 않은 라벨이 있습니다."),
	DUPLICATE_ADDRESS(409, "DUPLICATE_ADDRESS", "동일한 주소가 이미 등록되어 있습니다."),
	// 채팅 관련
	CHAT_ROOM_NOT_FOUND(404, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."),
	;

	private final int status;
	private final String code;
	private final String message;

	ErrorCode(int status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
}
