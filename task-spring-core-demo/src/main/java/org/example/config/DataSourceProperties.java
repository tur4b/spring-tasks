package org.example.config;

public record DataSourceProperties(
        String url,
        String username,
        String password,
        String driverClassName,
        JpaProperties jpaProperties,
        HikariProperties hikariCPProps
) {

    public record JpaProperties(
            String ddlAuto,
            String dialect,
            boolean showSql,
            boolean formatSql,
            String namingStrategy) {
    }

    public record HikariProperties(
            int maximumPoolSize,
            int minimumIdle,
            long connectionTimeout,
            long idleTimeout,
            long maxLifetime) {
    }

}
