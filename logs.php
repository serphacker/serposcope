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
include('inc/config.php');
include('inc/define.php');
include('inc/common.php');
if(isset($_GET['id'])){
    $res = null;
    if(is_numeric($_GET['id'])){
        $res=mysql_query("SELECT logs FROM  `".SQL_PREFIX."run` WHERE idRun = ".intval($_GET['id']));
    }else if( $_GET['id'] == "last" ){
        $res=mysql_query("SELECT logs FROM  `".SQL_PREFIX."run` ORDER BY idRun DESC LIMIT 1");
    }
    if($res && $logs=  mysql_fetch_assoc($res)){
        header('Content-Type: text/plain');
        echo $logs['logs'];
        die();
    }
}

include("inc/header.php");
?>
<div>
    <table class='table table-condensed table-bordered' >
        <thead>
            <tr><th>#</th><th>start</th><th>stop</th><th>length</th><th>logs</th></tr>
        </thead>
        <tbody>
<?php
    
    $perPage=20;
    $qCount = "SELECT count(*) FROM `".SQL_PREFIX."run`";
    $resultCount = mysql_query($qCount);
    $rowCount = mysql_fetch_row($resultCount);
    $count = $rowCount[0];
    
    $totalPages = ceil($count/$perPage);
    $page=isset($_GET['page']) && intval($_GET['page']) > 0 ? intval($_GET['page']) : 1;
    
    
    $start = ($page-1)*$perPage;
    
    $q="SELECT idRun,dateStart,dateStop,pid,haveError,timediff(dateStop,dateStart) diff FROM `".SQL_PREFIX."run` ORDER BY dateStart DESC LIMIT $start,$perPage";
    $result = mysql_query($q);
    
    while($result && ($run=mysql_fetch_assoc($result))){
        echo "<tr class=";
        if($run['dateStop'] == null){
            echo "warning";
        }else{
            if($run['haveError']){
                echo "error";
            }else{
                echo "success";
            }
        }
        echo " >";
        echo "<td>".$run['idRun']."</td>";
        echo "<td>".$run['dateStart']."</td>";
        echo "<td>".$run['dateStop']."</td>";
        echo "<td>".$run['diff']."</td>";
        echo "<td><a href='logs.php?id=".$run['idRun']."' >logs</a></td>";
        echo "</tr>";
    }
?>
        </tbody>
    </table>
    <ul class="pager">
<?php
    if($page===1){
        echo "<li class='disabled previous' ><a href='#' >&larr; Newer</a></li>";
    }else{
        echo "<li class='previous' ><a href='?page=".($page-1)."' >&larr; Newer</a></li>";
    }
    
    if($page >= $totalPages){
        echo "<li class='disabled next' ><a href='#' >Older &rarr;</a></li>";
    }else{
        echo "<li class='next' ><a href='?page=".($page+1)."' >Older &rarr;</a></li>";
    }

?>
    </ul>
</div>
<?php
include('inc/footer.php');
?>