package rice_monkey.listing.Repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rice_monkey.listing.domain.Listing;
import rice_monkey.listing.domain.ListingStatus;
import rice_monkey.listing.dto.ListingSearchCondition;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long> , ListingQueryDslRepository {

    @EntityGraph(attributePaths = {"tags", "comments"})

    Optional<Listing> findById(Long id);

    List<Listing> findAllByCondition(ListingSearchCondition condition);

    Optional<Listing> findByIdAndStatus(Long id, ListingStatus status);

    @Query("SELECT L.price FROM Listing L")
    List<Integer> findAllPrices();
}



