<!DOCTYPE html>
<html lang="fr">
    <head>
        <title>SeoTest</title>
        <meta charset="windows-1250" />
        <meta name="robots" content="noindex" />
        <link href="lib/bootstrap/css/bootstrap.css" rel="stylesheet" type="text/css" />
        <link href="lib/datepicker/css/datepicker.css" rel="stylesheet" type="text/css" />
        <link href="css/custom.css" rel="stylesheet" type="text/css" />

        <script src="lib/jquery/jquery-1.8.2.min.js" ></script>
        <script src="lib/jquery-blockUI/jquery.blockUI.js" ></script>

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
                           <li><a href='/help/' title='Pomoc' rel='tooltip' data-placement='bottom'><img src='img/help.png'/></a></li>
                           <li><a href='/' onclick='logout();' title='Zaloguj'rel='tooltip' data-placement='bottom'><img src='img/user.png' /></a></li>
                            </ul>
                        </div>
                    </div>

                </div>
            </div>
        </div>

        <div class="container" id="bodycontainer" >

<h1 class="tdregress"><big>Error 401</big><h3>Ta strona wymaga logowania...</h3></h1>
Wprowadzony login i has�o zawieraj� b�edy lub wygas�y. Kliknij <a href="/" >tutaj</a> by si� zalogowa� ponownie lub zapoznaj si� z <a href="/help/">instrukcj� obs�ugi</a>.
        </div>
   	<div class="footer">
		� 2014 by <a href="/">Google Position Checker</a> - Based by Google.inc search algorithm: Butterfly 1.0 beta | Penguin 3.0 | piRate 2.0 | Mobi 0.4
	</div>
    </body>
</html>