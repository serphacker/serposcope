<?php

/**
 * Serposcope - An open source rank checker for SEO
 * http://serphacker.com/serposcope/
 *
 * @link http://serphacker.com/serposcope Serposcope
 * @author SERP Hacker <pierre@serphacker.com>
 * @contributor 512Banque https://twitter.com/512banque
 * @license http://creativecommons.org/licenses/by-nc-sa/3.0/legalcode CC-BY-NC-SA
 *
 * Redistributions of files must retain the above notice.
 */
if (!file_exists('inc/config.php')) {
    header("Location: install/", TRUE, 302);
    die();
}
require('inc/config.php');
include('inc/define.php');
include('inc/common.php');

$err = null;


if (!empty($_POST)) {

    // we build the array of proxies
    $final = array();

    if (!empty($_POST['bulkimport-proxies'])) { // we want to import proxies
        $lines = explode("\n", $_POST['bulkimport-proxies']); // we explode with newlines


        $linecount = 0;
        foreach ($lines as $line) {
            ++$linecount;
            $line = trim($line);

            // check if the line is not empty
            if (!empty($line)) {

                //we explode again to split with ':'
                $proxy_line = explode(':', $line);

                if (empty($proxy_line[0])) { // line with single :
                    $err = "bulkimport error line $linecount : each line should have at least an ip address";
                }

                $final[] = array(
                    'type' => $_POST['bulkimport-type'], // sanity check done later
                    'ip' => $proxy_line[0],
                    'port' => isset($proxy_line[1]) ? $proxy_line[1] : null,
                    'user' => isset($proxy_line[2]) ? $proxy_line[2] : null,
                    'password' => isset($proxy_line[3]) ? $proxy_line[3] : null,
                );
            }
        }
    } else {
        $final[] = array(
            'type' => isset($_POST['type']) ? $_POST['type'] : null,
            'ip' => isset($_POST['ip']) ? $_POST['ip'] : null,
            'port' => isset($_POST['port']) ? $_POST['port'] : null,
            'user' => isset($_POST['user']) ? $_POST['user'] : null,
            'password' => isset($_POST['password']) ? $_POST['password'] : null
        );
    }

    // user sent nothing
    if (empty($final)) {
        $err = "bulkimport error:  each line should have at least an ip address";
    } else {

        $q = "INSERT INTO `" . SQL_PREFIX . "proxy`(type,ip,port,user,password) VALUES ";
        foreach ($final as $proxy) {

            // we want to add them 1 by 1
            if (!isset($proxy['ip'])) {
                $err = "ip is mandatory";
            } else {

                switch ($proxy['type']) {
                    case "http":
                    case "socks":
                        if (!isset($proxy['port']) || !is_numeric($proxy['port'])) {
                            $err = "invalid port <strong>".h8(isset($proxy['port']) ? $proxy['port'] : "")."</strong> for <strong>".h8($proxy['ip'])."</strong>";
                        }
                        break;

                    case "iface":
                        if (!empty($proxy['port']) || !empty($proxy['user']) || !empty($proxy['password'])) {
                            $err = "iface proxy doesn't need port/username or password";
                        }
                        break;

                    default:
                        $err = "invalid proxy type";
                }

                if ($err != null) {
                    break;
                }

                $q.="\n(";
                $q.="'" . addslashes($proxy['type']) . "',";
                $q.="'" . addslashes($proxy['ip']) . "',";
                $q.= (empty($proxy['port']) ? "NULL" : intval($proxy['port'])) . ",";
                $q.= (empty($proxy['user']) ? "NULL" : "'" . addslashes($proxy['user']) . "'") . ",";
                $q.= (empty($proxy['password']) ? "NULL" : "'" . addslashes($proxy['password']) . "'");
                $q.="),";
            }
        }
        
        //save in database if no error
        if ($err == null) {
            $q = trim($q, ",");
            if (!$db->query($q)) {
                $err = "sql error $q";
            }
        }
    }
}

$proxies = load_proxies();

