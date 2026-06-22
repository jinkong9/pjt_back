# Schema View and Index Performance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Align the runtime schema with the current MyBatis model and reduce real-estate deal and LH notice-detail latency with two focused views, query-matched indexes, and cache-first LH detail reads.

**Architecture:** A mergeable MySQL view exposes the existing three-table house-deal projection while base-table indexes provide the speedup. A second view joins LH notice headers to cached details; the service reads cached detail and supplies first, fetching and persisting only missing API data. The existing LH list API remains unchanged.

**Tech Stack:** Java 17, Spring Boot 4, MyBatis, MySQL 8, JUnit 5, Mockito, AssertJ

---

### Task 1: Lock the minimal schema contract with a failing test

**Files:**
- Create: `src/test/java/com/happyhome/schema/SchemaDefinitionTest.java`
- Read: `src/main/resources/schema.sql`

- [ ] **Step 1: Write the failing schema contract test**

```java
package com.happyhome.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SchemaDefinitionTest {

    private final String schema = readSchema();

    @Test
    void definesOnlyTheRequiredPerformanceViews() {
        assertThat(schema).contains("CREATE OR REPLACE VIEW v_house_deals AS");
        assertThat(schema).contains("CREATE OR REPLACE VIEW v_rental_notice_details AS");
        assertThat(schema).doesNotContain("MATERIALIZED VIEW");
    }

    @Test
    void definesIndexesUsedByDealAndRentalQueries() {
        assertThat(schema).contains("idx_housedeals_date_no");
        assertThat(schema).contains("idx_housedeals_apt_seq");
        assertThat(schema).contains("idx_houseinfos_region_seq");
        assertThat(schema).contains("idx_dongcodes_region_code");
        assertThat(schema).contains("idx_lh_notice_supplies_notice_supply");
    }

    private String readSchema() {
        try {
            return Files.readString(Path.of("src/main/resources/schema.sql"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
```

- [ ] **Step 2: Run the test and verify RED**

Run:

```powershell
./mvnw.cmd -Dtest=SchemaDefinitionTest test
```

Expected: FAIL because both view definitions and `idx_lh_notice_supplies_notice_supply` are absent.

- [ ] **Step 3: Commit the failing test**

```powershell
git add src/test/java/com/happyhome/schema/SchemaDefinitionTest.java
git commit -m "test: define schema performance contract"
```

### Task 2: Align `schema.sql` and add focused views and indexes

**Files:**
- Modify: `src/main/resources/schema.sql`
- Test: `src/test/java/com/happyhome/schema/SchemaDefinitionTest.java`

- [ ] **Step 1: Keep the current mapper-backed tables and remove duplicate or speculative definitions**

Keep these tables in dependency order: `members`, `member_financial_profiles`, `dongcodes`, `houseinfos`, `housedeals`, `favorite_deals`, `notices`, `transfers`, `transfer_images`, `rental_notice_cache`, `lh_notice_details`, `lh_notice_supplies`, `openapi_sync_logs`, `loan_products`, `loan_rate_options`, `analysis_snapshot`, `bus_city_codes`, `bus_stops`.

Do not add `commercial_places`, `property_deals`, `rental_notices`, `transfer_posts`, or `transfer_comments`, because current mappers do not use them.

- [ ] **Step 2: Define the query-matched indexes inside table DDL**

Use these definitions and avoid additional indexes:

```sql
INDEX idx_dongcodes_region_code (sido_name, gugun_name, dong_name, dong_code)
INDEX idx_houseinfos_region_seq (sgg_cd, umd_cd, apt_seq)
INDEX idx_housedeals_apt_seq (apt_seq)
INDEX idx_housedeals_date_no (deal_year DESC, deal_month DESC, deal_day DESC, no DESC)
INDEX idx_lh_notice_supplies_notice_supply (notice_id, supply_id)
```

- [ ] **Step 3: Add the house-deal view after its base tables**

```sql
CREATE OR REPLACE VIEW v_house_deals AS
SELECT
    d.no,
    d.apt_seq,
    h.apt_nm,
    dc.sido_name,
    dc.gugun_name,
    dc.dong_name,
    h.sgg_cd,
    h.umd_cd,
    h.umd_nm,
    h.jibun,
    h.road_nm,
    h.build_year,
    h.latitude,
    h.longitude,
    d.apt_dong,
    d.floor,
    d.deal_year,
    d.deal_month,
    d.deal_day,
    d.exclu_use_ar,
    d.deal_amount
FROM housedeals d
JOIN houseinfos h ON h.apt_seq = d.apt_seq
LEFT JOIN dongcodes dc ON dc.dong_code = CONCAT(h.sgg_cd, h.umd_cd);
```

- [ ] **Step 4: Add the rental-detail view after its base tables**

