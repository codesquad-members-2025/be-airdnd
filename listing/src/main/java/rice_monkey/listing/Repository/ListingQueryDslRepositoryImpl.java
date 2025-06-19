package rice_monkey.listing.Repository;


import com.querydsl.core.types.dsl.BooleanTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import rice_monkey.listing.domain.QListing.Listing;
import rice_monkey.listing.dto.ListingSearchCondition;
import com.querydsl.core.BooleanBuilder;

import java.time.LocalDate;
import java.util.List;

import static com.querydsl.core.types.dsl.Expressions.booleanTemplate;

@Repository
@RequiredArgsConstructor
public class ListingQueryDslRepositoryImpl implements ListingQueryDslRepository {

    @Override
    public List<Listing> getListingsFilteredByCondition(ListingSearchCondition condition) {
        return List.of();
    }

    private void addDateCondition(ListingSearchCondition condition, BooleanBuilder builder) {
        if (condition.hasDateCondition()) {
            List<LocalDate> reservationDates = condition.getReservationDates();
            builder.andNot(Listing.closedStayDates.any().in(reservationDates));
        }
    }

    private void addGuestCountCondition(ListingSearchCondition condition, BooleanBuilder builder) {
        if (condition.hasGuestCountCondition()) {
            Integer guestCount = condition.getGuestCount();
            builder.and(Listing.maxGuests.goe(guestCount));
        }
    }

    private void addPriceCondition(ListingSearchCondition condition, BooleanBuilder builder) {
        if (condition.hasPriceCondition()) {
            Integer minPrice = condition.getMinPrice();
            Integer maxPrice = condition.getMaxPrice();
            builder.and(Listing.price.goe(minPrice))
                    .and(Listing.price.loe(maxPrice));
        }
    }

    private void addLocationCondition(ListingSearchCondition condition, BooleanBuilder builder) {
        if (condition.hasLocationCondition()) {
            Double latitude = condition.getLatitude();
            Double longitude = condition.getLongitude();
            Integer distance = condition.getDistance();

            BooleanTemplate distanceCondition = booleanTemplate(
                    "ST_Distance_Sphere(point({0}, {1}), point({2}, {3})) < {4}",
                    longitude, latitude, Listing.location.longitude, Listing.location.latitude, distance);
            builder.and(distanceCondition);
        }
    }
}
