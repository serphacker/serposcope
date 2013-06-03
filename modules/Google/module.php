<?php
/**
 * Serposcope - An open source rank checker for SEO
 * http://serphacker.com/serposcope/
 * 
 * @link http://serphacker.com/serposcope Serposcope
 * @author SERP Hacker <pierre@serphacker.com>
 * @license http://creativecommons.org/licenses/by-nc-sa/3.0/legalcode CC-BY-NC-SA
 * 
 * Redistributions of files must retain the above notice.
 */
if(!defined('INCLUDE_OK'))
    die();

class Google extends GroupModule {
    
    public function getGlobalOptions() {
        return array(
            array(
                'page_sleep',
                '30',
                'pause in seconds between request to google',
                '/^[0-9]+$/',
                'text'
            ),
            array(
                'captcha_basesleep',
                '360',
                'pause in seconds if captcha',
                '/^[0-9]+$/',
                'text'
            ),            
        );
    }
    
    public function getGroupOptions() {
        return array(
            array(
                'tld',
                'fr',
                'The google search engine top level domain: google.<strong>com</strong>, google.<strong>co.uk</strong>',
                '/^[a-zA-Z.]+$/',
                'text'
            ),
            array(
                'datacenter',
                '',
                'A specific datacenter. Leave empty to use standard google.tld',
                '/^(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}|)$/',
                'text'
            ),
            array(
                'parameters',
                '',
                'Additional parameters in the request (like <strong>hl=fr&tbs=qdr:d</strong>)',
                '/^.*$/',
                'text'
            ),
            array(
                'local',
                '',
                'City or place for local search, should be in the country of the tld',
                '/^.*$/',
                'text'
            ),            
        );
    }
    
    private function init_session($domain, $proxy, $local){
        $url = "http://".$domain."/preferences";
        
        $opts = array(
            CURLOPT_URL => $url
        ) + buildCurlOptions($proxy);
        
        $curl=curl_init();
        curl_setopt_array($curl,$opts);
        $data = curl_cache_exec($curl, false);
        curl_close($curl);
        
        if(empty($local)){
            $this->d("local search not used");
            return $curl;
        }
        
        
//        echo $data['data'];
        
        $matches = array();
        //if( !isset($data['data'])  || !preg_match("|/&amp;sig=([^&\"']+)[&\"']|",$data['data'],$matches)  ){
        if( !isset($data['data'])  || !preg_match('|<input value="([^"]+)" type="hidden" name="sig">|',$data['data'],$matches)  ){
            $this->e("can't extract session, local search disabled ");
//            echo $data['data'];
            return $curl;
        }
        
        $prev = $data['data'];
        
        $url = "http://".$domain."/uul?muul=4_18&luul=".urlencode($local)."&uulo=1&usg=".$matches[1]."&hl=en";
        $opts = array(
            CURLOPT_URL => $url
        ) + buildCurlOptions($proxy);
        $curl=curl_init();
        curl_setopt_array($curl,$opts);
        $data = curl_cache_exec($curl, false);
        curl_close($curl);
        
        if( !isset($data['data']) || !empty($data['data'])  ){
            
            $this->e("Can't set location ".$local." : '".str_replace("\n"," ",substr($data['data'],0,128))."'");
            $this->d("URL used for location ".$url);
//            echo $prev;
            return $curl;
        }
        
        $this->d("local search on ".$local);
        return $curl;
    }
   
