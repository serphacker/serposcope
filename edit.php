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
if (!file_exists('inc/config.php')) {
    header("Location: install/", TRUE, 302);
    die();
}
require('inc/config.php');
include('inc/define.php');
include('inc/common.php');
include('inc/user.php');
    

function getUserOptions() {
	return array(
            array('setting','0','Edycja ustawieñ','Aktywuje dostêp do edycji<br />ustawieñ projektu'),
            array('info','1','Edycja opisu','Aktywuje dostêp do edycji opisów'),
            array('events','1','Edycja notatek','Aktywuje dostêp do edycji notatek '),
            array('del','0','Przycisk Usuñ','Umo¿liwia usuniêcie wybranego projektu'),
            array('restart','0','Przycisk Uruchom','Aktywuje dostêp do rêcznego startu oraz usuwania wybranych danych'),
            array('export','1','Przycisk Export','Aktywuje dostêp do zmian widoku'),
			);
}
$group = null;
$keywords = array();
$sites = array();
if (isset($_GET['idGroup'])) {

    $qGroup = "SELECT * FROM `" . SQL_PREFIX . "group` WHERE idGroup = " . intval($_GET['idGroup']);
    $resGroup = $db->query($qGroup);
    if (($group = mysql_fetch_assoc($resGroup))) {

        $group['options'] = json_decode($group['options']);
        $group['user'] = json_decode($group['user']);

        $qKeyword = "SELECT idKeyword,name FROM `" . SQL_PREFIX . "keyword` WHERE idGroup = " . intval($_GET['idGroup']);
        $resKeyword = $db->query($qKeyword);
        while ($keyword = mysql_fetch_assoc($resKeyword)) {
            $keywords[$keyword['idKeyword']] = $keyword['name'];
        }


        $qSite = "SELECT idTarget,name FROM `" . SQL_PREFIX . "target` WHERE idGroup = " . intval($_GET['idGroup']);
        $resSite = $db->query($qSite);
        while ($site = mysql_fetch_assoc($resSite)) {
            $sites[$site['idTarget']] = $site['name'];
        }
    }
}

if ($group == null) {
    die();
}

if (isset($_POST['edit']) && $_POST['edit'] == "edit") {
    $error = null;
    $keywordsEDIT = array();
    $keywordsADD = array();
    $sitesEDIT = array();
    $sitesADD = array();
    $groupOptions = array();
    $userOptions = array();

    // KEYWORD
    if (isset($_POST['keywords'])) {
        $_POST['keywords'] = explode("\n", $_POST['keywords']);
        foreach ($_POST['keywords'] as $keyword) {
            $keyword=trim($keyword);
            if (!empty($keyword)) {
                if (!$modules[$group['module']]->validateKeyword($keyword)) {
                    $error .= "Invalid target format " . $keyword;
                    break;
                } else {
                    if(!in_array($keyword, $keywordsEDIT)){
                        $keywordsEDIT[] = $keyword;
                        if (!in_array($keyword, $keywords)) {
                            $keywordsADD[] = $keyword;
                        }
                    }
                }
            }
        }
    }

    if ($error) {
        header("Location: edit.php?idGroup=" . intval($_GET['idGroup']) . "&error=" . $error, TRUE, 302);
        die();
    }

    // delete old keyword
    $qDeleteKW = "DELETE FROM `" . SQL_PREFIX . "keyword` WHERE idGroup = " . intval($_GET['idGroup']) .
            " AND name NOT IN ('" . implode("','", array_map('addslashes', $keywordsEDIT)) . "')";
    $db->query($qDeleteKW);

    // add keyword
    if (!empty($keywordsADD)) {
        $qAddKW = "INSERT INTO `" . SQL_PREFIX . "keyword` (idGroup,name) VALUES";
        for ($i = 0; $i < count($keywordsADD); $i++) {
            $qAddKW .= " (" . intval($_GET['idGroup']) . ",'" . addslashes($keywordsADD[$i]) . "') ";
            if ($i != count($keywordsADD) - 1) {
                $qAddKW .= ",";
            }
        }
        $db->query($qAddKW);
    }


    // TARGET
    if (isset($_POST['sites'])) {
        $_POST['sites'] = explode("\n", $_POST['sites']);
        foreach ($_POST['sites'] as $site) {
            $site=trim($site);
            if(!empty($site)){
                if (!$modules[$group['module']]->validateTarget($site)) {
                    $error .= "Invalid target format " . $site;
                    break;
                } else {
                    if(!in_array($site, $sitesEDIT)){
                        $sitesEDIT[] = $site;
                        if (!in_array($site, $sites)) {
                            $sitesADD[] = $site;
                        }
                    }
                }
            }
        }
    }

    if ($error) {
        header("Location: edit.php?idGroup=" . intval($_GET['idGroup']) . "&error=" . $error, TRUE, 302);
        die();
    }

    // delete old site
    $qDeleteSite = "DELETE FROM `" . SQL_PREFIX . "target` WHERE idGroup=" . intval($_GET['idGroup']) .
            " AND name NOT IN ('" . implode("','", array_map('addslashes', $sitesEDIT)) . "')";
    $db->query($qDeleteSite);

    // add new site
    if (!empty($sitesADD)) {
        $qAddSite = "INSERT INTO `" . SQL_PREFIX . "target` (idGroup,name) VALUES";
        for ($i = 0; $i < count($sitesADD); $i++) {
            $qAddSite .= " (" . intval($_GET['idGroup']) . ",'" . addslashes($sitesADD[$i]) . "') ";
            if ($i != count($sitesADD) - 1) {
                $qAddSite .= ",";
            }
        }
        $db->query($qAddSite);
    }

    // group option
    $moduleOptions = $modules[$group['module']]->getGroupOptions();
    foreach ($moduleOptions as $option) {
        if (!isset($_POST[$option[0]]))
            die(header("Location: edit.php?idGroup=" . intval($_GET['idGroup']) . "&error=" . urlencode("Missing group option " . $option[0])));
        else if (!preg_match($option[3], $_POST[$option[0]]))
            die(header("Location: edit.php?idGroup=" . intval($_GET['idGroup']) . "&error=" .
                            urlencode("Invalid group option " . $option[0] . " " . htmlentities($_POST[$option[0]]) . " doesn't match " . $option[3])
            ));
        else {
            $groupOptions[$option[0]] = $_POST[$option[0]];
        }
    }

    $qUpdateGroup = "UPDATE `" . SQL_PREFIX . "group` SET " .
            "name = '" . addslashes($_POST['name']) . "', " .
            "options = '" . addslashes(json_encode($groupOptions)) . "' " .
            "WHERE idGroup = " . intval($_GET['idGroup']);

    if ($adminAcces || $set->setting) {$db->query($qUpdateGroup);}

    // user option
    foreach (getUserOptions() as $uoption) {
    		if (!isset($_POST[$uoption[0]]))
            die(header("Location: edit.php?idGroup=" . intval($_GET['idGroup']) . "&error=" . urlencode("Missing user option " . $uoption[0])));
            $userOptions[$uoption[0]] = $_POST[$uoption[0]];
    }
    
    $qUpdateUser = "UPDATE `" . SQL_PREFIX . "group` SET " .
            "name = '" . addslashes($_POST['name']) . "', " .
            "user = '" . addslashes(json_encode($userOptions)) . "' " .
            "WHERE idGroup = " . intval($_GET['idGroup']);
    if ($adminAcces || $userAcces){$db->query($qUpdateUser);}

    header("Location: view.php?idGroup=" . intval($_GET['idGroup']));
}

