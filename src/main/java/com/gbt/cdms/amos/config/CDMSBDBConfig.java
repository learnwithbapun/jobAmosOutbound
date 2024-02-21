package com.gbt.cdms.amos.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CDMSBDBConfig {
	@Primary
	@Bean(name = "cdmsDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.cdms")

	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}
}