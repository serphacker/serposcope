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

include(OHMYROOT.'inc/functions.php');

ini_set("default_charset", 'utf-8');
set_time_limit(0);

if(!mysql_connect(SQL_HOST, SQL_LOGIN, SQL_PASS)){
    sleep(60);
    mysql_connect(SQL_HOST, SQL_LOGIN, SQL_PASS) or die('no database connection (wrong host/login/password)');
}

mysql_select_db(SQL_DATABASE) or die("can't select the database");
mysql_query("set names 'utf8'");

$modules = Array();

// load all the modules and register them in the array $modules
include(OHMYROOT.'modules/CheckModule.php');
if ($handle = opendir(OHMYROOT.'modules')) {

    while (false !== ($filename = readdir($handle))) {
        
        $modulepath = OHMYROOT.'modules/'.$filename.'/module.php';
        
        if( is_file($modulepath) ){
            include($modulepath);
            $modules[$filename] = new $filename;
        }
    }

    closedir($handle);
}

// load all the groups name
$groups = array();
$result = mysql_query("SELECT * FROM `".SQL_PREFIX."group` ORDER BY position");
while($result && ($row=mysql_fetch_assoc($result))){
    $groups[] = $row;
}

// load the options
$options = load_options();

// load the proxies
$proxies = load_proxies();

?>