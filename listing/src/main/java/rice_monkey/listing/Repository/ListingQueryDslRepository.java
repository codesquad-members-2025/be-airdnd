package rice_monkey.listing.Repository;

import rice_monkey.listing.domain.Listing;
import rice_monkey.listing.dto.ListingSearchCondition;

import java.util.List;

public interface ListingQueryDslRepository {
    List<Listing> getListingsFilteredByCondition(ListingSearchCondition condition);
}
