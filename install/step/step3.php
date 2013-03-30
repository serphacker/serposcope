<?php

if(!defined('INCLUDE_OK')){
    die();
}

if( 
    !isset($_POST['host']) ||
    !isset($_POST['login']) ||
    !isset($_POST['password']) ||
    !isset($_POST['database']) ||
    !isset($_POST['prefix'])
){
    header('Location: ?step=1',true,302);
    die();
}


$cfgToWrite='<?php
    
define(\'SQL_HOST\',\''.  addcslashes($_POST['host'],"'").'\');
define(\'SQL_LOGIN\',\''.addcslashes($_POST['login'],"'").'\');
define(\'SQL_PASS\',\''.addcslashes($_POST['password'],"'").'\');
define(\'SQL_DATABASE\',\''.addcslashes($_POST['database'],"'").'\'); 
define(\'SQL_PREFIX\',\''.addcslashes($_POST['prefix'],"'").'\');

?>';

if( file_put_contents($incdir."config.php", $cfgToWrite) ===FALSE ){
    echo "Can't write to <code>".$incdir."config.php</code> . Create the file with the following content : <br/><br/>
        <pre>".h8($cfgToWrite)."</pre>
    ";
}else{
    // everything OK
    echo "
    <div class='alert alert-success' >
        Installation done <br/>
        <a href='../' >Go ninja go</a><br/><br/>
    </div> 
";        
}

?>