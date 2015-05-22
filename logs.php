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
if(!file_exists('inc/config.php')){
    header("Location: install/",TRUE,302);
    die();
}
require('inc/config.php');
include('inc/define.php');
include('inc/common.php');
include('inc/user.php');

if(isset($_GET['id'])){
    $res = null;
    if(is_numeric($_GET['id'])){
        $res=$db->query("SELECT * FROM  `".SQL_PREFIX."run` WHERE idRun = ".intval($_GET['id']));
    }else if( $_GET['id'] == "last" ){
        $res=$db->query("SELECT * FROM  `".SQL_PREFIX."run` ORDER BY idRun DESC LIMIT 1");
    }
    if($res && $run=  mysql_fetch_assoc($res)){
        if($run['dateStop'] == null){
            if(!is_pid_alive($run['pid'])){
                // in this case the pid have been killed externally 
                // from command line or max execution time reached
                $db->query(
                    "UPDATE `".SQL_PREFIX."run` SET haveError=1, dateStop=now(), ".
                    "logs=CONCAT(logs,'ERROR ABNORMAL TERMINATION : process may have been killed or reached max execution time\n') ".
                    "WHERE idRun = ".$run['idRun']
                );
            }
        }
    }
    
    if(is_numeric($_GET['id'])){
        $res=$db->query("SELECT idRun,dateStart,dateStop,pid,haveError,timediff(dateStop,dateStart) diff,logs FROM `".SQL_PREFIX."run` WHERE idRun = ".intval($_GET['id']));
    }else if( $_GET['id'] == "last" ){
        $res=$db->query("SELECT idRun,dateStart,dateStop,pid,haveError,timediff(dateStop,dateStart) diff,logs FROM `".SQL_PREFIX."run` ORDER BY idRun DESC LIMIT 1");
    }    
    if($res && $run=  mysql_fetch_assoc($res)){
        header('Content-Type: text/plain');
        if($run['dateStop'] == null){
            echo "Cron is still running (PID: ".$run['pid']." started: ".$run['dateStart']."), press F5 to Refresh the log\n";
        }else{
            if($run['haveError']){
                echo "Run done in ".$run['diff']." with error (PID: ".$run['pid']." started: ".$run['dateStart'].")\n";
            }else{
                echo "Run successfully done in ".$run['diff']." (PID: ".$run['pid']." started: ".$run['dateStart'].")\n";
            }
        }
        echo "-----\n";
        echo $run['logs'];
        echo "-----\n";
        if($run['dateStop'] == null){
            echo "Press F5 to Refresh the log\n";
        }else{
            echo "Process terminated, end of log\n";
        }        
        
        die();
    }    
}

$error_msg="";
$info_msg="";
if(isset($_GET['did'])){
    if($db->query("DELETE from `".SQL_PREFIX."run`  WHERE idRun = ".intval($_GET['did'])." AND dateStop is not null") && mysql_affected_rows() > 0){
        $info_msg = "Run ".intval($_GET['did'])." deleted";
    }else{
        $error_msg = "Can't delete run ".intval($_GET['did']).". It doesn't exists or job is still running.";
    }
}

include("inc/header.php");
if (!empty($error_msg)) {
    echo "<div class='CloseScanInfo alert alert-error'>$error_msg<span style='float:right;'>[<a id='CloseScanInfo' href='#'>Zamknij</a>]</span></div>\n";
}
if (!empty($info_msg)) {
    echo "<div class='CloseScanInfo alert alert-info'>$info_msg<span style='float:right;'>[<a id='CloseScanInfo' href='#'>Zamknij</a>]</span></div>\n";
}
?>
<script>
    function warningDeleteRun(){
        return confirm("It will delete all the positions checked during this run, continue ?");
    }
</script>
<script>
        $( "#btn_1" ).css("border","solid 2px #D64B46");
        $( "#btn_1" ).css("border-radius","5px");
        $(document).ready(function() { setTimeout(function(){ hMsg(); }, 5000); });
        $( "#CloseScanInfo" ).click(function() { hMsg(); });
        function hMsg() {$( ".CloseScanInfo" ).slideUp(900);}
</script>
<div>
    <table class='table table-condensed table-bordered table-log' >
        <thead>
        	   <?php
        	       if(!$ID && !$adminAcces) {echo 'ustaw domyślny projekt!';}
        	       if(($set->log || !$adminAcces) || ($adminAcces && $ID)) {$filtr ='WHERE `logs` LIKE \'%group['.$ID.']%\'';} 
        	       if(!$adminAcces && !$ID){$filtr ='WHERE `logs` LIKE \'%'.$groupd['name'].' with module%\'';}
        	       if($groupd['name']) {if($ID){$usunf =" <a href='logs.php'>Usuń filtr</a>";}else {$groupd['module'] = 'tescie';} echo "<tr class='centered'><th colspan='7'>Filtrowanie wyników dla ".$groupd['name']." w ".$groupd['module'].$usunf." </th></tr>\n";}
        	   ?>
            <tr><th>#</th><?php if($adminAcces || (!$adminAcces && !$ID)){echo "<th>Test</th>";} ?><th>Start</th><th>Stop</th><th>Time</th><th>Logs</th><th class="<?php echo $hName; ?>">Delete</th></tr>
        </thead>
        <tbody>
<?php
    
    $perPage=23;
    $qCount = "SELECT count(*) FROM `".SQL_PREFIX."run`";
    $resultCount = $db->query($qCount);
    $rowCount = mysql_fetch_row($resultCount);
    $count = $rowCount[0];
    $totalPages = ceil($count/$perPage);
    $page=isset($_GET['page']) && intval($_GET['page']) > 0 ? intval($_GET['page']) : 1;
    
    
    $start = ($page-1)*$perPage;
    $q="SELECT idRun,dateStart,id,dateStop,pid,haveError,timediff(dateStop,dateStart) diff FROM `".SQL_PREFIX."run` ".$filtr." ORDER BY dateStart DESC LIMIT $start,$perPage";
    $result = $db->query($q);
    
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
        $infor = mysql_fetch_assoc($db->query("SELECT * FROM `".SQL_PREFIX."group` WHERE idGroup = " . intval($run['id'])));
        echo "<td>".$run['idRun']."</td>";
if($adminAcces || (!$adminAcces && !$ID)){echo "<td class='test' style='background-image:url(modules/".$infor['module']."/icon.png);'> ".$infor['name']."</td>";}
        echo "<td>".$run['dateStart']."</td>";
        echo "<td>".$run['dateStop']."</td>";
        echo "<td>".$run['diff']."</td>";
        echo "<td><a href='logs.php?id=".$run['idRun']."' target='log' >logs</a></td>";
if($adminAcces){        echo "<td><a href='logs.php?did=".$run['idRun']."' onclick='return warningDeleteRun()' >delete</a></td>";}
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
        echo "<li class='previous' ><a href='?idGroup=$ID&page=".($page-1)."' >&larr; Newer</a></li>";
    }
    
    if($page >= $totalPages){
        echo "<li class='disabled next' ><a href='#' >Older &rarr;</a></li>";
    }else{
        echo "<li class='next' ><a href='?idGroup=$ID&page=".($page+1)."' >Older &rarr;</a></li>";
    }

?>
    </ul>
</div>
<?php
include('inc/footer.php');
?>
