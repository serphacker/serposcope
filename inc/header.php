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
$date = new DateTime();
$cpt = $date->format("U");
$base = ucfirst($users);
$testuj = $_GET['idGroup'];

$access = $db->query("SELECT * FROM `".SQL_PREFIX."keyword` JOIN `".SQL_PREFIX."group` USING(idGroup) WHERE `".SQL_PREFIX."group`.`name` = '$base' && `".SQL_PREFIX."keyword`.`idGroup` = '$testuj'");
$myaces = mysql_num_rows($access);
if (($myaces == 0 && $testuj) && !$adminAcces){
    header("Location: error_401.php",TRUE,302);
    die();
} else {
header("Content-type: text/html;charset=windows-1250");
}
?>
<!DOCTYPE html>
<html lang="fr">
    <head>
        <title>SeoTest</title>
        <meta charset="utf-8" />
        <meta name="robots" content="noindex" />

        <link href="lib/bootstrap/css/bootstrap.css" rel="stylesheet" type="text/css" />
        <link href="lib/datepicker/css/datepicker.css" rel="stylesheet" type="text/css" />
        <link href="css/custom.css" rel="stylesheet" type="text/css" />

        <script src="lib/jquery/jquery-1.8.2.min.js" ></script>
        <script src="lib/jquery-blockUI/jquery.blockUI.js" ></script>
        <script src="lib/bootstrap/js/bootstrap.min.js" ></script>
        <script src="lib/datepicker/js/bootstrap-datepicker.js" ></script>
        <script src="lib/highcharts/js/highcharts.src.js" charset="windows-1252"></script>
        <script src="lib/highcharts/js/modules/exporting.src.js"  charset="utf-8"></script>
        <script src="lib/Stupid-Table-Plugin/stupidtable.min.js" ></script>
        <script>
            var current_version = "<?php echo VERSION; ?>";
            var latest_version = "<?php echo VERSION; ?>"; // will be overiden by latest.js
        </script>
        <script src="js/app.js" ></script>

        <!-- <script src="http://serposcope.serphacker.com/latest.php" ></script> -->
        <script type="text/javascript">
        function logout() {
        var xmlhttp;
        if (window.XMLHttpRequest) {
          xmlhttp = new XMLHttpRequest();
        }
        // code for IE
        else if (window.ActiveXObject) {
          xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
        }
        if (window.ActiveXObject) {
        // IE clear HTTP Authentication
        document.execCommand("ClearAuthenticationCache");
        window.location.href='/';
        } else {
        xmlhttp.open("GET", '/', true, "logout", "logout");
        xmlhttp.send("");
        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState == 4) {window.location.href='/';}
        }
        }
        return false;
        }
        </script>
        <script>
       $( document ).ready(function() {
        $("#Change").click(function(){
         render = $("#Change").attr("value");
          if(render == 'Multigraph'){
          	$('#btn_7').attr("src", "img/i_multi.png");
          	$('#Change').attr("value", "Line");
          	setCookie("serpolink", 'Line', 365);
          }
          if(render == 'Line'){
          	$('#btn_7').attr("src", "img/i_lines.png");
          	$('#Change').attr("value", "Table");
          	setCookie("serpolink", 'Table', 365);
          }
          if(render == 'Table'){
         		$('#btn_7').attr("src", "img/i_call.png");
          	$('#Change').attr("value", "Social");
          	setCookie("serpolink", 'Social', 365);
          }
          if(render == 'Social'){
          	$('#btn_7').attr("src", "img/i_herts.png");
          	$('#Change').attr("value", "Multigraph");
          	setCookie("serpolink", 'Multigraph', 365);
          }
         });
        });
       </script>
    </head>
    <body>
        <div class="navbar navbar-fixed-top">
            <div class="navbar-inner">
                <div class="container">
                    <div class="menu-vcenter" >
                        <div class='mybrand' >
                            <a class='link' href="/" ><span class="logo">fly</span><span id='cur-version' >V <?php echo VERSION; ?></span></a>
                            <span id='loading-span' ><img id="loading-img" src="img/loading.gif" /></span> 
                            <a id='new-version-link' href='http://serphacker.com/serposcope/download.html' ></a>
                        </div>
                        
                        <div class="nav-collapse collapse">
                            <ul class="nav">
