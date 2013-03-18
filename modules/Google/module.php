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
        );
    }
   
    public function check($group) {
        global $options;
        global $proxies;
        global $iProxy;
        
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
            
            $proxy=null;
            if($proxies != null && is_array($proxies) && !empty($proxies)){
                if(++$iProxy >= count($proxies)){
                    $iProxy=0;
                }
                $proxy = $proxies[$iProxy];
            }              
            
            $this->l('Checking '.$keyword." on $domain via ".($proxy == null ? "DIRECT" : proxyToString($proxy)));
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
            
                $opts = array(
                    CURLOPT_URL => $url,
                    CURLOPT_REFERER => $referrer
                ) + buildCurlOptions($proxy);
                
//                print_r($opts);
                $fetchRetry=1;
                
                do {
                    $curl=curl_init();
                    curl_setopt_array($curl,$opts);
                    
//                    $this->e("GET $url");
                    $data=curl_exec($curl);
                    $http_status = curl_getinfo($curl, CURLINFO_HTTP_CODE);
                    
                    switch($http_status){
                        case 302:{
                            if($fetchRetry <= 3){
                                $rateLimitSleepTime = intval($options[get_class($this)]['captcha_basesleep']);
                                $this->e("rate limit detected (captcha), retry $fetchRetry, sleeping $rateLimitSleepTime seconds");
                                sleep($rateLimitSleepTime);
                            }else{
                                $this->e("rate limit detected (captcha), after $fetchRetry retry");
                                return null;
                            }
                            break;
                        }
                        
                        case 200:{
                            break;
                        }
                        
                        case 0:{
                            $this->e("Curl error ".  curl_error($curl));
                            return null;
                        }
                        
                        default:{
                            $this->e("Bad retcode ".$http_status);
                            return null;
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
                    return null;
                }
                $allh3 = $doc->getElementsByTagName('h3');
                
                
                foreach($allh3 as $h3){
                    if(!$h3->hasAttribute("style") && $h3->getAttribute("class") == "r"){
                        try {
                            $href = $h3->getElementsByTagName('a')->item(0)->getAttribute('href');
                            $parsed = parse_url($href);
                            if(isset($parsed['host'])){
                                
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
        }
        
        curl_close($curl);
        return $ranks;
    }

}

?>