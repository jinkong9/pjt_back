package com.happyhome.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RentalNoticeSchemaMigration implements ApplicationRunner {

    private static final String SUPPLIES_TABLE = "lh_notice_supplies";
    private static final List<ColumnDefinition> SUPPLY_COLUMNS = List.of(
            new ColumnDefinition("lot_number", "VARCHAR(120)"),
            new ColumnDefinition("internet_apply_status", "VARCHAR(120)"),
            new ColumnDefinition("map_address", "VARCHAR(500)"),
            new ColumnDefinition("map_url", "VARCHAR(1000)"),
            new ColumnDefinition("latitude", "DOUBLE"),
            new ColumnDefinition("longitude", "DOUBLE")
    );

    private final DataSource dataSource;

    public RentalNoticeSchemaMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        addSupplyColumnsIfMissing();
    }

    void addSupplyColumnsIfMissing() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            for (ColumnDefinition column : SUPPLY_COLUMNS) {
                if (hasColumn(connection, column.name())) {
                    continue;
                }
                try (Statement statement = connection.createStatement()) {
                    statement.execute("ALTER TABLE " + SUPPLIES_TABLE + " ADD COLUMN " + column.name() + " " + column.type());
                }
            }
        }
    }

    private boolean hasColumn(Connection connection, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();
        try (ResultSet columns = metaData.getColumns(catalog, null, SUPPLIES_TABLE, columnName)) {
            return columns.next();
        }
    }

    private record ColumnDefinition(String name, String type) {
    }
}
