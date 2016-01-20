package edu.kit.ipd.crowdcontrol.objectservice;

import edu.kit.ipd.crowdcontrol.objectservice.database.operations.NotificationRestOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.TemplateOperations;
import edu.kit.ipd.crowdcontrol.objectservice.rest.NotificationResource;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Router;
import edu.kit.ipd.crowdcontrol.objectservice.rest.TemplateResource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * @author Niklas Keller
 */
public class Main {
    public static void main(String[] args) throws IOException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Properties properties = new Properties();

        try (InputStream in = Main.class.getResourceAsStream("/config.properties")) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        String url = properties.getProperty("database.url").trim();
        String username = properties.getProperty("database.username").trim();
        String password = properties.getProperty("database.password").trim();

        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Don't close connection.
        // FIXME: If connection breaks, we need to reconnect.
        boot(connection);
    }

    private static void boot(Connection connection) {
        DSLContext context = DSL.using(connection, SQLDialect.MYSQL);

        TemplateOperations templateOperations = new TemplateOperations(context);
        NotificationRestOperations notificationRestOperations = new NotificationRestOperations(context);

        new Router(
                new TemplateResource(templateOperations),
                new NotificationResource(notificationRestOperations)
        ).init();
    }
}
