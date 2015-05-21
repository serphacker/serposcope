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
// get max execution time before everything because we try to override it later 
// and default value would be incorrect even if it's still used (suhoshin patch)
$defaultMaxExecutionTime = ini_get('max_execution_time');

include('inc/config.php');
include('inc/define.php');
include('inc/common.php');
include("lib/deathbycaptcha/deathbycaptcha.php");

if(php_sapi_name() != "cli"){
    // don't abort the script on client disconnect
    ignore_user_abort(true); 
    header('Content-type: text/plain');
}

$logs="";
$runid=null;
function my_ob_logs($str){
    global $logs;
    global $runid;
    global $db;
    
    $logs .= $str;
    if(!$db->query("UPDATE `".SQL_PREFIX."run` SET logs = '".addslashes($logs)."' WHERE idRun = ".$runid)){
        die("Critical sql error");
    }
    return $str;
}
// we will store the output in the database
ob_start('my_ob_logs',2);

// if another job is running, abort
$res=$db->query("SELECT * FROM `".SQL_PREFIX."run` WHERE ISNULL(dateStop)");
if($res && ($run=mysql_fetch_assoc($res))){
    e('Cron',"Fatal, another job is running : PID = ".$run['pid'].", dateStart = ".$run['dateStart']);
    e('Cron',"Abort the current running jobs via the \"Logs\" page");
    die();
}

$res=$db->query("INSERT INTO `".SQL_PREFIX."run`(dateStart,pid) VALUES (now(),".getmypid().")");
if(!$res || ( $runid=mysql_insert_id())==0 ){
    e('Cron',"Fatal, can't insert run in the database");
    die();
}


l('Cron','Starting cron via "'.php_sapi_name().'"');
if(ini_get('safe_mode')){
    e('Cron','safe_mode should be off, may trigger errors');
}

// we did override max_execution_time before, but it doesn't always work
// force the user to do it in php.ini
if($defaultMaxExecutionTime > 0){
    e('Cron','max_execution_time is '.$defaultMaxExecutionTime.' it should be 0. All positions may not be checked. Edit your php.ini');
}


$urls = explode("\n", $options['general']['proxies_list_url']);
$prx_list = array();
foreach ($urls as $url) {
    $url = trim($url);
    if(empty($url)){
        continue;
    }
    l('Cron',"Fetching proxies list from URL $url");
    $added=0;
    $prx_list_new = load_proxies_url($url);
    foreach ($prx_list_new as $prx_new) {
        if(!in_array($prx_new, $prx_list)){
            ++$added;
            $prx_list[] = $prx_new;
        }
    }
    l('Cron',"$added new proxies added");
}
shuffle($prx_list);

$urlproxies = array();
foreach ($prx_list as $prx_new) {
    $proproxy = explode(":",$prx_new);
    $urlproxies[] = array(
        'type' => 'http',
        'ip' => $proproxy[0],
        'port' => $proproxy[1],
        'user' => null,
        'password' => null
    );
}
$dbproxies = load_proxies();
// load the proxies
$proxies = new Proxies( $dbproxies + $urlproxies);

$argIdGroup=-1;
if(isset($_GET['idGroup'])){
    $argIdGroup=intval($_GET['idGroup']);
}
if(isset($argv[1])){
    $argIdGroup=intval($argv[1]);
}

$query = "SELECT * FROM `".SQL_PREFIX."group`"; //" WHERE idGroup = 10";
if($argIdGroup > 0){
    $query .= " WHERE idGroup =  ".$argIdGroup;
}else{
    // shuffle
    $query .= " ORDER BY rand()";
}

