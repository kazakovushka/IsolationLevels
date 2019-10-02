package ru.kazakovushka.edu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class CounterUtils {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    CounterUtils self;


    @Transactional
    public void init() {
        try {
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            System.out.println("default isolation is" + connection.getTransactionIsolation());
            jdbcTemplate.execute("CREATE TABLE Counter (num INTEGER)");
            jdbcTemplate.update("INSERT INTO Counter VALUES (4)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Transactional
    void dirtyReads() {
        jdbcTemplate.update("UPDATE Counter SET num = 10");
        System.out.println("value was set to 10");
        self.readFromAnotherTransaction();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public void readFromAnotherTransaction() {
        int num = jdbcTemplate.queryForObject("SELECT num FROM Counter", Integer.class);
        System.out.println("dirty read , num = " + num);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeFromAnotherTransaction() {
        System.out.println("update num to 16");
        jdbcTemplate.update("UPDATE Counter SET num = 16");

    }


    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void repeatableReads() {
        int num = jdbcTemplate.queryForObject("SELECT num FROM Counter", Integer.class);
        System.out.println("before = " + num);

        self.changeFromAnotherTransaction();

        num = jdbcTemplate.queryForObject("SELECT num FROM Counter", Integer.class);
        System.out.println("after = " + num);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertFromAnotherTransaction() {
        jdbcTemplate.update("INSERT INTO Counter VALUES (80)");
        System.out.println("new row was insert");
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void phantomReads() {
        int rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Counter", Integer.class);
        System.out.println("counter before is " + rowCount);

        self.insertFromAnotherTransaction();

        rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Counter", Integer.class);
        System.out.println("counter after is " + rowCount);
    }
}
