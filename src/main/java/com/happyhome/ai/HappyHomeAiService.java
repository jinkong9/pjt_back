package com.happyhome.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.happyhome.house.dto.HouseDeal;
import com.happyhome.house.service.HouseDealService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class HappyHomeAiService {

    private static final Logger log = LoggerFactory.getLogger(HappyHomeAiService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final HouseDealService houseDealService;
    private final HappyHomeRagDocumentFactory documentFactory;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final int bootstrapLimit;
    private final String apiKey;
    private final String baseUrl;
    private final String completionsPath;
    private final String chatModel;
    private boolean indexed;
    private int indexedDocumentCount;

    public HappyHomeAiService(
            ChatClient chatClient,
            VectorStore vectorStore,
            HouseDealService houseDealService,
            HappyHomeRagDocumentFactory documentFactory,
            RestClient.Builder restClientBuilder,
            @Value("${spring.ai.openai.api-key:}") String apiKey,
            @Value("${spring.ai.openai.chat.base-url:${spring.ai.openai.base-url:https://api.openai.com}}") String baseUrl,
            @Value("${spring.ai.openai.chat.completions-path:/v1/chat/completions}") String completionsPath,
            @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}") String chatModel,
            @Value("${happyhome.ai.rag.bootstrap-limit:20}") int bootstrapLimit
    ) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.houseDealService = houseDealService;
        this.documentFactory = documentFactory;
        this.restClient = restClientBuilder.build();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.completionsPath = completionsPath;
        this.chatModel = chatModel;
        this.bootstrapLimit = bootstrapLimit;
    }

    public AiChatResponse chat(String question) {
        try {
            ensureIndexed();

            String answer = chatClient.prompt()
                    .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                            .searchRequest(SearchRequest.builder()
                                    .topK(5)
                                    .similarityThreshold(0.2)
                                    .build())
                            .build())
                    .user(question)
                    .call()
                    .content();

            return new AiChatResponse(answer, indexedDocumentCount);
        } catch (RuntimeException exception) {
            log.warn("HappyHome AI chat failed: {}", exception.getMessage());
            return chatWithGmsCompatibleApi(question);
        }
    }

    private AiChatResponse chatWithGmsCompatibleApi(String question) {
        List<HouseDeal> recentDeals = safeRecentDeals(Math.min(bootstrapLimit, 8));
        if (!StringUtils.hasText(apiKey)) {
            return new AiChatResponse(localDealAnswer(question, recentDeals), recentDeals.size());
        }

        try {
            String context = contextFromDeals(recentDeals);
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "system",
                    "content", """
                            You are HappyHome's real-estate assistant.
                            Answer in Korean.
                            Use the provided HappyHome context first.
                            If the context is not enough, say what information is missing instead of inventing facts.
                            """
            ));
            messages.add(Map.of(
                    "role", "user",
                    "content", "HappyHome context:\n" + context + "\n\nUser question:\n" + question
            ));

            Map<String, Object> requestBody = Map.of(
                    "model", chatModel,
                    "messages", messages,
                    "max_completion_tokens", 1024,
                    "temperature", 0.3
            );

            String responseBody = restClient.post()
                    .uri(gmsChatUrl())
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            String answer = objectMapper.readTree(responseBody)
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText("");

            if (!StringUtils.hasText(answer)) {
                return new AiChatResponse(localDealAnswer(question, recentDeals), recentDeals.size());
            }

            return new AiChatResponse(answer, recentDeals.size());
        } catch (IOException | RuntimeException exception) {
            log.warn("HappyHome GMS-compatible chat failed: {}", exception.getMessage());
            return new AiChatResponse(localDealAnswer(question, recentDeals), recentDeals.size());
        }
    }

    private List<HouseDeal> safeRecentDeals(int limit) {
        try {
            return houseDealService.findRecent(limit);
        } catch (RuntimeException exception) {
            log.warn("HappyHome local deal fallback failed: {}", exception.getMessage());
            return List.of();
        }
    }

    private String contextFromDeals(List<HouseDeal> deals) {
        List<Document> documents = documentFactory.fromHouseDeals(deals);
        if (documents.isEmpty()) {
            return "No recent house deal context is available.";
        }

        StringBuilder builder = new StringBuilder();
        for (Document document : documents) {
            if (builder.length() > 0) {
                builder.append("\n---\n");
            }
            builder.append(document.getText());
        }
        return builder.toString();
    }

    private String gmsChatUrl() {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = completionsPath.startsWith("/") ? completionsPath : "/" + completionsPath;
        return normalizedBaseUrl + normalizedPath;
    }

    private String localDealAnswer(String question, List<HouseDeal> deals) {
        if (deals == null || deals.isEmpty()) {
            return """
                    지금은 AI 응답 서버 연결이 불안정해서 간단 답변으로 안내할게요.
                    현재 조회 가능한 최근 실거래 데이터가 없어서 지역명이나 아파트명을 바꿔 다시 검색해 주세요.
                    """;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("AI 응답 서버 연결이 불안정해서 최근 실거래 데이터 기준으로 먼저 안내할게요.\n");
        builder.append("질문: ").append(question).append("\n\n");
        int count = Math.min(deals.size(), 5);
        for (int index = 0; index < count; index++) {
            HouseDeal deal = deals.get(index);
            builder.append(index + 1)
                    .append(". ")
                    .append(value(deal.getAptName()))
                    .append(" - ")
                    .append(value(regionOf(deal)))
                    .append(", 거래금액 ")
                    .append(value(deal.getDealAmount()))
                    .append(", 거래일 ")
                    .append(value(deal.getDealDate()))
                    .append("\n");
        }
        builder.append("\n연결이 복구되면 더 자세한 추천 답변도 제공할 수 있어요.");
        return builder.toString();
    }

    private String regionOf(HouseDeal deal) {
        List<String> parts = new ArrayList<>();
        addIfText(parts, deal.getSidoName());
        addIfText(parts, deal.getGugunName());
        addIfText(parts, deal.getDongName() != null ? deal.getDongName() : deal.getUmdName());
        return String.join(" ", parts);
    }

    private void addIfText(List<String> values, String value) {
        if (StringUtils.hasText(value)) {
            values.add(value.trim());
        }
    }

    private String value(Object value) {
        if (value == null) {
            return "-";
        }
        String text = value.toString();
        return text.isBlank() ? "-" : text;
    }

    private synchronized void ensureIndexed() {
        if (indexed) {
            return;
        }

        List<HouseDeal> recentDeals = houseDealService.findRecent(bootstrapLimit);
        List<Document> documents = documentFactory.fromHouseDeals(recentDeals);
        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
        indexedDocumentCount = documents.size();
        indexed = true;
    }
}
