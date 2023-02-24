<!DOCTYPE html>
<html lang="zh-cn">
<head>
    <meta charset="utf-8"/>
    <title>多媒体文件预览</title>
    <#include "*/commonHeader.ftl">
    <link rel="stylesheet" href="plyr/plyr.css"/>
    <script type="text/javascript" src="plyr/plyr.js"></script>
    <style>
        body {
            height:auto;
            background-color: #404040;
        }

        .m {
            width: 1024px;
            height:auto;
            margin: 0 auto;
        }
    </style>
</head>
<body>
<div id="mediaBox" class="m">
    <video>
        <source src="${mediaUrl}"/>
    </video>
</div>
<script>
    plyr.setup();
    window.onload = function () {
        let height = window.innerHeight;
        let mb = document.getElementById("mediaBox");
        let margin_top = (height - mb.offsetHeight) / 2;
        mb.style.marginTop = margin_top + "px";
        initWaterMark();
    }
</script>
</body>
</html>
