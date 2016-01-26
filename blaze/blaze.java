import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Contexts.fail;
import static com.fizzed.blaze.Contexts.withBaseDir;
import com.fizzed.blaze.Systems;
import static com.fizzed.blaze.util.Globber.globber;
import java.nio.file.Path;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.h2.tools.RunScript;

public class blaze {
    static private final Logger logger = Contexts.logger();
    
    public void initdb() throws Exception {
        logger.info("Initializing db...");
        
        Path databaseDir = withBaseDir("../core/src/main/resources/db").normalize();
        logger.info("Using script dir: {}", databaseDir);
        
        Path codegenDir = withBaseDir("../core/codegen").normalize();
        logger.info("Using codegen dir: {}", codegenDir);
        
        Path h2File = codegenDir.resolve("h2");
        
        // remove any previous dbs
        Systems.remove(globber(codegenDir, "*"));
        
        globber(databaseDir, "*.sql").stream().sorted().forEach((scriptFile) -> {
            logger.info("Applying script: {}", scriptFile);
            try {
                RunScript.main("-url", "jdbc:h2:" + h2File.toAbsolutePath(), "-script", scriptFile.toAbsolutePath().toString());
            } catch (SQLException e) {
                logger.error("{}", e);
                fail(e.getMessage());
            }
        });
    }

}
