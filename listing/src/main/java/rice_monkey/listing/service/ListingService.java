package rice_monkey.listing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rice_monkey.listing.fegin.ImageServiceClient;
import rice_monkey.listing.Repository.ListingRepository;
import rice_monkey.listing.Repository.ListingCommentRepository;
import rice_monkey.listing.Repository.TagRepository;
import rice_monkey.listing.domain.*;
import rice_monkey.listing.dto.*;
import rice_monkey.listing.exception.ListingNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final ListingCommentRepository listingCommentRepository;
    private final TagRepository tagRepository;
    private final ImageServiceClient imageServiceClient;

    @Transactional
    public Long registerListing(ListingCreateRequest request) {

        Long imageId = 0L;
        if(request.getImage() != null || !request.getImage().isEmpty()){
            imageId = imageServiceClient.uploadImage(request.getImage()).imageId();
        }

        List<Tag> tags = tagRepository.findAllById(request.getTagIds());

        Listing listing = Listing.builder()
                .name(request.getName())
                .price(request.getPrice())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(ListingStatus.valueOf(request.getStatus()))
                .imgId(imageId)
                .hostId(request.getHostId())
                .type(StayType.valueOf(request.getType()))
                .maxGuests(request.getMaxGuests())
                .address(request.getAddress().toValueObject())
                .tags(tags)
                .build();

        return listingRepository.save(listing).getId();
    }

    @Transactional
    public Listing findActiveListing(Long listingId) {
        return listingRepository.findById(listingId)
                .filter(listing -> listing.getStatus() == ListingStatus.AVAILABLE)
                .orElseThrow(() -> new IllegalArgumentException("숙소가 사용 불가능한 상태입니다."));
    }

    @Transactional(readOnly = true)
    public ListingDetailResponse getListingDetail(Long id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException("숙소를 찾을 수 없습니다."));

        return ListingDetailResponse.from(listing,getImageUrl(listing));
    }

    @Transactional(readOnly = true)
    public List<ListingListQueryResponse> getListingListWithQuery(ListingSearchCondition condition){
        List<Listing> listings = listingRepository.findAllByCondition(condition);
        List<ListingListQueryResponse> responses = new ArrayList<>();
        for (Listing listing : listings) {
            Double avgRating = getAvgRating(listing);
            Integer listingCommentCountingNumber = getListingCommentNumber(listing);
            List<TagResponse> tagResponses = switchToTagResponse(listing);
            String imageUrl = getImageUrl(listing);
            ListingListQueryResponse switchingResponse = ListingListQueryResponse.switching(listing,imageUrl, avgRating, listingCommentCountingNumber, tagResponses);
            responses.add(switchingResponse);
        }
        return responses;
    }

    private String getImageUrl(Listing listing) {
        return imageServiceClient.getImageById(listing.getId()).url();
    }


    @Transactional(readOnly = true)
    public ListingPricesMetaData getListingPricesMetaData() {
        List<Integer> allPrices = listingRepository.findAllPrices();
        Map<Integer, Long> countPerPrice = getCountPerPrice(allPrices);
        int avgPrice = calculateAvgPrice(allPrices);
        int maxPrice = calculateMaxPrice(allPrices);
        int minPrice = calculateMinPrice(allPrices);
        return new ListingPricesMetaData(countPerPrice, avgPrice, maxPrice, minPrice);
    }


    private Double getAvgRating(Listing listing) {
        return listingCommentRepository.findCommentRatingAvg(listing.getId(),CommentStatus.ACTIVE);
    }

    private Integer getListingCommentNumber(Listing listing) {
        return listingCommentRepository.countByListingIdAndStatus(listing.getId(),CommentStatus.ACTIVE);
    }

    private List<TagResponse> switchToTagResponse(Listing listing) {
        return listing.getTags().stream()
                .map(TagResponse::fromTag)
                .toList();
    }



    private int calculateAvgPrice(List<Integer> prices) {
        return (int) prices.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
    }

    private int calculateMaxPrice(List<Integer> prices) {
        return prices.stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }

    private int calculateMinPrice(List<Integer> prices) {
        return prices.stream()
                .mapToInt(Integer::intValue)
                .min()
                .orElse(0);
    }

    private Map<Integer, Long> getCountPerPrice(List<Integer> prices) {
        return prices.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }


}

