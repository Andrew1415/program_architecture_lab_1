create table if not exists books (
    id integer primary key autoincrement,
    isbn text not null unique,
    title text not null,
    author text not null,
    published_year integer not null
);

create table if not exists location_stock (
    id integer primary key autoincrement,
    book_isbn text not null,
    location_name text not null,
    quantity integer not null,
    foreign key (book_isbn) references books(isbn) on delete cascade
);
