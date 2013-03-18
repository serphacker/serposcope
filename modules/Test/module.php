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

class Test extends GroupModule {
    
    public function getGlobalOptions() {
        return null;
    }
   
    public function getGroupOptions() {
        return array(
            array('opt1','100','Must be a int','/^[0-9]{2}$/','text')
        );
    }

    public function check($group) {
        $ranks =  array();
        foreach ($group['keywords'] as $keyKW => $keyword) {
            l('CheckTest','Checking '.$keyword);
            foreach ($group['sites'] as $keySite => $site) {
                $ranks[$keyKW][$keySite][0] = rand(0,30);
                $ranks[$keyKW][$keySite][1] = "http://$site/".rand(0, 30).".php";
            }
        }
        return $ranks;
    }
    
}

?>