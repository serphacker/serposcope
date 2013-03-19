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


$supportedImport = array(IMPORT_SERPOSCOPE_CSV,IMPORT_RANKSFR_CSV,IMPORT_MYPOSEO_CSV);
$error="";

if( isset($_POST['group']) && is_numeric($_POST['group']) && isset($_POST['type']) && is_numeric($_POST['type'])){
    $fpFile=null;
    if(!isset($_FILES['file']) || $_FILES['file']['error'] !=0 || ($fpFile = fopen($_FILES['file']['tmp_name'], "r")) === false ){
        $error="File upload error";
    }else{
        $dateFormat="";
        $urlFormat="";
        $qGroup = "SELECT idGroup,name FROM `".SQL_PREFIX."group` WHERE idGroup = ".intval($_POST['group']);
        $resGroup=mysql_query($qGroup);
        if(!$resGroup || mysql_num_rows($resGroup) == 0){
            $error = "Unknown group";
        }else{
            switch(intval($_POST['type'])){
                case IMPORT_MYPOSEO_CSV:
                    $dateFormat="Y-m-d";
                    $urlFormat="url trouvée";
                    break;
                case IMPORT_RANKSFR_CSV:
                    $dateFormat="Y-m-d 00:00:00";
                    $urlFormat="url";
                    break;     
                case IMPORT_SERPOSCOPE_CSV:
                    $dateFormat="Y-m-d 00:00:00";
                    $urlFormat="url";
                default:
                    die("invalid import");
            }
            
            
            $line=fgets($fpFile);
            if(count(explode(",", $line)) > count(explode(";", $line))){
                $delim=",";
            }else{
                $delim=";";
            }
            
            $line=  explode($delim, $line);
            $dateIndex=-1;
            $positionIndex=-1;
            $kwIndex=-1;
            $urlIndex=-1;                    
            
            for($i=0;$i<count($line);$i++){
                switch(trim(strtolower($line[$i]),'"')){
                    case "position j":
                    case "position":
                        $positionIndex=$i;
                        break;
                    case "date":
                        $dateIndex=$i;
                        break;
                    case "keyword":
                    case "mot clé":
                    case "mot-clé":
                        $kwIndex=$i;
                        break;
                    
                    case $urlFormat:
                        $urlIndex=$i;
                        break;            
                }
            }
            if($dateIndex == -1 || $positionIndex == -1 || $kwIndex == -1 || $urlIndex == -1){
                $error = 
                "Invalid CSV file. File must be UTF-8 encoded. ".
                "Line must be delimited by char '$delim' .".
                "First line must be columns header.";
            }else{

                $keywords=array();
                $qSelectKeywords = "SELECT idKeyword,name FROM `".SQL_PREFIX."keyword` WHERE idGroup = ".intval($_POST['group']);
                $resSelectKeywords=mysql_query($qSelectKeywords);
                while($resSelectKeywords != null && ($kw=mysql_fetch_assoc($resSelectKeywords))){
                    $keywords[$kw['name']] = $kw['idKeyword'];
                }

                $sites=array();
                $qSelectSites = "SELECT idTarget,name FROM `".SQL_PREFIX."target` WHERE idGroup = ".intval($_POST['group']);
                $resSelectSites=mysql_query($qSelectSites);
                while($resSelectSites != null && ($targ=mysql_fetch_assoc($resSelectSites))){
                    $sites[$targ['name']] = $targ['idTarget'];
                }

                $check=array();

                while( ($line=fgetcsv($fpFile,0,$delim)) != null){
                    
                    if(count($line) > 3){
//                                $qInsertKW = "INSERT INTO keyword(idGroup,name) VALUES (".intval($_POST['group']).",'".addslashes($line[$kwIndex])."') ";
//                                echo $qInsertKW;
//                                continue;

                        $date = date($dateFormat,strtotime($line[$dateIndex]));
                        if(!isset($check[$date])){
                            $qInsertDate = "INSERT INTO `".SQL_PREFIX."check`(idGroup,`date`) VALUES (".intval($_POST['group']).",'".addslashes($date)."')";
                            if(mysql_query($qInsertDate)){
                                $check[$date] = $iCheck =mysql_insert_id();
                            }else{
                                $error = "SQL insert error (insert check)";
                                break;
                            }
                        }else{
                            $iCheck = $check[$date];
                        }                                

                        if(!isset($keywords[$line[$kwIndex]])){
                            $qInsertKW = "INSERT INTO `".SQL_PREFIX."keyword` (idGroup,name) VALUES (".intval($_POST['group']).",'".addslashes($line[$kwIndex])."') ";
                            if(mysql_query($qInsertKW)){
                                $keywords[$line[$kwIndex]] = $iKeyword = mysql_insert_id();
                            }else{
                                $error = "SQL insert error (insert keyword)";
                                break;
                            }
                        }else{
                            $iKeyword= $keywords[$line[$kwIndex]];
                        }                                

                        // add the site if not yet in the target table
                        $parsed = parse_url($line[$urlIndex]);
                        if(isset($parsed['host'])){
                            $site= $parsed['host'];

                            if(!isset($sites[$site])){
                                $qInsertTarget = "INSERT INTO `".SQL_PREFIX."target` (idGroup,name) VALUES (".intval($_POST['group']).",'".addslashes($site)."') ";
                                if(mysql_query($qInsertTarget)){
                                    $sites[$site] = $iSite = mysql_insert_id();
                                }else{
                                    $error = "SQL insert error (insert target)";
                                    break;
                                }
                            }else{
                                $iSite=$sites[$site];
                            }

                            $qInsertRank="INSERT INTO `".SQL_PREFIX."rank`(idCheck,idTarget,idKeyword,position,url) ".
                                "VALUES(".$iCheck.",".$iSite.",".$iKeyword.",".intval($line[$positionIndex]).",'".addslashes($line[$urlIndex])."') ".
                                "ON DUPLICATE KEY UPDATE position = ".intval($line[$positionIndex]).", url = '".addslashes($line[$urlIndex])."' ";
                            if(!mysql_query($qInsertRank)){
                                    $error = "SQL insert error (insert rank) <b>".h8($qInsertRank)."</b>.<br/>".mysql_error();
                                    break;
                            }                                    
                        }


                    }
                }

                if(empty($error)){
                    header("Location: view.php?idGroup=".intval($_POST['group']));
                }
            }
        }
        
        fclose($fpFile);
    }
}

