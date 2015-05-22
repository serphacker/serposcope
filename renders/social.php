<?php
if(!defined('INCLUDE_OK'))
    die();

function render($ranks, $target, $keywords){  

global $done;
$check = str_replace('*','',h8($target));

if (!$done) {
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
      if (!$result) {$result = '-';}
      return $result;
    }
  }
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
		$rank = number_format((string)$popularity[0]['TEXT'], 0, ',', ' '); 
		$reach = $xml->xpath("//RANK");
		$reac = number_format((string)$reach[0]['DELTA'], 0, ',', ' ');
		if ($reac) {$rank = '<span title="DELTA: '.$reac.'">'.$rank.'</span>';}
		if(!$rank) {$rank = '-';}
		return $rank;
	}
}



function get_tweets($url) {
    $json_string = file_get_contents('http://urls.api.twitter.com/1/urls/count.json?url=' . $url);
    $json = json_decode($json_string, true);
    return intval( $json['count'] );
}
 
function get_likes($url) {
	$fql  = "SELECT url, normalized_url, share_count, like_count, comment_count, ";
	$fql .= "total_count, commentsbox_count, comments_fbid, click_count FROM ";
	$fql .= "link_stat WHERE url = '".$url."'";	
	$apifql="https://api.facebook.com/method/fql.query?format=json&query=".urlencode($fql);
	$json=file_get_contents($apifql);
	$json = json_decode($json,true);
	echo "<td>" . $json[0]['share_count']. "</td>";
	echo "<td>" . $json[0]['like_count']. "</td>";
	echo "<td>" . $json[0]['comment_count']. "</td>";
	echo "<td>" . $json[0]['total_count']. "</td>";
	echo "<td>" . $json[0]['commentsbox_count']. "</td>";
//	echo "<td>" . $json[0]['comments_fbid']. "</td>";
	echo "<td>" . $json[0]['click_count']. "</td>";
}

function get_pinterest($url) {
 
    $get_pinterest = json_decode(preg_replace('/^receiveCount\((.*)\)$/', "\\1",file_get_contents('http://api.pinterest.com/v1/urls/count.json?callback=receiveCount&url='.$url)));
	@$count_Pinterest = $get_pinterest->count;
	if($count_Pinterest=='-')
	{
		$count_Pinterest=0;
	}
	return intval( $count_Pinterest );
}

function get_linkedin($url) {
    $json_string = file_get_contents('http://www.linkedin.com/countserv/count/share?format=json&url=' . $url);
    $json = json_decode($json_string, true);
	return intval( $json['count'] );
}

function get_stumbledupon($url) {
    $json_string = file_get_contents('http://www.stumbleupon.com/services/1.01/badge.getinfo?url=' . $url);
    $json = json_decode($json_string, true);
    return intval( @$json['result']['views'] );
}

$check = str_replace("http://www.", "", $check);
$check = str_replace("http://", "", $check);
$check = str_replace("www.", "", $check);
$check = str_replace("/", "", $check);

$url = $check;
$rank = GooglePageRankChecker::getRank($check); 
$alexa = new Get_Alexa_Ranking();

echo '<table  class="table table-bordered table-condensed tablesocial">';
		echo '<thead>    <tr class="header">
        <td colspan="6">Facebook</td>
        <td rowspan="2">+1</td>
        <td rowspan="2">Tweet</td>
        <td rowspan="2">Pin <br></td>
        <td rowspan="2">Linkedin <br></td>
        <td rowspan="2">Stumbleupon</td>
        <td colspan="2">Page Rank</td>
    </tr>
    <tr>
        <td>Share</td>
        <td>Like</td>
        <td>Comment <br></td>
        <td>Total <br></td>
        <td>Box <br></td>
        <td>Click</td>
        <td>Google</td>
        <td>Alexa <br></td>
    </tr></thead>';
		echo "<tr>";
		get_likes("http://www.".$url);
		echo "<td>" ."
<div class=\"g-plusone\" data-size=\"small\" data-href=\"http://www.".$check."\"></div>
<script type=\"text/javascript\">
  (function() {
    var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;
    po.src = 'https://apis.google.com/js/plusone.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);
  })();
</script>" . "</td>";
		echo "<td>" . get_tweets("http://www.".$url) . "</td>";
		echo "<td>" . get_pinterest("http://www.".$url) . "</td>";
		echo "<td>" . get_linkedin("http://www.".$url) . "</td>";
		echo "<td>" . get_stumbledupon("http://www.".$url) . "</td>";
		echo "<td>" . $rank . "</td>";
		echo "<td>" . $alexa->get_rank($check) . "</td>";
		echo "</tr>";
		echo "</table>";
		$done = 'true';
	}
}
?>