<!DOCTYPE html>
<html lang="fr">
    <head>
        <title>SeoTest</title>
        <meta charset="windows-1250" />
        <meta name="robots" content="noindex" />
        <link href="../lib/bootstrap/css/bootstrap.css" rel="stylesheet" type="text/css" />
        <link href="../lib/datepicker/css/datepicker.css" rel="stylesheet" type="text/css" />
        <link href="../css/custom.css" rel="stylesheet" type="text/css" />

        <script src="../lib/jquery/jquery-1.8.2.min.js" ></script>
        <script src="../lib/jquery-blockUI/jquery.blockUI.js" ></script>

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
        <style>
        	.aste {color:red; font-weight:bold; font-size:19px;}

<?php 
	if ($_GET['idGroup']) {echo ".aste, .asti {display:none;}";}
?>
        </style>
       </head>
    <body>
        <div class="navbar navbar-fixed-top">
            <div class="navbar-inner">
                <div class="container">
                    <div class="menu-vcenter" >
                        <div class='mybrand' >
                            <a class='link' href="../" ><span class="logo">fly</span><span id='cur-version' > HelpDesk</span></a>
                        </div>
                        
                        <div class="nav-collapse collapse">
                            <ul class="nav">
                           <li><a href='/help/' title='Pomoc' rel='tooltip' data-placement='bottom'><img src='../img/help.png'/></a></li>
                           <li><a href='../' onclick='logout();' title='Zaloguj'><img src='../img/user.png' /></a></li>
                            </ul>
                        </div>
                    </div>

                </div>
            </div>
        </div>

        <div class="container" id="bodycontainer" >
<div id="1"><h5>Czym jest GoogleFly</h5>
	<span>GoogleFly to zautomatyzowany system sprawdzający indeksacje przez wyszukiwarki internetowe zdefiniowanych fraz (słów kluczowych) witryny.<br/>
		Rozbudowany panel klienta umożliwia na bieżąco monitorowanie pozycji strony na dane zapytanie w popularnych wyszukiwarkach Google, Bing, Exalead<br/>
		</span>
</div>

<div id="2"><h5>Jak odczytać wyniki</h5>
	<span>GoogleFly oferuje dwa sposoby interpretacji wyników
		<ul>
			<li>wykres liniowy - <a href="../view.php?wiev=chart&idGroup=<?php echo $_GET['idGroup']; ?>">zobacz</a><span class="aste">*</span></li>
			<li>tabela pozycji - <a href="../view.php?wiev=table&idGroup=<?php echo $_GET['idGroup']; ?>">zobacz</a><span class="aste">*</span></li>
			<li>kondycja witryny i social media - <a href="../view.php?wiev=social&idGroup=<?php echo $_GET['idGroup']; ?>">zobacz</a><span class="aste">*</span></li>
		</ul>
	Ponadto system tworzy zestawienie czasowe z ostatnich 7 testów w formie multi-diagramu (<a href="../?idGroup=<?php echo $_GET['idGroup']; ?>">zobacz</a><span class="aste">*</span>) wraz z wyszczególnieniem spadków i wzlotów wszystkich słów kluczowych
		<ul>
			<li>Aby poznać szczegółowe wyniki, kliknij interesującą cię wartość na wykresie</li>
		</ul>
		</span>
</div>

<div id="3"><h5>Czy mogę wprowadzać własne słowa kluczowe</h5>
	<span>Dodawanie słow kluczowych dostępne jest w odpłatnej wersji systemu - niemniej jednak można zmienić istniejące słowa na własne.<br/>
		</span>
</div>
<div id="4"><h5>Jak często sprawdzana jest pozycja w wyszukiwarce</h5>
	<span>Test pozycji odbywa się codziennie w godzinach nocnych. Słowa kluczowe z poza listy TOP100 sprawdzane są co tydzień. Po pojawieniu się w TOP100 system sprawdza ich pozycje codziennie<br/>
		</span>
</div>
<br /><br /><br />
<div class="asti"><span class="aste">*</span> Wymaga aktywnego zalogowania do panelu klienta</div>
</div>
   	<div class="footer">
		© 2014 by <a href="/">Google Position Checker</a> - Based by Google.inc search algorithm: Butterfly 1.0 beta | Penguin 3.0 | piRate 2.0 | Mobi 0.4
	</div>
    </body>
</html>
