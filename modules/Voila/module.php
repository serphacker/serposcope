<?php
/**
 * Serposcope - An open source rank checker for SEO
 * http://serphacker.com/serposcope/
 * 
 * @link http://serphacker.com/serposcope Serposcope
 * @author SERP Hacker <pierre@serphacker.com>
 * @license http://creativecommons.org/licenses/by-nc-sa/3.0/legalcode CC-BY-NC-SA
 * 
 * Redistributions of files must retain the above notice.
 */
if(!defined('INCLUDE_OK'))
    die();

include(OHMYROOT.'inc/simple_html_dom.php');

class Voila extends GroupModule {
    
    public function getGlobalOptions() {
        return null;
    }
    
    public function getGroupOptions() {
        return array(
            /*
            array(
                'tld',
                'fr',
                'The google search engine top level domain: google.<strong>com</strong>, google.<strong>co.uk</strong>',
                '/^[a-zA-Z.]+$/'
            )*/
        );
    }
   
    
    public function check($group) {
        
        $curl=curl_init();
        $ranks =  array();
        foreach ($group['keywords'] as $keyKW => $keyword) {
            l('CheckVoila','Checking '.$keyword);
            
            $pos=1;
            $page=1;
           
            do{
                
                if($page==1){
                    $url="http://www.lemoteur.fr/?action=build&module=lemoteur&bhv=web_fr&kw=".urlencode($keyword);
                    $referrer= "http://www.lemoteur.fr/";                    
                }
                
                $opts = array(
                    CURLOPT_URL => $url,
                    CURLOPT_REFERER => $referrer
                ) + buildCurlOptions($proxy);                
                
                
                //l('CheckVoila',"Fetching $url");
                curl_setopt_array($curl,$opts);
                $curlout=curl_cache_exec($curl);
                $data=$curlout['data'];
                $http_status = $curlout['status'];                
                
                if($http_status != 200){
                    l('CheckVoila',"Error: bad retcode from google ".$http_status);
                    //unlink($cookiefile);
                    return null;
                }                
                

                
                $doc =  str_get_html($data);
                if($doc==null){
                    l('CheckVoila',"Error: parse html");
                    return null;
                }
                
                $divsBlockTitle=$doc->find('div[class=blockTitleOnly]');
                
                
                if($divsBlockTitle == null){
                    l('CheckVoila',"Error: can't find blockTitleOnly");
                    return null;                    
                }
                
                $childDiv = null;
                foreach ($divsBlockTitle as $div) {
                    if(strstr($div,"FRANCO") !== false){
                        $childDiv = $div;
                    }
                }
                
                if($childDiv == null){
                    l('CheckVoila',"Error: can't find blockTitleOnly [2]");
                    return null;                    
                }
                
                $childDiv = $childDiv->next_sibling();
                
                while($childDiv != null){
                    $voilaLinkTag = $childDiv->find('a[class=title]');
                    if($voilaLinkTag != null && !empty($voilaLinkTag)){
                        $voilaLink = $voilaLinkTag[0]->href;
                        $voilaLinkParsed = parse_url($voilaLink);
                        if($voilaLinkParsed!=null){
                            if(isset($voilaLinkParsed['query'])){
                                $arr = array();
                                parse_str($voilaLinkParsed['query'],$arr);
                                if(isset($arr['url'])){
                                    //echo "$pos ".$arr['url']."\n";
                                    $parsed = parse_url($arr['url']);
                                    if(isset($parsed['host'])){

                                        foreach ($group['sites'] as $keySite => $website) {

                                            // if we already have a rank for this keyword, continue
                                            if(isset($ranks[$keyKW][$keySite]))
                                                continue;

                                            // wildcard support
                                            $regex = wd_wildcard_to_preg($website);

                                            if(preg_match($regex, $parsed['host'])){
                                                $ranks[$keyKW][$keySite][0]= $pos;
                                                $ranks[$keyKW][$keySite][1]= $arr['url'];

                                                l('CheckVoila',"Rank[$pos] [$website] ".$arr['url']);
                                            }
                                        }                                
                                    }
                                }
                            }
                        }
                    }
                    
                    $childDiv = $childDiv->next_sibling();
                    ++$pos;
                }
                
                $bAllWebsiteFound=true;
                foreach ($group['sites'] as $keySite => $website) {
                    if(!isset($ranks[$keyKW][$keySite])){
                        $bAllWebsiteFound=false;
                    }
                }   

                ++$page;
                sleep(10);
                
                $linkNextPageTag= $doc->find('a[title=page '.$page.']');
                if($linkNextPageTag == null || empty($linkNextPageTag) || $linkNextPageTag[0]->href == null){
                    break;
                }
                
                $referrer=$url;
                $url=$linkNextPageTag[0]->href;
               
            }while($page<11 && !$bAllWebsiteFound);            
            
            $this->incrementProgressBarUnit();
        }
        
        curl_close($curl);
        return $ranks;
    }

}

?>