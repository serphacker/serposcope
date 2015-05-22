<!DOCTYPE html>
<html lang="fr">
    <head>
        <title>SeoTest</title>
        <meta charset="windows-1250" />
        <meta name="robots" content="noindex" />
        <link href="lib/datepicker/css/datepicker.css" rel="stylesheet" type="text/css" />
        <link href="lib/bootstrap/css/bootstrap.css" rel="stylesheet" type="text/css" />
        <link href="css/custom.css" rel="stylesheet" type="text/css" />

        <script type="text/javascript">
        function logout() {
        var xmlhttp;
        if (window.XMLHttpRequest) {
          xmlhttp = new XMLHttpRequest();
        }
        // code for IE
        else if (window.ActiveXObject) {
          xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
        }
        if (window.ActiveXObject) {
        // IE clear HTTP Authentication
        document.execCommand("ClearAuthenticationCache");
        window.location.href='/';
        } else {
        xmlhttp.open("GET", '/', true, "logout", "logout");
        xmlhttp.send("");
        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState == 4) {window.location.href='/';}
        }
        }
        return false;
        }
        </script>
    </head>
    <body>
        <div class="navbar navbar-fixed-top">
            <div class="navbar-inner">
                <div class="container">
                    <div class="menu-vcenter" >
                        <div class='mybrand' >
                            <a class='link' href="/" ><span class="logo">fly</span><span id='cur-version' >V 1.0.7beta</span></a>
                        </div>
                        
                        <div class="nav-collapse collapse">
                            <ul class="nav">
                           <li><a href='/' onclick='logout();' title='Zaloguj'><img src='img/user.png' /></a></li>
                            </ul>
                        </div>
                    </div>

                </div>
            </div>
        </div>

        <div class="container" id="bodycontainer" >

<h1 class="tdregress"><big>Error 401</big><h3>Ta strona wymaga logowania...</h3></h1>
Wprowadzony login i hasło zawierają błedy lub wygasły. Kliknij <a href="/" >tutaj</a> by się zalogować ponownie.
        </div>
   	<div class="footer">
		© 2014 by <a href="/">Google Position Checker</a> - Based by Google.inc search algorithm: Butterfly 1.0 beta | Penguin 3.0 | piRate 2.0 | Mobi 0.4
	</div>
    </body>
</html>