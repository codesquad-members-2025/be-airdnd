package rice_monkey.listing.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import rice_monkey.listing.domain.Address;
import rice_monkey.listing.domain.Listing;
import rice_monkey.listing.domain.StayType;

import java.util.List;


@RequiredArgsConstructor
@Builder
public class ListingListQueryResponse {

    private final Long id;
    private final String name;
    private final Integer price;
    private final Integer maxGuests;
    private final Address address;
    private final String imageUrl;
    private final StayType type;
    private final Double rating;
    private final Integer commentCount;
    private final List<TagResponse> tagResponses;

    public static ListingListQueryResponse switching(Listing listing,String imageUrl,Double aveRating,Integer commentCount,List<TagResponse> tagResponses) {
        return ListingListQueryResponse.builder()
                .id(listing.getId())
                .name(listing.getName())
                .price(listing.getPrice())
                .maxGuests(listing.getMaxGuests())
                .address(listing.getAddress())
                .imageUrl(imageUrl)
                .type(listing.getType())
                .rating(aveRating)//
                .commentCount(commentCount)
                .tagResponses(tagResponses).build();

    }



}
