<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <title>压缩包预览</title>
   <script src="js/jquery-3.6.1.min.js"></script>
     <#include "*/commonHeader.ftl">
   <script src="js/base64.min.js" type="text/javascript"></script>
   <link href="css/zTreeStyle.css" rel="stylesheet" type="text/css">
  <script type="text/javascript" src="js/jquery.ztree.core.js"></script>
        <style type="text/css">
        body {
            background-color: #404040;
        }
        h1, h2, h3, h4, h5, h6 {color: #2f332a;font-weight: bold;font-family: Helvetica, Arial, sans-serif;padding-bottom: 5px;}
        h1 {font-size: 24px;line-height: 34px;text-align: center;}
        h2 {font-size: 14px;line-height: 24px;padding-top: 5px;}
        h6 {font-weight: normal;font-size: 12px;letter-spacing: 1px;line-height: 24px;text-align: center;}
        a {color:#3C6E31;text-decoration: underline;}
        a:hover {background-color:#3C6E31;color:white;}
        code {color: #2f332a;}
        div.zTreeDemoBackground {width:600px;text-align:center;margin: 0 auto;background-color: #ffffff;}
    </style>
</head>
<body>
<div class="zTreeDemoBackground left">
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