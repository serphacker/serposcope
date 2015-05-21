<?php
$users = $_SERVER['REMOTE_USER'];
$ID = $_GET['idGroup'];
$debug .= 'user.php debug ok';

/******************* get administrations options from loggin admin *******************/
if ($users == 'admin'){
	$adminAcces = true;			// usage: if($adminAcces) {...}
	} else {
	$hName = 'hName';					//add hidden class usage: echo hName; in class
}

/******************* get administrations options from loggin user *******************/
if ($users == 'impress' || $users == 'hyzne'){
	$userAcces = true;
	} else {
	
}


/*********************** def standard user ID for header menu ************************/
if ($ID) {$IDH = $ID;}
if (!$IDH && $users == 'wideofilmowanie') {$IDH = '4';}
if (!$IDH && $users == 'impress') {$IDH = '9';}
if (!$IDH && $users == 'hyzne') {$IDH = '1';}

/********************* get administrations options from project **********************/
$qGroup = "SELECT * FROM `".SQL_PREFIX."group` WHERE idGroup = " . intval($IDH);
$resGroup = $db->query($qGroup);
if (($groupd = mysql_fetch_assoc($resGroup))) {$set = json_decode($groupd['user']);}
		//usage: $set->options  np: $set->setting


$qRund = "SELECT *  FROM `".SQL_PREFIX."option` WHERE `name` LIKE '%run_".$setname."%'";
$resRund = $db->query($qRund);
if (($round = mysql_fetch_assoc($resRund))) {$setround = $round['value'];}
		//usage: $set->options  np: $set->setting


?>
