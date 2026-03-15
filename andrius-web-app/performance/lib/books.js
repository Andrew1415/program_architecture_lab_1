import http from 'k6/http';
import { check } from 'k6';

function redirectsToBooks(response) {
    const location = response.headers.Location || '';

    return location === '/books' || location.endsWith('/books');
}

export function getBooksPage(baseUrl, params = {}) {
    const response = http.get(`${baseUrl}/books`, params);

    check(response, {
        'GET /books returns 200': (r) => r.status === 200,
        'GET /books contains page title': (r) => r.body.includes('Book Catalog'),
    });

    return response;
}

export function listBooks(baseUrl, params = {}) {
    const response = getBooksPage(baseUrl, params);
    const books = [];
    const rowPattern = /<tr>\s*<td[^>]*>\s*(\d+)\s*<\/td>\s*<td[^>]*>\s*([^<]+?)\s*<\/td>\s*<td[^>]*>\s*([^<]+?)\s*<\/td>\s*<td[^>]*>\s*(\d+)\s*<\/td>\s*<td[^>]*>\s*(\d+)\s*<\/td>[\s\S]*?<\/tr>/g;
    let match;

    while ((match = rowPattern.exec(response.body)) !== null) {
        books.push({
            id: Number(match[1]),
            title: match[2].trim(),
            author: match[3].trim(),
            year: Number(match[4]),
            stockQuantity: Number(match[5]),
        });
    }

    return books;
}

export function createBook(baseUrl, book, params = {}) {
    const response = http.post(`${baseUrl}/books`, book, {
        redirects: 0,
        ...params,
    });

    check(response, {
        'POST /books redirects': (r) => r.status === 302,
        'POST /books redirects to /books': (r) => redirectsToBooks(r),
    });

    return response;
}

export function getBookIdByTitle(baseUrl, title, params = {}) {
    const books = listBooks(baseUrl, params);

    for (const book of books) {
        if (book.title === title) {
            return book.id;
        }
    }

    return null;
}

export function getBookIdsByTitlePrefix(baseUrl, prefix, params = {}) {
    return listBooks(baseUrl, params)
        .filter((book) => book.title.startsWith(prefix))
        .map((book) => book.id);
}

export function purchaseBook(baseUrl, id, params = {}) {
    const response = http.post(`${baseUrl}/books/${id}/purchase`, null, {
        redirects: 0,
        ...params,
    });

    check(response, {
        'POST /books/{id}/purchase redirects': (r) => r.status === 302,
        'POST /books/{id}/purchase redirects to /books': (r) => redirectsToBooks(r),
    });

    return response;
}

export function editBook(baseUrl, id, book, params = {}) {
    const response = http.post(`${baseUrl}/books/${id}`, book, {
        redirects: 0,
        ...params,
    });

    check(response, {
        'POST /books/{id} redirects': (r) => r.status === 302,
        'POST /books/{id} redirects to /books': (r) => redirectsToBooks(r),
    });

    return response;
}

export function deleteBook(baseUrl, id, params = {}) {
    const response = http.post(`${baseUrl}/books/${id}/delete`, null, {
        redirects: 0,
        ...params,
    });

    check(response, {
        'POST /books/{id}/delete redirects': (r) => r.status === 302,
        'POST /books/{id}/delete redirects to /books': (r) => redirectsToBooks(r),
    });

    return response;
}
