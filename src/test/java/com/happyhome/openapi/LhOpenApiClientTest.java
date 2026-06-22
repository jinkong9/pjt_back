package com.happyhome.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.happyhome.config.OpenApiProperties;
import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalSupply;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class LhOpenApiClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LhOpenApiClient client = new LhOpenApiClient(new OpenApiProperties());

    @Test
    void mapsActualLhSupplyFields() throws Exception {
        JsonNode node = objectMapper.readTree("""
                {
                  "SBD_LGO_NM": "Incheon public housing",
                  "DDO_AR": "26.95",
                  "HTY_NNA": "26A",
                  "LS_GMY": "see notice",
                  "RFE": "see notice",
                  "NOW_HSH_CNT": "2"
                }
                """);

        RentalSupply supply = invokeSupply(node);

        assertThat(supply.address()).isEqualTo("Incheon public housing");
        assertThat(supply.area()).isEqualTo("26.95");
        assertThat(supply.houseType()).isEqualTo("26A");
        assertThat(supply.householdCount()).isEqualTo("2");
    }

    @Test
    void mapsActualLhDetailFields() throws Exception {
        JsonNode node = objectMapper.readTree("""
                {
                  "SBSC_ACP_ST_DT": "2026.06.29",
                  "SBSC_ACP_CLSG_DT": "2026.06.29",
                  "LGDN_ADR": "Incheon",
                  "LGDN_DTL_ADR": "Ganghwa",
                  "SIL_OFC_TLNO": "032-123-4567"
                }
                """);

        RentalDetail detail = invokeDetail(node);

        assertThat(detail).isEqualTo(new RentalDetail(
                "Incheon",
                "Ganghwa",
                "2026.06.29",
                "2026.06.29",
                "032-123-4567"
        ));
    }

    private RentalSupply invokeSupply(JsonNode node) throws Exception {
        Method method = LhOpenApiClient.class.getDeclaredMethod("supply", JsonNode.class);
        method.setAccessible(true);
        return (RentalSupply) method.invoke(client, node);
    }

    private RentalDetail invokeDetail(JsonNode node) throws Exception {
        Method method = LhOpenApiClient.class.getDeclaredMethod("detail", JsonNode.class);
        method.setAccessible(true);
        return (RentalDetail) method.invoke(client, node);
    }
}
