package com.sillage.fragrance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class CommonsClientConfig {

	@Bean
	RestClient commonsRestClient() {
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(4))
				.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(Duration.ofSeconds(10));

		return RestClient.builder()
				.baseUrl("https://commons.wikimedia.org")
				.requestFactory(requestFactory)
				.defaultHeader(
						HttpHeaders.USER_AGENT,
						"Sillage-Fragrance-App/1.0 (Wikimedia Commons media lookup)"
				)
				.build();
	}
}
