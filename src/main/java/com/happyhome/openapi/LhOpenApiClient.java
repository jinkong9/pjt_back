package com.happyhome.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.happyhome.config.OpenApiProperties;
import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalSearchCondition;
import com.happyhome.rental.dto.RentalSupply;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LhOpenApiClient {

    private static final Logger log = LoggerFactory.getLogger(LhOpenApiClient.class);

    private final OpenApiProperties properties;
    private final OpenApiJsonParser parser;
    private final RestClient restClient;

    public LhOpenApiClient(OpenApiProperties properties) {
        this.properties = properties;
        this.parser = new OpenApiJsonParser();
        this.restClient = RestClient.create();
    }

    public boolean isConfigured() {
        return OpenApiUri.hasText(properties.getData().getServiceKey());
    }

    public List<RentalNotice> notices(RentalSearchCondition condition) {
        if (!isConfigured()) {
            return SampleData.rentalNotices();
        }
        try {
            List<RentalNotice> notices = apiNotices(condition);
            return notices.isEmpty() ? SampleData.rentalNotices() : notices;
        } catch (Exception e) {
            return SampleData.rentalNotices();
        }
    }

    public List<RentalNotice> apiNotices(RentalSearchCondition condition) {
        return apiNotices(condition, "2020.01.01", "2099.12.31");
    }

    public List<RentalNotice> apiNotices(
            RentalSearchCondition condition,
            String noticeStartDate,
            String closeEndDate
    ) {
        if (!isConfigured()) {
            return List.of();
        }
        try {
            String body = restClient.get()
                    .uri(OpenApiUri.build(properties.getLh().getNoticeUrl(), Map.of(
                            "ServiceKey", properties.getData().getServiceKey(),
                            "PG_SZ", condition.size(),
                            "PAGE", condition.page(),
                            "PAN_NM", blankToEmpty(condition.keyword()),
                            "CNP_CD", blankToEmpty(condition.regionCode()),
                            "PAN_SS", blankToEmpty(condition.status()),
                            "PAN_NT_ST_DT", noticeStartDate,
                            "CLSG_DT", closeEndDate
                    )))
                    .retrieve()
                    .body(String.class);
            return parser.items(body).stream().map(this::notice).toList();
        } catch (Exception e) {
            log.warn("LH notice API failed on page {}: {}", condition.page(), e.getMessage());
            return List.of();
        }
    }

    public List<RentalSupply> supplies(RentalNotice notice) {
        if (!isConfigured()) {
            log.warn("OPENAPI_DATA_SERVICE_KEY is not configured; LH supply API was not called.");
            return List.of();
        }
        if ("sample".equals(notice.source())) {
            return List.of();
        }
        try {
            String body = restClient.get()
                    .uri(OpenApiUri.build(properties.getLh().getSupplyUrl(), detailParams(notice)))
                    .retrieve()
                    .body(String.class);
            List<RentalSupply> supplies = parser.items(body).stream().map(this::supply).toList();
            if (supplies.isEmpty()) {
                log.warn("LH supply API returned no items for notice {}", notice.noticeId());
            }
            return supplies;
        } catch (Exception e) {
            log.warn("LH supply API failed for notice {}: {}", notice.noticeId(), e.getMessage());
            return List.of();
        }
    }

    public RentalDetail detail(RentalNotice notice) {
        if (!isConfigured()) {
            log.warn("OPENAPI_DATA_SERVICE_KEY is not configured; LH detail API was not called.");
            return emptyDetail();
        }
        if ("sample".equals(notice.source())) {
            return emptyDetail();
        }
        try {
            String body = restClient.get()
                    .uri(OpenApiUri.build(properties.getLh().getDetailUrl(), detailParams(notice)))
                    .retrieve()
                    .body(String.class);
            return parser.items(body).stream().findFirst().map(this::detail).orElseGet(() -> {
                log.warn("LH detail API returned no items for notice {}", notice.noticeId());
                return emptyDetail();
            });
        } catch (Exception e) {
            log.warn("LH detail API failed for notice {}: {}", notice.noticeId(), e.getMessage());
            return emptyDetail();
        }
    }

    private RentalDetail emptyDetail() {
        return new RentalDetail("", "", "", "", "");
    }

    private Map<String, ?> detailParams(RentalNotice notice) {
        return Map.of(
                "ServiceKey", properties.getData().getServiceKey(),
                "serviceKey", properties.getData().getServiceKey(),
                "SPL_INF_TP_CD", firstText(notice.splInfTpCd(), "010"),
                "CCR_CNNT_SYS_DS_CD", firstText(notice.ccrCnntSysDsCd(), "01"),
                "PAN_ID", notice.noticeId(),
                "UPP_AIS_TP_CD", firstText(notice.uppAisTpCd(), "01"),
                "AIS_TP_CD", blankToEmpty(notice.aisTpCd())
        );
    }

    private RentalNotice notice(JsonNode node) {
        return new RentalNotice(
                text(node, "PAN_ID", "id"),
                text(node, "PAN_NM", "공공임대 모집공고"),
                text(node, "CNP_CD_NM", "전국"),
                text(node, "UPP_AIS_TP_NM", "임대주택"),
                text(node, "AIS_TP_CD_NM", "공공임대"),
                text(node, "PAN_SS", "공고중"),
                text(node, "PAN_NT_ST_DT", ""),
                text(node, "CLSG_DT", ""),
                text(node, "DTL_URL", ""),
                text(node, "CCR_CNNT_SYS_DS_CD", "01"),
                text(node, "UPP_AIS_TP_CD", "01"),
                text(node, "AIS_TP_CD", ""),
                text(node, "SPL_INF_TP_CD", "010"),
                "api"
        );
    }

    private RentalSupply supply(JsonNode node) {
        String usage = textAny(node, "공급유형",
                "LND_US_DS_CD_NM",
                "SPL_INF_TP_NM",
                "HTY_NNA",
                "HTYPE",
                "HOUSE_TY_NM",
                "SBD_LGO_NM"
        );
        String address = textAny(node, "",
                "LGDN_DTL_ADR",
                "LGDN_ADR",
                "ADDR",
                "SBD_LGO_NM",
                "SBD_NM",
                "DNG_NM"
        );
        String lotNumber = textAny(node, "", "LNO", "LOT_NO", "DNG_HO", "HO_NM", "SIL_HO_NM");
        String amountRaw = expectedAmountRaw(node);
        String mapAddress = joinNonBlank(" ", address, lotNumber);
        return new RentalSupply(
                usage,
                address,
                lotNumber,
                textAny(node, "", "AR", "DDO_AR", "SUPLY_AR", "CNTR_AR", "EXUS_AR"),
                expectedAmount(node, amountRaw),
                amountRaw,
                textAny(node, "", "HTYPE", "HOUSE_TY_NM", "HTY_NNA"),
                textAny(node, "", "HSH_CNT", "SPL_HSH_CNT", "NOW_HSH_CNT"),
                textAny(node, defaultApplyStatus(node), "SBSC_ACP_STTS_NM", "ACP_STTS_NM"),
                mapAddress,
                naverMapUrl(mapAddress)
        );
    }

    private RentalDetail detail(JsonNode node) {
        return new RentalDetail(
                text(node, "CTRT_PLC_ADR", text(node, "LGDN_ADR", "")),
                text(node, "CTRT_PLC_DTL_ADR", text(node, "LGDN_DTL_ADR", "")),
                text(node, "SBSC_ACP_ST_DT", ""),
                text(node, "SBSC_ACP_CLSG_DT", ""),
                text(node, "SIL_OFC_TLNO", "1600-1004")
        );
    }

    private String text(JsonNode node, String field, String fallback) {
        String value = node.path(field).asText("");
        return value.isBlank() ? fallback : value;
    }

    private String textAny(JsonNode node, String fallback, String... fields) {
        for (String field : fields) {
            String value = node.path(field).asText("");
            if (!value.isBlank()) {
                return value;
            }
        }
        return fallback;
    }

    private String firstText(String value, String fallback) {
        return OpenApiUri.hasText(value) ? value : fallback;
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String expectedAmountRaw(JsonNode node) {
        String landAmount = textAny(node, "", "SPL_XPC_AMT", "SPLPC", "SUPLY_XPC_AMT");
        if (OpenApiUri.hasText(landAmount)) {
            return landAmount;
        }
        String deposit = textAny(node, "", "LS_GMY", "IMT_GMY", "RENT_GTN", "DEPOSIT");
        String rent = textAny(node, "", "RFE", "MM_RNT", "MONTH_RENT");
        if (OpenApiUri.hasText(deposit) && OpenApiUri.hasText(rent)) {
            return "보증금 " + deposit + " / 월 " + rent;
        }
        if (OpenApiUri.hasText(deposit)) {
            return "보증금 " + deposit;
        }
        return rent;
    }

    private String expectedAmount(JsonNode node, String amountRaw) {
        if (OpenApiUri.hasText(textAny(node, "", "SPL_XPC_AMT", "SPLPC", "SUPLY_XPC_AMT"))) {
            return formatWon(amountRaw);
        }
        return amountRaw;
    }

    private String defaultApplyStatus(JsonNode node) {
        boolean landBidSupply = OpenApiUri.hasText(textAny(node, "", "SPL_XPC_AMT", "SPLPC", "SUPLY_XPC_AMT"))
                || OpenApiUri.hasText(textAny(node, "", "LNO", "LOT_NO"))
                || OpenApiUri.hasText(text(node, "LND_US_DS_CD_NM", ""));
        return landBidSupply ? "입찰신청전" : "";
    }

    private String formatWon(String value) {
        if (!OpenApiUri.hasText(value)) {
            return "";
        }
        try {
            return NumberFormat.getNumberInstance(Locale.KOREA).format(Long.parseLong(value.replace(",", "").trim()));
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private String naverMapUrl(String address) {
        if (!OpenApiUri.hasText(address)) {
            return "";
        }
        return "https://map.naver.com/v5/search/" + URLEncoder.encode(address, StandardCharsets.UTF_8);
    }

    private String joinNonBlank(String delimiter, String... values) {
        return java.util.Arrays.stream(values)
                .filter(OpenApiUri::hasText)
                .map(String::trim)
                .reduce((left, right) -> left + delimiter + right)
                .orElse("");
    }
}
