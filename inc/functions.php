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
// because PHP < 5.3.0 doesn't handle JSON_HEX_TAG
function json_encode_tag($string){
    return str_replace("<", "\\u003C", 
        str_replace(">", "\\u003E", 
            json_encode($string)
        )
    );
}


function proxyToString($proxy){
    
    if(!is_array($proxy)){
        return $proxy;
    }
    
    $string = $proxy['type']."://";
    if(!empty($proxy['user'])){
        $string .= $proxy['user'];
    }
    if(!empty($proxy['password'])){
        $string .= ":".$proxy['password'];
    }
    if(!empty($proxy['user']) || !empty($proxy['password'])){
        $string .= "@";
    }
    
    $string .= $proxy['ip'];
    if(!empty($proxy['port'])){
        $string .= ":".$proxy['port'];
    }
    
    $string .= "/";
    
    return $string;
}

function buildCurlOptions($proxy){
    global $options;
    
    $opts=array(
        CURLOPT_COOKIEJAR => COOKIE_PATH,
        CURLOPT_COOKIEFILE => COOKIE_PATH,
        CURLOPT_DNS_USE_GLOBAL_CACHE => false,
        CURLOPT_TIMEOUT => (isset($options['general']['timeout']) ? $options['general']['timeout'] : 30 ),
        CURLOPT_AUTOREFERER => true,
        CURLOPT_RETURNTRANSFER => true,
//        CURLOPT_USERAGENT => "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.".rand(10000,20000)." .NET CLR 3.5.".rand(10000,20000).")",
        CURLOPT_USERAGENT => "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)",
        CURLINFO_HEADER_OUT => true,
        CURLOPT_HTTPHEADER => array(
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7"
        )
    );
    
    if(is_array($proxy)){
        
        switch($proxy['type']){
            case "socks":
            case "http":
                $opts[CURLOPT_PROXYTYPE] = $proxy['type'] === "http" ? CURLPROXY_HTTP : CURLPROXY_SOCKS5;
                $opts[CURLOPT_PROXY] = $proxy['ip'].":".$proxy['port'];

                if(!empty($proxy['user'])){
                    $opts[CURLOPT_PROXYUSERPWD] = $proxy['user'];
                }

                if(!empty($proxy['password'])){
                    if(empty($opts[CURLOPT_PROXYUSERPWD])){
                        $opts[CURLOPT_PROXYUSERPWD] = "";
                    }
                    $opts[CURLOPT_PROXYUSERPWD] .= ":".$proxy['password'];
                }            

                break;

            case "iface":
                $opts[CURLOPT_INTERFACE] = $proxy['ip'];
                if(strstr($proxy['ip'],":") === FALSE){
                    $opts[CURLOPT_IPRESOLVE] = CURL_IPRESOLVE_V4;
                }else{
                    $opts[CURLOPT_IPRESOLVE] = CURL_IPRESOLVE_V6;
                }
    //                        '',
    //            'CURL_IPRESOLVE_V4'
                break;
        }
    }
    
    return $opts;
}

function h8($string){
    return htmlentities($string, ENT_QUOTES, "UTF-8");
}

function wd_wildcard_to_preg($pattern){
    return '/^' . str_replace(array('\*'), array('.*'), preg_quote($pattern)) . '$/';
}

function win_kill($pid){
    if(!class_exists("COM")){
        return false;
    }
    $ret=false;
    $wmi=new COM("winmgmts:{impersonationLevel=impersonate}!\\\\.\\root\\cimv2");
    $procs=$wmi->ExecQuery("SELECT * FROM Win32_Process WHERE ProcessId='".$pid."'");
    foreach($procs as $proc){
      $proc->Terminate();    
      $ret=true;
    }
    return $ret;
}

function portable_kill($pid){
    if (strtoupper(substr(PHP_OS, 0, 3)) === 'WIN') {
        return win_kill($pid);
    } else {
        return posix_kill($pid, 9);
    }    
}

function is_pid_alive($pid){
    if (strtoupper(substr(PHP_OS, 0, 3)) === 'WIN') {
        if(!class_exists("COM")){
            return true;
        }
        $wmi=new COM("winmgmts:{impersonationLevel=impersonate}!\\\\.\\root\\cimv2");
        $procs=$wmi->ExecQuery("SELECT * FROM Win32_Process WHERE ProcessId='".$pid."'");        
        return $procs && ($procs->Count !== 0);
    } else {
        return posix_getsid($pid) !== FALSE;
    }    
}

$generalOptions = array(
//    array('home_top_limit','15','Rank limit in the "TOP X change" home display','/^[0-9+]+$/','text'),
    array('timeout','20','Maximum HTTP request execution time','/^[0-9]+$/','text'),
    array('fetch_retry','10','Maximum GET retry on HTTP error / captcha','/^[0-9]+$/','text'),
    array('rm_bad_proxies','2','Remove bad proxies after X fails, 0 to never remove bad proxy','/^[0-9]+$/','text'),
    array('proxy_auto_rotate','yes','Rotate the proxy on new keyword','/^yes|no$/','yesno'),
    
    array('cache_lifetime','8','Maximum age of the cache in hour','/^[0-9]+$/','text'),
    array('cache_run_clear','yes','Clear the cache after each run even if lifetime isn\'t expired','/^yes|no$/','yesno'),
    array('home_unchanged','yes','Display unchanged position on home','/^yes|no$/','yesno'),
    array('serp_volatility','yes','Display serp volatility on home','/^yes|no$/','yesno'),
    array('rendering','highcharts','Possible values : highcharts,table','/^highcharts|table$/','text'),
    array('debug_log','no','Enable debug logging','/^yes|no$/','yesno'),
//    array('proxies',null,'Http proxy IP:PORT format, 1 per line.<br/>To mix direct connection with proxy, use DIRECT.','/^[0-9+]+$/','textarea'),
    // hiddens options
    array('proxies_list_url','','',''), 
    
);
$options=array();

