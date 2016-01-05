/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.controllers.admin;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class LogControllerTest {
    
    public LogControllerTest() {
    }

    @Test
    public void testSort() {
        Map<String,String> map = new HashMap<>();
        map.put("aaa","1");
        map.put("bb","2");
        map.put("c","3");
        map.put("111","1");
        map.put("22","2");
        map.put("3","3");        
        LogController ctrl = new LogController();
        System.out.println(ctrl.getSortedMapByReverseLength(map));
    }
    
}
