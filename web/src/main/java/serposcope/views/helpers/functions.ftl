<#function groupLink group>
    <#if group.module == "GOOGLExxx">
        <#return "google">
    </#if>
    <#return reverseRoute("google.GoogleGroupController", "view", "groupId", group.getId())>
</#function>

<#function groupIcon group>
    <#if group.module == "GOOGLE">
        <#return "fa-google-plus-square">
    </#if>
    <#if group.module == "TWITTER">
        <#return "fa-twitter-square">
    </#if>    
    <#if group.module == "GITHUB">
        <#return "fa-github-square">
    </#if>    
    <#return reverseRoute("google.GoogleGroupController", "view", "groupId", group.getId())>
</#function>

<#function noreferrer url>
    <#assign urlx = url?html >
    <#return 
        "data:text/html," + 
        "<html><head>" + 
        "<title>Hiding referrer</title>" + 
        "<meta http-equiv=refresh content=\"3;url=" + urlx + "\">" + 
        "<meta charset=\"UTF-8\">" + 
        "</head><body>Hiding referrer and redirecting to " + url + "</body></html>"
    >
</#function>

<#function prettysearch search>
    <#return "<i class=\"fa fa-globe\"></i>" + search.getKeyword() >
</#function>