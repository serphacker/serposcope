<?php

define('VERSION','1.0.5');
define('SQL_VERSION',2); 
define('INCLUDE_OK',1);

define('IMPORT_SERPOSCOPE_CSV',1);
define('IMPORT_RANKSFR_CSV',2);
define('IMPORT_MYPOSEO_CSV',3);

define('CACHE_DIR',sys_get_temp_dir()."/serposcope_cache/");
define('COOKIE_DIR', sys_get_temp_dir()."/serposcope_cookies/");

if(!defined('OHMYROOT')){
    define('OHMYROOT','./');
}

if(!defined('SQL_PREFIX')){
    define('SQL_PREFIX','');
}

// advanced options
define('RENDER_TABLE_NDAY',20);
define('RENDER_HIGHCHARTS_NDAY',30);

define('CACHE_LIFETIME',8); // Maximum age of the cache in hour
define('CACHE_RUN_CLEAR',TRUE); // Clear the cache after each run even if lifetime isn't expired
define('HOME_UNCHANGED',TRUE); // Display unchanged position on home
define('HOME_SERP_VOLATILITY',TRUE); // Display serp volatility on home
define('DEBUG_LOG',TRUE); // Enable debug logging

define('CAPTCHA_TIMEOUT',60);
define('CAPTCHA_RETRY',3);
define('CAPTCHA_MAX_RUN',100); // Maximum captcha to break in a run

?>