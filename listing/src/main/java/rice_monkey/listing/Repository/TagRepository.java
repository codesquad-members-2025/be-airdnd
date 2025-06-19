package rice_monkey.listing.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rice_monkey.listing.domain.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

}
