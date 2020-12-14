package org.felixrilling.musicbrainzenricher;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
class MusicbrainzLocalDbConfiguration {

    @ConfigurationProperties(prefix = "musicbrainz-local-db.datasource")
    @Bean("musicbrainzLocalDb")
    DataSource musicbrainzLocalDb() {
        return DataSourceBuilder
                .create()
                .build();
    }

    @Bean("musicbrainzLocalDbJdbcTemplate")
    JdbcTemplate jdbcTemplate(@Qualifier("musicbrainzLocalDb") DataSource dataSource)  {
        return new JdbcTemplate(dataSource);
    }

}
