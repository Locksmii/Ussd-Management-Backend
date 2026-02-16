package springboot_login_page.login_page.Config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "springboot_login_page.login_page.Repository.oracle",
        entityManagerFactoryRef = "oracleEntityManagerFactory",
        transactionManagerRef = "oracleTransactionManager"
)
public class OracleConfig {

    @Bean(name = "oracleDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.oracle")
    public DataSource oracleDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "oracleEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean oracleEntityManagerFactory(
            @Qualifier("oracleDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("springboot_login_page.login_page.Entity");
        em.setPersistenceUnitName("oraclePU");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");  // Keep as none
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.jdbc.lob.non_contextual_creation", true);

        // Add this to let Hibernate know about the sequence
        properties.put("hibernate.id.new_generator_mappings", true);

        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = "oracleTransactionManager")
    public PlatformTransactionManager oracleTransactionManager(
            @Qualifier("oracleEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }
}