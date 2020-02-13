#!/bin/sh
echo "Deploy jar to dev.bukkit.org"

rm -rf push # GITHUB - CLEANUP
#Script to release to Github Release
mkdir push
cp -r ./target/*.jar ./push/
rm -f ./push/original-*.jar
rm -f ./push/*-shaded.jar
rm -f ./push/*-sources.jar
mv -f ./push/*-javadoc.jar ./push/JavaDocument.zip
#Script to release to Bukkit Dev
mkdir devbukkit
cp -r ./target/*.jar ./devbukkit/
rm -f ./devbukkit/original-*.jar
rm -f ./devbukkit/*-javadoc.jar
rm -f ./devbukkit/*-shaded.jar
rm -f ./devbukkit/*-sources.jar
rm -f ./target/original-*.jar
ls ./devbukkit -1 | grep ".jar$" >jarname.txt

filename="./jarname.txt"

if [ ! -f $filename ]; then
  echo "The file $filename doesn't exist."
  exit 1
fi

read -r JAR <$filename
xToken=$1
curl -X POST -H "X-Api-Token: $xToken" -F 'metadata={"changelog":"Auto upload by TravisCI, see update details on spigotmc.org","gameVersions":[7667,7330,7105],"releaseType":"release"}' -F "file=@./devbukkit/$JAR" -s "https://dev.bukkit.org/api/projects/320536/upload-file" >fileid.json
cat fileid.json
echo "Finished upload to dev.bukkit.org."

exit 0
