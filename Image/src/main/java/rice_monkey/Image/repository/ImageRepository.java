package rice_monkey.Image.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rice_monkey.Image.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {}

