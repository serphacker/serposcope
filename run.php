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


$generalOptions = array(

    array('runs','','username','/^.+$/','text'),
    array('pass','','password','/^.+$/','textarea'),
//    array('prox','yes','Rotate the proxy on new keyword','/^yes|no$/','switch'),

);

function displayOptForm($groupName, $groupOptions) {
    global $options, $formErrors;

    foreach ($groupOptions as $opt) {
        if (!empty($opt[4])) {
            echo '        
           <div class="control-group ' . (isset($formErrors[$groupName][$opt[0]]) ? "error" : "") . '" >
               <label class="control-label" for="' . h8($groupName . "_" . $opt[0]) . '">' . h8($opt[0]) . '</label>
               <div class="controls">
   ';
            switch ($opt[4]) {
                case "switch":
                    echo "<input type=radio  name='" . h8($groupName . "_" . $opt[0]) . "' value='yes' " . ($options[$groupName][$opt[0]] === "yes" ? "checked" : "") . " /> Yes ";
                    echo "<input type=radio  name='" . h8($groupName . "_" . $opt[0]) . "' value='no' " . ($options[$groupName][$opt[0]] === "no" ? "checked" : "") . " /> No ";
                    break;

                case "text":
                    echo "<input type=text name='" . h8($groupName . "_" . $opt[0]) . "' value='" . h8($options[$groupName][$opt[0]]) . "' />";
                    break;
                case "textarea":
                    echo "<textarea name='" . h8($groupName . "_" . $opt[0]) . "' >" . h8($options[$groupName][$opt[0]]) . "</textarea>";
                    break;
            }
            echo '<p class="help-block">' . $opt[2] . '</p>';
            echo "
               </div>
           </div>
   ";
        }
    }
}

$formErrors = array();
if (!empty($_POST)) {
    foreach ($_POST as $key => $value) {

        $exploded = explode("_", $key);
        if (count($exploded) < 2) {
            continue;
        }

        $keyModule = array_shift($exploded);
        $keyName = implode("_", $exploded);

        $moduleDefaultOptions = null;
        if ($keyModule === "run") {
            $moduleDefaultOptions = $generalOptions;
        } else {
            if (isset($modules[$keyModule])) {
                $moduleDefaultOptions = $modules[$keyModule]->getGlobalOptions();
            }
        }

        if (!is_array($moduleDefaultOptions) || empty($moduleDefaultOptions)) {
            continue;
        }

        $defaultOption = null;
        foreach ($moduleDefaultOptions as $optionCandidat) {
            if ($optionCandidat[0] === $keyName) {
                $defaultOption = $optionCandidat;
            }
        }

        if ($defaultOption == null) {
            continue;
        }

        // if the option is different from the loaded one
        if ($options[$keyModule][$keyName] == $value) {
            continue;
        }

        // if it's the default value, erase from the DB
        if ($defaultOption[1] == $value) {
            $db->query("DELETE FROM `" . SQL_PREFIX . "option` WHERE name = '" . addslashes($keyModule . "_" . $keyName) . "'");
            continue;
        }

        // it's a new value must match the regex
        if (!preg_match($defaultOption[3], $value)) {
            $formErrors[$keyModule][$keyName] = 1;
            continue;
        }

        // the regex match, insert in the DB
        $db->query("INSERT INTO `" . SQL_PREFIX . "option` VALUES " .
                "('" . addslashes($keyModule . "_" . $keyName) . "', '" . addslashes($value) . "') " .
                "ON DUPLICATE KEY UPDATE value = '" . addslashes($value) . "'"
        );
    }
}

// reload the options

$options = load_options();

include("inc/header.php");

echo '<script>
        $( "#btn_0" ).css("border","solid 2px #D64B46");
        $( "#btn_0" ).css("border-radius","5px");
</script>
<form method=POST class="well form-horizontal" >
    <fieldset>';

echo "<div class='accordion-group'><h4>General</h4>";
displayOptForm('run', $generalOptions);
echo "</div>";
echo "<br />
<div class='controls' >";
if($generalOptions) {echo"        <input type=submit class='btn btn-primary' value='Zapisz ustawienia' />";}
echo "        <input class='btn btn-run-all btn-warning' title='Uruchom' rel='tooltip' data-placement='bottom' value='Ręczny start skanera' />
    </div>

    </fieldset>
    
</form>";
echo "";
include('inc/footer.php');
?>
