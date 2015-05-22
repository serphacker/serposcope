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

$render = $options['general']['rendering'];

$group = null;
$keywords = array();
$sites = array();
if (isset($_GET['idGroup'])) {
    $qGroup = "SELECT * FROM `".SQL_PREFIX."group` WHERE idGroup = " . intval($_GET['idGroup']);
    $resGroup = $db->query($qGroup);
    if (($group = mysql_fetch_assoc($resGroup))) {
		$set = json_decode($group['user']);
		//usage: $set->opcja  np: $set->setting

//        if(isset($_COOKIE[$group['idGroup']])){
//            if($_COOKIE[$group['idGroup']] == "h"){
//                $render = "highcharts";
//            }else if($_COOKIE[$group['idGroup']] == "t"){
//                $render = "table";
//            }
//        }
        
        if(isset($serposcopeCookie['r_h'])){
            if(in_array($group['idGroup'],$serposcopeCookie['r_h'])){
                $render = "highcharts";
            }
        }
        if(isset($serposcopeCookie['r_s'])){
            if(in_array($group['idGroup'],$serposcopeCookie['r_s'])){
                $render = "social";
            }
        }
        
        if(isset($serposcopeCookie['r_t'])){
            if(in_array($group['idGroup'],$serposcopeCookie['r_t'])){
                $render = "table";
            }
        }    
        
        if($render == "table"){
            $nDay = RENDER_TABLE_NDAY;
        }else{
            $nDay = RENDER_HIGHCHARTS_NDAY;
        }
        
        $startDate = strtotime("now") - (24*3600*$nDay);
        if(isset($_GET['startdate']) ){
            $dateArr = explode("/", $_GET['startdate']);
            if(is_array($dateArr) && count($dateArr) == 3 && checkdate($dateArr[1], $dateArr[0], $dateArr[2])){
                $startDate = strtotime($dateArr[0]."-".$dateArr[1]."-".$dateArr[2]);
            }
        }

        $endDate = strtotime("now");
        if(isset($_GET['enddate']) ){
            $dateArr = explode("/", $_GET['enddate']);
            if(is_array($dateArr) && count($dateArr) == 3 && checkdate($dateArr[1], $dateArr[0], $dateArr[2])){
                $endDate = strtotime($dateArr[0]."-".$dateArr[1]."-".$dateArr[2]);
            }
        }        

        $qKeyword = "SELECT idKeyword,name FROM `".SQL_PREFIX."keyword` WHERE idGroup = " . intval($_GET['idGroup']);
        $resKeyword = $db->query($qKeyword);
        while ($keyword = mysql_fetch_assoc($resKeyword)) {
            $keywords[$keyword['idKeyword']] = $keyword['name'];
        }


        $qSite = "SELECT idTarget,name FROM `".SQL_PREFIX."target` WHERE idGroup = " . intval($_GET['idGroup']);
        $resSite = $db->query($qSite);
        while ($site = mysql_fetch_assoc($resSite)) {
            $sites[$site['idTarget']] = $site['name'];
        }

        $rank = array();
        $qCheck = "SELECT * FROM  `".SQL_PREFIX."check` WHERE idGroup = " . intval($_GET['idGroup']) . 
                " AND date(`date`) >= '".date( 'Y-m-d',$startDate)."' ".
                " AND date(`date`) <= '".date( 'Y-m-d',$endDate)."' ORDER BY `date`";
        $resCheck = $db->query($qCheck);

        while ($check = mysql_fetch_assoc($resCheck)) {
            $rank[$check['idCheck']]['date'] = $check['date'];
            $qRank = "select `".SQL_PREFIX."rank`.idTarget, `".SQL_PREFIX."target`.name tname, ".
                "`".SQL_PREFIX."keyword`.name kname, position, url ".
                "FROM `".SQL_PREFIX."rank` ".
                "JOIN `".SQL_PREFIX."target` USING (idTarget) ".
                "JOIN `".SQL_PREFIX."keyword` USING(idKeyword) ".
                "WHERE idCheck= " . intval($check['idCheck']);
            
            $resRank = $db->query($qRank);
            while ($positions = mysql_fetch_assoc($resRank)) {
                $rank[$check['idCheck']][$positions['tname']][$positions['kname']][0] = $positions['position'];
                $rank[$check['idCheck']][$positions['tname']][$positions['kname']][1] = $positions['url'];
            }

            $qEvent = "SELECT name,idEvent,event FROM `".SQL_PREFIX."event` ".
                    "JOIN `".SQL_PREFIX."target` USING(idTarget) " .
                    "WHERE `date` = DATE('" . addslashes($rank[$check['idCheck']]['date']) . "') " .
                    "AND idTarget IN(" . implode(",", array_keys($sites)) . ")";
            $resEvent = $db->query($qEvent);
            while ($resEvent && $event = mysql_fetch_assoc($resEvent)) {
                if (!isset($rank[$check['idCheck']]['__event'][$event['name']][$event['event']])) {
                    $rank[$check['idCheck']]['__event'][$event['name']][$event['idEvent']] = $event['event'];
                }
            }
        }
    }
}

