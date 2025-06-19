package rice_monkey.listing.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rice_monkey.listing.domain.CommentStatus;
import rice_monkey.listing.domain.ListingComment;

import java.util.List;

public interface ListingCommentRepository extends JpaRepository<ListingComment, Long> {

    List<ListingComment> findCommentsByListingIdAndStatus(Long stayId, CommentStatus status);

    @Query("SELECT AVG(lc.rating) FROM ListingComment lc WHERE lc.listing.id = :listingId AND lc.status = :status")
    Double findCommentRatingAvg(@Param("listingId") Long listingId, @Param("status") CommentStatus status);

    Integer countByListingIdAndStatus(Long listingId, CommentStatus status);
}
