package com.dmz.airdnd.accommodation.repository.elasticsearch;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Repository;

import com.dmz.airdnd.accommodation.document.AccommodationDocument;

@Repository
public interface AccommodationSearchRepository extends ElasticsearchRepository<AccommodationDocument, String> {
	Page<AccommodationDocument> findByLocationNear(Point location, Distance distance, Pageable pageable);
}
