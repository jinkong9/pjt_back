# Open API Housing Analysis Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a MyBatis Spring Boot app that analyzes LH rental notices with nearby commercial data and ITS traffic risk.

**Architecture:** Controllers render Thymeleaf pages. Services call isolated Open API clients, calculate scores, and cache snapshots through MyBatis XML mappers. Missing keys or upstream failures return fallback data for demo stability.

**Tech Stack:** Spring Boot 4.0.6, Java 17, MyBatis 4.0.1, H2 default DB, Thymeleaf, RestClient, Jackson.

---

### Task 1: Project Configuration

**Files:**
- Modify: `build.gradle`
- Modify: `src/main/resources/application.properties`
- Create: `src/main/resources/schema.sql`

- [ ] Add MyBatis, JDBC, H2, and MySQL dependencies.
- [ ] Configure H2 as the default local datasource.
- [ ] Add Open API property placeholders for public data, ITS, and Kakao keys.

### Task 2: Tests First

**Files:**
- Create: `src/test/java/com/happyhome/analysis/AnalysisScoreServiceTest.java`
- Create: `src/test/java/com/happyhome/openapi/OpenApiJsonParserTest.java`

- [ ] Add score tests for strong commercial access and traffic risk penalty.
- [ ] Add JSON parser tests for LH `dsList`, public-data `items.item`, and ITS `body.items`.
- [ ] Run `./gradlew.bat test` and confirm tests fail because production classes are missing.

### Task 3: Domain, Parser, and Scoring

**Files:**
- Create: `src/main/java/com/happyhome/analysis/*`
- Create: `src/main/java/com/happyhome/openapi/OpenApiJsonParser.java`
- Create: `src/main/java/com/happyhome/rental/*`
- Create: `src/main/java/com/happyhome/commercial/*`
- Create: `src/main/java/com/happyhome/traffic/*`

- [ ] Implement records for notices, places, events, summaries, and analysis results.
- [ ] Implement tolerant JSON item extraction.
- [ ] Implement score calculation.
- [ ] Run targeted tests and confirm they pass.

### Task 4: API Clients and MyBatis Cache

**Files:**
- Create: `src/main/java/com/happyhome/openapi/*Client.java`
- Create: `src/main/java/com/happyhome/*/*Mapper.java`
- Create: `src/main/resources/mappers/*.xml`

- [ ] Implement LH, commercial-area, and ITS clients.
- [ ] Implement fallback sample providers.
- [ ] Implement MyBatis cache mappers.

### Task 5: Web UI

**Files:**
- Create: `src/main/java/com/happyhome/web/*.java`
- Create: `src/main/resources/templates/*.html`
- Create: `src/main/resources/static/css/app.css`

- [ ] Add `/`, `/rentals`, `/rentals/{noticeId}`, and `/analysis`.
- [ ] Render notice cards, detail tables, commercial summary, and traffic risk.
- [ ] Add JSON endpoints for rental notices and location analysis.

### Task 6: Verification

- [ ] Run `./gradlew.bat test`.
- [ ] Run `./gradlew.bat bootRun`.
- [ ] Open the local app and verify the main screens render.
