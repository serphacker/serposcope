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
}
