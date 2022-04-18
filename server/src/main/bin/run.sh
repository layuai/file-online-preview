## 启动kkFileView
cd ../bin
echo "Starting kkFileView..."
nohup java -Dfile.encoding=UTF-8 -Dspring.config.location=../config/application.properties -jar kkFileView-4.1.0-SNAPSHOT.jar > ../log/kkFileView.log 2>&1 &
echo "Please execute ./showlog.sh to check log for more information"
echo "You can get help in our official homesite: https://kkFileView.keking.cn"
echo "If this project is helpful to you, please star it on https://gitee.com/kekingcn/file-online-preview/stargazers"
PROCESS=$(ps -ef | grep kkFileView | awk 'NR==1{print $2}')
# 显示进程号
echo "进程号: $PROCESS"