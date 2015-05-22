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
	<span>GoogleFly to zautomatyzowany system sprawdzaj�cy indeksacje przez wyszukiwarki internetowe zdefiniowanych fraz (s��w kluczowych) witryny.<br/>
		Rozbudowany panel klienta umo�liwia na bie��co monitorowanie pozycji strony na dane zapytanie w popularnych wyszukiwarkach Google, Bing, Exalead<br/>
		</span>
</div>

<div id="2"><h5>Jak odczyta� wyniki</h5>
	<span>GoogleFly oferuje dwa sposoby interpretacji wynik�w
		<ul>
			<li>wykres liniowy - <a href="../view.php?wiev=chart&idGroup=<?php echo $_GET['idGroup']; ?>">zobacz</a><span class="aste">*</span></li>
			<li>tabela pozycji - <a href="../view.php?wiev=table&idGroup=<?php echo $_GET['idGroup']; ?>">zobacz</a><span class="aste">*</span></li>
			<li>kondycja witryny i social media - <a href="../view.php?wiev=social&idGroup=<?php echo $_GET['idGroup']; ?>">zobacz</a><span class="aste">*</span></li>
		</ul>
	Ponadto system tworzy zestawienie czasowe z ostatnich 7 test�w w formie multi-diagramu (<a href="../?idGroup=<?php echo $_GET['idGroup']; ?>">zobacz</a><span class="aste">*</span>) wraz z wyszczeg�lnieniem spadk�w i wzlot�w wszystkich s��w kluczowych
		<ul>
			<li>Aby pozna� szczeg�owe wyniki, kliknij interesuj�c� ci� warto�� na wykresie</li>
		</ul>
		</span>
</div>

<div id="3"><h5>Czy mog� wprowadza� w�asne s�owa kluczowe</h5>
	<span>Dodawanie s�ow kluczowych dost�pne jest w odp�atnej wersji systemu - niemniej jednak mo�na zmieni� istniej�ce s�owa na w�asne.<br/>
		</span>
</div>
<div id="4"><h5>Jak cz�sto sprawdzana jest pozycja w wyszukiwarce</h5>
	<span>Test pozycji odbywa si� codziennie w godzinach nocnych. S�owa kluczowe z poza listy TOP100 sprawdzane s� co tydzie�. Po pojawieniu si� w TOP100 system sprawdza ich pozycje codziennie<br/>
		</span>
</div>
<br /><br /><br />
<div class="asti"><span class="aste">*</span> Wymaga aktywnego zalogowania do panelu klienta</div>
</div>
   	<div class="footer">
		� 2014 by <a href="/">Google Position Checker</a> - Based by Google.inc search algorithm: Butterfly 1.0 beta | Penguin 3.0 | piRate 2.0 | Mobi 0.4
	</div>
    </body>
</html>