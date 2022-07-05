<!DOCTYPE html>

<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, user-scalable=yes, initial-scale=1.0">
    <#setting classic_compatible=true>
    <#include "*/commonHeader.ftl">
</head>
<body>
    <iframe src="${pdfUrl}" width="100%" frameborder="0"></iframe>
</body>
<script type="text/javascript">
    if (${needFilePassword} == true) {
        var filePassword = window.prompt("请输入文件密码");
        if (filePassword != null) {
            var redirectUrl = window.location.href + '&filePassword=' + filePassword;
            window.location.replace(redirectUrl);
        } else {
            location.reload();
        }
    } else {
        document.getElementsByTagName('iframe')[0].height = document.documentElement.clientHeight - 10;
        /**
         * 页面变化调整高度
         */
        window.onresize = function () {
            var fm = document.getElementsByTagName("iframe")[0];
            fm.height = window.document.documentElement.clientHeight - 10;
        }
        /*初始化水印*/
        window.onload = function () {
            initWaterMark();
        }
    }
</script>
</html>
