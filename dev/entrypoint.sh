# If you want only run without compile, please comment next line

mvn -Dsinglejar=true -Dminify=true clean install
./core/scripts/sqlcodegen.sh
java -jar -Xdebug -Dserposcope.conf=web/src/main/resources/serposcope.conf ./web/target/serposcope.jar