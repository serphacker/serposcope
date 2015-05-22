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
include("inc/header.php");



?>
<script>
    $(function() {
        $( "#btn_5" ).css("border","solid 2px #D64B46");
        $( "#btn_5" ).css("border-radius","5px");

        $( ".datepicker" ).datepicker({format : 'dd/mm/yyyy'});
        $( ".event-date" ).datepicker('setValue', Date());
        $( "#startdate" ).datepicker('setValue', '<?php echo date('d/m/Y', $startDate); ?>');
        $( "#enddate" ).datepicker('setValue', '<?php echo date('d/m/Y', $endDate); ?>');

        $('.group-btn-info').click(function(){
            var idSite = $(this).attr('data-id');
            $.ajax({
                type: "POST",  
                url: "ajax.php",
                data: "action=getSiteInfo&target=" + idSite
            }).done(function(rawdata){
                data = JSON.parse(rawdata);
                if(data != null){
                    if(data.info != undefined && data.info != null){
                        domodal('&nbsp;', data.info,onSaveGroupInfo,idSite);
                    }else{
                        domodal('&nbsp;', '',onSaveGroupInfo,idSite); 
                    }
                }else{
                    alert("unknow error [1]");
                }
            });
        });
        
        $('.group-btn-calendar').click(function(){
            var idSite = $(this).attr('data-id');
            $.ajax().done(function(){
            	$('#event-table-' + idSite).toggle(250,'swing');
            });
        });

        $('.event-add').click(function(){
            $.ajax({
                type: "POST",
                url: "ajax.php",
                data: $('#event-form-' + $(this).attr('data-id')).serialize()
            }).done(function(rawdata){
                data = JSON.parse(rawdata);
                if(data != null){
                    if(data.success != undefined){
                        window.location = window.location.pathname + window.location.search;
                    }else if(data.error != undefined){
                        alert(data.error);
                    }else{
                        alert("unknow error [2]");   
                    }
                }else{
                    alert("unknow error [1]");
                }
            });
        });

        $('.del-event').click(function(){
            $.ajax({
                type: "POST",
                url: "ajax.php",
                data: {
                    action: "delEvent",
                    idEvent: $(this).attr('data-id')
                }
            }).done(function(rawdata){
                data = JSON.parse(rawdata);
                if(data != null){
                    if(data.success != undefined){
                        window.location = window.location.pathname + window.location.search;
                    }else if(data.error != undefined){
                        alert(data.error);
                    }else{
                        alert("unknow error [2]");   
                    }
                }else{
                    alert("unknow error [1]");
                }
            });
        });

        $('.btn-del-group').click(function(){
            if(confirm("Czy napewno chcesz usun¹æ test " + $(this).attr('data-name') +"?")){
                $.ajax({
                    type: "POST",
                    url: "ajax.php",
                    data: {
                        action: "delGroup",
                        idGroup: $(this).attr('data-id')
                    }
                }).done(function(rawdata){
                    data = JSON.parse(rawdata);
                    if(data != null){
                        if(data.success != undefined){
                            window.location = "index.php";
                        }else if(data.error != undefined){
                            alert(data.error);
                        }else{
                            alert("unknow error [2]");   
                        }
                    }else{
                        alert("unknow error [1]");
                    }
                });
            }
        });

        $('.btn-force-run').click(function(){
        	testy = $(this).attr('data-name');
         idtest =($(this).attr('data-id'));
            var canRun=false;
            $.ajax({
                type: "POST",
                url: "ajax.php",
                data: {
                action: "is_running"
                }
            }).done(function(rawdata){
                data = JSON.parse(rawdata);
                if(data !== null){
                    if(data.running !== undefined){
                        if(!data.running){
                            canRun=true;
                        }
                    }else{
                        alert("unknow error [2]");   
                    }
                }else{
                    alert("unknow error [1]");
                }
                if(!canRun){
                    alert("A job is already running");
                    document.location.href = "index.php";
                    return;
                }
                if(!confirm("Zostanie uruchomiony skrypt skanowania domeny " + testy + ".\r\n\r\nCzy napewno chcesz kontynuowaæ?")){
                    return;
                }
                var imgRun = new Image();
                imgRun.src = "cron.php?idGroup=" + idtest;

                // lock everything for 3 sec
                $.blockUI({ message: '<h1>Skanowanie...</h1>' });
                setTimeout(function(){
                    document.location.href = "/";
                }, 2000);
            });
        });
      }); 
    
    
