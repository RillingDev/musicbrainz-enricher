package org.felixrilling.musicbrainzenricher;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "applicationDbEntityManagerFactory",
        transactionManagerRef = "applicationDbTransactionManager"
)
class ApplicationDbConfiguration {

    @Primary
    @Bean(name = "applicationDbDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource customerDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "applicationDbEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean
    entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("applicationDbDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages("org.felixrilling.musicbrainzenricher")
                .persistenceUnit("applicationDb")
                .build();
    }

    @Primary
    @Bean(name = "applicationDbTransactionManager")
    public PlatformTransactionManager customerTransactionManager(
            @Qualifier("applicationDbEntityManagerFactory") EntityManagerFactory
                    customerEntityManagerFactory
    ) {
        return new JpaTransactionManager(customerEntityManagerFactory);
    }
}
