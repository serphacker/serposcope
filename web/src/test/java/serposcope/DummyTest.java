/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import org.junit.Test;


public class DummyTest {
    public static void main(String[] args) {
        DateTimeFormatter ldtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime xxx = LocalDateTime.of(2000, 12, 20, 22, 30, 45);
        System.out.println(xxx.toString());
        System.out.println(ldtf.format(xxx));
        
    }
}
