#!/bin/sh
echo "Deploy jar to dev.bukkit.org"

# 閼惧嘲褰囨禒宸宯stall鐎瑰本鍨氶崥搴ょ翻閸戣櫣娈慗AR閺傚洣娆㈤惃鍕倳鐎涳拷
filename="./jarname.txt"

if [ ! -f $filename ]; then
  echo "The file $filename doesn't exist."
  exit 1
fi

# JAR娑撹櫣绱拠鎴濇倵jar閺傚洣娆㈤崥宥囆�
read JAR <$filename
xToken=$1
curl -X POST -H "X-Api-Token: $xToken" -F 'metadata={"changelog":"Auto upload by TravisCI, see update details on spigotmc.org","gameVersions":[7330,7105],"releaseType":"release"}' -F "file=@./devbukkit/$JAR" -s "https://dev.bukkit.org/api/projects/320536/upload-file" >fileid.json
cat fileid.json
echo "Finished upload to dev.bukkit.org."

#闁拷閸戦缚鍓奸張顒婄礉娑撴棃锟斤拷閸戣櫣濮搁幀浣虹垳娑擄拷0
exit 0
