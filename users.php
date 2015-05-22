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
if (!file_exists('inc/config.php')) {
    header("Location: install/", TRUE, 302);
    die();
}
require('inc/config.php');
include('inc/define.php');
include('inc/common.php');
include('inc/user.php');


include("inc/header.php");

echo "<div class='accordion-group'><h4>General</h4>";

include('inc/footer.php');
?>