function load_options(){
    global $modules,$generalOptions,$options,$db;
    
    $options = array();
    
    $options['general'] = array();
    foreach ($generalOptions as $option) {
        $options['general'][$option[0]] = $option[1];
    }
    
    foreach ($modules as $moduleName => $module) {
        $options[$moduleName] = array();
        $moduleGlobalOptions = $module->getGlobalOptions();
        if(is_array($moduleGlobalOptions)){
            foreach ($moduleGlobalOptions as $option) {
                $options[$moduleName][$option[0]] = $option[1];
            }
        }
    }
    
    $res=$db->query("SELECT * FROM `".SQL_PREFIX."option`");
    while($res != null && ($option=mysql_fetch_assoc($res))){
        $exploded=explode("_",$option['name']);
        $modulename = array_shift($exploded);
        $optionname = implode("_", $exploded);
        $options[$modulename][$optionname] = $option['value'];
    }    

    return $options;
}

function load_proxies(){
    global $db;
    
    $proxies=array();
    $result = $db->query("SELECT * FROM `".SQL_PREFIX."proxy`");
//    die("x => ".$result);
    while($proxy=  mysql_fetch_assoc($result)){
        $proxies[]=$proxy;
    }
    return $proxies;
}

function load_proxies_url($url){
    $proxies=array();
    $curl=curl_init();
    $opts = array(CURLOPT_URL => $url)  + buildCurlOptions(null);
    curl_setopt_array($curl, $opts);
    $data = curl_cache_exec($curl, false);
    
    if(!empty($data['data'])){
        $groups = array();
        preg_match_all('/[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+:[0-9]+/', $data['data'], $groups);   
        if(isset($groups[0]) && !empty($groups[0])){
            foreach ($groups[0] as $ip) {
                if(!in_array($ip, $proxies)){
                    $proxies[] = $ip;
                }
            }
        }
    }
    return $proxies;
}

// standard logging
function l($src,$str){
    echo "[".date('d/m/Y h:i:s')."][$src] $str\n";
}

// error logging
$haveError=false;
function e($src,$str){
    global $haveError;
    $haveError=true;
    echo "[".date('d/m/Y h:i:s')."][$src] ERROR: $str\n";
}

function w($src,$str){
    global $options;
    echo "[".date('d/m/Y h:i:s')."][$src] WARNING: $str\n";
}

// debug logging
function d($src,$str){
    global $options;
    if($options['general']['debug_log'] === "yes"){
        echo "[".date('d/m/Y h:i:s')."][$src] DEBUG: $str\n";
    }
}

function debug_memory(){
    return (memory_get_usage(true)/(1024*1024))."M/".ini_get('memory_limit');
}

// WARNING: this shit isn't thread safe
function curl_cache_exec($ch,$usecache=true){
    global $options;
    
    $url = curl_getinfo($ch,CURLINFO_EFFECTIVE_URL);
    if($url && !empty($url)){
        $cacheFile=CACHE_DIR.sha1($url);
        if($usecache && file_exists($cacheFile)){
            
            $cache_hto =  intval(isset($options['general']['cache_lifetime']) ? $options['general']['cache_lifetime'] : 4);
//            echo "DEBUG CACHE  Now : ".time()." cacheFile: ". filemtime($cacheFile)." hto : ".($cache_hto*3600)."\n";
            
            // HIT
            if( (time() - filemtime($cacheFile)) < $cache_hto*3600){
                $cacheData = @file_get_contents($cacheFile);
                if($cacheData !== FALSE){
                    // use the cache
                    $response['cache'] = true;
                    $response['data'] = $cacheData;
                    $response['status'] = 200; // we only cache 200, no problem
                    $response['cache_age'] = time() - filemtime($cacheFile);
                }
                
                return $response;
            }
        }
        
        $data=curl_exec($ch);
        $response=array();
        $response['cache'] = false;
        $response['data'] = $data;
        $response['status'] = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $response['cache_age'] = 0;
        
        // only cache 200 OK
        if($usecache && $response['status'] == 200 && strlen($data) > 0){
            // try to cache the stuff
            if(!file_exists(CACHE_DIR)){
                @mkdir(CACHE_DIR);
            }
            @file_put_contents($cacheFile, $data);
        }
        
        return $response;
    }
    
    return null;
}

// do not forgot to clear the cache
function clear_cache($force = false){
    global $options;
    
    $cache_hto =  intval(isset($options['general']['cache_lifetime']) ? $options['general']['cache_lifetime'] : 4);
    
    $dh  = opendir(CACHE_DIR);
    while ($dh && false !== ($filename = @readdir($dh))) {
        if($filename != null){
            $cacheFile = CACHE_DIR.$filename;
            
            if(is_file($cacheFile) && ($force || (time() - filemtime($cacheFile)) > $cache_hto*3600) ){
                unlink($cacheFile);
            }
            
        }
    }
}

?>