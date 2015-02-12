<?php

class WS_SQL {

    var $profile = false;
    var $queries = Array();

    function __construct($profile = false) {
        if ($profile) {
            $this->profile = true;
        }
    }

    function __destruct() {

        if($this->profile /*&& !isBot()*/){
            echo "<table class='table' ><thead><th>Query</th><th>File</th><th>PhpTime</th><th>MysqlTime</th></tbody></thead><tbody>\n";

            foreach ($this->queries as $query) {
                echo "<tr>".
                    "<td>".$query['query']."</td>".
                    "<td>".$query['source']."</td>".
                    "<td>".$query['php time']."</td>".
                    "<td>".$query['sql time']."</td>".
                    "</tr>\n";
            }
            echo "</tbody></table>";
        }
    }

    function connect($host, $user, $pass){
        return mysql_connect($host, $user, $pass);
        if($this->profile){
            mysql_query("set profiling = 1");
        }
    }

    function select_db($db){
        return mysql_select_db($db);
    }

    function query($sql) {
        if ($this->profile) {
            $begin = microtime( true );
            $res = mysql_query($sql);
            $end = microtime( true );

            $dbg=mysql_query("select query_id,sum(duration) as qtime from information_schema.profiling order by query_id DESC");
            $dbgtimes = mysql_fetch_row($dbg);

            $this->log($dbgtimes[0], $sql,$end-$begin,$dbgtimes[1]);

            return $res;
        }else{
            return mysql_query($sql);
        }
    }

    function log($qid, $query, $phptime, $sqltime){

        $trace = debug_backtrace();

        $this->queries[] = array (
            'qid' => $qid,
            'query' => strlen($query) > 64 ? substr($query, 0, 64) : $query,
            'source' => $trace[1]['file'].":".$trace[1]['line'],
            'php time' => $phptime,
            'sql time' => $sqltime
        );
    }

}