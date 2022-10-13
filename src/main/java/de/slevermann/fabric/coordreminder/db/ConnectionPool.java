package de.slevermann.fabric.coordreminder.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.slevermann.fabric.coordreminder.Configuration;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);

    private static final String DEFAULT_MARIADB_URL = "jdbc:mariadb://localhost:3306/coordreminder";
    private static final String DEFAULT_H2_URL = "jdbc:h2:file:./coordreminder/database;AUTO_SERVER=TRUE";

    private final HikariDataSource dataSource;

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public ConnectionPool(final Configuration configuration) {
        var db = configuration.getDatabaseType();
        FluentConfiguration config = Flyway.configure();
        config.failOnMissingLocations(true);

        if (db == null) {
            db = "h2";
            LOGGER.warn("No Database type set. Falling back to default H2!");
        }

        final boolean isMariaDb;
        if (db.equals("h2")) {
            config.locations("classpath:/db/migration/h2");
            isMariaDb = false;
        } else if (db.equals("mariadb") || db.equals("mysql")) {
            config.locations("classpath:/db/migration/mariadb");
            isMariaDb = true;
        } else {
            throw new IllegalArgumentException("Database for coordreminder must be h2, mysql or mariadb!");
        }

        if (isMariaDb) {
            if (configuration.getDbUser() == null || configuration.getDbPassword() == null) {
                throw new IllegalArgumentException("dbUser and dbPassword must be set for mariadb/mysql config!");
            }
        }
        var jdbcUrl = configuration.getJdbcUrl();
        if (jdbcUrl == null) {
            jdbcUrl = isMariaDb ? DEFAULT_MARIADB_URL : DEFAULT_H2_URL;
            LOGGER.warn("No JDBC Url set. Falling back to default {}", jdbcUrl);
        }

        final var hikariConfig = new HikariConfig();
        // Need to load driver in the legacy way because loom suppresses autoloading
        hikariConfig.setDriverClassName(isMariaDb ? "org.mariadb.jdbc.Driver" : "org.h2.Driver");
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(configuration.getDbUser());
        hikariConfig.setPassword(configuration.getDbPassword());
        dataSource = new HikariDataSource(hikariConfig);

        config.dataSource(dataSource);
        final var flyway = config.load();
        flyway.migrate();
    }
}
