package com.ninjamcr.waystones.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

final class DatabaseMigrations {
    private DatabaseMigrations() {
    }

    static void apply(Connection connection) throws IOException, SQLException {
        List<String> migrations = readResource("migrations/index.txt").lines().toList();
        try (Statement statement = connection.createStatement()) {
            // Lock before reading the schema version so two startups cannot apply the same migration.
            statement.execute("BEGIN IMMEDIATE");
            try {
                int version;
                try (ResultSet result = statement.executeQuery("PRAGMA user_version")) {
                    result.next();
                    version = result.getInt(1);
                }

                if (version < 0 || version > migrations.size()) {
                    throw new SQLException("Unsupported Waystones database schema version: " + version);
                }

                for (int next = version + 1; next <= migrations.size(); next++) {
                    String resource = "migrations/" + migrations.get(next - 1);
                    try {
                        statement.executeUpdate(readResource(resource));
                        statement.execute("PRAGMA user_version = " + next);
                    } catch (IOException | SQLException e) {
                        throw new SQLException("Could not apply database migration " + resource, e);
                    }
                }

                statement.execute("COMMIT");
            } catch (SQLException e) {
                try {
                    statement.execute("ROLLBACK");
                } catch (SQLException rollbackFailure) {
                    e.addSuppressed(rollbackFailure);
                }
                throw e;
            }
        }
    }

    static String readResource(String path) throws IOException {
        try (InputStream input = DatabaseMigrations.class.getResourceAsStream("/database/" + path)) {
            if (input == null) {
                throw new IOException("Missing database resource: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
