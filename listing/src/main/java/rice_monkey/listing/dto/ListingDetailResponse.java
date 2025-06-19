package rice_monkey.listing.dto;

import lombok.Builder;
import lombok.Getter;
import rice_monkey.listing.domain.Listing;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ListingDetailResponse {

    private Long id;
    private String name;
    private Integer price;
    private String type;
    private Integer maxGuests;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;

    private String imgUrl;
    private Long hostId;

    private AddressDto address;
    private List<TagDto> tags;
    private List<ListingCommentDto> comments;

    public static ListingDetailResponse from(Listing listing, String imageUrl) {
        return ListingDetailResponse.builder()
                .id(listing.getId())
                .name(listing.getName())
                .price(listing.getPrice())
                .type(String.valueOf(listing.getType()))
                .maxGuests(listing.getMaxGuests())
                .startDate(listing.getStartDate())
                .endDate(listing.getEndDate())
                .status(String.valueOf(listing.getStatus()))
                .imgUrl(imageUrl)
                .hostId(listing.getHostId())
                .address(AddressDto.builder()
                        .fullAddress(listing.getAddress().getFullAddress())
                        .latitude(listing.getAddress().getLatitude())
                        .longitude(listing.getAddress().getLongitude())
                        .build())
                .tags(listing.getTags().stream()
                        .map(tag -> TagDto.builder()
                                .description(tag.getDescription())
                                .name(tag.getName())
                                .build())
                        .toList())
                .comments(listing.getComments().stream()
                        .map(c -> ListingCommentDto.builder()
                                .id(c.getId())
                                .writer(c.getWriter())
                                .content(c.getContent())
                                .createdAt(c.getCreatedAt())
                                .rating(c.getRating())
                                .build())
                        .toList())
                .build();
    }

    @Getter @Builder
    public static class AddressDto {
        private String fullAddress;
        private Double latitude;
        private Double longitude;
    }

    @Getter @Builder
    public static class TagDto {
        private String name;
        private String description;
    }

    @Getter @Builder
    public static class ListingCommentDto {
        private Long id;
        private String writer;
        private String content;
        private LocalDateTime createdAt;
        private Double rating;
    }
}


