package com.happyhome.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

class OpenApiJsonParserTest {

    private final OpenApiJsonParser parser = new OpenApiJsonParser();

    @Test
    void extractsLhDsListItems() {
        String json = """
            {"dsList":[{"PAN_ID":"1","PAN_NM":"서울 청년임대"}]}
            """;

        assertThat(parser.items(json))
                .extracting(node -> node.path("PAN_NM").asText())
                .containsExactly("서울 청년임대");
    }

    @Test
    void extractsLhDsListItemsFromTopLevelArray() {
        String json = """
            [{"dsSch":[{"PAGE":"1"}]},{"dsList":[{"PAN_ID":"1","PAN_NM":"서울 공공임대"}]}]
            """;

        assertThat(parser.items(json))
                .extracting(node -> node.path("PAN_NM").asText())
                .containsExactly("서울 공공임대");
    }

    @Test
    void extractsPublicDataBodyItems() {
        String json = """
            {"response":{"body":{"items":{"item":[{"bizesNm":"중앙약국"}]}}}}
            """;

        assertThat(parser.items(json))
                .extracting(node -> node.path("bizesNm").asText())
                .containsExactly("중앙약국");
    }

    @Test
    void acceptsSingleItemObject() {
        String json = """
            {"response":{"body":{"items":{"item":{"eventType":"공사"}}}}}
            """;

        assertThat(parser.items(json))
                .extracting(JsonNode::isObject)
                .containsExactly(true);
    }

    @Test
    void extractsItsResponseDataItems() {
        String json = """
            {"response":{"data":[{"eventType":"공사","coordX":"126.9","coordY":"37.4"}]}}
            """;

        assertThat(parser.items(json))
                .extracting(node -> node.path("eventType").asText())
                .containsExactly("공사");
    }
}
