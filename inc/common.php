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

include(OHMYROOT.'inc/class.ws_sql.php');
include(OHMYROOT.'inc/functions.php');

// critical check, don't even start if :
// php version is < 5.2
if( PHP_MAJOR_VERSION < 5 ||
    (PHP_MAJOR_VERSION == 5 && PHP_MINOR_VERSION < 2)
){
    die("Serposcope need at least PHP <strong>5.2</strong>, you are using <strong>".PHP_MAJOR_VERSION.".".PHP_MINOR_VERSION."</strong> check <a href='http://serphacker.com/serposcope/doc/install.html' >install instructions</a>");
}

// curl not installed
if(!function_exists('curl_version')){
    die("Serposcope need <strong>curl</strong>, check <a href='http://serphacker.com/serposcope/doc/install.html' >install instructions</a>");
}
// critical check

ini_set("default_charset", 'utf-8');
set_time_limit(0);

$db = new WS_SQL();

//if(!mysql_connect(SQL_HOST, SQL_LOGIN, SQL_PASS)){
//    sleep(60);
//    mysql_connect(SQL_HOST, SQL_LOGIN, SQL_PASS) or die('no database connection (wrong host/login/password)');
//}
$db->connect(SQL_HOST, SQL_LOGIN, SQL_PASS) or die('no database connection (wrong host/login/password)');

//mysql_select_db(SQL_DATABASE) or die("Can't select ".SQL_DATABASE." database");
$db->select_db(SQL_DATABASE) or die("Can't select ".SQL_DATABASE." database");

$db->query("set names 'utf8'");

if($db->query('select 1 from `'.SQL_PREFIX.'group`') === false){
    die("Database ".SQL_DATABASE." OK but can't find tables. Need to reinstall. Delete <code>inc/config.php</code> and go to <a href='".dirname($_SERVER['PHP_SELF'])."/install/' >installer</a> or check <a href='http://serphacker.com/serposcope/doc/install.html' >install instruction</a>"); 
}

$dbversion=1;
$result=@$db->query("SELECT version FROM `".SQL_PREFIX."version`");
if($result){
    $array =  mysql_fetch_assoc($result);
    if($array){
        $dbversion=$array['version'];
    }
}

if($dbversion != SQL_VERSION){
    die("You need to upgrade your SQL tables to use this version, delete <code>inc/config.php</code> and go to <a href='".dirname($_SERVER['PHP_SELF'])."/install/' >installer</a>");
}

$modules = Array();

// load all the modules and register them in the array $modules
include(OHMYROOT.'modules/CheckModule.php');
if ($handle = opendir(OHMYROOT.'modules')) {

    while (false !== ($filename = readdir($handle))) {
        if (is_dir(OHMYROOT.'modules/'.$filename)) {
            $modulepath = OHMYROOT.'modules/'.$filename.'/module.php';
            
            if( is_file($modulepath) ){
                include($modulepath);
                $modules[$filename] = new $filename;
            }
        }
    }

    closedir($handle);
}

// load all the groups name
$groups = array();
$result = $db->query("SELECT * FROM `".SQL_PREFIX."group` ORDER BY position");
while($result && ($row=mysql_fetch_assoc($result))){
    $groups[] = $row;
}

// load the options
$options = load_options();

// load the proxies
$proxies = load_proxies();

?>