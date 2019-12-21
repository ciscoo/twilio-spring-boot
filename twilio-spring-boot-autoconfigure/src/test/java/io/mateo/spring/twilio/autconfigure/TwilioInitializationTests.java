/*
 * Copyright 2002-2019 the original author or authors.
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
package io.mateo.spring.twilio.autconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.twilio.Twilio;
import com.twilio.http.TwilioRestClient;
import io.mateo.spring.twilio.autconfigure.TwilioAutoConfiguration;
import io.mateo.spring.twilio.autconfigure.TwilioCredentialsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;

class TwilioInitializationTests {

	private ApplicationContextRunner contextRunner;

	@BeforeEach
	void setup() {
		contextRunner = new ApplicationContextRunner()
				.withConfiguration(AutoConfigurations.of(TwilioAutoConfiguration.class));
	}

	@Test
	void testCredentials() {
		contextRunner = contextRunner.withPropertyValues("twilio.credentials.account-sid=test-account",
				"twilio.credentials.auth-token=test-token");

		contextRunner.run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context).hasSingleBean(TwilioCredentialsProperties.class);

			TwilioCredentialsProperties credentialsProperties = context.getBean(TwilioCredentialsProperties.class);

			assertThat(credentialsProperties.getAccountSid()).isEqualTo("test-account");
			assertThat(credentialsProperties.getAuthToken()).isEqualTo("test-token");
		});
	}

	@Test
	void testTwilioConfiguration() {
		contextRunner = contextRunner.withUserConfiguration(TestConfiguration.class)
				.withInitializer(new TwilioApplicationContextInitializer())
				.withPropertyValues("twilio.credentials.account-sid=my-sid", "twilio.credentials.auth-token=my-token");

		contextRunner.run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context).hasSingleBean(TwilioRestClient.class);
			assertThat(context).hasSingleBean(ListeningExecutorService.class);
			assertThat(mockingDetails(Twilio.getRestClient()).isMock()).isTrue();
			assertThat(mockingDetails(Twilio.getExecutorService()).isMock()).isTrue();

			String username = (String) ReflectionTestUtils.getField(Twilio.class, "username");
			String password = (String) ReflectionTestUtils.getField(Twilio.class, "password");

			assertThat(username).isEqualTo("my-sid");
			assertThat(password).isEqualTo("my-token");
		});
	}

	@Configuration(proxyBeanMethods = false)
	static class TestConfiguration {

		@Bean
		public TwilioRestClient twilioRestClient() {
			return Mockito.mock(TwilioRestClient.class);
		}

		@Bean
		public ListeningExecutorService listeningExecutorService() {
			return Mockito.mock(ListeningExecutorService.class);
		}

	}

}
