#!/bin/sh
echo "Deploy jar to dev.bukkit.org"

# 获取从install完成后输出的JAR文件的名字
filename="./jarname.txt"

if [ ! -f $filename ]
then
    echo "The file $filename doesn't exist."
    exit 1
fi

# 定义变量count，其值为0
count=0

# 下载JSON库
wget -O "jq" "https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64"

# 设置运行权限
chmod +x "./jq"

# JAR为编译后jar文件名称
read JAR < $filename
# 将变量count的值加1
xToken=$1
xUJson='{"changelog":"Auto upload by TravisCI, see update details on spigotmc.org","changelogType":text,"gameVersions":[7330,7132,7081],"releaseType":"release"}'
curl -X POST -H "X-Api-Token: $xToken" -F "metadata=$xUJson" -F "file=@./push/$JAR" -s "https://dev.bukkit.org/api/projects/320536/upload-file" > fileid.json
cat fileid.json
jq '.id' > fileid.txt
cat fileid.txt
echo -e "\nTotal $count linces read and deployed."

#退出脚本，且退出状态码为0
exit 0
