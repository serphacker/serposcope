<?php

if(!defined('INCLUDE_OK')){
    die();
}

?>

<style>
    table tr td {
        padding-left:10px;
        padding-right:10px;
    }
</style>
<table>
<?php

function can_write_dir($dir){
    $randStuff = sha1(microtime());
    $randFilename = $dir."/install_testrw_".uniqid();
    if( file_put_contents($randFilename, $randStuff) !== FALSE){
        if( file_get_contents($randFilename) === $randStuff ){
            unlink($randFilename);
            return true;
        }
    }
    return false;
}

$success=true;

// php version
echo '<tr><td><strong>PHP Version</strong> </td>';
if( PHP_MAJOR_VERSION < 5 || (PHP_MAJOR_VERSION == 5 && PHP_MINOR_VERSION < 2)
){
    echo '<td class="text-error" > Need <strong>5.2</strong> (current <strong>'.PHP_MAJOR_VERSION.'.'.PHP_MINOR_VERSION.')</strong></td></tr>';
    $success=false;
}else{
    echo '<td class="text-success" > OK</td></tr>';
}

// check RW permission
echo '<tr><td><strong>File system </strong> </td>';
$incdir=dirname(dirname($_SERVER['SCRIPT_FILENAME'])."..")."/inc/";
if( !can_write_dir($incdir) ){
    echo '<td class="text-error" > Can\' write <strong>'.$incdir.'</strong> edit fs perm <code>chmod o+rwx '.$incdir.'</code></td></tr>';
    $success=false;
}else{
    echo '<td class="text-success" > OK </td></tr>';
}

// mysql
echo '<tr><td><strong>MySQL extension</strong> </td>';
if( !function_exists('$db->query')){
    echo '<td class="text-error" > MySQL extension not installed</td></tr>';
    $success=false;
}else{
    echo '<td class="text-success" > OK</td></tr>';
}

// curl
echo '<tr><td><strong>Curl extension</strong> </td>';
if( !function_exists('curl_version')){
    echo '<td class="text-error" > Curl extension not installed</td></tr>';
    $success=false;
}else{
    echo '<td class="text-success" > OK</td></tr>';
}

// safe mode
echo '<tr><td><strong>Safe mode</strong> </td>';
if( ini_get('safe_mode') ){
    echo '<td class="text-error" > Safe mode is ON, turn off in php.ini <code>safe_mode = Off</code></td></tr>';
    $success=false;
}else{
    echo '<td class="text-success" > OK</td></tr>';
}

// ini time
echo '<tr><td><strong>Max execution time</strong> </td>';
if( ini_get('max_execution_time') ){
    echo '<td class="text-warning" > Max execution time is <strong>'.ini_get('max_execution_time').'</strong>, edit in php.ini <code>max_execution_time = 0</code></td></tr>';
}else{
    echo '<td class="text-success" > OK</td></tr>';
}


if (strtoupper(substr(PHP_OS, 0, 3)) === 'WIN') {
    // check for windows
    
}else{
    // check for UNIX (mac/linux/...)
    echo '<tr><td><strong>Disabled functions</strong> </td>';
    $usedFu = array('posix_kill','posix_getsid');
    $disabled = array();
    foreach ($usedFu as $fu) {
        if(!function_exists($fu)){
            $disabled[] = $fu;
        }
    }
    
    if(!empty($disabled)){
        echo '<td class="text-warning" > <strong>'.implode(",",$disabled).'</strong> disabled, edit php.ini <code>disable_functions = </code> </td></tr>';
    }else{
        echo '<td class="text-success" > OK</td></tr>';
    }
    
}

echo "</table>";

echo "<div style='text-align: center; margin-top: 20px;' >";
if($success){
    echo "<a href='?step=2' class='btn btn-primary' >Next</a>";
}else{
    echo "<span class='btn disabled' >Next</a>";
}
echo "</div>";
?>