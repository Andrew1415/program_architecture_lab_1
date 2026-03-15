import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';

import {
    createBook,
    deleteBook,
    editBook,
    getBookIdByTitle,
    getBookIdsByTitlePrefix,
    getBooksPage,
} from './lib/books.js';

const bookCrudDuration = new Trend('book_crud_duration', true);
const missingBookIds = new Counter('missing_book_ids');
const BROWSE_TARGET_VUS = Number(__ENV.BROWSE_TARGET_VUS || 10);
const MUTATE_VUS = Number(__ENV.MUTATE_VUS || 8);
const MUTATE_ITERATIONS = Number(__ENV.MUTATE_ITERATIONS || 2);
const HTTP_P95_THRESHOLD_MS = Number(__ENV.HTTP_P95_THRESHOLD_MS || 3000);
const CRUD_P95_THRESHOLD_MS = Number(__ENV.CRUD_P95_THRESHOLD_MS || 12000);

export const options = {
    scenarios: {
        browse_catalog: {
            executor: 'ramping-vus',
            exec: 'browseCatalog',
            stages: [
                { duration: '15s', target: Math.max(1, Math.floor(BROWSE_TARGET_VUS / 2)) },
                { duration: '30s', target: BROWSE_TARGET_VUS },
                { duration: '15s', target: 0 },
            ],
        },
        mutate_catalog: {
            executor: 'per-vu-iterations',
            exec: 'mutateCatalog',
            vus: MUTATE_VUS,
            iterations: MUTATE_ITERATIONS,
            startTime: '5s',
            maxDuration: '2m',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.05'],
        http_req_duration: [`p(95)<${HTTP_P95_THRESHOLD_MS}`],
        book_crud_duration: [`p(95)<${CRUD_P95_THRESHOLD_MS}`],
        missing_book_ids: ['count==0'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:25256';
const TEST_BOOK_PREFIX = 'Performance Test Book';

function createHeaders() {
    return {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
    };
}

function uniqueBook(prefix) {
    const suffix = `${__VU}-${__ITER}-${Date.now()}`;

    return {
        title: `${prefix} ${suffix}`,
        author: `k6 Runner ${__VU}`,
        year: 2024,
    };
}

export function browseCatalog() {
    getBooksPage(BASE_URL);
    sleep(1);
}

export function mutateCatalog() {
    const startedAt = Date.now();
    const createdBook = uniqueBook(TEST_BOOK_PREFIX);
    const updatedBook = {
        ...createdBook,
        title: `${createdBook.title} Updated`,
        year: 2025,
    };
    const params = createHeaders();
    let createdBookId = null;

    try {
        getBooksPage(BASE_URL);
        createBook(BASE_URL, createdBook, params);

        createdBookId = getBookIdByTitle(BASE_URL, createdBook.title);
        if (createdBookId === null) {
            sleep(1);
            createdBookId = getBookIdByTitle(BASE_URL, createdBook.title);
        }

        if (createdBookId === null) {
            missingBookIds.add(1);
            return;
        }

        editBook(BASE_URL, createdBookId, updatedBook, params);

        const updatedBookId = getBookIdByTitle(BASE_URL, updatedBook.title);
        check(updatedBookId, {
            'updated book can be found in list': (id) => id !== null,
        });

        deleteBook(BASE_URL, createdBookId, params);
        createdBookId = null;

        const deletedBookId = getBookIdByTitle(BASE_URL, updatedBook.title);
        check(deletedBookId, {
            'deleted book is removed from list': (id) => id === null,
        });

        bookCrudDuration.add(Date.now() - startedAt);
        sleep(1);
    } finally {
        if (createdBookId !== null) {
            deleteBook(BASE_URL, createdBookId, params);
        }
    }
}

export function teardown() {
    const params = createHeaders();
    const leftoverIds = getBookIdsByTitlePrefix(BASE_URL, TEST_BOOK_PREFIX);

    for (const id of leftoverIds) {
        deleteBook(BASE_URL, id, params);
    }
}
