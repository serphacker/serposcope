/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.http.proxy;

import java.util.Objects;


public class SocksProxy extends ScrapProxyWithContext {
    String ip;
    int port;
    String username;
    String password;

    public SocksProxy(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public SocksProxy(String ip, int port, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        String str = "proxy:socks://";
        if(username != null || password != null){
            str += username + ":" + password + "@";
        }
        str += ip + ":" + port + "/";
        return str;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.ip);
        hash = 71 * hash + this.port;
        hash = 71 * hash + Objects.hashCode(this.username);
        hash = 71 * hash + Objects.hashCode(this.password);
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
        final SocksProxy other = (SocksProxy) obj;
        if (this.port != other.port) {
            return false;
        }
        if (!Objects.equals(this.ip, other.ip)) {
            return false;
        }
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        return true;
    }
    
    
    
    public int compareHost(SocksProxy p){
        if (ip == null ^ p.ip == null) {
            return (ip == null) ? -1 : 1;
        }

        if (ip == null && p.ip == null) {
            return 0;
        }
        
        int compareTo = ip.compareTo(p.ip);
        if(compareTo != 0){
            return compareTo;
        }
        
        return Integer.compare(port, p.port);
    }
    
    public boolean match(SocksProxy p){
        return match(p.ip,  p.port);
    }
    
    public boolean match(String ip, int port){
        return Objects.equals(this.ip, ip) && this.port == port;
    }
    
}
