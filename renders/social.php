<?php

parse_str($_SERVER['QUERY_STRING']);
if($test) {
include_once('../inc/functions.php');
render('', $test, '');
	}

function nf($n, $precision = 1) {
	$n = (int)($n);
    if ($n < 1000) {
        $n_format = number_format($n);
    } else if ($n < 1000000) {
        $n_format = number_format($n / 1000, $precision) . ' K';
    } else if ($n < 1000000000) {
        $n_format = number_format($n / 1000000, $precision) . ' M';
    } else {
        $n_format = number_format($n / 1000000000, $precision) . 'G';
    }
    return $n_format;
}

class Get_Alexa_Ranking{
	public function get_rank($domain){
		$url = "http://data.alexa.com/data?cli=10&dat=snbamz&url=".$domain;
		$ch = curl_init();  
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
		curl_setopt($ch,CURLOPT_CONNECTTIMEOUT,2); 
		curl_setopt($ch, CURLOPT_URL, $url);  
		$data = curl_exec($ch);  
		curl_close($ch);  
		$xml = new SimpleXMLElement($data);  
		$popularity = $xml->xpath("//POPULARITY");
		$rank = nf((string)$popularity[0]['TEXT']); 
		$reach = $xml->xpath("//RANK");
		$reac = nf((string)$reach[0]['DELTA']);
		if ($reac) {$rank = '<span title="DELTA: '.$reac.'">'.$rank.'</span>';}
		if(!$rank) {$rank = '0';}
		return $rank;
	}
}

class GooglePageRankChecker {
  private static $instance;
  public static function getRank($page){
    if(!isset(self::$instance)) {
      self::$instance = new self();
    }
    return self::$instance->check($page);
  }

  function stringToNumber($string,$check,$magic) {
    $int32 = 4294967296;  // 2^32
      $length = strlen($string);
      for ($i = 0; $i < $length; $i++) {
          $check *= $magic;
          if($check >= $int32) {
              $check = ($check - $int32 * (int) ($check / $int32));
              $check = ($check < -($int32 / 2)) ? ($check + $int32) : $check;
          }
          $check += ord($string{$i});
      }
      return $check;
  }

  function createHash($string) {
    $check1 = $this->stringToNumber($string, 0x1505, 0x21);
      $check2 = $this->stringToNumber($string, 0, 0x1003F);

    $factor = 4;
    $halfFactor = $factor/2;

      $check1 >>= $halfFactor;
      $check1 = (($check1 >> $factor) & 0x3FFFFC0 ) | ($check1 & 0x3F);
      $check1 = (($check1 >> $factor) & 0x3FFC00 ) | ($check1 & 0x3FF);
      $check1 = (($check1 >> $factor) & 0x3C000 ) | ($check1 & 0x3FFF);

      $calc1 = (((($check1 & 0x3C0) << $factor) | ($check1 & 0x3C)) << $halfFactor ) | ($check2 & 0xF0F );
      $calc2 = (((($check1 & 0xFFFFC000) << $factor) | ($check1 & 0x3C00)) << 0xA) | ($check2 & 0xF0F0000 );

      return ($calc1 | $calc2);
  }

  function checkHash($hashNumber)
  {
      $check = 0;
    $flag = 0;

    $hashString = sprintf('%u', $hashNumber) ;
    $length = strlen($hashString);

    for ($i = $length - 1;  $i >= 0;  $i --) {
      $r = $hashString{$i};
      if(1 === ($flag % 2)) {
        $r += $r;
        $r = (int)($r / 10) + ($r % 10);
      }
      $check += $r;
      $flag ++;
    }

    $check %= 10;
    if(0 !== $check) {
      $check = 10 - $check;
      if(1 === ($flag % 2) ) {
        if(1 === ($check % 2)) {
          $check += 9;
        }
        $check >>= 1;
      }
    }

    return '7'.$check.$hashString;
  }

