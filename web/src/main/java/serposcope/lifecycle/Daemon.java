/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.lifecycle;


public class Daemon {
    
    
    public static void main(String[] args) {
        start(args);
    }
    public static void start(String[] args) {
        System.out.println("starting serposcope service");
        new MyNinjaJetty().run();
    }
    public static void stop(String[] args){
        System.out.println("stopping serposcope service");
        System.exit(0);
    }
}
