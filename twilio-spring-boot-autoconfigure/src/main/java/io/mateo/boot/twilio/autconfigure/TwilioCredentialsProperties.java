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
package io.mateo.boot.twilio.autconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Twilio credentials.
 *
 * @author Francisco Mateo
 * @since 1.0.0
 * @see <a href="https://twil.io/secure">Store Your Twilio Credentials
 *      Securely</a>
 */
@ConfigurationProperties("twilio.credentials")
public class TwilioCredentialsProperties {

	private String accountSid;

	private String authToken;

	public String getAccountSid() {
		return accountSid;
	}

	public void setAccountSid(String accountSid) {
		this.accountSid = accountSid;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

}
