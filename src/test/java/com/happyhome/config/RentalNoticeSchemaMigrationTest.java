package com.happyhome.config;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

class RentalNoticeSchemaMigrationTest {

    private final DataSource dataSource = org.mockito.Mockito.mock(DataSource.class);
    private final Connection connection = org.mockito.Mockito.mock(Connection.class);
    private final DatabaseMetaData metaData = org.mockito.Mockito.mock(DatabaseMetaData.class);
    private final ResultSet columns = org.mockito.Mockito.mock(ResultSet.class);
    private final Statement statement = org.mockito.Mockito.mock(Statement.class);

    @Test
    void addsMissingSupplyMapColumns() throws Exception {
        RentalNoticeSchemaMigration migration = new RentalNoticeSchemaMigration(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(connection.getCatalog()).thenReturn("ssafyhome");
        when(metaData.getColumns(org.mockito.Mockito.eq("ssafyhome"), org.mockito.Mockito.isNull(),
                org.mockito.Mockito.eq("lh_notice_supplies"), org.mockito.Mockito.anyString()))
                .thenReturn(columns);
        when(columns.next()).thenReturn(false);
        when(connection.createStatement()).thenReturn(statement);

        migration.addSupplyColumnsIfMissing();

        verify(statement).execute("ALTER TABLE lh_notice_supplies ADD COLUMN lot_number VARCHAR(120)");
        verify(statement).execute("ALTER TABLE lh_notice_supplies ADD COLUMN latitude DOUBLE");
        verify(statement, atLeastOnce()).execute(org.mockito.Mockito.startsWith("ALTER TABLE lh_notice_supplies"));
    }
}
