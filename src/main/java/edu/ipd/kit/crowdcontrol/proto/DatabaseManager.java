package edu.ipd.kit.crowdcontrol.proto;

import com.sun.tools.internal.ws.processor.model.ModelException;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * initializes the database.
 * @author LeanderK
 * @version 1.0
 */
public class DatabaseManager {
    private final Connection connection;
    private final DSLContext context;


    public DatabaseManager(String userName, String password, String url) throws SQLException, ModelException {
        connection = DriverManager.getConnection(url, userName, password);
        context = DSL.using(connection, SQLDialect.MYSQL);
        initDatabase();
    }

    private void initDatabase() {
        //TODO init database;
    }

    public DSLContext getContext() {
        return context;
    }
}
