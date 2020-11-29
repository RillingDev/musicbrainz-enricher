package org.felixrilling.musicbrainzenricher;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
