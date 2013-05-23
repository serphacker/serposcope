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
        );
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
        
        foreach ($group['keywords'] as $keyKW => $keyword) {
            
            $proxy=$proxies->next();
            
            $this->l("Checking $keyword on $domain");
            $pos=1;
            $start_index=0;

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
                    $curl=curl_init();
                    $opts = array(CURLOPT_URL => $url,CURLOPT_REFERER => $referrer) + buildCurlOptions($proxy);
                    curl_setopt_array($curl,$opts);
                    
                    $this->d("GET $url via ".($proxy == null ? "DIRECT" : proxyToString($proxy))." (try: $fetchRetry) (mem: ".  debug_memory().")");
                    $curlout=curl_cache_exec($curl);
                    $this->d("GOT status=".$curlout['status']." cache=".($curlout['cache'] ? "HIT" : "MISS")." age=".$curlout['cache_age']." (mem: ".  debug_memory().")");
                    $data=$curlout['data'];
                    $http_status = $curlout['status'];
                    
                    switch($http_status){
                        case 302:{
                            if($fetchRetry <= 3){
                                // if rate limit, sleep
                                $rateLimitSleepTime = intval($options[get_class($this)]['captcha_basesleep']);
                                $this->w("rate limit detected (captcha), retry $fetchRetry, sleeping $rateLimitSleepTime seconds");
                                sleep($rateLimitSleepTime);
                                
                                // and change proxy
                                $proxy=$proxies->next();
                                $this->w("switching to proxy ".($proxy == null ? "DIRECT" : proxyToString($proxy)));
                                
                                
                            }else{
                                $this->e("rate limit detected (captcha), after $fetchRetry retry");
                                $ranks['__have_error'] = 1;
                                return $ranks;
                            }
                            break;
                        }
                        
                        case 200:{
                            break;
                        }
                        
                        case 0:{
                            $this->e("Curl error ".  curl_error($curl));
                            $ranks['__have_error'] = 1;
                            return $ranks;
                        }
                        
                        default:{
                            $this->e("Bad retcode ".$http_status);
                            $ranks['__have_error'] = 1;
                            return $ranks;
                        }
                    }
                    
                    $fetchRetry++;
                }while($http_status != 200);
                
//                curl_setopt_array($curl,$opts);
//                $data=curl_exec($curl);
//                $http_status = curl_getinfo($curl, CURLINFO_HTTP_CODE);
//                
//                if($http_status != 200){
//                    if($http_status == 302){
//                        
//                    }else{
//                        $this->e("Bad retcode from google ".$http_status);
//                        return null;
//                    }
//                }                
                

                
                $doc = new DOMDocument;
                if(!@$doc->loadHTML($data)){
                    $this->e("Can't parse HTML");
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
            
            // trigger a bug
            $this->e("Forced bug");
            $ranks['__have_error'] = 1;
            return $ranks;
            $this->incrementProgressBarUnit();
        }
        
        curl_close($curl);
        return $ranks;
    }

}

?>