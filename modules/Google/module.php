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
            array(
                'tld',
                'com',
                'The google search engine top level domain: google.<strong>com</strong>, google.<strong>co.uk</strong>',
                '/^[a-zA-Z.]+$/',
                'text'
            ),            
        );
    }
    
    public function getGroupOptions() {
        global $options;
        return array(
            array(
                'tld',
                $options[get_class($this)]['tld'],
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
        
        $data = curl_cache_exec(array(CURLOPT_URL => $url), $proxy, false);
        
        if(empty($local)){
            $this->d("local search not used");
            return;
        }
        
        
//        echo $data['data'];
        
        $matches = array();
        //if( !isset($data['data'])  || !preg_match("|/&amp;sig=([^&\"']+)[&\"']|",$data['data'],$matches)  ){
        if( !isset($data['data'])  || !preg_match('/<input value="([^"]+)" (type="hidden" name="sig"|name="sig" type="hidden")>/',$data['data'],$matches)  ){
            $this->e("can't extract session, local search disabled ");
            echo $data['data'];
            die();
            return;
        }
        
        $prev = $data['data'];
        
        $url = "http://".$domain."/uul?muul=4_18&luul=".urlencode($local)."&uulo=1&usg=".$matches[1]."&hl=en";
        $data = curl_cache_exec(array(CURLOPT_URL => $url), $proxy, false);
        
        if( !isset($data['data']) || !empty($data['data'])  ){
            
            $this->e("Can't set location ".$local." : '".str_replace("\n"," ",substr($data['data'],0,128))."'");
            $this->d("URL used for location ".$url);
//            echo $prev;
            return;
        }
        
        $this->d("local search on ".$local);
        return;
    }
   
    public function check($group) {
        global $options;
        global $proxies;
        global $dbc;
        global $captchaBrokenCurrentRun;
        
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
            // TODO: should be done on proxy switch too for local search
            $this->init_session($domain, $proxy, !empty($group['options']['local']) ? $group['options']['local'] : null);

            do{
                
                if($start_index==0){
                    $url="https://$domain/search?q=".urlencode($keyword);
                    $referrer= "https://$domain/";                    
                }else{
                    $referrer=$url;
                    $url="https://$domain/search?q=".urlencode($keyword)."&start=".($start_index);
                }
                
                if(!empty($group['options']['parameters'])){
                    $url .= "&".$group['options']['parameters'];
                }
//                print_r($opts);
                $fetchRetry=1;
                
                do {
                    $opts = array(CURLOPT_URL => $url, CURLOPT_REFERER => $referrer);
                    $curlout=curl_cache_exec($opts, $proxy, empty($group['options']['local'])); // don't use cache if local search
                    
                    $data=$curlout['data'];
                    $http_status = $curlout['status'];
                    
                    $error=false;
                    $doc = new DOMDocument;
                    switch($http_status){
                        case 200:{
                            break;
                        }                        
                        
                        case 302:{
                            
                            $redir = $curlout['redir'];
                            if(
//                                strncmp($redir, "http://www.google.com/sorry/?continue=", 38) !== 0 &&
//                                strncmp($redir, "https://www.google.com/sorry/?continue=", 38) !== 0
                                !strstr($redir, "/IndexRedirect?continue=")
                            ){
                                $this->w("Invalid redir"); 
                                $error = true;
                            }else{
//                                $redir = str_replace("://www.google.com/sorry/?continue=", "://".$domain."/sorry/?continue=", $redir);
                                
                                $opts = array(
                                    CURLOPT_URL => $redir,
                                    CURLOPT_REFERER => $referrer
                                );
                                $data = curl_cache_exec($opts, $proxy, false);
                                
                                if($data['status'] == 403){
                                    $proxies->ban($proxy);
                                    $this->w("IP banned from google (no captcha), force proxy remove");
                                    $error = true;
                                }else{
                                    $this->w("Google captcha");
                                    if($dbc == null){
                                        $rateLimitSleepTime = intval($options[get_class($this)]['captcha_basesleep']);
                                        $this->w("DeathByCaptcha not configured, sleeping $rateLimitSleepTime seconds");
                                        sleep($rateLimitSleepTime);
                                        $error=true;
                                    }else if($captchaBrokenCurrentRun > CAPTCHA_MAX_RUN){
                                        $rateLimitSleepTime = intval($options[get_class($this)]['captcha_basesleep']);
                                        $this->w("Broke too many captcha ($captchaBrokenCurrentRun), sleeping $rateLimitSleepTime seconds");
                                        sleep($rateLimitSleepTime);
                                        $error=true;
                                    }else{
                                        $this->w("Handling captcha with DeathByCaptcha (already solved $captchaBrokenCurrentRun captchas)");
                                        $data = $this->handleCaptcha($data['data'], $proxy, $domain, $url);
                                        if($data == null){
                                            $error = true;
                                        }
                                    }
                                }
                            }
                            
                            // go ninja go

                            break;
                        }
                        
                        case 0:{
                            $this->w("Curl error ".  $curlout['error']);
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
//                            file_put_contents("/tmp/noserp_". sha1("".time().rand(0, 10000)), $data);
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
                        rm_cache($url);
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
                        $this->init_session($domain, $proxy, !empty($group['options']['local']) ? $group['options']['local'] : null);
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
    
    private function handleCaptcha($html, $proxy, $dc, $origUrl){
        global $dbc;
        global $proxies;
        global $captchaBrokenCurrentRun;
        
        if($dbc == null){
            $this->w("DeathByCaptcha not initialized");
            return null;
        }
        
        $docCaptcha = new DOMDocument;
        if(empty($html) || !@$docCaptcha->loadHTML($html)){
            $this->w("Can't parse captcha HTML");
            return null;                                   
        }
        
        $imgsrc = null;
        $elts = $docCaptcha->getElementsByTagName("img");

        if($elts != null){
            foreach ($elts as $elt){
                $src=$elt->getAttribute("src");
                if($src != null && strncmp($src,"/sorry/image",12) === 0){
                    $imgsrc = "http://ipv4.google.com".$src;
                }
            }
        }
        
        $qValue = "";
        $elts = $docCaptcha->getElementsByTagName("input");
        if($elts != null){
            foreach ($elts as $elt){
                if($elt->getAttribute("name") === "q"){
                    $qValue = $elt->getAttribute("value");
                }
            }
        }
        
        $groupRegex=array();
        if($imgsrc == null || !preg_match("|/sorry/image\?id=([0-9]+)&?|", $imgsrc, $groupRegex)){
            $this->w("Can't extract captcha from HTML");
            return null;
        }
        
        $params = array();
        $params['id'] = $groupRegex[1];
        $params['continue'] = $origUrl;
        $params['submit']="Submit";
        $params['q'] = $qValue;

        $data = curl_cache_exec(array(CURLOPT_URL => $imgsrc), $proxy, false);

        $captcha = $data['data'];
        if($captcha == null || strlen($captcha) < 10 || ord($captcha[0]) !== 0xFF || ord($captcha[1]) !== 0xD8) {
            $this->w("Can't fetch captcha image");
            return null;
        }
        
        $time = round(microtime(true) * 1000);
        $solved =null;
        try {
            $solved =$dbc->decode(unpack('C*',$captcha), CAPTCHA_TIMEOUT);
            ++$captchaBrokenCurrentRun;
        }catch(Exception $ex){
            $solved =null;
        }
        $time = (round(microtime(true) * 1000) - $time);
        
        if($solved == null || !is_array($solved) 
            || !isset($solved['is_correct']) 
            || !isset($solved['text'])
            || !isset($solved['captcha'])   
        ){
            // not proxy fault
            $proxies->preventfail($proxy);
            $this->w("DeathByCaptcha failed breaking");
            return null;
        }
        
        $this->w("Solved captcha to '".$solved['text']."' in $time ms");
        
        $params['captcha'] = $solved['text'];
        
        $sendUrl = "http://ipv4.google.com/sorry/CaptchaRedirect?";
        foreach ($params as $key => $value) {
            $sendUrl .= $key."=".urlencode($value)."&";
        }
        $sendUrl = rtrim($sendUrl,"&");
        
        $data = curl_cache_exec(array(CURLOPT_URL => $sendUrl), $proxy, false);
        
        if(empty($data['data'])){
            $this->w("Can't send captcha answer [1]");
            return null;
        }
        
        if(strstr($data['data'],'<img src="/sorry/')){
            $this->w("Captcha incorrectly solved, report to DeathByCaptcha");
            try {
                $dbc->report($captcha['captcha']);
            }catch(Exception $ex){}
            // not proxy fault
            $proxies->preventfail($proxy);
            return null;
        }
        
        if(empty($data['redir'])){
            $this->w("Invalid redirection after captcha");
            return null;            
        }
        
        $this->l("Captcha succesfully solved");
        $data = curl_cache_exec(array(CURLOPT_URL => $data['redir']), $proxy, false);
        
        if($data['status'] == 302 && !empty($data['redir'])){
            $data = curl_cache_exec(array(CURLOPT_URL => $data['redir']), $proxy, false);
        }

        if($data['status'] != 200 ){
            $this->w("Bad redirection after captcha solving");
            print_r($data);
            return null;
        }else if(empty($data['data'])){
            $this->w("Bad content after captcha solving");
            return null;
        }else{
            return $data['data'];
        }
        
    }

}

?>