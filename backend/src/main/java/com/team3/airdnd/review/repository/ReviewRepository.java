package com.team3.airdnd.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.team3.airdnd.accommodation.dto.ReviewDto;
import com.team3.airdnd.review.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	@Query("""
		    SELECT new com.team3.airdnd.accommodation.dto.ReviewDto(
		        r.id, r.content, r.createdAt,
		        u.id, u.username, u.profileUrl,
		        r.rating
		    )
		    FROM Review r
		    JOIN r.reservation res
		    JOIN res.guest u
		    WHERE res.accommodation.id = :accommodationId
		""")
	List<ReviewDto> findReviewByAccommodationId(@Param("accommodationId") Long accommodationId);

	boolean existsByReservationId(Long reservationId);
}
