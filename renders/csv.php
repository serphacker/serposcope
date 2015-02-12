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
if (!defined('INCLUDE_OK'))
    die();

function render($ranks, $targets, $keywords) {
    global $group,$startDate,$endDate; // ugly fix it

    header('Content-type: text/plain');
    header( 'Content-Disposition: attachment;filename=export_'.preg_replace('/[^a-z0-9A-Z]/', '', $group['name']).'_'.date('Y-m-d',$startDate).'_'.date('Y-m-d',$endDate).'.csv');
    $fp = fopen('php://output', 'w');
    fputcsv($fp, array("date", "keyword", "position", "url"));

    foreach ($targets as $target) {
        foreach ($keywords as $keyword) {
            foreach ($ranks as $rank) {
                $line = array();
                $line[0] = $rank['date'];
                $line[1] = $keyword;
                if (isset($rank[$target]) && isset($rank[$target][$keyword])) {
                    $line[2] = $rank[$target][$keyword][0];
                    $line[3] = $rank[$target][$keyword][1];
                } else {
                    $line[2] = null;
                    $line[3] = null;
                }
                fputcsv($fp, $line);
            }
        }
    }
    fclose($fp);
}