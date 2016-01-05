/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.base;

import static com.serphacker.serposcope.models.base.Proxy.Status.UNCHECKED;
import com.serphacker.serposcope.scraper.http.proxy.BindProxy;
import com.serphacker.serposcope.scraper.http.proxy.HttpProxy;
import com.serphacker.serposcope.scraper.http.proxy.ScrapProxy;
import java.time.LocalDateTime;
import java.util.Objects;


public class Proxy {
    
    public enum Status {
        UNCHECKED,
        OK,
        ERROR
    }
    
    public enum Type {
        BIND,
        HTTP
    }

    public Proxy() {
    }

    public Proxy(int id, Type type, String ip, int port, String username, String password) {
        this.id = id;
        this.type = type;
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.status = UNCHECKED;
    }
    
    public Proxy(ScrapProxy proxy) {
        if(proxy instanceof BindProxy){
            type = Type.BIND;
            ip = ((BindProxy) proxy).ip;
        } else if(proxy instanceof HttpProxy){
            type = Type.HTTP;
            ip = ((HttpProxy) proxy).getIp();
            port = ((HttpProxy) proxy).getPort();
            username = ((HttpProxy) proxy).getUsername();
            password = ((HttpProxy) proxy).getPassword();
        } else {
            throw new IllegalStateException("unsupported proxy type");
        }
        this.status = UNCHECKED;
    }    

    public Proxy(int id, Type type, String ip, int port, String username, String password, String remoteip, LocalDateTime lastCheck, Status status) {
        this.id = id;
        this.type = type;
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.remoteip = remoteip;
        this.lastCheck = lastCheck;
        this.status = status;
    }
    
    int id;
    Type type;
    String ip;
    int port;
    String username;
    String password;
    
    String remoteip;
    LocalDateTime lastCheck;
    Status status = UNCHECKED;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

    public String getRemoteip() {
        return remoteip;
    }

    public void setRemoteip(String remoteip) {
        this.remoteip = remoteip;
    }

    public LocalDateTime getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(LocalDateTime lastCheck) {
        this.lastCheck = lastCheck;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    
    public ScrapProxy toScrapProxy(){
        switch(type){
            case HTTP:
                return new HttpProxy(ip, port, username, password);
                
            case BIND:
                return new BindProxy(ip);
            
            default:
                throw new UnsupportedOperationException();
        }
    }
    
//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 47 * hash + this.id;
//        hash = 47 * hash + Objects.hashCode(this.type);
//        hash = 47 * hash + Objects.hashCode(this.ip);
//        hash = 47 * hash + this.port;
//        hash = 47 * hash + Objects.hashCode(this.username);
//        hash = 47 * hash + Objects.hashCode(this.password);
//        return hash;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final Proxy other = (Proxy) obj;
//        if (this.id != other.id) {
//            return false;
//        }
//        if (this.port != other.port) {
//            return false;
//        }
//        if (!Objects.equals(this.ip, other.ip)) {
//            return false;
//        }
//        if (!Objects.equals(this.username, other.username)) {
//            return false;
//        }
//        if (!Objects.equals(this.password, other.password)) {
//            return false;
//        }
//        if (this.type != other.type) {
//            return false;
//        }
//        return true;
//    }
//    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + this.id;
        hash = 19 * hash + Objects.hashCode(this.type);
        hash = 19 * hash + Objects.hashCode(this.ip);
        hash = 19 * hash + this.port;
        hash = 19 * hash + Objects.hashCode(this.username);
        hash = 19 * hash + Objects.hashCode(this.password);
        hash = 19 * hash + Objects.hashCode(this.remoteip);
        hash = 19 * hash + Objects.hashCode(this.lastCheck);
        hash = 19 * hash + Objects.hashCode(this.status);
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
        final Proxy other = (Proxy) obj;
        if (this.id != other.id) {
            return false;
        }
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
        if (!Objects.equals(this.remoteip, other.remoteip)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.lastCheck, other.lastCheck)) {
            return false;
        }
        if (this.status != other.status) {
            return false;
        }
        return true;
    }
    
    
}
