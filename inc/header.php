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
$users = $_SERVER['REMOTE_USER'];
header("Content-type: text/html;charset=windows-1250");
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
        <script src="lib/highcharts/js/highcharts.js" ></script>
        <script src="lib/highcharts/js/modules/exporting.js" ></script>
        <script src="lib/Stupid-Table-Plugin/stupidtable.min.js" ></script>
        <script>
            var current_version = "<?php echo VERSION; ?>";
            var latest_version = "<?php echo VERSION; ?>"; // will be overiden by latest.js
        </script>
        <script src="js/app.js" ></script>
        <script src="http://serposcope.serphacker.com/latest.php" ></script>
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
if ($users == 'admin'){
echo"                           <li><a href='new.php' title='Nowy test'><img  id='btn_6' src='img/new.png' /></a></li>\r\n";
echo"                           <li><a href='website.php' title='Zobacz domeny'><img id='btn_5' src='img/www.png' /></a></li>\r\n";
echo"                           <li><a href='options.php' title='Zmieñ ustawienia'><img id='btn_4' src='img/setting.png' /></a></li>\r\n";
echo"                           <li><a href='proxies.php' title='Ustaw Proxy'><img id='btn_3' src='img/proxy.png' /></a></li>\r\n";
echo"                           <li><a href='import.php' title='Importuj'><img id='btn_2' src='img/import.png' /></a></li>\r\n";
echo"                           <li><a href='logs.php' title='Zobacz logi'><img id='btn_1' src='img/log.png' /></a></li>\r\n";
echo"                           <li><a href='#' class='btn-run-all' title='Uruchom'><img id='btn_0' src='img/start.png' /></a></li>\r\n";
}
echo"                           <li><a href='/' onclick='logout();' title='Wyloguj: ".$users."'><img src='img/user.png' /></a></li>\r\n";
?>
                            </ul>
                        </div>
                    </div>

                </div>
            </div>
        </div>

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
                <a class="'.$classp.'" data-id="' . $headerGroup['idGroup'] . '" id="group-link-' . $headerGroup['idGroup'] . '" href="view.php?idGroup=' . $headerGroup['idGroup'] . '" draggable="true" ondragstart="drag(event)" >
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