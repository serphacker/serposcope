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

$error = "";
if(isset($_POST['name'])){

    $keywords = array();
    $sites= array();
    $groupOptions=array();

    if(empty($_POST['name'])){
        $error .= "No group name.<br/>";
    }

    if(empty($error)){
        if(!isset($_POST['module']) || !isset($modules[$_POST['module']])){
            $error .= "Need a valid module.<br/>";
        }else{
            $groupDefaultOptions=$modules[$_POST['module']]->getGroupOptions();
            foreach ($groupDefaultOptions as $option) {
                if(!isset($_POST[$option[0]]))
                    $error .= "Missing group option ".$option[0].".<br/>";
                else if(!preg_match($option[3], $_POST[$option[0]]))
                    $error .= "Invalid group option ".$option[0]." \"<strong>".h8($_POST[$option[0]])."</strong>\", doesn't match ".h8($option[3])." <br/>";
                else
                    $groupOptions[$option[0]] = $_POST[$option[0]];
            }
        }
    }

    if(isset($_POST['keywords'])){
        $_POST['keywords'] = explode("\n", $_POST['keywords']);
        foreach ($_POST['keywords'] as $keyword) {
            $keyword=trim($keyword);
            if(!empty($keyword)){
                if(!$modules[$_POST['module']]->validateKeyword($keyword)){
                    $error .= "Invalid target format ".$keyword;
                    break;
                }else{
                    if(!in_array($keyword, $keywords)){
                        $keywords[] = $keyword;
                    }
                }
            }
        }
    }

    if(isset($_POST['sites'])){
        $_POST['sites'] = explode("\n", $_POST['sites']);
        foreach ($_POST['sites'] as $site) {
            $site=trim($site);
            if(!empty($site)){
                if(!$modules[$_POST['module']]->validateTarget($site)){
                    $error .= "Invalid target format ".$site;
                    break;
                }else{
                    if(!in_array($site, $sites)){
                        $sites[] = $site;
                    }
                }
            }
        }
    }



    if(empty($error)){
        $q="INSERT INTO `".SQL_PREFIX."group`(`name`,`module`,`options`) VALUES(".
                "'".addslashes($_POST['name'])."',".
                "'".addslashes($_POST['module'])."',".
                "'".addslashes(json_encode($groupOptions))."')";

        if($db->query($q) !== true){
            $error = "SQL Error #1";
        }
    }

    if(empty($error)){
        $id=mysql_insert_id();
        $q="UPDATE `".SQL_PREFIX."group` SET position = ".intval($id)." WHERE idGroup = ".intval($id);
        if($db->query($q) !== true){
            $error = "SQL Error #2";
        }
    }

    if(empty($error) && !empty($keywords)){
        // insert the keywords
        $qKW="INSERT INTO `".SQL_PREFIX."keyword`(`idGroup`,`name`) VALUES ";
        for($i=0;$i<count($keywords);$i++){
            $qKW .= "(".intval($id).",'".addslashes($keywords[$i])."')";
            if($i < count($keywords)-1)
                $qKW .= ",";
        }
        if($db->query($qKW) !== true){
            $error = "SQL Error #3";
        }
    }

    if(empty($error) && !empty($sites)){
        // insert the sites
        $qSite="INSERT INTO `".SQL_PREFIX."target`(`idGroup`,`name`) VALUES ";
        for($i=0;$i<count($sites);$i++){
            $qSite .= "(".intval($id).",'".addslashes($sites[$i])."')";
            if($i < count($sites)-1)
                $qSite .= ",";
        }
        if($db->query($qSite) !== true){
            $error = "SQL Error #4";
        }
    }

    if(empty($error) && $id > 0){
        header("Location: view.php?idGroup=".$id);
    }
}
include("inc/header.php");
?>
<h2>New group</h2>
<?php
if(!empty($error)){
    echo "<div class='alert alert-error'>$error</div>\n";
}
?>
<div>
    <form method="POST" class="well form-horizontal" >
        <fieldset>
            <div class="control-group">
                <label class="control-label" for="name">Group name</label>
                <div class="controls">
                    <input type="text" class="input-xlarge" id="name" name="name" value="<?php echo isset($_POST['name']) ? h8($_POST['name']) : ""; ?>" >
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="keywords">Keywords</label>
                <div class="controls keywords">
                    <textarea type="text" name="keywords" class="input-xlarge" placeholder="one keyword per line" ><?php
                    echo isset($_POST['keywords']) && !empty($_POST['keywords']) ? h8(implode("\n", $_POST['keywords'])) : "";
                    ?></textarea>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="sites">Domain</label>
                <div class="controls sites">
                    <textarea type="text" name="sites" class="input-xlarge" placeholder="www.site.com or *.site.com" ><?php
                    echo isset($_POST['sites']) && !empty($_POST['sites']) ? h8(implode("\n", $_POST['sites'])) : "";
                    ?></textarea>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="module">Module</label>
                <div class="controls">
<?php
foreach ($modules as $name => $module) {
    echo
"<input type='radio' name='module' value='$name' class=itype >".
"   <img src='modules/$name/icon.png' /> $name <i></i>".
"<br/>";
}
?>
                </div>
            </div>
            <div id="groupopt" >

            </div>
            <div class="control-group">
                <div class="controls">
                    <button class="btn btn-primary" type="submit">Save</button>
                </div>
            </div>
        </fieldset>
    </form>
</div>
<script>
    $(function(){
        $('.itype').each(function(){
            $(this).prop('checked', false);
        });

        $('.itype').click(function(){
            $('#groupopt').empty();

            // retrieve all the option for this group
            $.ajax({
                type: "POST",
                url: "ajax.php",
                data: {
                    action: "getGroupOptions",
                    module: $(this).val()
                }
            }).done(function(rawdata){
                if(rawdata === ""){
                    return;
                }
                data = JSON.parse(rawdata);
                if(data !== null){
                    var html = "";
                    $(data).each(function(i,option){
                        if(option.length === 5){
                            html += '<div class="control-group">\n';
                            html += '<label class="control-label" for="' + option[0] + '">' + option[0] + '</label>\n';
                            html += '<div class="controls">\n';
                            html += '<input type="text" class="input-xlarge" id="' + option[0] + '" name="' + option[0] + '" value="' + option[1] + '" >\n';
                            if(option[2] != "")
                                html += '<p class="help-block">' + option[2] + '</p>\n';
                            html += '</div>\n';
                            html += '</div>\n';
                        }
                    });
                    $('#groupopt').html(html);
                }
            });
        });
    });
</script>
<?php
include('inc/footer.php');
?>