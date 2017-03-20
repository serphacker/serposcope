/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.base;


public class Group {
    
    public enum Module {
        GOOGLE,
        TWITTER,
        GITHUB;
        
        public static Module getByOrdinal(Integer moduleId){
            if(moduleId == null || moduleId < 0 || moduleId >= Module.values().length ){
                return null;
            }

            return values()[moduleId];
        }
    }

    int id;
    Module module;
    String name;

    public Group(int id, Module module, String name) {
        this.id = id;
        this.module = module;
        this.name = name;
    }

    public Group(Module module, String name) {
        this.module = module;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Group other = (Group) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
    
}