include("inc/header.php");
?>
<script>
        $( "#btn_4" ).css("border","solid 2px #D64B46");
        $( "#btn_4" ).css("border-radius","5px");
</script>
<h2>Edycja ustawieñ</h2>
<?php
if (isset($_GET['error'])) {
    $error = h8($_GET['error']);
}
if (!empty($error)) {
    echo "<div class='alert alert-error'>$error</div>\n";
}
?>
<div class="accordion-group">
    <form method="POST" class="well form-horizontal" action="edit.php?idGroup=<?php echo intval($_GET['idGroup']); ?>" >
        <fieldset>
            <div class="control-group <?php echo $hName; ?>">
                <label class="control-label" for="name">Nazwa</label>
                <div class="controls">
                    <input type="text" class="input-xlarge" id="name" name="name"  value="<?php echo h8($group['name']); ?>" >
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="keywords">S³owa kluczowe</label>
                <div class="controls keywords">
                    <textarea type="text" name="keywords" class="input-xlarge" placeholder="jedna fraza w lini" ><?php
                    echo !empty($keywords) ? h8(implode("\n", $keywords)) : "";
                    ?></textarea>
                </div>
            </div>
            
            <div class="control-group">
                <label class="control-label" for="sites">Domena</label>
                <div class="controls sites">
                    <textarea type="text" name="sites" class="input-xlarge" placeholder="www.site.com or *site.com" ><?php
                    echo !empty($sites) ? h8(implode("\n", $sites)) : "";
                    ?></textarea>
                </div>                
            </div>   

            <div id="groupopt" >
                <?php
                foreach ($modules[$group['module']]->getGroupOptions() as $option) {
                    echo '<div class="control-group">';
                    echo '<label class="control-label" for="' . $option[0] . '">' . $option[0] . '</label>';
                    echo '<div class="controls">';
                    echo '<input type="text" class="input-xlarge" id="' . $option[0] . '" name="' . $option[0] . '" value="' . h8(isset($group['options']->$option[0]) ? $group['options']->$option[0] : $option[1]) . '" >';
                    if ($option[2] != "")
                        echo '<p class="help-block">' . $option[2] . '</p>';
                    echo '</div>';
                    echo '</div>';
                    echo "\n";
                }
                ?>                
            </div>
            <div id="useropt"><center>
                <?php
                // echo h8($group['user']->setting).'<br/>';  //debug
                 foreach (getUserOptions() as $options) {
                 	$regexd = h8(isset($group['user']->$options[0]) ? $group['user']->$options[0] : $options[1]);
                 	if($regexd == '1'){$rex1 = "selected"; $rex0 = "";} else {$rex0 = "selected"; $rex1 = "";}
                 	if ($options[3] != "") {$helps = 'rel="tooltip" title ="'.$options[3].'"';} else {$helps = '';}
                 	if (!$adminAcces && !$userAcces){$helps ='rel="tooltip" title ="Nie posiadasz wystarczaj¹cych<br />uprawnieñ do zmiany tych opcji" class="disabled"';}
                    echo '<div class="control-group" >';
                    echo '<label class="control-label" for="' . $options[0] . '">' . $options[2] . '</label>';
                    echo '<div class="controls">';
                  	echo '<select id="' . $options[0] . '" name="' . $options[0] . '" ' . $helps .'>
                  <option value="1" '. $rex1 . '>TAK</option>
                  <option value="0" '. $rex0 . '>NIE</option>
                  </select>';
                    echo '</div>';
                    echo '</div>';
                    echo "\n";
                }
                ?>                
            </center></div>

            <div class='control-group' >
                <div class="finalize">
                    <button class="btn btn-primary" type="submit" name="edit" value="edit" >Zapisz</button>
                </div>
            </div>
        </fieldset>
    </form>
</div>
<?php
include('inc/footer.php');
?>
