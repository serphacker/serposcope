<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <title>SOFTPAE Google Magick Position Checker - Installation</title>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <meta name="description" content="SOFTPAE Google Position Checker" />
    <meta name="author" content="SOFTPAE, http://www.softpae.com" />
    <meta name="robots" content="noindex, nofollow" />
    <link media="all" href="Custom/App_Themes/css/screen2.css" type="text/css" rel="stylesheet" />
    <link href="favicon.ico" type="image/x-icon" rel="shortcut icon" />
  </head>
  <body>
    <div id="logo">
      <h1>Google Position Checker - Installation</h1>
    </div>
    <div id="navwrapper">
      <ul id="nav">
        <li id="home" class="nav-active">
          <a href="/">Daily Statistics</a>
        </li>
        <li id="results" class="">
          <a href="?process=results">All Results</a>
        </li>
        <li id="sites" class="">
          <a href="?process=addkey">Import</a>
        </li>
        <li id="members" class="">
          <a href="?process=account">My Account</a>
        </li>
        <li id="tools" class="">
          <a href="?process=tools">Tools</a>
        </li>
      </ul>
    </div>
    <div id="h2content">
      <strong>SOFTPAE.com</strong>
    </div>
    <hr />
    <div id="content" class="install">
    
		<br />
		
		<?php
		
		include "System/Plugins/configuration.php";
	
		if(isset($_GET["install"]) && $_GET["install"] == '1') {
			
			try {
				
				$dbh = new PDO($cn, $user, $pwd);

				@$dbh->query('SET NAMES UTF-8');
				@$dbh->query('SET NAMES UTF8');
				
				$dbh->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
				
				$sql = file_get_contents('System/Plugins/install.sql');
				
				$sql = str_replace("%%tblprefix%%", $tblprefix, $sql);
				
				$dbh->exec($sql);
				
		?>
		
		<p class="text-c">
			<strong>Installation completed, continue here: <a href=".">SOFTPAE Google Position Checker</a><strong>
		</p>
		<br />
		<p class="red text-c">
			<strong>NOTE: Default username and password is admin/admin. For security reason change your password after log in.</strong>
		</p>
		
		<?php
				unset($dbh);
				
			} catch (Exception $e) {
			
				echo '<p style=\"text-align:center;margin:15px;\"><strong>An error occured: ',  $e->getMessage(), "\n </strong><br /></p><br />";
				
			}
			
		} else {
		
		?>
		
			<h1>Database installation of GChecker - Google position checker by SOFTPAE.com</h1>
			
			<p>
				Note: Before you run installation, do following:
			</p>
			
			<ol>
				<li>Prepare MySQl database - create (or use existing) database and appropriate user with password</li>
				<li>Set up all required database information to System/Plugins/configuration.php file</li>
				<li>You can refresh this page to see your new configuation bellow</li>
				<li>Run installation by click to Install button</li>
			</ol>
			
			<h2>Your configuration data</h2>

			<ul>
				<li>DB Server: <?php echo $host ?></li>
				<li>Port: <?php echo $port ?></li>
				<li>Database: <?php echo $dbname ?></li>
				<li>Username: <?php echo $user ?></li>
				<li>Password: <?php echo $pwd ?></li>
				<li>Tables Prefix: <?php echo $tblprefix ?></li>
			</ul>
			
			<p class="red"><strong>If this informations is correct, run this installation.</strong></p>
			
			<br />
			
			<p><a href="?install=1" title="Run installation" class="button"> Install </a></p>
		
		<?php
	
		}

		?>
    </div>
    <hr />
    <div id="footer">
		(c) 2011 by SOFTPAE | email: <a href="mailto:magic@softpae.com" title="Send us an email.">magic@softpae.com</a> | <a href="http://codecanyon.net" title="Visit our website">SOFTPAE.com website</a></div>
  </body>
</html>