if($group == NULL){
    header("Location: index.php?error=Can't find group");
    die();
}

if(isset($_GET['export'])){
    include('renders/csv.php');
    render($rank, $sites, $keywords);
    die();
}
if ($_GET['wiev'] == 'table') {$render = 'table';}
if ($_GET['wiev'] == 'chart') {$render = 'highcharts';}
if ($_GET['wiev'] == 'social') {$render = 'social';}
if ($_GET['wiev'] == 'mobile') {$render = 'mobile';}
if ($_GET['wiev'] == 'multi') {header("Location: /?idGroup=" . intval($_GET['idGroup']), TRUE, 302);
}
include("inc/header.php");
include('renders/'.$render.'.php');
?>
<script>
        $( "#btn_7" ).css("border","solid 2px #D64B46");
        $( "#btn_7" ).css("border-radius","5px");
</script>
<!-- <h2><?php echo ($group != null && isset($group['name']) ? h8($group['name']) : ""); ?></h2> -->
<div>
    <input class="datepicker datepicker-group <?php if ($render == 'social') {echo 'inactive" disabled="true';} ?>"  id="startdate" name="startdate" type="text" />
    <input class="datepicker datepicker-group <?php if ($render == 'social') {echo 'inactive" disabled="true';} ?>"  id="enddate" name="enddate" type="text" />
    <a type="button" class="btn <?php if ($render == 'social') {echo 'inactive" disabled="true';} ?>" id="btn-date-scope"  >Odśwież</a>
<?php
if ($adminAcces || $set->export){echo "<a type='button' class='btn' id='btn-export' >Eksport</a>\n";}
if ($adminAcces || $userAcces || $set->setting){echo "<a href='edit.php?idGroup=".$group['idGroup']."' class='btn' >Edycja</a>\n";}
if ($adminAcces || $set->del){echo"<a id=btn-del-group class='btn btn-danger' >Usuń</a>\n";}
if ($adminAcces || $set->restart){echo"<a href='#' id='btn-force-run' class='btn btn-warning' >Uruchom</a>";}
?>
    <div style='float:right;' >
        <span rel="tooltip" title="Wykres" class="radio-render" value="highcharts">
            <i class='icon-signal icon-<?php echo $render === "highcharts" ? "red" : "true" ?>' ></i>
        </span>
        
        <span rel="tooltip" title="Tabela" class="radio-render" value="table">
            <i class='icon-th icon-<?php echo $render === "table" ? "red" : "true" ?>' ></i>
        </span>

        <span rel="tooltip" title="Kondycja & SocialMedia" class="radio-render" value="social">
            <i class='icon-heart icon-<?php echo $render === "social" ? "red" : "true" ?>' ></i>
        </span>
        
        <span rel="tooltip" title="Multigraph" id="multigraph" class="radio-render" value="multigraph">
            <i class='icon-picture'></i>
        </span>
    </div>
