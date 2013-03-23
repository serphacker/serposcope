<?php

$incdir=dirname(dirname($_SERVER['SCRIPT_FILENAME'])."..")."/inc/";


function h8($str){
    return htmlentities($str, ENT_QUOTES, "UTF-8");
}

include('../inc/define.php');
include('install-inc/db.php');

ini_set("default_charset", 'utf-8');

function check_db_co($host,$login,$pass,$db){
    return @mysql_connect($host, $login, $pass) && @mysql_select_db($db) && @mysql_query("set names 'utf8'");
}

if(isset($_GET['do'])){
    if($_GET['do'] === "info"){
        die(phpinfo());
    }
}
?>
<html>
    <head>
        <title>Serposcope installer</title>
        <meta name="robots" content="noindex">
        <link href="../lib/bootstrap/css/bootstrap.min.css" rel="stylesheet" type="text/css" />
        <style>
            .container-narrow {
                margin: 0 auto;
                max-width: 700px;
              }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="masthead">
                <div class="pull-right" >
                    <b>PHP version : <?php echo phpversion(); ?></b>
                    <a href='?do=info' >phpinfo</a>
                </div>
                <h3><a href='?step=1' class="muted" >Serposcope Installer</a></h3>
            </div>

            <hr/>
            <div style='text-align:center;' ><h4>Before updating be sure to have done a SQL backup of your serposcope DB</h4></div>
            <hr/>

            <div>
                <?php
                
                    if(file_exists($incdir."/config.php")){
                        
//                        echo "<h3>Installation done !</h3>";
//                        echo "<a href='".dirname(dirname($_SERVER['PHP_SELF'])."..")."' >Go ninja go</a><br/><br/>";
                        echo "For security purpose, if you want to reinstall or upgrade the current installation, you must first remove <code>inc/config.php</code>.<br/>";
                    }else{
                
                        if(!isset($_GET['step']) || !is_numeric($_GET['step'])){
                            $_GET['step'] = 1;
                        }
                        echo '<div style="text-align:center" ><h2>Step '.$_GET['step'].'</h2></div>';
                        @include("step/step".intval($_GET['step']).".php");
                    
                    }
                ?>
            </div>
        </div>
    </body>
</html>