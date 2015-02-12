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
header("Content-type: text/html;charset=UTF-8");
?>
<!DOCTYPE html>
<html lang="fr">
    <head>
        <title>Serposcope</title>
        <meta charset="utf-8" />
        <meta name="robots" content="noindex" />

        <link href="lib/bootstrap/css/bootstrap.min.css" rel="stylesheet" type="text/css" />
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
        <script src="//serposcope.serphacker.com/latest.php" ></script>

    </head>
    <body>
        <div class="navbar navbar-fixed-top">
            <div class="navbar-inner">
                <div class="container">
                    <div class="pull-right" >
                        <a id="uzi_link" href="." >
                            <img id="uzi_img" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAOoAAAA8CAMAAABfLdZOAAAAA1BMVEX6+vqsEtnpAAAAJElEQVR42u3BAQ0AAADCIPunfg43YAAAAAAAAAAAAAAAAABwIjcUAAHdSbRTAAAAAElFTkSuQmCC" />
                        </a>
                    </div>
                    <div class="menu-vcenter" >
                        <span id='new-version-span' >
                            <a id='new-version-link' href='http://serphacker.com/serposcope/download.html' ></a>
                        </span>
                        <span id='loading-span' ><img id="loading-img" src="img/spinner.gif" /></span>
                        <span class='mybrand' >
                            <a class='brand-link' href="index.php" >Serposcope <span id='cur-version' ><?php echo VERSION; ?></span></a>
                            <br/>
                            <span class='support-link' >
                                [ <a href='https://github.com/serphacker/serposcope/issues' >bugs</a> ]
                                [ <a href='http://forum.serphacker.com/' >support</a> ]
                            </span>
                        </span>

                        <div class="nav-collapse collapse">
                            <ul class="nav">
                                <li >
                                    <a href="new.php" >New Group</a>
                                </li>
                                <li>
                                    <a href="logs.php" >Logs</a>
                                </li>
                                <li>
                                    <a href="proxies.php" >Proxies</a>
                                </li>
                                <li>
                                    <a href="options.php" >Options</a>
                                </li>
                                <li >
                                    <a href="import.php" >Import</a>
                                </li>
                                <li >
                                    <a href="#" class='btn-run-all' >RUN ALL</a>
                                </li>
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
                        <li class="nav-header">Groups</li>
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

                            echo '
            <li class="draggable-li ' . $liclass . '" ondrop="drop(event)" ondragover="allowDrop(event)" >
                <a data-id="' . $headerGroup['idGroup'] . '" id="group-link-' . $headerGroup['idGroup'] . '" href="view.php?idGroup=' . $headerGroup['idGroup'] . '" draggable="true" ondragstart="drag(event)" >
                    <i class="handleicon"></i><i class="groupicon" style="' . $istyle . '" ></i> ' .
                            h8($headerGroup['name']) . "
                </a>
            </li>\n";
                        }
                        ?>
                    </ul>
                </div>
                <div class="span9" id="page-body" >