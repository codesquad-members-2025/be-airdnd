package rice_monkey.listing.domain;

import java.util.Arrays;

public enum StayType {
    APT, PENSION, HOTEL;

    public static StayType of(String typeValue) {
        return Arrays.stream(values())
                .filter(stayType -> stayType.name().equals(typeValue.toUpperCase()))
                .findAny()
                .orElseThrow(() -> new NotFoundException("일치하는 숙소 타입을 찾을 수 없습니다."));
    }
}