<?php
//echo $setround->pass."";
if(!$_COOKIE['serpolink']) {$i_url = 'view.php?wiev=chart&idGroup='; $i_value = 'Line'; $i_src = 'lines';}
if($_COOKIE['serpolink'] == 'Line') {$i_url = 'view.php?wiev=chart&idGroup='; $i_value = 'Line'; $i_src = 'lines';}
if($_COOKIE['serpolink'] == 'Table') {$i_url = 'view.php?wiev=table&idGroup='; $i_value = 'Table'; $i_src = 'call';}
if($_COOKIE['serpolink'] == 'Social') {$i_url = 'view.php?wiev=social&idGroup='; $i_value = 'Social'; $i_src = 'herts';}
if($_COOKIE['serpolink'] == 'Multigraph') {$i_url = '/?idGroup='; $i_value = 'Multigraph'; $i_src = 'multi';}
echo"                           <li><a id='Change' href='javascript:location.reload()' title='Zmieñ widok podstawowy' value='".$i_value."' rel='tooltip' data-placement='bottom'><img id='btn_7' src='img/i_".$i_src.".png' /></a></li>\r\n";
if ($adminAcces || $userAcces){echo"                           <li><a href='new.php' title='Nowy test' rel='tooltip' data-placement='bottom'><img id='btn_6' src='img/new.png' /></a></li>\r\n";}
if ($adminAcces || $userAcces){echo"                           <li><a href='website.php' title='Zobacz domeny' rel='tooltip' data-placement='bottom'><img id='btn_5' src='img/www.png' /></a></li>\r\n";}
if ($adminAcces){echo"                           <li><a href='options.php' title='Zmieñ ustawienia' rel='tooltip' data-placement='bottom'><img id='btn_4' src='img/setting.png' /></a></li>\r\n";}
if ($adminAcces){echo"                           <li><a href='proxies.php' title='Ustaw Proxy' rel='tooltip' data-placement='bottom'><img id='btn_3' src='img/proxy.png' /></a></li>\r\n";}
if ($adminAcces || $userAcces){echo"                           <li><a href='import.php' title='Importuj' rel='tooltip' data-placement='bottom'><img id='btn_2' src='img/import.png' /></a></li>\r\n";}
if ($adminAcces || $userAcces){if($ID){$filtr = "?idGroup=".$ID;} echo"                           <li><a href='logs.php".$filtr."' title='Zobacz logi' rel='tooltip' data-placement='bottom'><img id='btn_1' src='img/log.png' /></a></li>\r\n";}
if ($adminAcces){echo"                           <li><a href='run.php' class='btn-run-all' title='Uruchom' rel='tooltip' data-placement='bottom'><img id='btn_0' src='img/cron.png' /></a></li>\r\n";}

if (!$adminAcces){echo"                           <li><a href='/help/?idGroup=".$IDH."' title='Pomoc' rel='tooltip' data-placement='bottom'><img src='img/help.png' /></a></li>\r\n";}
echo"                           <li><a href='/' onclick='logout();' title='Wyloguj: ".$users."' rel='tooltip' data-placement='bottom'><img src='img/user.png' /></a></li>\r\n";
?>
                            </ul>
                        </div>
                    </div>

                </div>
            </div>
        </div>
   	<div class="footer">
		© 2014 by <a href="/">Google Butterfly SEO</a> - Based by Google.inc search algorithm: Butterfly SEO 1.0 beta | Penguin 3.0 | piRate 2.0 | Mobi 0.4
			<?php	$date = new DateTime();	$cpt = $date->format("U"); ?>
		 | Google Serwer Time <span id="txt">00:00:00</span>
      <script>
var F=true;var T='';setInterval(function(){A()},1000);function A(){if(F){T=<?php echo $cpt;?>*1000}var d=new Date(T);var h=d.getHours();var m=d.getMinutes();var s=d.getSeconds();h=h<10?'0'+h:h;m=m<10?'0'+m:m;s=s<10?'0'+s:s;var C=h+':'+m+':'+s;document.getElementById("txt").innerHTML=C;F=false;T=T+1000} //need time stamp unix format in header
</script></div>
        <div class="container" id="bodycontainer" >
            <div class="row">
                <div class="span3">
                    <ul class="nav nav-list">
                        <?php
                        $pos = 0;
                        foreach ($groups as $headerGroup) {
                            ++$pos;
                            $liclass = "";
                            $iclass = "";
                            if (isset($_GET['idGroup']) && $_GET['idGroup'] == $headerGroup['idGroup']) {
                                $liclass .= " active";
                            }
                            $istyle = 'background-image:url(modules/' . $headerGroup['module'] . '/icon.png);';
if($users == strtolower (h8($headerGroup['name'])) || $users == "admin"){
	$ee++; if($ee == 1){$classp = 'first';} else {$classp = 'next';}
                            echo '
            <li class="draggable-li ' . $liclass . '" ondrop="drop(event)" ondragover="allowDrop(event)" >
                <a class="navUrl '.$classp.'" data-id="' . $headerGroup['idGroup'] . '" id="group-link-' . $headerGroup['idGroup'] . '" href="'.$i_url. $headerGroup['idGroup'] . '" draggable="true" ondragstart="drag(event)" >
                    <i class="handleicon"></i><i class="groupicon" style="' . $istyle . '" ></i> ' .
                            h8($headerGroup['name']) . "
                </a>
            </li>\n";
            }
                        }
                        ?>
                    </ul>
                </div>
		       <div class="span9" id="page-body" >