<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <style type="text/css">
        body {
            margin: 0 auto;
            width: 900px;
            /*background-color: #CCB;*/
        }

        .container {
            width: 700px;
            height: 700px;
            margin: 0 auto;
        }

        img {
            width: auto;
            height: auto;
            max-width: 100%;
            max-height: 100%;
            padding-bottom: 36px;
        }

        .tip {
            font-size: 24px;
            color: black;
            margin: 20px 0px;
            text-align: center;
        }

        .msgs {
            font-size: 22px;
            color: #fbb03e;
            text-align: center;
        }

        .dlbox {
            text-align: center;
            margin-top: 30px;
        }

        .btnDownload {
            border: 0;
            padding: 8px;
            background-color: #22b255;
            color: #fff;
            width: 200px;
            border-radius: 5px;
            cursor: pointer;
        }

        .imgbox {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 80%;
            text-align: center;
        }
    </style>
</head>

<body>
<div class="container">
    <div class="imgbox">
        <img src="images/sorry.png"/>
    </div>
    <div class="tip">
        <#if fileType??>
            <span>该(${fileType})文件，系统暂不支持在线预览。</span>
        <#else>
            <span>该文件，系统暂不支持在线预览。</span>
        </#if>
    </div>

    <#if msg??>
        <div class="msgs">
            <p style="margin: 0;">具体原因：${msg}</p>
        </div>
    </#if>
    <div class="dlbox">
        <button id="btnDownload" class="btnDownload">下载文件</button>
    </div>
    <#--    <p>有任何疑问，请加入kk开源社区知识星球咨询：<a href="https://t.zsxq.com/09ZHSXbsQ">https://t.zsxq.com/09ZHSXbsQ</a><br></p>-->
</div>
<script type="text/javascript">
    const btnDownload = document.getElementById('btnDownload')
    btnDownload.addEventListener('click', (e) => {
        downloadFile();
    });

    function downloadFile() {
        btnDownload.innerText = "下载中...";
        btnDownload.disabled = true;
        btnDownload.style.backgroundColor = "#999";
        // 用fetch发送请求 对请求回来的二进制文件流进行处理
        fetch("${file.url}").then((res) => {
            res.blob().then((blob) => {
                const blobUrl = window.URL.createObjectURL(blob);
                const filename = "${file.name}";
                const a = document.createElement('a');
                a.href = blobUrl;
                a.download = filename;
                a.click();
                window.URL.revokeObjectURL(blobUrl);
            });
        }).finally(() => {
            setTimeout(() => {
                btnDownload.innerText = "下载文件";
                btnDownload.disabled = false;
                btnDownload.style.backgroundColor = "#22b255";
            }, 1200)
        });
    }

</script>
</body>
</html>
