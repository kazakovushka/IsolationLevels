package ru.kazakovushka.edu;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@ComponentScan
@EnableTransactionManagement
public class Main {

    @Autowired
    CounterUtils counterUtils;

    @Bean
    DataSource dataSource() {
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.DERBY).build();
    }

    @Bean
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    PlatformTransactionManager getTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    public static void main(String[] args) {
        System.getProperties().put("derby.locks.waitTimeout", "1");

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Main.class);
        context.getBean(Main.class).run();
    }

    public void run() {
        counterUtils.init();
        try {
            counterUtils.dirtyReads();
        } catch (CannotAcquireLockException e) {
            System.out.println("----can't acquire lock for dirty reads----");
        }


        try {
            counterUtils.repeatableReads();
        } catch (CannotAcquireLockException e) {
            System.out.println("----can't acquire lock for repeatable reads----");
        }

        try {
            counterUtils.phantomReads();
        }catch (CannotAcquireLockException e){
            System.out.println("----can't acquire lock for phantom reads----");
        }

    }


}
