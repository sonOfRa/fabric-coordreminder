package de.slevermann.fabric.coordreminder;

public class Configuration {

    private String databaseType;

    private String jdbcUrl;

    private String dbUser;

    private String dbPassword;

    public Configuration(final String databaseType, final String jdbcUrl, final String dbUser, final String dbPassword) {
        this.databaseType = databaseType;
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public Configuration() {}

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(final String databaseType) {
        this.databaseType = databaseType;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(final String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(final String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(final String dbPassword) {
        this.dbPassword = dbPassword;
    }
}
