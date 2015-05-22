<?php

class Proxies {
    
    private $mapFail;
    private $proxies;
    private $iProxy;
    
    function __construct($prx_array) {
        if(is_null($prx_array) || !is_array($prx_array) || empty($prx_array) ){
            $this->proxies = array("DIRECT");
            $this->mapFail[proxyToString($proxy)] = 0;
        }else{
            $this->mapFail = array();
            $this->proxies = array();
            foreach ($prx_array as $proxy) {
                if( !isset($this->mapFail[proxyToString($proxy)] ) ){
                    $this->mapFail[proxyToString($proxy)] = 0;
                    $this->proxies[] = $proxy;
                }
                
            }
        }
        $this->iProxy=rand(0, count($this->proxies)-1);
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
    
    public function ban($proxy){
        global $options;
        $this->mapFail[proxyToString($proxy)] = intval($options['general']['rm_bad_proxies']);
    }

    public function preventfail($proxy){
        return --$this->mapFail[proxyToString($proxy)];
    }
    
    public function fail($proxy){
        return ++$this->mapFail[proxyToString($proxy)];
    }
    
    public function success($proxy){
        $this->mapFail[proxyToString($proxy)] = 0;
    }
    
    public function remove($proxy){
        unset($this->mapFail[proxyToString($proxy)]);
        for($i=0;$i<count($this->proxies);$i++){
            if(proxyToString($this->proxies[$i]) === proxyToString($proxy)){
                $indexRemove=$i;
            }
        }
        
        if($indexRemove != -1){
            unset($this->proxies[$indexRemove]);
            $this->proxies= array_values($this->proxies);
            if($indexRemove <= $this->iProxy){
                --$this->iProxy;
            }
        }
    }
    
    public function getMapFail(){
        return $this->mapFail;
    }
    
}
?>