</script>
<h2>Websites</h2>
<div>
<div class='website'>
<?php
if(!$ID) {echo 'ustaw domyœlny projekt!';}
if(!$adminAcces) {$filter = "WHERE `".SQL_PREFIX."group`.`name` = '".ucfirst($groupd['name'])."' ";}
    $q="SELECT `".SQL_PREFIX."target`.name tname,idTarget, ".
            "`".SQL_PREFIX."group`.idGroup,`".SQL_PREFIX."group`.name gname ".
            "FROM `".SQL_PREFIX."target`".
            "JOIN `".SQL_PREFIX."group` USING(idGroup)".$filter;
    $result = $db->query($q);
    
    while($result && ($row=mysql_fetch_assoc($result))){
        echo "<div class='rows'><ul> \r\n" .
            "<li class='name'>".str_replace('*','',h8($row['tname']))."</li> \r\n".
            "<li class='site'>".h8($row['gname'])."</li> \r\n".
            "<li class='bton'> \r\n".
            " <a class='btn' href='edit.php?idGroup=".$row['idGroup']."#".h8($row['tname'])."' ><img src='img/setting.gif' rel='tooltip' title='Zmieñ ustawienia' /></a> \r\n".
            " <a class='btn group-btn-info' data-id='".$row['idTarget']."' ><img src='img/info.gif' rel='tooltip' title='Zmieñ opis' /></a> \r\n".
            " <a class='btn group-btn-calendar' data-id='".$row['idTarget']."'><img src='img/edit.gif' rel='tooltip' title='Przegl¹daj notatki' /></a></li> \r\n".
            " <a class='btn btn-danger btn-del-group' data-id='".$row['idTarget']."' data-name='".str_replace('*','',h8($row['gname']))."' ><img src='img/trash.png' rel='tooltip' title='Usuñ test - ".str_replace('*','',h8($row['gname']))."' /></a> \r\n".
            " <a class='btn btn-warning btn-force-run' data-id='".$row['idTarget']."' data-name='".str_replace('*','',h8($row['gname']))."' ><img src='img/play.gif' rel='tooltip' title='Uruchom test - ".str_replace('*','',h8($row['gname']))."' /></a> \r\n".
            "</ul></div> \r\n";
        echo " <a class='btn btn-warning btn-view-group' href='/?idGroup=".$row['idGroup']."' ><img src='img/graph.png' rel='tooltip' title='Zobacz wykres domeny<br />".str_replace('*','',h8($row['tname']))."' /></a> \r\n";
        echo '
        <div id="event-table-'.$row['idGroup'].'" class="table-event-container" >
        <form id="event-form-'.$row['idGroup'].'" class="form-event" action="ajax.php" >
            <input type="hidden" name="action" value="addEvent" />
            <input type="hidden" name="target" value="'.$row['idGroup'].'" />
            <table class="table table-striped table-event">
            <tr><th style="width:80px; text-align:center;">Data</th><th>Wpis dla domeny '.str_replace('*','',h8($row['tname'])).'</th><th style="width:8px"></th></tr>';
        $qEvent = "SELECT idEvent,date_format(`date`,'%d/%m/%Y') `dateF`,event,name " .
                "FROM `".SQL_PREFIX."event` " .
                "JOIN `".SQL_PREFIX."target` USING (idTarget) " .
                "WHERE idTarget = ".$row['idGroup']." ORDER BY `date` ASC ";
        $resEvent = $db->query($qEvent);
        while ($event = mysql_fetch_assoc($resEvent)) {
            echo "<tr>" .
            '<td rel="tooltip" title="'.$event['dateF'].'" class="dats" > ' . $event['dateF'] .
            "</td>" .
            "<td>" . ($event['event']) . "</td>" .
            "<td><img class=del-event data-id=" . $event['idEvent'] . " src='img/trash.png' rel='tooltip' title='Usuñ ten wpis' /></td>" .
            "</tr>";
        }
echo'                <tr>
                    <td><input class="datepicker event-date" name="date" type="text" style="width:80px" ></td>
                    <td><textarea class="event-data" name="note" ></textarea></td>
                    <td><img src="img/add.png" class=event-add data-id='.$row['idGroup'].' rel="tooltip" title="Dodaj nowy wpis" /></td>
                </tr>
            </table>
        </form>
    </div>';
    }
?>

</div>
</div>

<?php
include('inc/footer.php');
?>