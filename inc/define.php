<?php

define('VERSION','1.0.2');
define('SQL_VERSION',2); 
define('INCLUDE_OK',1);

define('IMPORT_SERPOSCOPE_CSV',1);
define('IMPORT_RANKSFR_CSV',2);
define('IMPORT_MYPOSEO_CSV',3);

define('CACHE_DIR',sys_get_temp_dir()."/serposcope_cache/");
define('COOKIE_PATH', sys_get_temp_dir()."/serposcope_cookies.txt");

if(!defined('OHMYROOT')){
    define('OHMYROOT','./');
}

if(!defined('SQL_PREFIX')){
    define('SQL_PREFIX','');
}

?>