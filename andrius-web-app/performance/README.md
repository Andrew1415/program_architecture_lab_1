# k6 Performance Tests

This folder contains k6 performance tests for the book catalog UI flow.

## Covered flows

- `GET /books`
- `POST /books`
- `POST /books/{id}`
- `POST /books/{id}/delete`
- `POST /books/{id}/purchase`
- `POST /api/books/{id}/purchase`

## Prerequisites

- Start the Spring Boot app locally
- Install `k6`

## Run

Use the default local app URL:

```powershell
k6 run performance/books-crud.js
k6 run performance/purchase-contention.js
```

Target a different environment:

```powershell
$env:BASE_URL="http://localhost:25256"
k6 run performance/books-crud.js
$env:PURCHASE_VUS="100"
k6 run performance/purchase-contention.js
```

Tune the default load or thresholds:

```powershell
$env:BROWSE_TARGET_VUS="10"
$env:MUTATE_VUS="8"
$env:MUTATE_ITERATIONS="2"
$env:HTTP_P95_THRESHOLD_MS="3000"
$env:CRUD_P95_THRESHOLD_MS="12000"
k6 run performance/books-crud.js
```

## Lighthouse

Use Lighthouse CI against the server-rendered page:

```powershell
npx @lhci/cli autorun --config=performance/lighthouserc.json
```

## What the script does

- ramps read-only traffic against `/books`
- runs concurrent create/update/delete cycles
- attempts cleanup in each iteration and again in `teardown()`
- removes leftover rows whose title starts with `Performance Test Book`
- fails when requests error too often or latency thresholds are exceeded
- validates the 100-user/1-item contention case with `purchase-contention.js`
