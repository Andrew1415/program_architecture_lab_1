# Non-Functional Evaluation Report

## Application

- Project: `andrius-web-app`
- Architecture: Spring Boot MVC monolith with Thymeleaf and PostgreSQL
- Core user flows measured: browse catalog, create/edit/delete book, purchase book

## Tools

- `k6` for backend load and concurrency testing
- `Lighthouse` for browser-facing performance measurements on the rendered `/books` page

## Baseline

Capture before optimizations:

- average and p95 response times
- throughput
- error rate
- CPU and memory usage of the app process
- Lighthouse performance score

## Load Scenarios

### Scenario 1: Browse and CRUD

- Script: `performance/books-crud.js`
- Variables used:
- Results:

### Scenario 2: 100 users purchase 1 remaining item

- Script: `performance/purchase-contention.js`
- Shared database:
- Results:

## Horizontal Scaling

- Single instance target: `http://localhost:25256`
- Two-instance target through Nginx: `http://localhost:8088`
- Load balancer config: `infra/nginx/nginx.conf`

Document:

- single-instance metrics
- two-instance metrics
- observed throughput change
- observed latency change
- correctness under concurrent purchase load

## Performance Improvements Applied

1. Added application-side caching for the catalog list using Spring Cache + Caffeine
2. Added database indexes on `books.title` and `books.author`
3. Added atomic stock decrement query to enforce integrity under concurrency

## Testing

- Unit tests:
- Integration tests:
- Concurrency integration test:

## Conclusions

- What improved:
- What remained the bottleneck:
- Whether horizontal scaling helped:
- Final recommendation:
