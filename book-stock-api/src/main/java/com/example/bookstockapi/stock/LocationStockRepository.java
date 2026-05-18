package com.example.bookstockapi.stock;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LocationStockRepository {

    private static final RowMapper<LocationStock> ROW_MAPPER = (rs, rowNum) -> new LocationStock(
            rs.getLong("id"),
            rs.getString("book_isbn"),
            rs.getString("location_name"),
            rs.getInt("quantity")
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public LocationStockRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<LocationStock> findAll() {
        return jdbcTemplate.getJdbcTemplate().query(
                "select id, book_isbn, location_name, quantity from location_stock order by book_isbn asc, location_name asc",
                ROW_MAPPER
        );
    }

    public Optional<LocationStock> findById(long id) {
        List<LocationStock> items = jdbcTemplate.query(
                "select id, book_isbn, location_name, quantity from location_stock where id = :id",
                new MapSqlParameterSource("id", id),
                ROW_MAPPER
        );
        return items.stream().findFirst();
    }

    public List<LocationStock> findByBookIsbn(String bookIsbn) {
        return jdbcTemplate.query(
                "select id, book_isbn, location_name, quantity from location_stock where book_isbn = :bookIsbn order by location_name asc",
                new MapSqlParameterSource("bookIsbn", bookIsbn),
                ROW_MAPPER
        );
    }

    public boolean existsById(long id) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from location_stock where id = :id",
                new MapSqlParameterSource("id", id),
                Integer.class
        );
        return count != null && count > 0;
    }

    public LocationStock create(LocationStockRequest request) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("bookIsbn", request.bookIsbn().trim())
                .addValue("locationName", request.locationName().trim())
                .addValue("quantity", request.quantity());
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update("""
                        insert into location_stock (book_isbn, location_name, quantity)
                        values (:bookIsbn, :locationName, :quantity)
                        """,
                params,
                keyHolder,
                new String[]{"id"}
        );

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to create location stock.");
        }
        return findById(key.longValue()).orElseThrow(() -> new IllegalStateException("Failed to load location stock."));
    }

    public LocationStock update(long id, LocationStockRequest request) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("bookIsbn", request.bookIsbn().trim())
                .addValue("locationName", request.locationName().trim())
                .addValue("quantity", request.quantity());

        jdbcTemplate.update("""
                        update location_stock
                        set book_isbn = :bookIsbn,
                            location_name = :locationName,
                            quantity = :quantity
                        where id = :id
                        """,
                params
        );

        return findById(id).orElseThrow(() -> new IllegalStateException("Failed to load updated location stock."));
    }

    public void delete(long id) {
        jdbcTemplate.update(
                "delete from location_stock where id = :id",
                new MapSqlParameterSource("id", id)
        );
    }
}
