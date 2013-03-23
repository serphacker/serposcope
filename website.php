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
include("inc/header.php");



?>
<h2>Websites</h2>
<div>
    <table class='table' >
        <thead>
            <tr><th>hostname</th><th>group</th><th>actions</th></tr>
        </thead>
        <tbody>
<?php
    $q="SELECT `".SQL_PREFIX."target`.name tname,idTarget, ".
            "`".SQL_PREFIX."group`.idGroup,`".SQL_PREFIX."group`.name gname ".
            "FROM `".SQL_PREFIX."target` ".
            "JOIN `".SQL_PREFIX."group` USING(idGroup) ";
    $result = $db->query($q);
    
    while($result && ($row=mysql_fetch_assoc($result))){
        echo "<tr>" .
            "<td>".h8($row['tname'])."</td>".
            "<td>".h8($row['gname'])."</td>".
            "<td> <a class='btn' href='view.php?idGroup=".$row['idGroup']."#".h8($row['tname'])."' >view</a> ".
            " <a class='btn group-btn-info' data-id='".$row['idTarget']."' >info</a> ".
            " <a class='btn' >calendar</a></td>".
            "</tr>";
    }
?>
        </tbody>
    </table>
</div>
<?php
include('inc/footer.php');
?>