  function check($page) {
    $socket = fsockopen("toolbarqueries.google.com", 80, $errno, $errstr, 30);
    if($socket) {
      $out = "GET /tbr?client=navclient-auto&ch=".$this->checkHash($this->createHash($page)).
              "&features=Rank&q=info:".$page."&num=100&filter=0 HTTP/1.1\r\n";
      $out .= "Host: toolbarqueries.google.com\r\n";
      $out .= "User-Agent: Mozilla/4.0 (compatible; GoogleToolbar 2.0.114-big; Windows XP 5.1)\r\n";
      $out .= "Connection: Close\r\n\r\n";
      fwrite($socket, $out);
      $result = "";
      while(!feof($socket)) {
        $data = fgets($socket, 128);
        $pos = strpos($data, "Rank_");
        if($pos !== false){
          $pagerank = substr($data, $pos + 9);
          $result += $pagerank;
        }
      }
      fclose($socket);
      if (!$result) {$result = 'PR 0';} else {$result = 'PR '.$result;}
      return $result;
    }
  }
}

function get_tweets($url) {
    $json_string = file_get_contents('http://urls.api.twitter.com/1/urls/count.json?url=' . $url);
    $json = json_decode($json_string, true);
    return nf(intval( $json['count'] ));
}

function get_likes($url) {
	$fql  = "SELECT url, normalized_url, share_count, like_count, comment_count, ";
	$fql .= "total_count, commentsbox_count, click_count FROM ";
	$fql .= "link_stat WHERE url = '".$url."'";	
	$apifql="https://api.facebook.com/method/fql.query?format=json&query=".urlencode($fql);
	$json=file_get_contents($apifql);
	$json = json_decode($json,true);
	echo "        <td>".nf($json[0]['share_count'])."</td>\n";
	echo "        <td>".nf($json[0]['like_count'])."</td>\n";
	echo "        <td>".nf($json[0]['comment_count'])."</td>\n";
	echo "        <td>".nf($json[0]['commentsbox_count'])."</td>\n";
	echo "        <td>".nf($json[0]['click_count'])."</td>\n";
	echo "        <td>".nf($json[0]['total_count'])."</td>\n";
}

function get_pinterest($url) {
  $get_pinterest = json_decode(preg_replace('/^receiveCount\((.*)\)$/', "\\1",file_get_contents('http://api.pinterest.com/v1/urls/count.json?callback=receiveCount&url='.$url)));
	@$count_Pinterest = $get_pinterest->count;
	if($count_Pinterest=='-')
	{
		$count_Pinterest=0;
	}
	return nf(intval( $count_Pinterest ));
}

function get_linkedin($url) {
    $json_string = file_get_contents('http://www.linkedin.com/countserv/count/share?format=json&url=' . $url);
    $json = json_decode($json_string, true);
	return nf(intval( $json['count'] ));
}

function get_stumbledupon($url) {
    $json_string = file_get_contents('http://www.stumbleupon.com/services/1.01/badge.getinfo?url=' . $url);
    $json = json_decode($json_string, true);
    return nf(intval( @$json['result']['views'] ));
}

function GoogleBL($url) {
$json = file_get_contents("http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=link:%20".$url."&filter=0");
$data=json_decode($json,true);
$gbl = $data['responseData']['cursor']['resultCount'];
if($gbl < 1) {$gbl = 0;}
return nf($gbl);
}

function GoogleIP($domain){
$json = file_get_contents("http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=site:".$domain."&filter=0");
$data=json_decode($json,true);
$gip = $data['responseData']['cursor']['resultCount'];
if($gip < 1) {$gip = 0;}
return nf($gip);
}

function get_mobi($url) {
    $json_string = file_get_contents('https://www.googleapis.com/pagespeedonline/v3beta1/mobileReady?locale=pl_PL&url=http://' . $url);
    $json = json_decode($json_string, true);
    return $json;
}

