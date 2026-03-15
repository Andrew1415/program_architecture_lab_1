import http from 'k6/http';
import { Counter } from 'k6/metrics';
import { check, fail } from 'k6';

import {
    createBook,
    deleteBook,
    getBookIdByTitle,
    getBooksPage,
    listBooks,
} from './lib/books.js';

const purchaseSuccesses = new Counter('purchase_successes');
const purchaseFailures = new Counter('purchase_failures');

export const options = {
    scenarios: {
        contention_purchase: {
            executor: 'per-vu-iterations',
            vus: Number(__ENV.PURCHASE_VUS || 100),
            iterations: 1,
            maxDuration: '1m',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.05'],
        purchase_successes: ['count==1'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:25256';
const TEST_TITLE = `Purchase Contention Book ${Date.now()}`;

function headers() {
    return {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
    };
}

export function setup() {
    createBook(BASE_URL, {
        title: TEST_TITLE,
        author: 'k6 Contention',
        year: 2026,
        stockQuantity: 1,
    }, headers());

    const id = getBookIdByTitle(BASE_URL, TEST_TITLE);
    if (id === null) {
        fail(`Unable to locate seeded contention book: ${TEST_TITLE}`);
    }

    return { id };
}

export default function (data) {
    const response = http.post(`${BASE_URL}/api/books/${data.id}/purchase`, null);

    check(response, {
        'api purchase returns 200': (r) => r.status === 200,
    });

    const payload = response.status === 200 ? JSON.parse(response.body) : { purchased: false };
    if (payload.purchased) {
        purchaseSuccesses.add(1);
    } else {
        purchaseFailures.add(1);
    }
}

export function teardown(data) {
    const ids = listBooks(BASE_URL)
        .filter((book) => book.id === data.id || book.title === TEST_TITLE)
        .map((book) => book.id);

    for (const id of ids) {
        deleteBook(BASE_URL, id, headers());
    }
}