</div>
<br/>
<script>
    $(function() {

        $( ".datepicker" ).datepicker({format : 'dd/mm/yyyy'});
        $( ".event-date" ).datepicker('setValue', Date());
        $( "#startdate" ).datepicker('setValue', '<?php echo date('d/m/Y', $startDate); ?>');
        $( "#enddate" ).datepicker('setValue', '<?php echo date('d/m/Y', $endDate); ?>');
<?php if ($adminAcces || $set->info){ 								// admin functions
 echo '
        $(".del-event").click(function(){
	            $.ajax({
                type: "POST",
                url: "ajax.php",
                data: {
                    action: "delEvent",
                    idEvent: $(this).attr("data-id")
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
    
        $(".event-add").click(function(){
            $.ajax({
                type: "POST",
                url: "ajax.php",
                data: $("#event-form-" + $(this).attr("data-id")).serialize()
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
        
        $("#btn-del-group").click(function(){
            if(confirm("Czy napewno chcesz usunąć ten test?")){
                $.ajax({
                    type: "POST",
                    url: "ajax.php",
                    data: {
                        action: "delGroup",
                        idGroup: '.$group["idGroup"].'
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
        
        $("#btn-force-run").click(function(){
            var canRun=false;                        // check if a run is already launched
            $.ajax({
                type: "POST",
                url: "ajax.php",
                data: { action: "is_running" }
            }).done(function(rawdata){
                data = JSON.parse(rawdata);
                if(data !== null){
                    if(data.running !== undefined){
                        if(!data.running){ canRun=true; }
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
                if(!confirm("Zostanie uruchomiony skrypt skanowania. Czy napewno chcesz kontynuować?")){
                    return;
                }
                var imgRun = new Image();
                imgRun.src = "cron.php?idGroup='.$group["idGroup"].'";
                $.blockUI({ message: "<h1>Skanowanie...</h1>" });                // lock everything for 2 sec
                setTimeout(function(){
                    document.location.href = "/";
                }, 2000);
            });
        });
      
        $(".group-btn-info").click(function(){
            var idSite = $(this).attr("data-id");
            $.ajax({
                type: "POST",  
                url: "ajax.php",
                data: "action=getSiteInfo&target=" + idSite
            }).done(function(rawdata){

                data = JSON.parse(rawdata);
                if(data != null){
                    if(data.info != undefined && data.info != null){
                        domodal("Opis", data.info,onSaveGroupInfo,idSite);
                    }else{
                        domodal("Opis", "",onSaveGroupInfo,idSite); 
                    }
                }else{
                    alert("unknow error [1]");
                }
            });
        });
        
';} else { 										// def user function
	echo '        $(".event-add").click(function(){alert("Brak uprawnień...");});
        $(".del-event").click(function(){alert("Brak uprawnień...");});
        
        $(".group-btn-info").click(function(){
            var idSite = $(this).attr("data-id");
            $.ajax({
                type: "POST",  
                url: "ajax.php",
                data: "action=getSiteInfo&target=" + idSite
            }).done(function(rawdata){

                data = JSON.parse(rawdata);
                if(data != null){
                    if(data.info != undefined && data.info != null){
                        domodal("&nbsp;", data.info,"",idSite);
                    }else{
                        domodal("&nbsp;", "","",idSite); 
                    }
                }else{
                    alert("unknow error [1]");
                }
            });
        });

';} ?>
        $('#btn-date-scope').click(function(){
            var url = "view.php?idGroup=<?php echo $group['idGroup']; ?>" + 
                "&startdate=" + $('#startdate').val() + 
                "&enddate="+ $('#enddate').val();
            window.location = url;
        });
        
        $('#multigraph').click(function(){
            var url = "index.php?idGroup=<?php echo $group['idGroup']; ?>";
            window.location = url;
        });
        
        $('#btn-export').click(function(){
            var url = "view.php?idGroup=<?php echo $group['idGroup']; ?>" + 
                "&export=true" +
                "&startdate=" + $('#startdate').val() + 
                "&enddate="+ $('#enddate').val();
            window.location = url;
        });        
        
        $('.group-btn-calendar').click(function(){
            var id = $(this).attr('data-id');
            $('#event-table-' + id).toggle(250,'swing',
                function(it){
                    $('#event-table-' + id ).animate({
                        scrollTop: $('#event-table-' + id ).get(0).scrollHeight
                    }, 250);
                }
            );
        });
        
        $('.radio-render').click(function(){
            var idGroup = <?php echo $group['idGroup']; ?>;
            var key =  idGroup;
            var cookie = readCookie("serposcope");
            var render = $(this).attr("value");
            if(cookie !== null){
                cookie = JSON.parse(cookie);
            }
            
            if(cookie === null){
                cookie = {};
            }
            
            if( cookie.r_h === undefined){
                cookie.r_h = [];
            }
            if( cookie.r_s === undefined){
                cookie.r_s = [];
            }
            
            if( cookie.r_t === undefined){
                cookie.r_t = [];
            }
            
            if(cookie.r_h.indexOf(key) !== -1){
                cookie.r_h.splice(cookie.r_h.indexOf(key),1);
            }            
            if(cookie.r_s.indexOf(key) !== -1){
                cookie.r_s.splice(cookie.r_h.indexOf(key),1);
            }            
            if(cookie.r_t.indexOf(key) !== -1){
                cookie.r_t.splice(cookie.r_t.indexOf(key),1);
            }   
            
            if(render === "table"){
                cookie.r_t.push(key);
            }else if(render === "highcharts"){
                cookie.r_h.push(key);
            }else if(render === "social"){
                cookie.r_s.push(key);
            }else{
                return;
            }
            
            setCookie("serposcope", JSON.stringify(cookie), 365);
            location.replace('?idGroup=<?php echo $group['idGroup']; ?>');
        });
        
    }); 
    
    
</script>
<?php
foreach ($sites as $idSite => $site) {
    echo "<a class=linkdiv id='" . h8($site) . "' href='' ></a>";
    echo "<div>\n";
    echo "<div class='row group-head' >".
            "<div id='event-bar-$idSite' data-id='$idSite' class='group-title' >" .str_replace('*','',h8($site)) . "</div>".
            "<div class='group-action-btn' >".
                "<span data-id='$idSite' class='btn group-btn-info' > opis </span> ".
                "<span data-id='$idSite' class='btn group-btn-calendar' > notatki </span> ".
            "</div>".
         "</div>";
    ?>

    <div id="event-table-<?php echo $idSite; ?>" class="table-event-container" >
        <form id="event-form-<?php echo $idSite; ?>" class="form-event" action="ajax.php" >
            <input type="hidden" name="action" value="addEvent" />
            <input type="hidden" name="target" value="<?php echo $idSite; ?>" />
            <table class='table table-bordered table-striped table-event'>
                <tr><th style='width:80px; text-align:center;'>Data</th><th>Wpis</th><th style='width:8px'></th></tr>
    <?php
    if (isset($_GET['idGroup'])) {
        $qEvent = "SELECT idEvent,date_format(`date`,'%d/%m/%Y') `dateF`,event,name ".
                "FROM `".SQL_PREFIX."event` ".
                "JOIN `".SQL_PREFIX."target` USING (idTarget) ".
                "WHERE idTarget = $idSite ORDER BY `date` ASC ";
        $resEvent = $db->query($qEvent);
        while ($event = mysql_fetch_assoc($resEvent)) {

            echo "<tr>" .
            '<td rel="tooltip" title="'.$event['dateF'].'" style="text-align:center;" > ' . $event['dateF'] .
            "</td>" .
            "<td>" . ($event['event']) . "</td>" .
            "<td><img class=del-event data-id=" . $event['idEvent'] . " src='img/trash.png' rel='tooltip' title='Usuń ten wpis' /></td>" .
            "</tr>";
        }
    }
    ?>
                <tr>
                    <td><input class="datepicker event-date" name="date" type="text" style="width:80px" ></td>
                    <td><textarea class="event-data" name="note" ></textarea></td>
                    <td><img src="img/add.png" class=event-add data-id=<?php echo $idSite; ?> rel="tooltip" title="Dodaj nowy wpis" /></td>
                </tr>
            </table>
        </form>
    </div>
 	<?php
    render($rank, $site, $keywords);
    echo "</div>\n";
}
include('inc/footer.php');
?>
