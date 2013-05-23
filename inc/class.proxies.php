<?php

class Proxies {
    
    private $proxies;
    private $iProxy;
    
    function __construct($prx_array) {
        $this->proxies = $prx_array;
        $this->iProxy= (is_null($prx_array) || count($prx_array) <= 1) ? 0 : rand(0, count($prx_array)-1);
    }
    
    public function current(){
        if($this->proxies == null || empty($this->proxies)){
            return null;
        }
        
        return $this->proxies[$this->iProxy];
    }
    
    public function next(){
        if($this->proxies == null || empty($this->proxies)){
            return null;
        }        
        
        if(++$this->iProxy >= count($this->proxies)){
            $this->iProxy=0;
        }
        
        return $this->proxies[$this->iProxy];
    }
    
    public function get(){
        return $this->proxies;
    }
    
    
}

?>