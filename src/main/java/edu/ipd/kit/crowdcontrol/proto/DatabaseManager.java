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
    @SuppressWarnings("FieldCanBeLocal")
    private final Connection connection;
    private final DSLContext context;


    public DatabaseManager(String userName, String password, String url) throws SQLException, ModelException {
        connection = DriverManager.getConnection(url, userName, password);
        context = DSL.using(connection, SQLDialect.MYSQL);
        initDatabase();
    }

    private void initDatabase() {
        //much ugly!
        context.fetch("CREATE TABLE IF NOT EXISTS `experiment` (\n" +
                "  `id` int(11) NOT NULL,\n" +
                "  `question` varchar(45) DEFAULT NULL,\n" +
                "  `taskQuestion` varchar(45) DEFAULT NULL,\n" +
                "  `task_picture_url` varchar(45) DEFAULT NULL,\n" +
                "  `task_picture_license_url` varchar(45) DEFAULT NULL,\n" +
                "  `task_description` varchar(45) DEFAULT NULL,\n" +
                "  `hit_title` varchar(45) DEFAULT NULL,\n" +
                "  `hit_description` varchar(45) DEFAULT NULL,\n" +
                "  `basicPaymentHIT` int(11) DEFAULT NULL,\n" +
                "  `basicPaymentAnswer` int(11) DEFAULT NULL,\n" +
                "  `basicPaymentRating` int(11) DEFAULT NULL,\n" +
                "  `bonusPayment` int(11) DEFAULT NULL,\n" +
                "  `maxAnswersPerAssignment` int(11) DEFAULT NULL,\n" +
                "  `maxRatingsPerAssignment` int(11) DEFAULT NULL,\n" +
                "  `titel` varchar(45) NOT NULL,\n" +
                "  `budget` int(11) DEFAULT NULL,\n" +
                "  `running` bit(1) DEFAULT b'0',\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  UNIQUE KEY `id_UNIQUE` (`id`),\n" +
                "  UNIQUE KEY `titel_UNIQUE` (`titel`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;");

        context.fetch("REATE TABLE IF NOT EXISTS `RatingOptions` (\n" +
                "  `idRatingOptions` int(11) NOT NULL,\n" +
                "  `key` varchar(45) DEFAULT NULL,\n" +
                "  `value` double DEFAULT NULL,\n" +
                "  `experiment` int(11) NOT NULL,\n" +
                "  PRIMARY KEY (`idRatingOptions`),\n" +
                "  KEY `experiment_idx` (`experiment`),\n" +
                "  CONSTRAINT `experiment` FOREIGN KEY (`experiment`) REFERENCES `experiment` (`id`) ON DELETE CASCADE ON UPDATE CASCADE\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n");
    }

    public DSLContext getContext() {
        return context;
    }
}
