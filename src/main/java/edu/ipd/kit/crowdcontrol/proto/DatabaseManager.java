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
        //much ugly!
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
                "  `idexperiment` INT NOT NULL,\n" +
                "  `picture_url` VARCHAR(45) NULL,\n" +
                "  `picture_license_url` VARCHAR(45) NULL,\n" +
                "  `question` VARCHAR(45) NULL,\n" +
                "  `rating_options` JSON NULL,\n" +
                "  `titel` VARCHAR(45) NULL,\n" +
                "  `max_ratings_per_assignment` INT NULL,\n" +
                "  `max_answers_per_assignment` INT NULL,\n" +
                "  PRIMARY KEY (`idexperiment`))\n" +
                "ENGINE = InnoDB;\n" +
                "\n" +
                "\n" +
                "-- -----------------------------------------------------\n" +
                "-- Table `crowdcontrolproto`.`Qualifications`\n" +
                "-- -----------------------------------------------------\n" +
                "CREATE TABLE IF NOT EXISTS `crowdcontrolproto`.`Qualifications` (\n" +
                "  `idQualifications` INT NOT NULL,\n" +
                "  `text` VARCHAR(45) NOT NULL,\n" +
                "  `experiment_q` INT NOT NULL,\n" +
                "  PRIMARY KEY (`idQualifications`),\n" +
                "  INDEX `idexperiment_idx` (`experiment_q` ASC),\n" +
                "  CONSTRAINT `idexperimentqual`\n" +
                "    FOREIGN KEY (`experiment_q`)\n" +
                "    REFERENCES `crowdcontrolproto`.`Experiment` (`idexperiment`)\n" +
                "    ON DELETE CASCADE\n" +
                "    ON UPDATE CASCADE)\n" +
                "ENGINE = InnoDB;\n" +
                "\n" +
                "\n" +
                "-- -----------------------------------------------------\n" +
                "-- Table `crowdcontrolproto`.`Tags`\n" +
                "-- -----------------------------------------------------\n" +
                "CREATE TABLE IF NOT EXISTS `crowdcontrolproto`.`Tags` (\n" +
                "  `idTags` INT NOT NULL,\n" +
                "  `tag` VARCHAR(45) NOT NULL,\n" +
                "  `experiment_t` INT NOT NULL,\n" +
                "  PRIMARY KEY (`idTags`),\n" +
                "  INDEX `idexperiment_idx` (`experiment_t` ASC),\n" +
                "  CONSTRAINT `idexperimenttags`\n" +
                "    FOREIGN KEY (`experiment_t`)\n" +
                "    REFERENCES `crowdcontrolproto`.`Experiment` (`idexperiment`)\n" +
                "    ON DELETE CASCADE\n" +
                "    ON UPDATE CASCADE)\n" +
                "ENGINE = InnoDB;\n" +
                "\n" +
                "\n" +
                "-- -----------------------------------------------------\n" +
                "-- Table `crowdcontrolproto`.`HIT`\n" +
                "-- -----------------------------------------------------\n" +
                "CREATE TABLE `HIT` (\n" +
                "  `idHIT` int(11) NOT NULL,\n" +
                "  `experiment_h` int(11) NOT NULL,\n" +
                "  `type` varchar(45) NOT NULL,\n" +
                "  `running` bit(1) NOT NULL,\n" +
                "  `current_amount` int(11) NOT NULL,\n" +
                "  `max_amount` int(11) NOT NULL,\n" +
                "  `payment` int(11) NOT NULL,\n" +
                "  `bonus` int(11) DEFAULT '0',\n" +
                "  PRIMARY KEY (`idHIT`),\n" +
                "  KEY `idexperiment_idx` (`experiment_h`),\n" +
                "  CONSTRAINT `idexperimenthit` FOREIGN KEY (`experiment_h`) REFERENCES `Experiment` (`idexperiment`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n" +
                "\n" +
                "\n" +
                "-- -----------------------------------------------------\n" +
                "-- Table `crowdcontrolproto`.`Answers`\n" +
                "-- -----------------------------------------------------\n" +
                "CREATE TABLE IF NOT EXISTS `crowdcontrolproto`.`Answers` (\n" +
                "  `idAnswers` INT NOT NULL,\n" +
                "  `hit_a` INT NOT NULL,\n" +
                "  `answer` VARCHAR(45) NOT NULL,\n" +
                "  `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "  PRIMARY KEY (`idAnswers`),\n" +
                "  INDEX `idexperiment_idx` (`hit_a` ASC),\n" +
                "  CONSTRAINT `idHITanswers`\n" +
                "    FOREIGN KEY (`hit_a`)\n" +
                "    REFERENCES `crowdcontrolproto`.`HIT` (`idHIT`)\n" +
                "    ON DELETE CASCADE\n" +
                "    ON UPDATE CASCADE)\n" +
                "ENGINE = InnoDB;\n" +
                "\n" +
                "\n" +
                "-- -----------------------------------------------------\n" +
                "-- Table `crowdcontrolproto`.`Ratings`\n" +
                "-- -----------------------------------------------------\n" +
                "CREATE TABLE IF NOT EXISTS `crowdcontrolproto`.`Ratings` (\n" +
                "  `idRatings` INT NOT NULL,\n" +
                "  `hit_r` INT NOT NULL,\n" +
                "  `answer_r` INT NOT NULL,\n" +
                "  `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "  `rating` INT NOT NULL,\n" +
                "  PRIMARY KEY (`idRatings`),\n" +
                "  INDEX `idHIT_idx` (`hit_r` ASC),\n" +
                "  INDEX `idAnswers_idx` (`answer_r` ASC),\n" +
                "  CONSTRAINT `idHITrating`\n" +
                "    FOREIGN KEY (`hit_r`)\n" +
                "    REFERENCES `crowdcontrolproto`.`HIT` (`idHIT`)\n" +
                "    ON DELETE CASCADE\n" +
                "    ON UPDATE CASCADE,\n" +
                "  CONSTRAINT `idAnswersratins`\n" +
                "    FOREIGN KEY (`answer_r`)\n" +
                "    REFERENCES `crowdcontrolproto`.`Answers` (`idAnswers`)\n" +
                "    ON DELETE CASCADE\n" +
                "    ON UPDATE CASCADE)\n" +
                "ENGINE = InnoDB;");
    }

    public DSLContext getContext() {
        return context;
    }
}
