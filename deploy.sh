#!/bin/sh
echo "Deploy jar to dev.bukkit.org"

filename="./jarname.txt"

if [ ! -f $filename ]; then
  echo "The file $filename doesn't exist."
  exit 1
fi

read -r JAR <$filename
xToken=$1
curl -X POST -H "X-Api-Token: $xToken" -F 'metadata={"changelog":"Auto upload by TravisCI, see update details on spigotmc.org","gameVersions":[7330,7105],"releaseType":"release"}' -F "file=@./devbukkit/$JAR" -s "https://dev.bukkit.org/api/projects/320536/upload-file" >fileid.json
cat fileid.json
echo "Finished upload to dev.bukkit.org."

exit 0
