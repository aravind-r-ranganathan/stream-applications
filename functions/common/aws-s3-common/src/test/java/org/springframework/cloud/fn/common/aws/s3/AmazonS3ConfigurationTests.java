/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.fn.common.aws.s3;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import io.awspring.cloud.core.region.RegionProvider;
import io.awspring.cloud.core.region.StaticRegionProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

/**
 * @author Timo Salm
 * @author Artem Bilan
 */
public class AmazonS3ConfigurationTests {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withConfiguration(
					AutoConfigurations.of(
							CompatibleStorageAmazonS3Configuration.class,
							AmazonS3Configuration.class))
			.withUserConfiguration(TestConfiguration.class);

	private static final String TEST_REGION_NAME = "eu-central-1";

	@Test
	public void testAmazonS3Configuration() {
		runner.withPropertyValues().run(context -> {
			final AmazonS3Client amazonS3 = (AmazonS3Client) context.getBean(AmazonS3.class);
			Assertions.assertNotNull(amazonS3);
			Assertions.assertEquals(TEST_REGION_NAME, amazonS3.getRegionName());
			Assertions.assertTrue(amazonS3.getResourceUrl("b", "k")
					.startsWith("https://s3.eu-central-1.amazonaws.com"));
		});
	}

	@Test
	public void testAmazonS3ConfigurationForS3CompatibleStorage() {
		runner.withPropertyValues(
				"s3.common.endpoint-url=http://localhost:8080"
		).run(context -> {
			final AmazonS3Client amazonS3 = (AmazonS3Client) context.getBean(AmazonS3.class);
			Assertions.assertNotNull(amazonS3);
			Assertions.assertTrue(amazonS3.getResourceUrl("b", "k")
					.startsWith("http://localhost:8080"));
		});
	}

	private static class TestConfiguration {

		@Bean
		RegionProvider regionProvider() {
			return new StaticRegionProvider(TEST_REGION_NAME);
		}

		@Bean
		AWSCredentialsProvider awsCredentialsProvider() {
			return new AWSStaticCredentialsProvider(new BasicAWSCredentials("accessKey", "secretKey"));
		}

	}

}
