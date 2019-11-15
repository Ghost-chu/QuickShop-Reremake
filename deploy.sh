#!/bin/sh
echo "Deploy jar to dev.bukkit.org"

# 鑾峰彇浠巌nstall瀹屾垚鍚庤緭鍑虹殑JAR鏂囦欢鐨勫悕瀛�
filename="./jarname.txt"

if [ ! -f $filename ]; then
  echo "The file $filename doesn't exist."
  exit 1
fi

# JAR涓虹紪璇戝悗jar鏂囦欢鍚嶇О
read JAR <$filename
xToken=$1
curl -X POST -H "X-Api-Token: $xToken" -F 'metadata={"changelog":"Auto upload by TravisCI, see update details on spigotmc.org","gameVersions":[7330,7105],"releaseType":"release"}' -F "file=@./devbukkit/$JAR" -s "https://dev.bukkit.org/api/projects/320536/upload-file" >fileid.json
cat fileid.json
echo "Finished upload to dev.bukkit.org."

#閫�鍑鸿剼鏈紝涓旈��鍑虹姸鎬佺爜涓�0
exit 0
