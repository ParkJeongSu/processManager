package kr.co.aim.infra.config;

import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.opensearch.client.transport.OpenSearchTransport; // 👈 여기 있습니다!
import org.opensearch.client.transport.rest_client.RestClientTransport; // 👈 구현체는 이거!


// 추후 openSearch 사용시 bean 생성
@Configuration
public class OpenSearchConfig {
	@Bean
    public OpenSearchClient openSearchClient() {
        // 1. Low-level RestClient 생성 (기존과 동일하게 서버 연결 정보 설정)
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        ).build();

        // 2. Transport 계층 생성
        // (역할: RestClient와 JSON 파서(Jackson)를 묶어주는 역할)
        OpenSearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        // 3. 최종 OpenSearchClient 생성
        // (역할: 실제 API 호출을 담당하는 객체)
        return new OpenSearchClient(transport);
    }
}
