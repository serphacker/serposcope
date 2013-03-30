# Serposcope changelog

## UPCOMING

* Additionnal parameters for Google search (hl=fr&whatyou=want)

## 1.0.2 - 30/03/2013

* better keywords/sites insertion (bulk/textarea, no more empty form on error)
* fix class COM not found bug (affected: Windows + PHP > 5.4.5)
* detect COM on install (affected: Windows + PHP > 5.4.5)
* fix undefined PHP_MAJOR_VERSION / PHP_MINOR_VERSION (affected: PHP < 5.2.7)
* fix last check not displayed on main page
* more verbose error message on import fail
* fix "go ninja go" install link
* display bug / support link

## 1.0.1 - 28/03/2013

* proxies bulk import (contributed by @512banque http://twitter.com/512banque)
* fixed open_basedir warning issue (contributed by @alxblog https://twitter.com/alxblog)
* fixed parsing fatal error in google module

## 1.0.0 - 25/03/2013

* stable release

## 0.9.10 - 24/03/2013

* fixed no change on home page if only one check
* minor fixes/enhancement

## 0.9.9 - 23/03/2013

* fixed wild card bug
* fixed clear_cache() on mutu with hundred of thousand of files in the same dir

## 0.9.8 - 23/03/2013

* One click installer
* Better SQL handling : SQL_PREFIX / Upgrade
* HTTP Caching
* FIX target/keyword validation on edit
* export_%group-name%_%date-begin%_%date-end%

## 0.9.7 - 22/03/2013

* more debug output in Google module
* debug option and new debug function d()
* debug_memory()
* using ignore_user_abort()

## 0.9.6 - 20/03/2013

* fixed default validateTarget regex

## 0.9.5 - 19/03/2013

* Check if valid keyword/site on new.php (need to be done on edit/import)
* Run all groups from UI
* fix bug import
* better log message on logs.php
* fixed is_pid_alive sous windows
* no more using short_open_tag  (<?=)
* error message if SQL tables arn't created
* Added severals boulet-proof fix (force rtfm)
* Changed voila icon

## 0.9.4 - 19/03/2013

* Fixed PHP 5.2 unsupported json_encode options 
* Prevent soft from running if using PHP < 5.2
* Fixed SQL install file

## 0.9.3 - 18/03/2013

* Fixed fucked detection of invalid max_execution_time
* using highcharts as default rendering
* better SQL error detection on group creation new.php
* Non blocking run lauching + redirect to the log

## 0.9.2 - 18/03/2013

* Fixed a terrible issue where my password was leaked
* Fixed obsolete install.sql file

## 0.9.1 - 18/03/2013

* Testing changelog
* Fixed deprecated function usage

## 0.9.0 - 18/03/2013

* Initial beta release
