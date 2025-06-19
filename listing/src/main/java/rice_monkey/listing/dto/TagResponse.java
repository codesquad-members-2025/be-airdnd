package rice_monkey.listing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import rice_monkey.listing.domain.Tag;

@Getter
@AllArgsConstructor
public class TagResponse {

    private String name;

    private String description;

    public static TagResponse fromTag(final Tag tag) {
        return new TagResponse(tag.getName(), tag.getDescription());
    }
}
