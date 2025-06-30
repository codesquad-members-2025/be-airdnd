package com.dmz.airdnd.accommodation.document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.geo.Point;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "accommodations")
public class AccommodationDocument {

	@Id
	private String id;

	@Field(type = FieldType.Text)
	private String name;

	@Field(type = FieldType.Text)
	private String description;

	@Field(type = FieldType.Long)
	private Long pricePerDay;

	@Field(type = FieldType.Keyword)
	private String currency;

	@Field(type = FieldType.Integer)
	private Integer maxGuests;

	@Field(type = FieldType.Integer)
	private Integer bedCount;

	@Field(type = FieldType.Integer)
	private Integer bedroomCount;

	@Field(type = FieldType.Integer)
	private Integer bathroomCount;

	@Field(type = FieldType.Date)
	private Instant createdAt;

	@Field(type = FieldType.Date)
	private Instant updatedAt;

	@GeoPointField
	private Point location;

	@Field(type = FieldType.Keyword)
	private String country;

	@Field(type = FieldType.Text)
	private String baseAddress;

	@Field(type = FieldType.Text)
	private String detailedAddress;

	@Field(type = FieldType.Keyword)
	private List<String> labels;

	@Field(type = FieldType.Date, format = DateFormat.date)
	private List<LocalDate> availableDates;
}
