/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package serposcope;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Singleton;

@Singleton
public class Version implements Comparable<Version> {

    private final static Pattern PATTERN = Pattern.compile("^([0-9]+)\\.([0-9]+)\\.([0-9]+)(-[A-Z]+[0-9]+)?$");
    public final static Version CURRENT = new Version("2.1.0");

    int major;
    int minor;
    int micro;
    String complement;

    public Version(int major, int minor, int micro) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
    }

    public Version(String version) throws IllegalArgumentException {
        Matcher matcher = PATTERN.matcher(version);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(version);
        }
        major = Integer.parseInt(matcher.group(1));
        minor = Integer.parseInt(matcher.group(2));
        micro = Integer.parseInt(matcher.group(3));
        if(matcher.group(4) != null){
            complement = matcher.group(4).substring(1);
        }
    }

    @Override
    public int compareTo(Version o) {
        int diff = major - o.major;
        if (diff == 0) {
            diff = minor - o.minor;
            if (diff == 0) {
                diff = micro - o.micro;
                if(diff == 0){
                    if(complement == null && o.complement != null){
                        return 1;
                    }
                    if(complement != null && o.complement == null){
                        return -1;
                    }
                    if(complement != null && o.complement != null){
                        return complement.compareTo(o.complement);
                    }
                }
            }
        }
        return diff;
    }

    @Override
    public String toString() {
        String toString = major + "." + minor + "." + micro;
        if(this.complement != null){
            toString += "-" + this.complement;
        }
        return toString;
    }
    
    public String shortString(){
        String toString = major + "." + minor + "." + micro;
        if(this.complement != null){
            toString += "-" + this.complement.replace("BETA", "B").replace("ALPHA","A");
        }
        return toString;        
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.major;
        hash = 83 * hash + this.minor;
        hash = 83 * hash + this.micro;
        hash = 83 * hash + Objects.hashCode(this.complement);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Version other = (Version) obj;
        if (this.major != other.major) {
            return false;
        }
        if (this.minor != other.minor) {
            return false;
        }
        if (this.micro != other.micro) {
            return false;
        }
        if (!Objects.equals(this.complement, other.complement)) {
            return false;
        }
        return true;
    }

    
    
    public static void main(String[] args) {
        System.out.println(Version.CURRENT);
    }
}
