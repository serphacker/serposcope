function version_compare(v1, v2) {
    var v1parts = v1.split('.');
    var v2parts = v2.split('.');

    for (var i = 0; i < v1parts.length; ++i) {
        if (v2parts.length == i) {
            return 1;
        }

        v1dot = parseInt(v1parts[i]);
        v2dot = parseInt(v2parts[i]);


        if (v1dot === v2dot) {
            continue;
        } else if (v1dot > v2dot) {
            return 1;
        } else {
            return -1;
        }
    }

    if (v1parts.length != v2parts.length) {
        return -1;
    }

    return 0;
}

function domodal(title, data, onsave, cbdata) {

    if ($('#text-modal').length != 0) {
        $('#text-modal').remove();
    }

    $(
            '<div id="text-modal" class="modal hide fade textModal" >' +
            '<div class="modal-header">' +
            '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>' +
            '<h3>' + title + '</h3>' +
            '</div>' +
            '<div class="modal-body" style="height:200px;" >' +
            '<textarea id="text-modal-value" style="width:96%; height:98%;"></textarea>' +
            '</div>' +
            '<div class="modal-footer">' +
            '<a href="#" class="btn" data-dismiss="modal" >Close</a>' +
            ((typeof(onsave) == 'function') ? '<a href="#" class="btn btn-primary" id="text-modal-save" >Save</a>' : '') +
            '</div>' +
            '</div>'
            ).appendTo('body');

    if (typeof(onsave) == 'function') {
        $('#text-modal-save').bind('click', {
            cbdata: cbdata
        }, onsave);
    } else {
        $('#text-modal-save').unbind('click');
    }

    $('#text-modal').modal('show');
    $('#text-modal-value').val(data);


}

function onSaveGroupInfo(data) {
    var idSite = data.data.cbdata;
    $.ajax({
        type: "POST",
        url: "ajax.php",
        data: "action=addSiteInfo&target=" + idSite + "&info=" + encodeURIComponent($('#text-modal-value').val())
    }).done(function(rawdata) {
        data = JSON.parse(rawdata);
        if (data != null) {
            if (data.success != undefined) {
                $('#text-modal').modal('hide');
            } else if (data.error != undefined) {
                alert(data.error);
            } else {
                alert("unknow error [2]");
            }
        } else {
            alert("unknow error [1]");
        }
    });
}

function allowDrop(ev) {
    ev.preventDefault();
}

function drag(ev) {
    ev.dataTransfer.setData("data-id", $(ev.target).attr("data-id"));
}

function drop(ev) {

    ev.preventDefault();

    var sourceGroupId = ev.dataTransfer.getData("data-id");
    var sourceA = $("#group-link-" + sourceGroupId);
    var sourceLi = sourceA.parent();

    var targetA = $(ev.currentTarget).children();
    var targetLi = $(ev.currentTarget);
    var targetGroupId = targetA.attr("data-id");

    $.ajax({
        type: "POST",
        url: "ajax.php",
        data: "action=swap&target=" + targetGroupId + "&source=" + sourceGroupId
    }).done(function(rawdata) {
        data = JSON.parse(rawdata);
        if (data != null) {
            if (data.swap) {
                sourceA.remove();
                targetA.remove();

                sourceLi.append(targetA);
                targetLi.append(sourceA);

                if (sourceLi.hasClass("active")) {
                    sourceLi.removeClass("active");
                    targetLi.addClass("active");
                } else if (targetLi.hasClass("active")) {
                    targetLi.removeClass("active");
                    sourceLi.addClass("active");
                }
            } else {
                alert("unknow error [2]");
            }
        } else {
            alert("unknow error [1]");
        }
    });




}

$(function() {

    $(".tablerender td").tooltip();

    $('#loading-img').hide().ajaxStart(function() {
        $(this).show();  // show Loading Div
    }).ajaxStop(function() {
        $(this).hide(); // hide loading div
    });

    // run all btn
    $('.btn-run-all').click(function() {
        var canRun = false;

        // check if a run is already launched
        $.ajax({
            type: "POST",
            url: "ajax.php",
            data: {
                action: "is_running"
            }
        }).done(function(rawdata) {
            data = JSON.parse(rawdata);
            if (data !== null) {
                if (data.running !== undefined) {
                    if (!data.running) {
                        canRun = true;
                    }
                } else {
                    alert("unknow error [2]");
                }
            } else {
                alert("unknow error [1]");
            }

            if (!canRun) {
                alert("A job is already running");
                document.location.href = "index.php";
                return;
            }

            if (!confirm("Warning, run should be done from COMMAND LINE or via cron, continue ?")) {
                return;
            }
            var imgRun = new Image();
            imgRun.src = "cron.php";

            // lock everything for 3 sec
            $.blockUI({message: '<h1>Launching run...</h1>'});
            setTimeout(function() {
                document.location.href = "logs.php?id=last";
            }, 2000);
        });
    });

    // stats stuff and ads
    $('#uzi_img')[0].onerror = function() {
        $('#uzi_link').css("cursor", "default");
        $('#uzi_link')[0].href = ".";
        $('#uzi_img')[0].onload = null;
        $('#uzi_img')[0].src = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAOoAAAA8CAMAAABfLdZOAAAAA1BMVEX6+vqsEtnpAAAAJElEQVR42u3BAQ0AAADCIPunfg43YAAAAAAAAAAAAAAAAABwIjcUAAHdSbRTAAAAAElFTkSuQmCC";
    }

    $('#uzi_img')[0].onload = function(elt) {
        $('#uzi_link').css("cursor", "pointer");
        $('#uzi_link')[0].href = "http://stats.serphacker.com/uzi.php?s=s&t=l";
    }

    $('#uzi_img')[0].src = "http://stats.serphacker.com/uzi.php?s=s&t=i&v=" + current_version;

    // check if new version available
    if (current_version !== undefined && latest_version !== undefined) {
        if (version_compare(current_version, latest_version) === -1) {
            $('#new-version-link').text("v" + latest_version + " available !");
        }
    }

});