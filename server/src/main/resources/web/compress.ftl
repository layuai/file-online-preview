<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <title>压缩包预览</title>
   <script type="text/javascript" src="ztree/js/jquery-1.4.4.min.js"></script>
     <#include "*/commonHeader.ftl">
      <script src="js/base64.min.js" type="text/javascript"></script>
    <link rel="stylesheet" href="ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet" type="text/css"/>

    <!--
    其他两种css风格样式
    <link rel="stylesheet" href="ztree/css/metroStyle/metroStyle.css" rel="stylesheet" type="text/css"/>
    <link rel="stylesheet" href="ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet" type="text/css" />
    <link rel="stylesheet" href="ztree/css/awesomeStyle/awesome.css" rel="stylesheet" type="text/css" />
    -->
    <script type="text/javascript" src="ztree/js/jquery.ztree.all.min.js"></script>
</head>
<body>
<div>
    <h1>kkFileView</h1>
    <ul id="treeDemo" class="ztree"></ul>
</div>
<script>
    var settings = {
        data: {
            simpleData: {
                enable: true,  //true 、 false 分别表示 使用 、 不使用 简单数据模式
                idKey: "id",   //节点数据中保存唯一标识的属性名称
                pIdKey: "pid", //节点数据中保存其父节点唯一标识的属性名称
                rootPId: ""
            }
        },
        callback: {
            onClick: chooseNode,
        }
    };

    function chooseNode(event, treeId, treeNode) {
        var path = '${baseUrl}' + treeNode.id;
        location.href = "${baseUrl}onlinePreview?url=" + encodeURIComponent(Base64.encode(path));

    }

    $(document).ready(function () {
    var url = '${fileTree}';
        $.ajax({
            type: "get",
            url: "${baseUrl}directory?urls="+encodeURIComponent(Base64.encode(url)),
            success: function (res) {
                zTreeObj = $.fn.zTree.init($("#treeDemo"), settings, res); //初始化树
                zTreeObj.expandAll(true);   //true 节点全部展开、false节点收缩
            }
        });
    });
</script>
</body>
</html>