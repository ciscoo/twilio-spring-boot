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

import com.google.common.util.concurrent.ListeningExecutorService;
import com.twilio.Twilio;
import com.twilio.exception.TwilioException;
import com.twilio.http.TwilioRestClient;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * {@link ApplicationContextInitializer} for configuring the {@link Twilio} singleton.
 *
 * @author Francisco Mateo
 * @since 1.0.0
 */
public class TwilioApplicationContextInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final Log logger = LogFactory.getLog(TwilioApplicationContextInitializer.class);

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		applicationContext.addApplicationListener(new Listener());
	}

	private static class Listener implements ApplicationListener<ContextRefreshedEvent> {

		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
			ApplicationContext context = event.getApplicationContext();

			TwilioCredentialsProperties credentials = context.getBean(TwilioCredentialsProperties.class);

			try {
				Twilio.init(credentials.getAccountSid(), credentials.getAuthToken());
			}
			catch (TwilioException e) {
				if (logger.isWarnEnabled()) {
					logger.warn("Failed to initialize the Twilio environment", e);
				}
			}

			context.getBeanProvider(TwilioRestClient.class).ifAvailable(applyTwilioRestClient());
			context.getBeanProvider(ListeningExecutorService.class)
					.ifAvailable(applyListeningExecutorServiceConsumer());
		}

		private Consumer<ListeningExecutorService> applyListeningExecutorServiceConsumer() {
			return listeningExecutorService -> {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"Overriding default ListeningExecutorService with provided ListeningExecutorService bean");
				}
				Twilio.setExecutorService(listeningExecutorService);
			};
		}

		private Consumer<TwilioRestClient> applyTwilioRestClient() {
			return restClient -> {
				if (logger.isDebugEnabled()) {
					logger.debug("Overriding default TwilioRestClient with provided TwilioRestClient bean");
				}
				Twilio.setRestClient(restClient);
			};
		}

	}

}
