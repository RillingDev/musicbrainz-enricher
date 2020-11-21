package org.felixrilling.musicbrainzenricher;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
class AppConfiguration {

    // Configures for the musicbrainz server docker image.
    // https://github.com/metabrainz/musicbrainz-docker
    @ConfigurationProperties(prefix = "datasource.postgres")
    @Bean("musicbrainzLocalDb")
    @Profile("musicbrainzLocalDb")
    DataSource musicbrainzLocalDb() {
        return DataSourceBuilder
                .create()
                .username("musicbrainz")
                .password("musicbrainz")
                .url("jdbc:postgresql://127.0.0.1:5432/musicbrainz_db")
                .build();
    }
}
