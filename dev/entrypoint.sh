# If you want only run without compile, please comment next line

./core/scripts/sqlcodegen.sh
mvn -Dsinglejar=true -Dminify=true clean install
java -jar -Xdebug -Dserposcope.conf=web/src/main/resources/serposcope.conf ./web/target/serposcope.jar