```sql
CREATE OR REPLACE VIEW v_rental_notice_details AS
SELECT
    n.notice_id,
    n.title,
    n.region_name,
    n.notice_type,
    n.detail_type,
    n.status,
    n.notice_date,
    n.close_date,
    n.detail_url,
    n.ccr_cnnt_sys_ds_cd,
    n.upp_ais_tp_cd,
    n.ais_tp_cd,
    n.spl_inf_tp_cd,
    n.source,
    n.cached_at AS notice_cached_at,
    d.contract_address,
    d.contract_detail_address,
    d.apply_start_date,
    d.apply_end_date,
    d.contact,
    d.cached_at AS detail_cached_at
FROM rental_notice_cache n
LEFT JOIN lh_notice_details d ON d.notice_id = n.notice_id;
```

- [ ] **Step 5: Run schema contract test and verify GREEN**

Run:

```powershell
./mvnw.cmd -Dtest=SchemaDefinitionTest test
```

Expected: PASS.

- [ ] **Step 6: Commit schema changes**

```powershell
git add src/main/resources/schema.sql src/test/java/com/happyhome/schema/SchemaDefinitionTest.java
git commit -m "perf: add focused schema views and indexes"
```

### Task 3: Route house-deal reads through the view

**Files:**
- Modify: `src/main/resources/mappers/HouseDealMapper.xml`
- Create: `src/test/java/com/happyhome/house/dao/HouseDealMapperSqlTest.java`

- [ ] **Step 1: Write a failing mapper SQL test**

```java
package com.happyhome.house.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class HouseDealMapperSqlTest {

    @Test
    void readsHouseDealsThroughPerformanceView() throws Exception {
        String mapper = Files.readString(
                Path.of("src/main/resources/mappers/HouseDealMapper.xml")
        );

        assertThat(mapper).contains("FROM v_house_deals");
        assertThat(mapper).doesNotContain("STRAIGHT_JOIN");
    }
}
```

- [ ] **Step 2: Run the test and verify RED**

Run:

```powershell
./mvnw.cmd -Dtest=HouseDealMapperSqlTest test
```

Expected: FAIL because the mapper still contains direct joins and `STRAIGHT_JOIN`.

- [ ] **Step 3: Replace repeated join SQL with view columns**

Define a single projection:

```xml
<sql id="houseDealColumns">
    no, apt_seq, apt_nm, sido_name, gugun_name, dong_name,
    umd_nm, jibun, road_nm, build_year, latitude, longitude,
    apt_dong, floor, deal_year, deal_month, deal_day, exclu_use_ar, deal_amount
</sql>
```

Use `FROM v_house_deals` for `search`, `findByNo`, and `findByNos`. Preserve all current filters and JSON fields, replacing aliases such as `d.deal_year` with view columns such as `deal_year`.

For `findRecent`, use:

```xml
SELECT <include refid="houseDealColumns"/>
FROM v_house_deals
ORDER BY deal_year DESC, deal_month DESC, deal_day DESC, no DESC
LIMIT #{limit}
```

- [ ] **Step 4: Run mapper and schema tests**

Run:

```powershell
./mvnw.cmd -Dtest=HouseDealMapperSqlTest,SchemaDefinitionTest test
```

Expected: PASS.

- [ ] **Step 5: Commit mapper changes**

```powershell
git add src/main/resources/mappers/HouseDealMapper.xml src/test/java/com/happyhome/house/dao/HouseDealMapperSqlTest.java
git commit -m "perf: query house deals through view"
```

### Task 4: Implement LH detail cache-first behavior

**Files:**
- Modify: `src/main/resources/mappers/RentalNoticeMapper.xml`
- Modify: `src/main/java/com/happyhome/rental/service/RentalService.java`
- Modify: `src/test/java/com/happyhome/rental/service/RentalServiceTest.java`
- Use existing: `src/main/java/com/happyhome/batch/mapper/NoticeLHBatchMapper.java`

- [ ] **Step 1: Add a failing cache-hit service test**

Construct `RentalService(lhClient, mapper, noticeLHBatchMapper)`, return cached `RentalDetail` and `RentalSupply` from the mapper, call `detail("LH-001")`, and assert:

```java
assertThat(result.detail()).isEqualTo(cachedDetail);
assertThat(result.supplies()).containsExactly(cachedSupply);
verifyNoInteractions(lhClient, noticeLHBatchMapper);
```

- [ ] **Step 2: Add a failing cache-miss service test**

Return an existing notice, `Optional.empty()` detail, and an empty supply list. Stub the LH client and assert:

```java
verify(lhClient).detail(notice);
verify(lhClient).supplies(notice);
verify(noticeLHBatchMapper).upsertDetail(NoticeLHDetail.from("LH-001", fetchedDetail));
verify(noticeLHBatchMapper).deleteSuppliesByNoticeId("LH-001");
verify(noticeLHBatchMapper).insertSupply(NoticeLHSupply.from("LH-001", fetchedSupply));
```

- [ ] **Step 3: Run service tests and verify RED**

Run:

```powershell
./mvnw.cmd -Dtest=RentalServiceTest test
```

Expected: FAIL because the current constructor has no batch mapper and always calls the LH client.

- [ ] **Step 4: Read cached detail through the view**

Change `findDetailByNoticeId` to:

