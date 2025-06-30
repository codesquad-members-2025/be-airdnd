package com.dmz.airdnd.accommodation.domain;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "address",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_address_base_detail",
		columnNames = {"base_address", "detailed_address"}
	)
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Address {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String country;

	@Column(length = 255, nullable = false)
	private String baseAddress;

	@Column(length = 255)
	private String detailedAddress;

	@Column(nullable = false, columnDefinition = "POINT SRID 4326 NOT NULL")
	private Point location;
}

