<?php

if(!defined('INCLUDE_OK')){
    die();
}

$nextStep = 2;
if(isset($_POST['host']) && isset($_POST['database'])){
    if(check_db_co($_POST['host'], (isset($_POST['login']) ? $_POST['login'] : null), (isset($_POST['password']) ? $_POST['password'] : null), $_POST['database'])){

        echo '
    <div class="alert alert-success">
        Successfully connected to the DB
    </div>
';
        // it's ok
        $prefix="";
        if(isset($_POST['prefix']) && preg_match('/^[a-zA-Z0-9_]+$/', $_POST['prefix'])){
            $prefix=$_POST['prefix'];
        }

        if(!mysql_query("SELECT 1 FROM `".$prefix."group` LIMIT 1")){

//            die(mysql_error());
            echo '
        <div class="alert alert-info">
            No serposcope tables found, recreating all the tables...
        </div>
    ';

            drop_tables($prefix);
            if(create_tables($prefix) && mysql_errno() == 0){
                echo "
                    <div class='alert alert-success'>
                        Successfully recreated the tables
                    </div>
                ";
                $nextStep=3;
            }else{
                echo "
            <div class='alert alert-error'>
                SQL error : ".mysql_error()."
            </div>";
                drop_tables($prefix);
            }

        } else {

            $oldversion=null;
            if($res=mysql_query("SELECT version FROM `".$prefix."version` LIMIT 1")){
                if( $array=  mysql_fetch_assoc($res) ){
                    $oldversion=$array['version'];
                }
            }

            if($oldversion == null){
                $oldversion=1;
            }

            if($oldversion == SQL_VERSION){

                echo '
            <div class="alert alert-info">
                Serposcope tables found, no table to upgrade ...
            </div>
        ';
                $nextStep=3;
            }else{
                echo '
            <div class="alert alert-info">
                Serposcope tables found, upgrading from '.($oldversion ? $oldversion : "pre-0.9.8").' ...
            </div>
        ';
                if(($query=upgrade_tables($prefix,$oldversion)) === true){
                    echo "
                <div class='alert alert-success'>
                    Successfully upgraded the tables
                </div>
                    ";
                    $nextStep=3;
                }else{
                    echo "
                <div class='alert alert-error'>
                    SQL error : $query <br/>
                    ".mysql_error()."
                </div>";
                }

            }
        }
    }else{
        echo '
    <div class="alert alert-error">
        <button type="button" class="close" data-dismiss="alert">&times;</button>
        Can\'t connect to the DB, check the credentials
    </div>
';
    }
}


if($nextStep == 2){

?>

<div>
Specify database credentials. <br/>
If a previous serposcope database is found, a table upgrade will be attempted. <br/>
Be sur to have done a SQL backup of your serposcope DB before doing an upgrade attempt. <br/>
<br/>
</div>
<form method='post' class="form-horizontal" action='?step=<?php echo $nextStep; ?>' >

    <div class="control-group">
        <label class="control-label" for="host">Host</label>
        <div class="controls">
            <input id="host" name="host" value="<?php echo isset($_POST['host']) ? h8($_POST['host']) : ""; ?>" />
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="login">Login</label>
        <div class="controls">
            <input id="login" name="login" value="<?php echo isset($_POST['login']) ? h8($_POST['login']) : ""; ?>" />
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="password">Password</label>
        <div class="controls">
            <input id="password" name="password" type="password" value="<?php echo isset($_POST['password']) ? h8($_POST['password']) : ""; ?>" />
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="database">Database</label>
        <div class="controls">
            <input id="database" name="database" value="<?php echo isset($_POST['database']) ? h8($_POST['database']) : ""; ?>" />
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="prefix">Prefix</label>
        <div class="controls">
            <input id="prefix" name="prefix" value="<?php echo isset($_POST['prefix']) ? h8($_POST['prefix']) : ""; ?>" />
        </div>
    </div>

<?php

echo "<div style='text-align: center; margin-top: 20px;' >";
echo "<input type=submit class='btn btn-primary' value='Next' />";
echo "</div>";

}else if($nextStep == 3){
    echo "
        <form method='post' class='form-horizontal'  action='?step=$nextStep' >
            <input type=hidden name=host value='".(isset($_POST['host']) ? h8($_POST['host']) : "")."' />
            <input type=hidden name=login value='".(isset($_POST['login']) ? h8($_POST['login']) : "")."' />
            <input type=hidden name=password value='".(isset($_POST['password']) ? h8($_POST['password']) : "")."' />
            <input type=hidden name=database value='".(isset($_POST['database']) ? h8($_POST['database']) : "")."' />
            <input type=hidden name=prefix value='".h8($prefix)."' />
            <div style='text-align: center; margin-top: 20px;' >
                <input type=submit class='btn btn-primary' value='Next' />
            </div>
        </form>
    ";
}

?>

</form>

