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
function proxyToString($proxy){
    
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
        CURLOPT_DNS_USE_GLOBAL_CACHE => false,
        CURLOPT_TIMEOUT => $options['general']['timeout'],
        CURLOPT_AUTOREFERER => true,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_USERAGENT => "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.".rand(10000,20000)." .NET CLR 3.5.".rand(10000,20000).")",
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
        $wmi=new COM("winmgmts:{impersonationLevel=impersonate}!\\\\.\\root\\cimv2");
        $procs=$wmi->ExecQuery("SELECT * FROM Win32_Process WHERE ProcessId='".$pid."'");
        return !empty($procs);
    } else {
        return posix_getsid($pid) !== FALSE;
    }    
}

$generalOptions = array(
//    array('home_top_limit','15','Rank limit in the "TOP X change" home display','/^[0-9+]+$/','text'),
    array('timeout','20','Maximum HTTP request execution time','/^[0-9]+$/','text'),
    array('home_unchanged','yes','Display unchanged position on home','/^yes|no$/','yesno'),
    array('rendering','highcharts','Possible values : highcharts,table','/^highcharts|table$/','text'),
//    array('proxies',null,'Http proxy IP:PORT format, 1 per line.<br/>To mix direct connection with proxy, use DIRECT.','/^[0-9+]+$/','textarea'),
);
$options=array();

function load_options(){
    global $modules,$generalOptions,$options;
    
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
    
    $res=mysql_query("SELECT * FROM `".SQL_PREFIX."option`");
    while($res != null && ($option=mysql_fetch_assoc($res))){
        $exploded=explode("_",$option['name']);
        $modulename = array_shift($exploded);
        $optionname = implode("_", $exploded);
        $options[$modulename][$optionname] = $option['value'];
    }    

    return $options;
}

$proxies=array();
function load_proxies(){
    global $proxies;
    
    $proxies=array();
    $result = mysql_query("SELECT * FROM `".SQL_PREFIX."proxy`");
    while($proxy=  mysql_fetch_assoc($result)){
        $proxies[]=$proxy;
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

?>