include("inc/header.php");
?>
<h2>Import group</h2>
<?php
if(isset($_GET['error'])){
    $error = h8($_GET['error']);
}
if(!empty($error)){
    echo "<div class='alert alert-error'>$error</div>\n";
}
?>
<div>
    <form method="POST" class="well form-horizontal" action="import.php" enctype="multipart/form-data" >
    <fieldset>
        <div class="control-group">
            <label class="control-label" for="group">Group</label>
            <div class="controls">
                <select name="group" >
<?php
$groups = array();
$qImportGroup = "SELECT idGroup,name FROM `".SQL_PREFIX."group` ORDER BY idGroup DESC"; // recent first
$resImportGroup = mysql_query($qImportGroup);
while($resImportGroup && ($group=mysql_fetch_assoc($resImportGroup)) ){
    echo "<option value=".$group['idGroup']." >".h8($group['name'])."</option>";
}
?>
                </select>
                <div class="help-block" >
                    <span class="label label-warning">Warning</span> It's highly 
                    recommanded to use import on new and empty group.
                </div>
            </div>
        </div>
        
        <div class="control-group">
            <label class="control-label" for="type">Source type</label>
            <div class="controls">
                <select name="type" >
                    <option value="<?php echo IMPORT_SERPOSCOPE_CSV; ?>" >Serposcope CSV</option>
                    <option value="<?php echo IMPORT_RANKSFR_CSV; ?>" >Ranks.fr CSV</option>
                    <option value="<?php echo IMPORT_MYPOSEO_CSV; ?>" >Myposeo CSV</option>
                </select>
                <div class="help-block" >
                    <!--
                    Help for other kind of import :
                    <ul>
                    </ul>
                    -->
                </div>
            </div>
        </div>
        
        <div class="control-group">
            <label class="control-label" for="type">File</label>
            <div class="controls">
                <input type="file" class="input-file" name="file" />
            </div>
        </div>        
        <button class="btn btn-primary" type="submit" name="import" value="import" >Import</button>            
    </fieldset>
</form>
</div>
<?php
include('inc/footer.php');
?>