include('inc/header.php');
if ($err != null) {
    echo "<div class='alert alert-error' >$err</div>";
}
?>
<script>
    bAllSelected = false;
        $(function() {

            $('#selectallproxy').click(function() {

            var selected = $('[name="idproxy[]"]');
                if (bAllSelected) {
            selected.attr('checked', false);
                } else {
            selected.attr('checked', true);
            }
        bAllSelected = !bAllSelected;
        });

            $('#btndeleteproxy').click(function() {
            var selected = $('[name="idproxy[]"]:checked');
                if (selected.length === 0) {
                alert("no proxy selected");
            return;
            }

            var proxiesId = "";
                selected.each(function(i, item) {
            proxiesId += "&id[]=" + item.value;
            });

                $.ajax({
                type: "POST",
                url: "ajax.php",
            data: "action=deleteproxy" + proxiesId
                }).done(function(rawdata) {
                data = JSON.parse(rawdata);
                    if (data !== null) {
                        if (data.deleted === true) {
                    $('[name="idproxy[]"]:checked').parent().parent().remove();
                        } else {
                    alert("Can't delete proxies");
             
                    }                 } else {
                alert("unknow error [1]");
            }
        });

        });

            $('#btncheckproxy').click(function() {
            var selected = $('[name="idproxy[]"]:checked');
                if (selected.length === 0) {
                alert("no proxy selected");
            return;
            }

                selected.each(function(i, item) {
                    $.ajax({
                    type: "POST",
                    url: "ajax.php",
                data: "action=checkproxy&id=" + item.value
                    }).done(function(rawdata) {
                    data = JSON.parse(rawdata);
                        if (data !== null) {
                            if (data.result === "ok") {
                            $('#status' + item.value).html("OK");
                        $('#status' + item.value).css("color", "green");
                            } else {
                            $('#status' + item.value).html("ERR");
                        $('#status' + item.value).css("color", "red");
                    }
                        } else {
                    alert("unknow error [1]");
                }
            });
        });



    });

    });
</script>
<h2>Proxies</h2>
<table class='table'>
    <thead>
    <td><a href='#' id=selectallproxy>#</a></td>
    <td>type</td>
    <td>ip</td>
    <td>port</td>
    <td>user</td>
    <td>password</td>
    <td>status</td>
</thead>
<tbody>
<?php

foreach ($proxies as $proxy) {
    echo "<tr>";
    echo "<td><input type=checkbox name=idproxy[] value=" . $proxy['id'] . " /></td>";
    echo "<td>" . $proxy['type'] . "</td>";
    echo "<td>" . h8($proxy['ip']) . "</td>";
    echo "<td>" . $proxy['port'] . "</td>";
    echo "<td>" . h8($proxy['user']) . "</td>";
    echo "<td>" . h8($proxy['password']) . "</td>";
    echo "<td id=status" . $proxy['id'] . " >N/A</td>";
    echo "</tr>";
}
?>
    <tr>
        <td colspan=7><button class="btn" id="btndeleteproxy">Delete</button>
            <button class="btn" id="btncheckproxy">Check</button></td>
    </tr>
</tbody>
</table>
<hr/>
<form method=POST>
    <h4>Add a proxy</h4>
    <table>
        <tr>
            <td><select name=type class="input-mini">
                    <option>http</option>
                    <option>socks</option>
                    <option>iface</option>
                </select>
            </td>
            <td><input name=ip class=input-small placeholder="ip" /></td>
            <td><input name=port class=input-mini placeholder="port" /></td>
            <td><input name=user placeholder="username" /></td>
            <td><input name=password placeholder="password" /></td>
            <td><input type=submit class="btn btn-primary" value=Add /></td>
        </tr>
    </table>
    <hr/>
    <h4>Bulk import</h4>
    <select name=bulkimport-type class="input-mini">
        <option>http</option>
        <option>socks</option>
        <option>iface</option>
    </select>
    <textarea name=bulkimport-proxies style="width:100%; height: 120px" placeholder="ip:port:username:password" ></textarea>
    <input type=submit class="btn btn-primary" value="Bulk Import" />

</form>
<?php

include('inc/footer.php');
?>