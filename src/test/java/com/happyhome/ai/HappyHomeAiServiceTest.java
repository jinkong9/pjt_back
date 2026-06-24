package com.happyhome.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.happyhome.house.dto.HouseDeal;
import com.happyhome.house.service.HouseDealService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HappyHomeAiServiceTest {

    @Test
    void returnsLocalDealAnswerWhenAiProviderFailsAndNoCompatibleApiKeyExists() {
        ChatClient chatClient = mock(ChatClient.class);
        VectorStore vectorStore = mock(VectorStore.class);
        HouseDealService houseDealService = mock(HouseDealService.class);
        HappyHomeRagDocumentFactory documentFactory = new HappyHomeRagDocumentFactory();
        HouseDeal deal = new HouseDeal();
        deal.setNo(1);
        deal.setAptName("테스트 아파트");
        deal.setSidoName("서울특별시");
        deal.setGugunName("강남구");
        deal.setDongName("역삼동");
        deal.setDealAmount("120,000");
        deal.setDealYear(2026);
        deal.setDealMonth(6);
        deal.setDealDay(20);
        when(houseDealService.findRecent(8)).thenReturn(List.of(deal));
        doThrow(new RuntimeException("invalid api key")).when(vectorStore).add(anyList());

        HappyHomeAiService service = new HappyHomeAiService(
                chatClient,
                vectorStore,
                houseDealService,
                documentFactory,
                RestClient.builder(),
                "",
                "https://api.openai.com",
                "/v1/chat/completions",
                "gpt-4o-mini",
                20
        );

        AiChatResponse response = service.chat("강남구 최근 실거래가 알려줘");

        assertThat(response.answer())
                .contains("테스트 아파트", "강남구", "120,000")
                .doesNotContain("AI 설정", "GMS API");
        assertThat(response.indexedDocumentCount()).isEqualTo(1);
    }

    @Test
    void sendsMaxCompletionTokensWhenUsingGmsCompatibleApi() {
        ChatClient chatClient = mock(ChatClient.class);
        VectorStore vectorStore = mock(VectorStore.class);
        HouseDealService houseDealService = mock(HouseDealService.class);
        HappyHomeRagDocumentFactory documentFactory = new HappyHomeRagDocumentFactory();
        when(houseDealService.findRecent(20)).thenReturn(List.of());
        when(houseDealService.findRecent(8)).thenReturn(List.of());

        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        server.expect(requestTo("https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer gms-key"))
                .andExpect(content().string(allOf(
                        containsString("\"max_completion_tokens\""),
                        not(containsString("\"max_tokens\""))
                )))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"content":"GMS 응답 성공"}}]}
                        """, MediaType.APPLICATION_JSON));

        HappyHomeAiService service = new HappyHomeAiService(
                chatClient,
                vectorStore,
                houseDealService,
                documentFactory,
                restClientBuilder,
                "gms-key",
                "https://gms.ssafy.io/gmsapi/api.openai.com",
                "/v1/chat/completions",
                "gpt-5.4-mini",
                20
        );

        AiChatResponse response = service.chat("ping");

        assertThat(response.answer()).isEqualTo("GMS 응답 성공");
        server.verify();
    }
}
