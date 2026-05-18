create table if not exists books
(
    id
    integer
    primary
    key
    autoincrement,
    isbn
    text
    not
    null
    unique,
    title
    text
    not
    null,
    author
    text
    not
    null,
    published_year
    integer
    not
    null
);

create table if not exists stories
(
    id
    integer
    primary
    key
    autoincrement,
    book_id
    integer
    not
    null,
    title
    text
    not
    null,
    synopsis
    text
    not
    null,
    foreign
    key
(
    book_id
) references books
(
    id
) on delete cascade
    );
