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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of GroupModule
 *
 * @author admin
 */
abstract class GroupModule {
    
    /*
     * Specify the global options of this group.
     * These options are common for all the group.
     * For group dependant option se getGroupOptions
     * 
     * Must return an array of options.
     * Each option is an array with :
     * [0] name
     * [1] value
     * [2] description
     * [3] validation regex
     * [4] input type (text,textarea)
     * 
     * Example:
     * array(
     *  array('tld','com','Top level domain of the google search site','/^[a-zA-Z]{2}^$/'),
     *  array('maxpos','100','The maximum position to check ...','/^[0-9]+$./')
     * );
     * 
     * If no option, can return null or an empty array. 
     */
    abstract public function getGlobalOptions();
    
    /*
     * Specify the options a group using this module can have.
     * 
     * Must return an array of options.
     * Each option is an array with :
     * [0] name
     * [1] value
     * [2] description
     * [3] validation regex
     * [4] input type (text,textarea)
     * 
     * Example:
     * array(
     *  array('tld','com','Top level domain of the google search site','/^[a-zA-Z]{2}^$/','text'),
     *  array('maxpos','100','The maximum position to check ...','/^[0-9]+$./','text')
     * );
     * 
     * If no option, can return null or an empty array. 
     */
    abstract public function getGroupOptions();
    
    /*
     * $group structure is 
     * $group['id']
     * $group['name']
     * $group['options'] null or the group options
     * $group['keywords'] array of keywords
     * $group['sites'] array of sit
     */
    abstract public function check($group);
 
    /**
     * Validate if a keyword is ok for this checker
     * better to be override
     */
    public function validateKeyword($keyword){
        return true;
    }
    
    /**
     * Validate if a target is ok for this checker
     * by example, google module only accept domaine name
     * better to be override
     */
    public function validateTarget($target){
        return preg_match('/^[a-zA-Z0-9.-]$/', $target);
    }
    
    protected function e($msg){
        return e(get_class($this),$msg);
    }    
    
    protected function l($msg){
        return l(get_class($this),$msg);
    }
    
}

?>
