package rice_monkey.listing.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@Builder
@RequiredArgsConstructor
public class ListingPricesMetaData {

    private final Map<Integer,Long> countPerPrice;

    private final Integer minPrice;

    private final Integer maxPrice;

    private final Integer avgPrice;
}
