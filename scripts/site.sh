#!/bin/sh
echo "Deploying to the Github..."
git clone --depth=1 https://$Github_token@github.com/Ghost-chu/maven-repo.git maven-repo
mvn deploy -DaltDeploymentRepository=mvn-repo::default::file:./maven-repo
cd maven-repo
git add .
git commit -m "Auto update by Travis-CI bot"
git push --force
cd ..
echo "Finish upload to Maven Repository"