$currentUnit=0;
$totalUnit=0;
$allGroups = array();
$resGroup=$db->query($query);
while( $resGroup && ($row =  @mysql_fetch_assoc($resGroup)) ){
    
    if(!isset($modules[$row['module']])){
        e('Cron',"Can't find ".$row['module']." for group ".$row['name']." (uninstalled ?)");
        continue;
    }    
    
    $keywords = array();
    $qKW = "SELECT idKeyword,name FROM `".SQL_PREFIX."keyword` WHERE idGroup = ".intval($row['idGroup']);
    // shuffle keywords
    $qKW .= " ORDER BY rand()";
    $result=$db->query($qKW);
    while($kw=  mysql_fetch_assoc($result)){
        $keywords[$kw['idKeyword']] = $kw['name'];
    }
    if(empty($keywords)){
        e('Cron',"No keywords for group ".$row['name']."  ");
        continue;        
    }
    $row['keywords'] = $keywords;
    
    $sites = array();
    $qSite = "SELECT idTarget,name FROM `".SQL_PREFIX."target` WHERE idGroup = ".intval($row['idGroup']);
    $result=$db->query($qSite);
    while($site=  mysql_fetch_assoc($result)){
        $sites[$site['idTarget']] = $site['name'];
    }
    if(empty($sites)){
        e('Cron',"No site for group ".$row['name']."  ");
        continue;        
    }  
    
    $group = array();
    $group['id'] = $row['idGroup'];
    $group['name'] = $row['name'];
    $group['module'] = $row['module'];
    $group['options'] = empty($row['options']) ? array() : json_decode($row['options'],true);
    $group['keywords'] = $keywords;
    $group['sites'] = $sites;
    
    $allGroups[] = $group;
    $totalUnit += $modules[$row['module']]->getTotalProgressBarUnit($group);
}
d('Cron','display total unit '.$totalUnit);

$dbc = null;
$captchaBrokenCurrentRun=0;
if(!empty($options['general']['dbc_user']) && !empty($options['general']['dbc_pass'])){
    $balance = 0;
    try {
        $dbc = new DeathByCaptcha_SocketClient($options['general']['dbc_user'], $options['general']['dbc_pass']);
        $balance = $dbc->get_balance();
    }catch(Exception $ex){
        $balance=0;
    }
    
    if($balance == 0){
        $dbc = null;
        e('Cron','DeathByCaptcha balance is to low');
    }else{
        l('Cron','DeathByCaptcha balance = '.$balance);
    }
    
}else{
    l('Cron','DeathByCaptcha not configured, skipping');
}

l('Cron','Clearing the cache (force='.strval(CACHE_RUN_CLEAR).')...');
clear_cache(CACHE_RUN_CLEAR); 
l('Cron','Cache clear');

if(!file_exists(COOKIE_DIR)){
    @mkdir(COOKIE_DIR);
}else{
    clear_cookies();
}


foreach ($allGroups as $group) {
    
    // if everything looks OK, let's go for a run
    l('Cron','Checking group['.$group['id'].'] '.$group['name'].' with module '.$group['module']);

    // save current total unit
    
    $date = "NOW()";
    //$date = "DATE_SUB(NOW(),INTERVAL ".$interval." DAY)";
    $qRun = "INSERT INTO `".SQL_PREFIX."check`(idGroup,idRun,date) VALUES(".intval($group['id']).",".intval($runid).",".$date.")";

    $db->query($qRun);
    $id=  mysql_insert_id();
    $res = $modules[$group['module']]->check($group);
    $currentUnit += $modules[$group['module']]->getTotalProgressBarUnit($group);
    
    // update the progress bar

    if($res === null || !is_array($res)){
        e('Cron','Module returned fatal error for group['.$group['id'].'] '.$group['name']);
        continue;
    }else if(isset($res['__have_error'])){
    		n();
        e('Cron','Module have non fatal error for group['.$group['id'].'] '.$group['name'].'. All positions may have not been checked');
    }else{
    		n();
        l('Cron','Checking done for group['.$group['id'].'] '.$group['name']);
    }
    
    unset($res['__have_error']);

    $qRank = "INSERT INTO `".SQL_PREFIX."rank` VALUES ";
    foreach ($res as $idKW => $sites) {
        foreach ($sites as $idSite => $rank) {
            $qRank .= " (".
                    intval($id).",".
                    intval($idSite).",".
                    intval($idKW).",".
                    intval($rank[0]).",".
                    "'".addslashes($rank[1])."'),";
        }
    }
    $qRank = substr($qRank, 0, strlen($qRank)-1);
//    e('DEBUG', $qRank);
    $db->query($qRank);
}

l('Cron','All groups done');
if($dbc != null){
    l('Cron','Broke a total of '.$captchaBrokenCurrentRun.' captchas');
}


l('Cron','Clearing the cache (force='.strval(CACHE_RUN_CLEAR).')...');
clear_cache(CACHE_RUN_CLEAR); 
l('Cron','Cache clear');
//$logid = $group['id'].' '.$group['name'].' '.$group['module'];
//l('Cron',$logid);
ob_end_flush();
$db->query( 
    "UPDATE `".SQL_PREFIX."run` SET ".
    "dateStop = NOW(), ".
    "logs = '".addslashes($logs)."', ".
    "haveError = ".($haveError ? 1 : 0).", ".
    "id = ".$group['id']." ".
    "WHERE idRun = ".$runid
);
?>
