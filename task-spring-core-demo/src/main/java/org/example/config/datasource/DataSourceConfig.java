package org.example.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "org.example.dao")
@EnableJpaAuditing
public class DataSourceConfig {

    @Bean
    public DataSourceProperties dataSourceProperties(
            @Value("${db.url}") String databaseUrl,
            @Value("${db.username}") String databaseUsername,
            @Value("${db.password}") String databasePassword,
            @Value("${db.driverClassName}") String driverClassName,
            @Value("${db.jpa.ddlAuto}") String jpaDdlAuto,
            @Value("${db.jpa.dialect}") String jpaDialect,
            @Value("${db.jpa.showSql}") boolean jpaShowSql,
            @Value("${db.jpa.formatSql}") boolean jpaFormatSql,
            @Value("${db.jpa.namingStrategy}") String namingStrategy,
            @Value("${db.pool.maximumPoolSize}") int maximumPoolSize,
            @Value("${db.pool.minimumIdle}") int minimumIdle,
            @Value("${db.pool.connectionTimeout}") int connectionTimeout,
            @Value("${db.pool.idleTimeout}") int idleTimeout,
            @Value("${db.pool.maxLifetime}") int maxLifetime) {

        return new DataSourceProperties(
                databaseUrl,
                databaseUsername,
                databasePassword,
                driverClassName,
                new DataSourceProperties.JpaProperties(
                        jpaDdlAuto,
                        jpaDialect,
                        jpaShowSql,
                        jpaFormatSql,
                        namingStrategy
                ),
                new DataSourceProperties.HikariProperties(
                        maximumPoolSize,
                        minimumIdle,
                        connectionTimeout,
                        idleTimeout,
                        maxLifetime
                )
        );
    }

    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        HikariDataSource hikariDataSource = new HikariDataSource();

        // set db params
        DataSourceProperties.HikariProperties hikariProperties = dataSourceProperties.hikariCPProps();

        hikariDataSource.setJdbcUrl(dataSourceProperties.url());
        hikariDataSource.setUsername(dataSourceProperties.username());
        hikariDataSource.setPassword(dataSourceProperties.password());
        hikariDataSource.setDriverClassName(dataSourceProperties.driverClassName());
        hikariDataSource.setMaximumPoolSize(hikariProperties.maximumPoolSize());
        hikariDataSource.setMinimumIdle(hikariProperties.minimumIdle());
        hikariDataSource.setConnectionTimeout(hikariProperties.connectionTimeout());
        hikariDataSource.setIdleTimeout(hikariProperties.idleTimeout());
        hikariDataSource.setMaxLifetime(hikariProperties.maxLifetime());

        return hikariDataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            DataSourceProperties dataSourceProperties) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("org.example.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaProperties(jpaProperties(dataSourceProperties));
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean emfBean) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(emfBean.getObject());
        return txManager;
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource,
                                                       @Value("classpath:data/schema.sql")Resource schemaSqlResource,
                                                       @Value("classpath:data/data.sql") Resource dataSqlResource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);

        // The populator is where you define the scripts to execute
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(schemaSqlResource);
        populator.addScript(dataSqlResource);

        // Optional: fail-fast setting or dealing with specific script requirements
        populator.setContinueOnError(false);

        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    private Properties jpaProperties(DataSourceProperties dataSourceProperties) {
        Properties props = new Properties();
        DataSourceProperties.JpaProperties jpaProperties = dataSourceProperties.jpaProperties();
        props.put("hibernate.hbm2ddl.auto", jpaProperties.ddlAuto());
        props.put("hibernate.dialect", jpaProperties.dialect());
        props.put("hibernate.show_sql", jpaProperties.showSql());
        props.put("hibernate.format_sql", jpaProperties.formatSql());
        props.put("hibernate.implicit_naming_strategy",
                Objects.requireNonNullElse(
                        jpaProperties.namingStrategy(),
                "org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl"
                )
        );
        return props;
    }

}
