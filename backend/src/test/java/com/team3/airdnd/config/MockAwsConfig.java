package com.team3.airdnd.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.team3.airdnd.aws.S3FileService;
import com.team3.airdnd.storedFile.StoredFileService;
import com.team3.airdnd.storedFile.repository.StoredFileRepository;

@TestConfiguration
public class MockAwsConfig {
	@Bean
	public S3FileService s3FileService() {
		S3FileService mock = Mockito.mock(S3FileService.class);
		Mockito.when(mock.upload(Mockito.any()))
			.thenReturn("https://mock-s3.com/test.jpg");
		return mock;
	}

	@Bean
	public StoredFileService storedFileService(
		StoredFileRepository storedFileRepository,
		S3FileService s3FileService
	) {
		return new StoredFileService(storedFileRepository, s3FileService);
	}
}
