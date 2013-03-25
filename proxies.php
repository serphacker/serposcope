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

	// we build the array of proxies
	$final = array();

	if(!empty($_POST['bulkimport-proxies'])) { // we want to import proxies
		$array_proxy = explode("\n", $_POST['bulkimport-proxies']); // we explode with newlines
		foreach ($array_proxy as $a) {
			if(!empty($a)) { // check if the line is not empty
				$a2 = explode(':', $a); //we explode again to split with ':'

				//ip
				if(!empty($a2[0]))
					$ip = array('ip'=>trim($a2[0]));
				else
					$err= "bulkimport error : each proxy should have an ip address !";

				//port
				if(!empty($a2[1]))
					$port = array('port'=>trim($a2[1]));
				else
					$port = array();

				//user
				if(!empty($a2[2]))
					$user = array('user'=>trim($a2[2]));
				else
					$user = array();

				//password
				if(!empty($a2[3]))
					$password = array('password'=>trim($a2[3]));
				else
					$password = array();

				//finally, a new proxy line will be added in proxies array
				$final[] = array_merge($ip,$port,$user,$password,array('type'=>$_POST['bulkimport-type']));

			}
		}
	} else {
		$final[] = array('type'=>$_POST['type'],'ip'=>$_POST['ip'], 'port'=>$_POST['port'], 'user'=>$_POST['user'], 'password'=>$_POST['password']);
	}
	
	
	//save in database
	foreach($final as $f) {

		// we want to add them 1 by 1
		if(!isset($f['ip'])){
			$err = "ip is mandatory";
		}else{

			switch($f['type']){
				case "http":
				case "socks":
					if(!isset($f['port']) || !is_numeric($f['port'])){
						$err = "invalid port";
					}
					break;

				case "iface":
					if(!empty($f['port']) || !empty($f['user']) || !empty($f['password'])){
						$err= "iface proxy doesn't need port/username or password";
					}
					break;

				default:
					$err= "invalid proxy type";
			}
		}

		if($err==null){
			$q="INSERT INTO `".SQL_PREFIX."proxy`(type,ip,port,user,password) VALUES (";
			$q.="'".addslashes($f['type'])."',";
			$q.="'".addslashes($f['ip'])."',";
			$q.= (empty($f['port']) ? "NULL" : intval($f['port'])).",";
			$q.= (empty($f['user']) ? "NULL" : "'".addslashes($f['user'])."'").",";
			$q.= (empty($f['password']) ? "NULL" : "'".addslashes($f['password'])."'");
			$q.=")";
			if(!$db->query($q)){
				$err="sql error";
			}
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
<table class='table'>
	<thead>
		<td><a href='#' id=selectallproxy>#</a></td>
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
		<tr>
			<td colspan=7><button class="btn" id="btndeleteproxy">Delete</button>
				<button class="btn" id="btncheckproxy">Check</button></td>
		</tr>
	</tbody>
</table>
<h4>Add a proxy</h4>
<form method=POST>
	<table>
		<tr>
			<td><select name=type class="input-mini">
					<option>http</option>
					<option>socks</option>
					<option>iface</option>
			</select>
			</td>
			<td><input name=ip class=input-small placeholder="ip" /></td>
			<td><input name=port class=input-mini placeholder="port" /></td>
			<td><input name=user placeholder="username" /></td>
			<td><input name=password placeholder="password" /></td>
			<td><input type=submit class="btn btn-primary" value=Add /></td>
		</tr>
	</table>
	<h4>Or bulk import</h4>
	<table>
		<tr>
			<td><select name=bulkimport-type class="input-mini">
					<option>http</option>
					<option>socks</option>
					<option>iface</option>
			</select>
			</td>
			<td colspan=3><textarea name=bulkimport-proxies></textarea><br />ip:port:username:password<br />ip:port:username:password<br />...
		</tr>
		<tr>
			<td><input type=submit class="btn btn-primary" value=Import /></td>
		</tr>
	</table>
</form>
<?php
include('inc/footer.php');
?>