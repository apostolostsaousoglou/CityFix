@echo off
set JAVAFX_LIB=C:\Users\nikol\Downloads\openjfx-26_windows-x64_bin-sdk\javafx-sdk-26\lib
set JDBC_JAR=resources\postgresql-42.7.5.jar

javac --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web -d bin -cp "bin;%JDBC_JAR%" src/com/citydamage/app/*.java
java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web -cp "bin;resources;%JDBC_JAR%" com.citydamage.app.MainApp
