package com.zorvyn.finance.repository;

import com.zorvyn.finance.db.Database;
import com.zorvyn.finance.dto.CategoryTotal;
import com.zorvyn.finance.dto.RecordFilter;
import com.zorvyn.finance.dto.TrendPoint;
import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.model.RecordType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RecordRepository {

    private final Database database;

    public RecordRepository(Database database) {
        this.database = database;
    }

    public Optional<FinancialRecord> findById(UUID id) {
        String sql = """
                SELECT id, amount, type, category, record_date, notes, created_by, created_at, updated_at
                FROM financial_records
                WHERE id = ? AND deleted = FALSE
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRecord(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch record", exception);
        }
    }

    public FinancialRecord create(FinancialRecord record) {
        String sql = """
                INSERT INTO financial_records (id, amount, type, category, record_date, notes, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id, amount, type, category, record_date, notes, created_by, created_at, updated_at
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, record.getId());
            statement.setBigDecimal(2, record.getAmount());
            statement.setString(3, record.getType().name());
            statement.setString(4, record.getCategory());
            statement.setObject(5, record.getDate());
            statement.setString(6, record.getNotes());
            statement.setObject(7, record.getCreatedBy());

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return mapRecord(resultSet);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to create record", exception);
        }
    }

    public Optional<FinancialRecord> update(FinancialRecord record) {
        String sql = """
                UPDATE financial_records
                SET amount = ?, type = ?, category = ?, record_date = ?, notes = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND deleted = FALSE
                RETURNING id, amount, type, category, record_date, notes, created_by, created_at, updated_at
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, record.getAmount());
            statement.setString(2, record.getType().name());
            statement.setString(3, record.getCategory());
            statement.setObject(4, record.getDate());
            statement.setString(5, record.getNotes());
            statement.setObject(6, record.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRecord(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to update record", exception);
        }
    }

    public boolean softDelete(UUID id) {
        String sql = """
                UPDATE financial_records
                SET deleted = TRUE, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND deleted = FALSE
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to delete record", exception);
        }
    }

    public List<FinancialRecord> search(RecordFilter filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, amount, type, category, record_date, notes, created_by, created_at, updated_at
                FROM financial_records
                WHERE deleted = FALSE
                """);
        List<Object> params = new ArrayList<>();

        appendRecordFilters(sql, params, filter);
        sql.append(" ORDER BY record_date DESC, created_at DESC LIMIT ? OFFSET ?");
        params.add(filter.getSize());
        params.add((filter.getPage() - 1) * filter.getSize());

        List<FinancialRecord> records = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParameters(statement, params);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapRecord(resultSet));
                }
                return records;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch records", exception);
        }
    }

    public long count(RecordFilter filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM financial_records
                WHERE deleted = FALSE
                """);
        List<Object> params = new ArrayList<>();
        appendRecordFilters(sql, params, filter);

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParameters(statement, params);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to count records", exception);
        }
    }

    public BigDecimal calculateTotalByType(RecordType type, LocalDate fromDate, LocalDate toDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT COALESCE(SUM(amount), 0)
                FROM financial_records
                WHERE deleted = FALSE AND type = ?
                """);
        List<Object> params = new ArrayList<>();
        params.add(type.name());
        appendDateRange(sql, params, fromDate, toDate);

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParameters(statement, params);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                BigDecimal total = resultSet.getBigDecimal(1);
                return total == null ? BigDecimal.ZERO : total;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to calculate totals", exception);
        }
    }

    public List<CategoryTotal> findCategoryTotals(LocalDate fromDate, LocalDate toDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT category, type, COALESCE(SUM(amount), 0) AS total
                FROM financial_records
                WHERE deleted = FALSE
                """);
        List<Object> params = new ArrayList<>();
        appendDateRange(sql, params, fromDate, toDate);
        sql.append(" GROUP BY category, type ORDER BY total DESC, category ASC");

        List<CategoryTotal> totals = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParameters(statement, params);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    totals.add(new CategoryTotal(
                            resultSet.getString("category"),
                            resultSet.getString("type"),
                            resultSet.getBigDecimal("total")
                    ));
                }
                return totals;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch category totals", exception);
        }
    }

    public List<FinancialRecord> findRecentActivity(LocalDate fromDate, LocalDate toDate, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, amount, type, category, record_date, notes, created_by, created_at, updated_at
                FROM financial_records
                WHERE deleted = FALSE
                """);
        List<Object> params = new ArrayList<>();
        appendDateRange(sql, params, fromDate, toDate);
        sql.append(" ORDER BY record_date DESC, created_at DESC LIMIT ?");
        params.add(limit);

        List<FinancialRecord> records = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParameters(statement, params);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapRecord(resultSet));
                }
                return records;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch recent activity", exception);
        }
    }

    public List<TrendPoint> findMonthlyTrends(LocalDate fromDate, LocalDate toDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT TO_CHAR(record_date, 'YYYY-MM') AS month_label, type, COALESCE(SUM(amount), 0) AS total
                FROM financial_records
                WHERE deleted = FALSE
                """);
        List<Object> params = new ArrayList<>();
        appendDateRange(sql, params, fromDate, toDate);
        sql.append(" GROUP BY month_label, type ORDER BY month_label ASC");

        Map<String, TrendPoint> points = new LinkedHashMap<>();

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParameters(statement, params);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String month = resultSet.getString("month_label");
                    TrendPoint point = points.computeIfAbsent(
                            month,
                            key -> new TrendPoint(key, BigDecimal.ZERO, BigDecimal.ZERO)
                    );

                    BigDecimal total = resultSet.getBigDecimal("total");
                    if (RecordType.from(resultSet.getString("type")) == RecordType.INCOME) {
                        point.setIncome(total);
                    } else {
                        point.setExpense(total);
                    }
                }

                return new ArrayList<>(points.values());
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch monthly trends", exception);
        }
    }

    private void appendRecordFilters(StringBuilder sql, List<Object> params, RecordFilter filter) {
        if (filter.getType() != null) {
            sql.append(" AND type = ?");
            params.add(filter.getType().name());
        }

        if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
            sql.append(" AND LOWER(category) = LOWER(?)");
            params.add(filter.getCategory().trim());
        }

        appendDateRange(sql, params, filter.getFromDate(), filter.getToDate());

        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            sql.append(" AND (category ILIKE ? OR COALESCE(notes, '') ILIKE ?)");
            String searchValue = "%" + filter.getSearch().trim() + "%";
            params.add(searchValue);
            params.add(searchValue);
        }
    }

    private void appendDateRange(StringBuilder sql, List<Object> params, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null) {
            sql.append(" AND record_date >= ?");
            params.add(fromDate);
        }

        if (toDate != null) {
            sql.append(" AND record_date <= ?");
            params.add(toDate);
        }
    }

    private void setParameters(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int index = 0; index < params.size(); index++) {
            statement.setObject(index + 1, params.get(index));
        }
    }

    private FinancialRecord mapRecord(ResultSet resultSet) throws SQLException {
        return new FinancialRecord(
                resultSet.getObject("id", UUID.class),
                resultSet.getBigDecimal("amount"),
                RecordType.from(resultSet.getString("type")),
                resultSet.getString("category"),
                resultSet.getDate("record_date").toLocalDate(),
                resultSet.getString("notes"),
                resultSet.getObject("created_by", UUID.class),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
