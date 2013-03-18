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

function render($ranks, $target, $keywords){  
    
    $hashKeywords=array();
    $hashMonths=array();
    $hashYears=array();
    foreach ($ranks as $id => $check) {
        $day= date('d',strtotime($check['date']));
        $month= date('m',strtotime($check['date']));
        $year= date('Y',strtotime($check['date']));
        
        if(isset($hashMonths[$month])){
            ++$hashMonths[$month];
        }else{
            $hashMonths[$month] = 1;
        }
        
        if(isset($hashYears[$year])){
            ++$hashYears[$year];
        }else{
            $hashYears[$year] = 1;
        }

    }
    
    $nYear = count($hashYears);
    
    echo "<table class='table table-bordered table-condensed tablerender' >";
    
    echo "<tr><td></td>";
    foreach ($hashYears as $year => $nDay) {
        echo "<td colspan=$nDay >$year</td>";
    }
    echo "</tr>";
    
    echo "<tr><td></td>";
    foreach ($hashMonths as $month => $nDay) {
        echo "<td colspan=$nDay >$month</td>";
    }
    echo "</tr>";    
    
    echo "<tr><td></td>";
    foreach ($ranks as $id => $check) {
        $day= date('d',strtotime($check['date']));
        $hEvent = false;
        $event = date('d M Y',strtotime($check['date']))."<br/>";
        $tdclass="tdsame";
        
        if(isset($check['__event']) && isset($check['__event'][$target]) && is_array($check['__event'][$target])){
            $tdclass="tdevent";
            foreach ($check['__event'][$target] as $evt) {
                $hEvent = true;
                $event .= "<b>".h8($target)."</b>:<br/>".h8($evt)."<br/>";
            }
        }
        echo "<td class='$tdclass' data-toggle='tooltip' title='".h8($event)."' >$day</td>";
    }
    echo "</tr>";
    
    foreach ($keywords as $keyword) {
        echo "<tr><td>".h8($keyword)."</td>";
        
        $prev="";
        foreach ($ranks as $id => $check) {
            
            $tooltip="=";
            $tdclass="tdsame";
            $now="";
            if(isset($check[$target][$keyword])){
                $now = $check[$target][$keyword][0];
            }
            
            if( ($prev === "" && $now !== "") || $prev > $now){
                if(($prev === "" && $now !== "")){
                    $tooltip="+".(100-$now);
                }else{
                    $tooltip="+".($prev-$now);
                }
                $tdclass="tdprogess";
            }else if($prev < $now){
                $tdclass="tdregress";
                $tooltip="-".($now-$prev);
            }
            
            echo "<td class='$tdclass' data-toggle='tooltip' title='$tooltip' >";
            echo $now;
            echo "</td>";
            $prev=$now;
        }
        echo "</tr>";
        
    }
    
    
    echo "</table>";
//    echo count($hashYears)." ".count($hashMonths)." ".count($ranks);
}
    
?>