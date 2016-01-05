/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;

public class Validator {

    
    static Pattern patternEmail = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$");
    public static boolean isEmailAddress(String email) {
        if(email == null || email.isEmpty() || email.length() > 254){
            return false;
        }
        Matcher m = patternEmail.matcher(email);
        return m.matches();
    }
    
    static Pattern patternGenericName = Pattern.compile("^(\\w|\\d| )+$", UNICODE_CHARACTER_CLASS);
    public static boolean isGenericName(String value){
        if(value == null || value.trim().isEmpty()){
            return false;
        }
        return patternGenericName.matcher(value).matches();
    }
    
//    static Pattern patternGenericName = Pattern.compile("^(\\w|\\d| )+$", UNICODE_CHARACTER_CLASS);
//    public static boolean isGenericName(String value){
//        if(value == null || value.trim().isEmpty()){
//            return false;
//        }
//        return patternGenericName.matcher(value).matches();
//    }    
    
    public static boolean isInt(String value){
        if(value == null || value.isEmpty()){
            return false;
        }
        
        for (int i = 0; i < value.length(); i++) {
            if(value.charAt(i) < '0' || value.charAt(i) > '9'  ){
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean isUrl(String url){
        if(url == null){
            return false;
        }
        
        return url.startsWith("https://") || url.startsWith("http://");
    }
    
    public static boolean isEmpty(String url){
        return url == null || url.isEmpty();
    }
    
    public static boolean isNotEmpty(String url){
        return !isEmpty(url);
    }    
    
    final static Pattern patternIPv4 = Pattern.compile("^[0-9.]+$");
    public static boolean isIPv4(String ip){
        return patternIPv4.matcher(ip).find();
    }
    
    public static boolean isGoogleTLD(String tld){
        return tld != null && googleTLD.contains(tld);
    }
    
    
    
    final static List<String> googleTLD = Arrays.asList("com","ad","ae","com.af","com.ag","com.ai","al","am","co.ao","com.ar","as","at","com.au","az","ba","com.bd","be","bf","bg","com.bh","bi","bj","com.bn","com.bo","com.br","bs","bt","co.bw","by","com.bz","ca","cd","cf","cg","ch","ci","co.ck","cl","cm","cn","com.co","co.cr","com.cu","cv","com.cy","cz","de","dj","dk","dm","com.do","dz","com.ec","ee","com.eg","es","com.et","fi","com.fj","fm","fr","ga","ge","gg","com.gh","com.gi","gl","gm","gp","gr","com.gt","gy","com.hk","hn","hr","ht","hu","co.id","ie","co.il","im","co.in","iq","is","it","je","com.jm","jo","co.jp","co.ke","com.kh","ki","kg","co.kr","com.kw","kz","la","com.lb","li","lk","co.ls","lt","lu","lv","com.ly","co.ma","md","me","mg","mk","ml","com.mm","mn","ms","com.mt","mu","mv","mw","com.mx","com.my","co.mz","com.na","com.nf","com.ng","com.ni","ne","nl","no","com.np","nr","nu","co.nz","com.om","com.pa","com.pe","com.pg","com.ph","com.pk","pl","pn","com.pr","ps","pt","com.py","com.qa","ro","ru","rw","com.sa","com.sb","sc","se","com.sg","sh","si","sk","com.sl","sn","so","sm","sr","st","com.sv","td","tg","co.th","com.tj","tk","tl","tm","tn","to","com.tr","tt","com.tw","co.tz","com.ua","co.ug","co.uk","com.uy","co.uz","com.vc","co.ve","vg","co.vi","com.vn","vu","ws","rs","co.za","co.zm","co.zw","cat");
    
}
