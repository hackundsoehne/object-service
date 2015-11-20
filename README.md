# Welcome to the Prototype for CrowdControl
you can start the Prototype with the following commands: `java -jar JARNAME USERNAME PASSWORD DATABASEURL`

#### to build this project you need:
- java 8
- gradle
- a mysql-server


#### to generate the database-classes (all the classes in the package databasemodel)
fill in the relevant jooq-informations in build.gradle, then run the generateJooq gradle task.