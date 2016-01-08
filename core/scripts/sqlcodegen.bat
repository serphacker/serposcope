cd %~dp0
 
 
java -cp "..\lib\h2-1.4.189.jar" org.h2.tools.RunScript -url jdbc:h2:%~dp0\\..\\codegen\\h2 -script %~dp0\\..\\src\\main\\resources\\db\\00-base.h2.sql
java -cp "..\lib\h2-1.4.189.jar" org.h2.tools.RunScript -url jdbc:h2:%~dp0\\..\\codegen\\h2 -script %~dp0\\..\\src\\main\\resources\\db\\01-google.h2.sql
 
cd ..
mvn -Dcodegen clean install