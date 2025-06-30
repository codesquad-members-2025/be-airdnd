package com.dmz.airdnd.accommodation.domain;

import com.dmz.airdnd.common.exception.ErrorCode;
import com.dmz.airdnd.common.exception.LabelNotFoundException;

public enum LabelType {
	WIFI("와이파이"),
	KITCHEN("주방"),
	PARKING("주차 공간"),
	AIR_CONDITIONING("에어컨"),
	HEATING("난방"),
	TV("TV"),
	WASHER("세탁기"),
	DRYER("건조기"),
	POOL("수영장"),
	GYM("헬스장"),
	BREAKFAST("조식 제공"),
	PETS_ALLOWED("반려동물 동반 가능"),
	SMOKE_DETECTOR("연기 탐지기"),
	CARBON_MONOXIDE_DETECTOR("일산화탄소 탐지기"),
	FIRE_EXTINGUISHER("소화기"),
	FIRST_AID_KIT("구급상자"),
	FIREPLACE("벽난로"),
	HOT_TUB("온수 욕조"),
	INTERNET_WORKSPACE("업무 가능 공간"),
	FAMILY_FRIENDLY("가족 환영"),
	KIDS_FRIENDLY("어린이 환영"),
	ELEVATOR("엘리베이터"),
	BEACH_ACCESS("해변 바로 앞"),
	MOUNTAIN_VIEW("산 전망"),
	LAKE_VIEW("호수 전망"),
	CITY_CENTER("도심 접근성"),
	PRIVATE_ENTRANCE("개별 출입구"),
	SELF_CHECKIN("셀프 체크인"),
	KEYPAD_ENTRY("키패드 출입"),
	LONG_TERM_STAYS_ALLOWED("장기 숙박 가능");

	private final String displayName;

	private LabelType(String displayName) {
		this.displayName = displayName;
	}

	public String getId() {
		return this.name();
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public static String fromDisplayName(String displayName) {
		for (LabelType type : values()) {
			if (type.displayName.equals(displayName)) {
				return String.valueOf(type);
			}
		}
		throw new LabelNotFoundException(ErrorCode.LABEL_NOT_FOUND);
	}
}
