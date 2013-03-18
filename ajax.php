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

header('Content-Type: text/plain');

if(isset($_POST['action'])){
    switch($_POST['action']){
        
        case "is_running":{
            $res=mysql_query("SELECT 1 FROM `".SQL_PREFIX."run` WHERE ISNULL(dateStop)");
            die(json_encode(array("running" => (mysql_num_rows($res) > 0))));
        }
        
        case "checkproxy":{
            if(
                isset($_POST['id']) && is_numeric($_POST['id'])
            ){
                $query="SELECT * FROM `".SQL_PREFIX."proxy` WHERE id = ".intval($_POST['id']);
                $result=mysql_query($query);
                $proxy=  mysql_fetch_assoc($result);
                if(!is_array($proxy)){
                    die(json_encode(array("result" => "no proxy with this id")));
                }
                $opts = array(
                    CURLOPT_URL => "http://revolt.vu.cx/"
                ) + buildCurlOptions($proxy);
                
                $curl=curl_init();
                curl_setopt_array($curl,$opts);
                
                $data=curl_exec($curl);
                $http_status = curl_getinfo($curl, CURLINFO_HTTP_CODE);
                if($http_status != 200 || strstr($data,"Your IP:") === FALSE){
                    die(json_encode(array("result" => "error")));
                }
                die(json_encode(array("result" => "ok")));
            }
            break;
        }
        
        case "deleteproxy":{
            if(
                isset($_POST['id']) && is_array($_POST['id'])
            ){
                $ids=array_map("intval", $_POST['id']);
                $query = "DELETE FROM `".SQL_PREFIX."proxy` WHERE id IN (".implode(",",$ids).")";
                $swap = mysql_query($query);
                die(json_encode(array("deleted" => $swap)));
            }
            break;            
        }
        
        // swap the position between two groups
        case "swap":{
            if(
                isset($_POST['source']) && is_numeric($_POST['source']) && 
                isset($_POST['target']) && is_numeric($_POST['target'])
            ){
                // elite query
                $query = "UPDATE ".
                    "`".SQL_PREFIX."group` AS g1 JOIN `".SQL_PREFIX."group` AS g2 ".
                    "ON ( g1.idGroup = ".intval($_POST['source'])." AND g2.idGroup = ".intval($_POST['target'])." ) ".
                    "SET g1.position = g2.position, g2.position = g1.position";
                $swap = mysql_query($query);
                die(json_encode(array("swap" => $swap)));
            }
            break;
        }
        
        // kill a running process
        case "kill":{
            if(
                isset($_POST['pid']) && is_numeric($_POST['pid']) && 
                isset($_POST['runid']) && is_numeric($_POST['runid'])
            ){
                portable_kill($_POST['pid']);
                sleep(1); // hacking, should wait for pid to exit
                if(is_pid_alive($_POST['pid'])){
                    die(json_encode(array("kill" => FALSE)));
                }
                mysql_query(
                    "UPDATE `".SQL_PREFIX."run` SET haveError=1, dateStop=now(), ".
                    "logs=CONCAT(logs,'ERROR KILLED FROM WEB PANEL\n') ".
                    "WHERE idRun = ".intval($_POST['runid'])
                );
                die(json_encode(array("kill" => TRUE)));
            }
            break;
        }
        
        case "getSiteInfo":{
                if(isset($_POST['target']) && is_numeric($_POST['target'])){
                    $q="SELECT info FROM `".SQL_PREFIX."target` WHERE idTarget = ".intval($_POST['target']);
                    $result=mysql_query($q);
                    if($result && ($row=mysql_fetch_assoc($result))){
                        die(json_encode($row));
                    }
                }
            }
            break;
            
        case "addSiteInfo":{
                if(isset($_POST['target']) && is_numeric($_POST['target']) && isset($_POST['info'])){
                    $q="UPDATE `".SQL_PREFIX."target` SET info = '".addslashes($_POST['info'])."' WHERE idTarget = ".intval($_POST['target']);
                    if(mysql_query($q) == 1){
                        echo json_encode(array('success' => 'ok'));
                    }else{
                        echo json_encode(array('error' => 'sql error'));
                    }
                }
            }
            break;            
        
        case "getGroupOptions":
            if(isset($_POST['module'])){
                if(isset($modules[$_POST['module']])){
                    $moduleOptions = $modules[$_POST['module']]->getGroupOptions();
                    if($moduleOptions != null && !empty($moduleOptions)){
                        echo json_encode($moduleOptions);
                    }
                }
            }
            break;
            
            
        case "addEvent":
            if(isset($_POST['target']) && is_numeric($_POST['target']) && 
                    isset($_POST['date']) && preg_match('/^[0-9]{2}\/[0-9]{2}\/[0-9]{4}$/', $_POST['date']) &&
                    isset($_POST['note']) && !empty($_POST['note'])){
                
                $_POST['note'] = preg_replace("/(\r?\n)+/","<br />",strip_tags($_POST['note']));
                
                $qAddEvent = "INSERT INTO `".SQL_PREFIX."event` (idTarget, `date`, event) ".
                        "VALUES (
                            ".intval($_POST['target']).",
                            STR_TO_DATE('".$_POST['date']."','%d/%m/%Y'),
                            '".addslashes($_POST['note'])."'
                        )";
                
                mysql_query($qAddEvent);
                
                die(json_encode(array("success" => "ok")));
            }else{
                die(json_encode(array("error" => "bad inputs")));
            }
            break;
            
        case "delEvent":
            if(isset($_POST['idEvent']) && is_numeric($_POST['idEvent'])){
                mysql_query("DELETE FROM `".SQL_PREFIX."event` WHERE idevent = ".intval($_POST['idEvent']));
                die(json_encode(array("success" => "ok")));
            }else{
                die(json_encode(array("error" => "bad inputs")));
            }
            break;        
            
            
        case "delGroup":
            if(isset($_POST['idGroup']) && is_numeric($_POST['idGroup'])){
                mysql_query("DELETE FROM `".SQL_PREFIX."group` WHERE idGroup = ".intval($_POST['idGroup']));
                die(json_encode(array("success" => "ok")));
            }else{
                die(json_encode(array("error" => "bad inputs")));
            }
            break;                        
               
    }
}
?>
