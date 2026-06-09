# Open API Housing Analysis Design

## Goal

Build a MyBatis-based Spring Boot application that combines LH rental notices, nearby small-business data, and ITS traffic risk data into youth-housing recommendation screens.

## Scope

The app exposes rental notice list/detail pages and a location analysis page. Public-data keys are optional at runtime: if keys are missing or an upstream call fails, services return sample data so the UI and project demo still work.

## Architecture

Controllers render Thymeleaf pages and JSON endpoints. Service classes coordinate API clients, score calculation, and MyBatis cache mappers. API clients are isolated per provider so LH, commercial-area, and ITS integrations can evolve independently.

## Data Flow

1. User opens `/rentals`.
2. `RentalService` requests LH notices or falls back to sample notices.
3. Notices are cached through MyBatis into H2 by default.
4. User opens `/analysis` with latitude, longitude, and radius.
5. `AnalysisService` fetches commercial places and traffic events, calculates a livability score, caches the snapshot, and renders category counts plus risk summary.

## Error Handling

Missing keys, HTTP errors, parsing errors, and empty provider responses all fall back to sample data. The page shows whether a result came from live API data or fallback data.

## Testing

Unit tests cover score calculation and tolerant JSON parsing. A Spring context test verifies the app starts with default local configuration.
