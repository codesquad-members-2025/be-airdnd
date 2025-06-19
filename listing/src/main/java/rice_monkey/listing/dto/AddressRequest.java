package rice_monkey.listing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import rice_monkey.listing.domain.Address;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {
    private String fullAddress;
    private Double latitude;
    private Double longitude;

    public Address toValueObject() {
        return Address.builder()
                .fullAddress(fullAddress)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