```xml
SELECT
    contract_address,
    contract_detail_address,
    apply_start_date,
    apply_end_date,
    contact
FROM v_rental_notice_details
WHERE notice_id = #{noticeId}
  AND detail_cached_at IS NOT NULL
```

Keep supply retrieval separate and ordered by `(notice_id, supply_id)`.

- [ ] **Step 5: Implement minimal cache-first service flow**

Inject `NoticeLHBatchMapper`. Resolve the notice as today, then:

```java
RentalDetail detail = mapper.findDetailByNoticeId(noticeId)
        .orElseGet(() -> fetchAndCacheDetail(notice));

List<RentalSupply> supplies = mapper.findSuppliesByNoticeId(noticeId);
if (supplies.isEmpty()) {
    supplies = fetchAndCacheSupplies(notice);
}

return new RentalNoticeDetail(notice, detail, supplies);
```

`fetchAndCacheDetail` calls `lhClient.detail`, then `upsertDetail`. `fetchAndCacheSupplies` calls `lhClient.supplies`; only after a successful return does it delete old rows and insert each new row.

- [ ] **Step 6: Run cache tests and verify GREEN**

Run:

```powershell
./mvnw.cmd -Dtest=RentalServiceTest,RentalNoticeMapperTest test
```

Expected: PASS.

- [ ] **Step 7: Commit cache-first changes**

```powershell
git add src/main/java/com/happyhome/rental/service/RentalService.java src/main/resources/mappers/RentalNoticeMapper.xml src/test/java/com/happyhome/rental/service/RentalServiceTest.java
git commit -m "perf: serve LH details from cache first"
```

### Task 5: Apply the approved DDL to the current development DB

**Files:**
- Read: `src/main/resources/schema.sql`
- No migration file is created; the current DB is updated once after metadata checks.

- [ ] **Step 1: Query current metadata without changing state**

Use JDBC with credentials loaded from `.env` and query:

```sql
SELECT table_name, index_name
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND index_name IN (
      'idx_housedeals_date_no',
      'idx_housedeals_apt_seq',
      'idx_houseinfos_region_seq',
      'idx_dongcodes_region_code',
      'idx_lh_notice_supplies_notice_supply'
  );
```

- [ ] **Step 2: Add only missing indexes**

Execute the corresponding statement only when its index name is absent:

```sql
ALTER TABLE housedeals
    ADD INDEX idx_housedeals_date_no
    (deal_year DESC, deal_month DESC, deal_day DESC, no DESC);
ALTER TABLE housedeals
    ADD INDEX idx_housedeals_apt_seq (apt_seq);
ALTER TABLE houseinfos
    ADD INDEX idx_houseinfos_region_seq (sgg_cd, umd_cd, apt_seq);
ALTER TABLE dongcodes
    ADD INDEX idx_dongcodes_region_code
    (sido_name, gugun_name, dong_name, dong_code);
ALTER TABLE lh_notice_supplies
    ADD INDEX idx_lh_notice_supplies_notice_supply (notice_id, supply_id);
```

- [ ] **Step 3: Create or replace both views from `schema.sql`**

Execute the exact `CREATE OR REPLACE VIEW` statements defined in Task 2.

- [ ] **Step 4: Verify metadata**

Run `SHOW INDEX` for all five indexes and `SHOW CREATE VIEW` for both views. Expected: each named object exists exactly once.

### Task 6: Verify plans, response time, and regressions

**Files:**
- Modify only if verification reveals a defect in files already listed above.

- [ ] **Step 1: Verify the real-estate query plan**

Run `EXPLAIN ANALYZE` for the 500-row latest query through `v_house_deals`.

Expected: `idx_housedeals_date_no` index scan with an early LIMIT; no 6.99-million-row table scan followed by sort.

- [ ] **Step 2: Measure API latency three times per endpoint**

Run:

```powershell
curl.exe -s -o NUL -w "%{http_code} %{time_total}`n" "http://localhost:8080/api/houses?limit=500"
curl.exe -s -o NUL -w "%{http_code} %{time_total}`n" "http://localhost:8080/api/houses/recent?limit=20"
```

Expected: HTTP 200 and a clear reduction from the measured 12.5 seconds and 4.4 seconds.

- [ ] **Step 3: Verify LH cache behavior over HTTP**

Call the same LH detail endpoint twice. Expected: identical HTTP 200 payloads; the second call reads populated `lh_notice_details` and `lh_notice_supplies` rows without another external detail/supply call.

- [ ] **Step 4: Run targeted and full tests**

Run:

```powershell
./mvnw.cmd -Dtest=SchemaDefinitionTest,HouseDealMapperSqlTest,RentalServiceTest,RentalNoticeMapperTest test
./mvnw.cmd test
```

Expected: targeted tests PASS. If the full build is still blocked by pre-existing unrelated malformed AI/analysis sources, report those exact compiler errors separately and do not attribute them to this change.

- [ ] **Step 5: Inspect final diff and commit any verification-only correction**

```powershell
git diff --check
git status --short
```

Expected: no whitespace errors and no temporary probe files.