    public function check($group) {
        global $options;
        global $proxies;
        
        $ranks =  array();
        
        $domain = "";
        
        if(!empty($group['options']['datacenter'])){
            $domain = $group['options']['datacenter'];
        }else if(!empty($group['options']['tld'])){
            $domain = "www.google.".$group['options']['tld'];
        }else {
            $domain = "www.google.com";
        }        
        
        
        $curl = null;
        foreach ($group['keywords'] as $keyKW => $keyword) {
            
            if($options['general']['proxy_auto_rotate'] === "yes"){
                $proxy=$proxies->next();
                if($proxy == null){
                    $this->e("No more valid proxy, aborting");
                    $ranks['__have_error'] = 1;
                    return $ranks;
                }else{
                    $this->l("Switched to proxy ".proxyToString($proxy));
                }
            }else{
                $proxy=$proxies->current();
                $this->l("Using current proxy ".proxyToString($proxy));
            }
            
            

            
            $this->l("Checking $keyword on $domain");
            $pos=1;
            $start_index=0;
            
            // init a new session
            @unlink(COOKIE_PATH);
            $this->init_session($domain, $proxy, !empty($group['options']['local']) ? $group['options']['local'] : null);

            do{
                
                if($start_index==0){
                    $url="http://$domain/search?q=".urlencode($keyword);
                    $referrer= "http://$domain/";                    
                }else{
                    $referrer=$url;
                    $url="http://$domain/search?q=".urlencode($keyword)."&start=".($start_index);
                }
                
                if(!empty($group['options']['parameters'])){
                    $url .= "&".$group['options']['parameters'];
                }
//                print_r($opts);
                $fetchRetry=1;
                
                do {
                    $opts = array(CURLOPT_URL => $url, CURLOPT_REFERER => $referrer) + buildCurlOptions($proxy);
                    $curl = curl_init();
                    curl_setopt_array($curl,$opts);
                    
                    $this->d("GET $url via ".proxyToString($proxy)." (try: $fetchRetry) (mem: ".  debug_memory().")");
                    $curlout=curl_cache_exec($curl, empty($group['options']['local'])); // don't use cache if local search
                    $this->d("GOT status=".$curlout['status']." cache=".($curlout['cache'] ? "HIT" : "MISS")." age=".$curlout['cache_age']." (mem: ".  debug_memory().")");
                    $curl_error=curl_error($curl);
                    curl_close($curl);
                    
                    $data=$curlout['data'];
                    $http_status = $curlout['status'];
                    
                    $error=false;
                    $doc = new DOMDocument;
                    switch($http_status){
                        case 200:{
                            break;
                        }                        
                        
                        case 302:{
                            $rateLimitSleepTime = intval($options[get_class($this)]['captcha_basesleep']);
                            $this->w("Google captcha, sleeping $rateLimitSleepTime seconds");
                            sleep($rateLimitSleepTime);
                            $error=true;
                            break;
                        }
                        
                        case 0:{
                            $this->w("Curl error ".  $curl_error);
                            $error=true;
                            break;
                        }
                        
                        default:{
                            $this->w("Bad retcode ".$http_status);
                            $error=true;
                            break;
                        }
                    }
                    
                    if(!$error){
                        // is it really google
                        if(strstr($data, "window.google=") === FALSE){
                            $this->w("Not a valid google SERP");
                            $error=true;
                        }
                    }
                    
                    if(!$error){
                        if(!@$doc->loadHTML($data)){
                            $this->w("Can't parse HTML");
                            $error=true;
                        }
                    }
                    
                    if($error){
                        $nPrxCsfFail = $proxies->fail($proxy);
                        if( 
                            intval($options['general']['rm_bad_proxies']) > 0 && 
                            $nPrxCsfFail >= intval($options['general']['rm_bad_proxies'])  
                        ){
                            $this->w("Removing proxy ".proxyToString($proxy)." after ".$nPrxCsfFail." consecutives fails");
                            $proxies->remove($proxy);
                        }
                        $proxy=$proxies->next();
                        if($proxy == null){
                            $this->e("No more valid proxy, aborting");
                            $ranks['__have_error'] = 1;
                            return $ranks;
                        }
                        $this->w("Previous proxy failed, switched to proxy ".proxyToString($proxy));
                    }else{
                        $proxies->success($proxy);
                    }
                    
                    ++$fetchRetry;
                    
                }while($error && $fetchRetry <= intval($options['general']['fetch_retry']));
                
                if($error){
                    $this->e("Too many consecutive fail ($fetchRetry), aborting");
                    $ranks['__have_error'] = 1;
                    return $ranks;
                }
                
                $allh3 = $doc->getElementsByTagName('h3');
                
                foreach($allh3 as $h3){
                    if(!$h3->hasAttribute("style") && $h3->getAttribute("class") == "r"){
                        try {
                            $h3_a=$h3->getElementsByTagName('a');
                            if($h3_a == null || $h3_a->length == 0){
                                continue;
                            }
                            $href = $h3_a->item(0)->getAttribute('href');
                            $parsed = @parse_url($href);
                            if($parsed !== FALSE && isset($parsed['host'])){
                                
                                foreach ($group['sites'] as $keySite => $website) {
                                    
                                    // if we already have a rank for this keyword, continue
                                    if(isset($ranks[$keyKW][$keySite]))
                                        continue;
                                    
                                    // wildcard support
                                    $regex = wd_wildcard_to_preg($website);
                                    
                                    if(preg_match($regex, $parsed['host'])){
                                        $ranks[$keyKW][$keySite][0]= $pos;
                                        $ranks[$keyKW][$keySite][1]= $href;
                                        
                                        $this->l("Rank[$pos] [$website] ".$href);
                                    }
                                }                                
                                $pos++;
                            }
                        }catch(Exception $e){
                            $this->e("Parsing error (unexpected bug)");
                        }
                    }
                }
                
                $bAllWebsiteFound=true;
                foreach ($group['sites'] as $keySite => $website) {
                    if(!isset($ranks[$keyKW][$keySite])){
                        $bAllWebsiteFound=false;
                    }
                }   

                $start_index += 10;
                sleep($options[get_class($this)]['page_sleep']);
               
            }while($start_index<100 && !$bAllWebsiteFound);
            
            $this->incrementProgressBarUnit();
        }
        
        return $ranks;
    }

}

?>