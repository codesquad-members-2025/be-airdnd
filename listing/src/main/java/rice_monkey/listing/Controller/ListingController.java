package rice_monkey.listing.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rice_monkey.listing.domain.Listing;
import rice_monkey.listing.dto.*;
import rice_monkey.listing.service.ListingService;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    public ResponseEntity<Long> createListing(@RequestBody ListingCreateRequest request) {
        Long listingId = listingService.registerListing(request);
        return ResponseEntity.ok(listingId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingDetailResponse> getListingDetail(@PathVariable Long id) {
        ListingDetailResponse response = listingService.getListingDetail(id);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public List<ListingListQueryResponse> queryListingList(@ModelAttribute ListingSearchCondition condition){
        return listingService.getListingListWithQuery(condition);
    }


    @GetMapping("/prices_meta")
    public ListingPricesMetaData getListingPricesMetaData(){
        return listingService.getListingPricesMetaData();
    }




}

