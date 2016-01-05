/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope */

serposcope.adminUsersController = function () {
    
    var resizeTabContent = function(){
        $('.tab-content.admin-users').css("min-height", serposcope.theme.availableHeight() - 100);
    };
    
    var delUser = function(elt){
        if(confirm("are you sure you want to delete this user ?")){
            $(elt.currentTarget).parent().submit();
        }
        return false;
    };
    
    var addUserModal = function(){
        $('#add-user-modal').modal();
    };
    
    var togglePermission = function(){
        var elt = $(this);
        var userId = $(this).attr("data-user");
        var groupId = $(this).attr("data-group");
        var newValue = $(this).is(':checked');
        
        $.post("/admin/users/permissions/set",
            {
                "user-id": userId,
                "group-id": groupId,
                "value": newValue,
                "_xsrf": $('#_xsrf').attr('data-value')
            }
        ).done(function(parsed) {
            try{
                if(typeof(parsed.error) !== "undefined"){
                    alert(parsed.error);
                } else {
                    elt.prop("checked", parsed.perm);
                }
            }catch(e){
                alert("an error occured");
                console.log(e);
            }
        })
        .fail(function(data) {
            alert("an error occured");
        });
        return false;
    };

    var users = function() {
        $(window).bind("load resize", function () {
            resizeTabContent();
        });
        $('#add-user-btn').click(addUserModal);
        $('.del-user-btn').click(delUser);
        $('.btn-toggle-perm').click(togglePermission);
    };
    
    var oPublic = {
        users: users
    };
    
    return oPublic;

}();
