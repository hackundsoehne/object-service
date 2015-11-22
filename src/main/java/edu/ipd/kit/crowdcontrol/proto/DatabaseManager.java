package edu.ipd.kit.crowdcontrol.proto;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * initializes the database.
 * @author Leander Kurscheidt (Leander.Kurscheidt@gmx.de)
 * @version 1.0
 */
public class DatabaseManager {
    @SuppressWarnings("FieldCanBeLocal")
    private final Connection connection;
    private final DSLContext context;


    public DatabaseManager(String userName, String password, String url) throws SQLException {
        connection = DriverManager.getConnection(url, userName, password);
        context = DSL.using(connection, SQLDialect.MYSQL);
        initDatabase();
    }

    private void initDatabase() {
        context.fetch("-- -----------------------------------------------------\n" +
                "-- Schema crowdcontrolproto\n" +
                "-- -----------------------------------------------------\n" +
                "CREATE SCHEMA IF NOT EXISTS `crowdcontrolproto` DEFAULT CHARACTER SET utf8mb4 ;\n" +
                "USE `crowdcontrolproto` ;\n" +
                "\n" +
                "-- -----------------------------------------------------\n" +
                "-- Table `crowdcontrolproto`.`Experiment`\n" +
                "-- -----------------------------------------------------\n" +
                "CREATE TABLE IF NOT EXISTS `crowdcontrolproto`.`Experiment` (\n" +
                "  `idexperiment` INT(11) NOT NULL,\n" +
                "  `picture_url` VARCHAR(45) NULL DEFAULT NULL,\n" +
                "  `picture_license_url` VARCHAR(45) NULL DEFAULT NULL,\n" +
                "  `question` VARCHAR(45) NULL DEFAULT NULL,\n" +
                "  `rating_options` JSON NULL DEFAULT NULL,\n" +
                "  `titel` VARCHAR(45) NULL DEFAULT NULL,\n" +
                "  `max_ratings_per_assignment` INT(11) NULL DEFAULT NULL,\n" +
                "  `max_answers_per_assignment` INT(11) NULL DEFAULT NULL,\n" +
                "  PRIMARY KEY (`idexperiment`))\n" +
                "ENGINE = InnoDB\n" +
                "DEFAULT CHARACTER SET = utf8mb4;\n" +
                "\n" +
                "\n" +
                "-- -----------------------------------------------------\n" +
                "-- Table `crowdcontrolproto`.`HIT`\n" +
                "-- -----------------------------------------------------\n" +
                "CREATE TABLE IF NOT EXISTS `crowdcontrolproto`.`HIT` (\n" +
                "  `idHIT` INT(11) NOT NULL,\n" +
                "  `experiment_h` INT(11) NOT NULL,\n" +
                "  `type` VARCHAR(45) NOT NULL,\n" +
                "  `running` BIT(1) NOT NULL,\n" +
                "  `current_amount` INT(11) NOT NULL,\n" +
                "  `max_amount` INT(11) NOT NULL,\n" +
                "  `payment` INT(11) NOT NULL,\n" +
                "  `bonus` INT(11) NOT NULL DEFAULT '0',\n" +
                "  `id_crowd_platform` VARCHAR(45) NULL DEFAULT NULL,\n" +
                "  `crowd_platform` VARCHAR(45) NOT NULL,\n" +
                "  PRIMARY KEY (`idHIT`),\n" +
                "  INDEX `idexperiment_idx` (`experiment_h` ASC),\n" +
                "  CONSTRAINT `idexperimenthit`\n" +
                "    FOREIGN KEY (`experiment_h`)\n" +
                "    REFERENCES `crowdcontrolproto`.`Experiment` (`idexperiment`))\n" +
                "ENGINE = InnoDB\n" +
                "DEFAULT CHARACTER SET = utf8mb4;\n" +
                "\n" +
                "\n" +
                "-- -----------------------------------------------------\n" +
                "-- Table `crowdcontrolproto`.`Answers`\n" +
                "-- -----------------------------------------------------\n" +
                "CREATE TABLE IF NOT EXISTS `crowdcontrolproto`.`Answers` (\n" +
                "  `idAnswers` INT(11) NOT NULL,\n" +
                "  `hit_a` INT(11) NOT NULL,\n" +
                "  `answer` VARCHAR(45) NOT NULL,\n" +
                "  `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "  `workerID` VARCHAR(45) NULL DEFAULT NULL,\n" +
                "  PRIMARY KEY (`idAnswers`),\n" +
                "  INDEX `idexperiment_idx` (`hit_a` ASC),\n" +
                "  CONSTRAINT `idHITanswers`\n" +
                "    FOREIGN KEY (`hit_a`)\n" +
                "    REFERENCES `crowdcontrolproto`.`HIT` (`idHIT`)\n" +
                "    ON DELETE CASCADE\n" +
                "    ON UPDATE CASCADE)\n" +
                "ENGINE = InnoDB\n" +
                "DEFAULT CHARACTER SET = utf8mb4;\n" +
                "\n" +
                "\n" +
                "-- -----------------------------------------------------\n" +
                "-- Table `crowdcontrolproto`.`Qualifications`\n" +
                "-- -----------------------------------------------------\n" +
                "CREATE TABLE IF NOT EXISTS `crowdcontrolproto`.`Qualifications` (\n" +
                "  `idQualifications` INT(11) NOT NULL,\n" +
                "  `text` VARCHAR(45) NOT NULL,\n" +
                "  `experiment_q` INT(11) NOT NULL,\n" +
                "  PRIMARY KEY (`idQualifications`),\n" +
                "  INDEX `idexperiment_idx` (`experiment_q` ASC),\n" +
                "  CONSTRAINT `idexperimentqual`\n" +
                "    FOREIGN KEY (`experiment_q`)\n" +
                "    REFERENCES `crowdcontrolproto`.`Experiment` (`idexperiment`)\n" +
                "    ON DELETE CASCADE\n" +
                "    ON UPDATE CASCADE)\n" +
                "ENGINE = InnoDB\n" +
                "DEFAULT CHARACTER SET = utf8mb4;\n" +
                "\n" +
                "\n" +
                "-- -----------------------------------------------------\n" +
                "-- Table `crowdcontrolproto`.`Ratings`\n" +
                "-- -----------------------------------------------------\n" +
                "CREATE TABLE IF NOT EXISTS `crowdcontrolproto`.`Ratings` (\n" +
                "  `idRatings` INT(11) NOT NULL,\n" +
                "  `hit_r` INT(11) NOT NULL,\n" +
                "  `answer_r` INT(11) NOT NULL,\n" +
                "  `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "  `rating` INT(11) NOT NULL,\n" +
                "  `workerID` VARCHAR(45) NULL DEFAULT NULL,\n" +
                "  PRIMARY KEY (`idRatings`),\n" +
                "  INDEX `idHIT_idx` (`hit_r` ASC),\n" +
                "  INDEX `idAnswers_idx` (`answer_r` ASC),\n" +
                "  CONSTRAINT `idAnswersratins`\n" +
                "    FOREIGN KEY (`answer_r`)\n" +
                "    REFERENCES `crowdcontrolproto`.`Answers` (`idAnswers`)\n" +
                "    ON DELETE CASCADE\n" +
                "    ON UPDATE CASCADE,\n" +
                "  CONSTRAINT `idHITrating`\n" +
                "    FOREIGN KEY (`hit_r`)\n" +
                "    REFERENCES `crowdcontrolproto`.`HIT` (`idHIT`)\n" +
                "    ON DELETE CASCADE\n" +
                "    ON UPDATE CASCADE)\n" +
                "ENGINE = InnoDB\n" +
                "DEFAULT CHARACTER SET = utf8mb4;\n" +
                "\n" +
                "\n" +
                "-- -----------------------------------------------------\n" +
                "-- Table `crowdcontrolproto`.`Tags`\n" +
                "-- -----------------------------------------------------\n" +
                "CREATE TABLE IF NOT EXISTS `crowdcontrolproto`.`Tags` (\n" +
                "  `idTags` INT(11) NOT NULL,\n" +
                "  `tag` VARCHAR(45) NOT NULL,\n" +
                "  `experiment_t` INT(11) NOT NULL,\n" +
                "  PRIMARY KEY (`idTags`),\n" +
                "  INDEX `idexperiment_idx` (`experiment_t` ASC),\n" +
                "  CONSTRAINT `idexperimenttags`\n" +
                "    FOREIGN KEY (`experiment_t`)\n" +
                "    REFERENCES `crowdcontrolproto`.`Experiment` (`idexperiment`)\n" +
                "    ON DELETE CASCADE\n" +
                "    ON UPDATE CASCADE)\n" +
                "ENGINE = InnoDB\n" +
                "DEFAULT CHARACTER SET = utf8mb4;");
    }

    public DSLContext getContext() {
        return context;
    }
}