function get_speed($url) {
    $json_string = file_get_contents('https://www.googleapis.com/pagespeedonline/v2/runPagespeed?locale=pl_PL&url=http://' . $url);
    $json = json_decode($json_string, true);
    return $json;
}

function render($ranks, $target, $keywords){
$check = str_replace('*','',h8($target));
$check = str_replace("http://www.", "", $check);
$check = str_replace("http://", "", $check);
$check = str_replace("www.", "", $check);
$check = str_replace("/", "", $check);
$rank = GooglePageRankChecker::getRank($check); 
$alexa = new Get_Alexa_Ranking();
echo "<table class='table table-bordered table-condensed tablesocial' style='display:none;'>
	<thead>
		<tr class='header'>
        <td colspan='6' style='color:#324B81;'><img src='../img/ico-view-f.png' style='margin-top:-5px;'/>acebook</td>
        <td rowspan='2'><img src='../img/ico-view-g.png' title='PlusOne' /></td>
        <td rowspan='2'><img src='../img/ico-view-t.png' title='Twitter' /></td>
        <td rowspan='2'><img src='../img/ico-view-p.png' title='PinTerest' /></td>
        <td rowspan='2'><img src='../img/ico-view-i.png' title='LinkedIn' /></td>
        <td rowspan='2'><img src='../img/ico-view-s.png' title='StumbleUpon' /></td>
        <td colspan='4' style='min-width:150px;'>Domain Page Rank</td>
    </tr>
    <tr>
        <td>Share</td>
        <td>Like</td>
        <td>Comment</td>
        <td>Box</td>
        <td>Click</td>
        <td>Total <br></td>
        <td>Google</td>
        <td>Alexa</td>
        <td>Backlink</td>
        <td>Indexed</td>
    </tr>
  </thead>
    <tr>\n";
    get_likes("http://www.".$check);
		echo "        <td><div class=\"g-plusone\" data-size=\"small\" data-href=\"http://www.".$check."\"></div></td>\n";
		echo "        <td>" . get_tweets("http://www.".$check) . "</td>\n";
		echo "        <td>" . get_pinterest("http://www.".$check) . "</td>\n";
		echo "        <td>" . get_linkedin("http://www.".$check) . "</td>\n";
		echo "        <td>" . get_stumbledupon("http://www.".$check) . "</td>\n";
		echo "        <td>" . $rank . "</td>\n";
		echo "        <td>" . $alexa->get_rank($check) . "</td>\n";
		echo "        <td>" . GoogleBL($check) . "</td>\n"; //get backlinks
		echo "        <td>" . GoogleIP($check) . "</td>\n"; //get indexed page
		echo "      </tr>";
		echo "      <tr></table>
		<script>
			$('.table').css('display', 'inline-table').hide().fadeIn(1999, function() { $('#Mobi').css('display', 'block').hide().fadeIn(1999); });
		</script>\n";
   $speed = get_speed($check);
    $mobi = get_mobi($check);
    $img = str_replace('_','/',$mobi['screenshot']['data']);
    $img = str_replace('-','+',$img);
		echo "      <div id='Mobi' style='display:none;'>
				<div class='screenshot'>
					<div class='img-container'><img src='data:".$mobi['screenshot']['mime_type'].";base64,".$img."' id='phn-img' /></div>
					<div class='score'>Mobi Score: ".$mobi['ruleGroups']['USABILITY']['score']."%</div>
				</div>\n";
   $sheet = $speed['pageStats'];
   $formr = $speed['formattedResults']['ruleResults'];
   $allitm = $sheet['numberStaticResources'] + $sheet['numberJsResources'] + $sheet['numberCssResources'];
   $othitm = $allitm - $sheet['numberResources'];
   	echo "<div class='speedshot'>
   		<div class='contents'>
   			<div class='graph' id='containers'></div>
			</div>
 	   <div class='tab-progres'>Preview in progress...</div>
 	   <div class='tab-view'> <!--- SITE SNAPSHOT --->
 	   	<img src='http://www.site2img.com/?fresh=1&w=502&url=".$check."' id='tab-img'>
 	   </div>
  	 <div class='score'>SPEED Score: ".$speed['ruleGroups']['SPEED']['score']."%</div>
    </div>
   </div>
		<script>
			$(document).ready(function() { $('#tab-img').hide(); });
			$('#tab-img').click(function() { $('.tab-view').fadeOut(999); $('.tab-progres').html('Show preview'); });
			$('.tab-progres').click(function() { $('.tab-view').fadeIn(999); });
			$('#tab-img').load(function() { $('#tab-img').css('display', 'block').hide().fadeIn(999); });
			$('#phn-img').load(function() { $('#phn-img').css('display', 'block').hide().fadeIn(1999); });
			var chart;
				chart = new Highcharts.Chart({
					exporting: false,
			      title: {text:'".$allitm." Resources in ".$sheet['numberHosts']." Hosts'},
			      credits: {enabled: false},
			        plotOptions: {
			            pie: {cursor: 'pointer',}
			        },
			        legend: {
            	layout: 'vertical',
            	align: 'left',
            	verticalAlign: 'top',
            	borderWidth: 0,
        			},
			        chart: {renderTo: 'containers'},
			        series: [{
			            type: 'pie',
			            name: 'resources',
			            data: [\n";
if($sheet['totalRequestBytes'] > 0){echo"			           	 {name:'Request: ".nf($sheet['totalRequestBytes'])."',z:'',y:".$sheet['totalRequestBytes'].",x:'".nf($sheet['totalRequestBytes'])."'},\n";}
if($sheet['imageResponseBytes'] > 0){echo"			           	 {name:'Image:  ".nf($sheet['imageResponseBytes'])."',z:'".$sheet['numberStaticResources']."',y:".$sheet['imageResponseBytes'].",x:'".nf($sheet['imageResponseBytes'])."'},\n";}
if($sheet['htmlResponseBytes'] > 0){echo"			           	 {name:'Html Code: ".nf($sheet['htmlResponseBytes'])."',z:'',y:".$sheet['htmlResponseBytes'].",x:'".nf($sheet['htmlResponseBytes'])."'},\n";}
if($sheet['javascriptResponseBytes'] > 0){echo"			           	 {name:'JS Code:  ".nf($sheet['javascriptResponseBytes'])."',z:'".$sheet['numberJsResources']."',y:".$sheet['javascriptResponseBytes'].",x:'".nf($sheet['javascriptResponseBytes'])."'},\n";}
if($sheet['cssResponseBytes'] > 0){echo"			           	 {name:'CSS Code: ".nf($sheet['cssResponseBytes'])."',z:'".$sheet['numberCssResources']."',y:".$sheet['cssResponseBytes'].",x:'".nf($sheet['cssResponseBytes'])."'},\n";}
if($sheet['otherResponseBytes'] > 0){echo"			           	 {name:'Other Code: ".nf($sheet['otherResponseBytes'])."',z:'".$othitm."',y:".$sheet['otherResponseBytes'].",x:'".nf($sheet['otherResponseBytes'])."'},\n";}
if($formr['LeverageBrowserCaching']['ruleImpact'] > 0){echo"			           	 {name:'Cashe: ".nf($formr['LeverageBrowserCaching']['ruleImpact'] * 1024)."',z:'',y:".$formr['LeverageBrowserCaching']['ruleImpact'] * 1024 .",x:'".nf($formr['LeverageBrowserCaching']['ruleImpact'] * 1024)."'},\n";}
echo "			            ],
			        		tooltip: {
			            pointFormat: 'in {point.z} resources',
			            shared: true,
			       			},
			            center: [160, 80],
			            size: 170,
			            showInLegend: true,
			            dataLabels: false
			          }]                    
			    });
		</script>
		<script type='text/javascript'>
		(function() {
			var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;
			po.src = 'https://apis.google.com/js/plusone.js';
			var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);
		})();
		</script>\n";
}
?>