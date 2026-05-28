package org.example.config.datasource;

record DataSourceProperties(
        String url,
        String username,
        String password,
        String driverClassName,
        JpaProperties jpaProperties,
        HikariProperties hikariCPProps
) {

    record JpaProperties(
            String ddlAuto,
            String dialect,
            boolean showSql,
            boolean formatSql,
            String namingStrategy) {
    }

    record HikariProperties(
            int maximumPoolSize,
            int minimumIdle,
            long connectionTimeout,
            long idleTimeout,
            long maxLifetime) {
    }

}
