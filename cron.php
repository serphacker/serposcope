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
    $logs .= $str;
    mysql_query("UPDATE `".SQL_PREFIX."run` SET logs = '".addslashes($logs)."' WHERE idRun = ".$runid);    
    return $str;
}
// we will store the output in the database
ob_start('my_ob_logs',2);

// if another job is running, abort
$res=mysql_query("SELECT * FROM `".SQL_PREFIX."run` WHERE ISNULL(dateStop)");
if($res && ($run=mysql_fetch_assoc($res))){
    e('Cron',"Fatal, another job is running : PID = ".$run['pid'].", dateStart = ".$run['dateStart']);
    e('Cron',"Abort the current running jobs via the \"Logs\" page");
    die();
}

$res=mysql_query("INSERT INTO `".SQL_PREFIX."run`(dateStart,pid) VALUES (now(),".getmypid().")");
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

$iProxy= empty($proxies) ? 0 : rand(0, count($proxies)-1);

$query = "SELECT * FROM `".SQL_PREFIX."group`"; //" WHERE idGroup = 10";
if(isset($_GET['idGroup'])){
    $query .= " WHERE idGroup =  ".intval($_GET['idGroup']);
}else{
    // shuffle
    $query .= " ORDER BY rand()";
}

$resGroup=mysql_query($query);
while( $resGroup && ($row =  @mysql_fetch_assoc($resGroup)) ){


    if(!isset($modules[$row['module']])){
        e('Cron',"Can't find ".$row['module']." for group ".$row['name']." (uninstalled ?)");
        continue;
    }

    $keywords = array();
    $qKW = "SELECT idKeyword,name FROM `".SQL_PREFIX."keyword` WHERE idGroup = ".intval($row['idGroup']);
    // shuffle keywords
    $qKW .= " ORDER BY rand()";
    $result=mysql_query($qKW);
    while($kw=  mysql_fetch_assoc($result)){
        $keywords[$kw['idKeyword']] = $kw['name'];
    }
    if(empty($keywords)){
        e('Cron',"No keywords for group ".$row['name']."  ");
        continue;        
    }
    
    $sites = array();
    $qSite = "SELECT idTarget,name FROM `".SQL_PREFIX."target` WHERE idGroup = ".intval($row['idGroup']);
    $result=mysql_query($qSite);
    while($site=  mysql_fetch_assoc($result)){
        $sites[$site['idTarget']] = $site['name'];
    }
    if(empty($sites)){
        e('Cron',"No site for group ".$row['name']."  ");
        continue;        
    }    

    // if everything looks OK, let's go for a run
    l('Cron','Checking group['.$row['idGroup'].'] '.$row['name'].' with module '.$row['module']);

    $group['id'] = $row['idGroup'];
    $group['name'] = $row['name'];
    $group['options'] = empty($row['options']) ? array() : json_decode($row['options'],true);
    $group['keywords'] = $keywords;
    $group['sites'] = $sites;


    $date = "NOW()";
    //$date = "DATE_SUB(NOW(),INTERVAL ".$interval." DAY)";
    $qRun = "INSERT INTO `".SQL_PREFIX."check`(idGroup,date) VALUES(".intval($row['idGroup']).",".$date.")";

    mysql_query($qRun);
    $id=  mysql_insert_id();
    $res = $modules[$row['module']]->check($group);
    
    

    if($res === null){
        e('Cron','Module returned fatal error for group['.$row['idGroup'].'] '.$row['name']);
        continue;
    }else{
        l('Cron','Checking done for group['.$row['idGroup'].'] '.$row['name']);
    }

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
    mysql_query($qRank);
}

l('Cron','All groups done');

ob_end_flush();

mysql_query( 
    "UPDATE `".SQL_PREFIX."run` SET ".
    "dateStop = NOW(), ".
    "logs = '".addslashes($logs)."', ".
    "haveError = ".($haveError ? 1 : 0)." ".
    "WHERE idRun = ".$runid
);
?>