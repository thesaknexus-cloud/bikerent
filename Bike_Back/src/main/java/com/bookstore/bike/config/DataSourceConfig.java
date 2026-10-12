package com.bookstore.bike.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Bean
    @Primary
    DataSource dataSource(
            @Value("${DB_URL:jdbc:h2:file:./data/bikemate;MODE=MySQL;DB_CLOSE_DELAY=-1}") String configuredUrl,
            @Value("${DB_USERNAME:sa}") String username,
            @Value("${DB_PASSWORD:}") String password,
            @Value("${DB_DRIVER:}") String configuredDriver) {
        String jdbcUrl = toJdbcUrl(configuredUrl);
        String driver = configuredDriver.isBlank()
                ? (jdbcUrl.startsWith("jdbc:postgresql:") ? "org.postgresql.Driver" : "org.h2.Driver")
                : configuredDriver;

        return DataSourceBuilder.create()
                .driverClassName(driver)
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .build();
    }

    private String toJdbcUrl(String url) {
        if (url.startsWith("postgresql://")) return "jdbc:" + url;
        if (url.startsWith("postgres://")) return "jdbc:postgresql://" + url.substring("postgres://".length());
        return url;
    }
}
