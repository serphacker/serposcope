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

$err=null;

if(!empty($_POST)){
    if(!isset($_POST['ip'])){
        $err = "ip is mandatory";
    }else{
        
        switch($_POST['type']){
            case "http":
            case "socks":
                if(!isset($_POST['port']) || !is_numeric($_POST['port'])){
                    $err = "invalid port";
                }
                break;

            case "iface":
                if(!empty($_POST['port']) || !empty($_POST['user']) || !empty($_POST['password'])){
                    $err= "iface proxy doesn't need port/username or password";
                }
                break;

            default:
                $err= "invalid proxy type";
        }
    }
    
    if($err==null){
        $q="INSERT INTO `".SQL_PREFIX."proxy`(type,ip,port,user,password) VALUES (";
        $q.="'".addslashes($_POST['type'])."',";
        $q.="'".addslashes($_POST['ip'])."',";
        $q.= (empty($_POST['port']) ? "NULL" : intval($_POST['port'])).",";
        $q.= (empty($_POST['user']) ? "NULL" : "'".addslashes($_POST['user'])."'").",";
        $q.= (empty($_POST['password']) ? "NULL" : "'".addslashes($_POST['password'])."'");
        $q.=")";
        if(!$db->query($q)){
            $err="sql error";
        }
    }
}

$proxies=  load_proxies();

include('inc/header.php');
if($err != null){
    echo "<div class='alert alert-error' >$err</div>";    
}
?>
<script>
bAllSelected=false;
$(function(){
    
    $('#selectallproxy').click(function(){
        
        var selected=$('[name="idproxy[]"]');
        if(bAllSelected){
            selected.attr('checked', false);
        }else{
            selected.attr('checked', true);
        }
        bAllSelected=!bAllSelected;
    });
        
    $('#btndeleteproxy').click(function(){
        var selected = $('[name="idproxy[]"]:checked');
        if(selected.length === 0){
            alert("no proxy selected");
            return;
        }
        
        var proxiesId="";
        selected.each(function(i,item){
            proxiesId += "&id[]=" + item.value;
        });
        
        $.ajax({
            type: "POST",
            url: "ajax.php",
            data: "action=deleteproxy" + proxiesId
        }).done(function(rawdata){
            data = JSON.parse(rawdata);
            if(data !== null){
                if(data.deleted === true){
                    $('[name="idproxy[]"]:checked').parent().parent().remove();
                }else{
                    alert("Can't delete proxies");
                }
            }else{
                alert("unknow error [1]");
            }
        });
        
    });
    
    $('#btncheckproxy').click(function(){
        var selected = $('[name="idproxy[]"]:checked');
        if(selected.length === 0){
            alert("no proxy selected");
            return;
        }
        
        selected.each(function(i,item){
            $.ajax({
                type: "POST",
                url: "ajax.php",
                data: "action=checkproxy&id=" + item.value
            }).done(function(rawdata){
                data = JSON.parse(rawdata);
                if(data !== null){
                    if(data.result === "ok"){
                        $('#status' + item.value).html("OK");
                        $('#status' + item.value).css("color","green");
                    }else{
                        $('#status' + item.value).html("ERR");
                        $('#status' + item.value).css("color","red");
                    }
                }else{
                    alert("unknow error [1]");
                }
            });
        });
        

        
    });    
        
});
</script>
<h2>Proxies</h2>
<table class='table' >
    <thead>
       <td><a href='#' id=selectallproxy >#</a></td>
       <td>type</td>
       <td>ip</td>
       <td>port</td>
       <td>user</td>
       <td>password</td>
       <td>status</td>
    </thead>
    <tbody>
<?php
foreach ($proxies as $proxy) {
    echo "<tr>";
    echo "<td><input type=checkbox name=idproxy[] value=".$proxy['id']." /></td>";
    echo "<td>".$proxy['type']."</td>";
    echo "<td>".h8($proxy['ip'])."</td>";
    echo "<td>".$proxy['port']."</td>";
    echo "<td>".h8($proxy['user'])."</td>";
    echo "<td>".h8($proxy['password'])."</td>";
    echo "<td id=status".$proxy['id']." >N/A</td>";
    echo "</tr>";
}
?>
    <tr><td colspan=7 ><button class="btn" id="btndeleteproxy" >Delete</button> <button class="btn" id="btncheckproxy"  >Check</button></td></tr>
    </tbody>
</table>
<h4>Add a proxy</h4>
<form method=POST >
<table>
    <tr>
        <td>
            <select name=type class="input-mini" >
                <option>http</option>
                <option>socks</option>
                <option>iface</option>
            </select>
        </td>
        <td><input name=ip class=input-small placeholder="ip"  /></td>
        <td><input name=port class=input-mini placeholder="port" /></td>
        <td><input name=user placeholder="username" /></td>
        <td><input name=password placeholder="password" /></td>
        <td><input type=submit class="btn btn-primary" value=Add /></td>
    </tr>
</table>
</form>
<?php
include('inc/footer.php');
?>