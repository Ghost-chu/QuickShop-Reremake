#!/bin/sh

basedir='C:\Users\Administrator\'
workdir=$(
  cd $(dirname $0)
  pwd
)
proj_id=$(mvn -q -N -Dexec.executable="echo" -Dexec.args='${project.artifactId}' org.codehaus.mojo:exec-maven-plugin:1.3.1:exec)
proj_version=$(mvn -q -N -Dexec.executable="echo" -Dexec.args='${project.version}' org.codehaus.mojo:exec-maven-plugin:1.3.1:exec)
repo_path=$basedir'akarin-repo/repository/'

(
  if [ ! -d $basedir'akarin-repo' ]; then
    echo '[Deploy] Synchronizing remote..'
    cd $basedir
    git clone 'https://github.com/Akarin-project/Akarin-repo.git'
    echo '[Deploy] Synchronized remote!'
  fi

  echo '[Deploy] Deploy to local repository..'
  cd $workdir && cd ..
  mvn deploy -DaltDeploymentRepository=akarin-repo::default::file:$repo_path
  echo '[Deploy] Deployed to local repository successfully'

  cd $repo_path && cd ..
  echo '[Deploy] Synchronizing remote..'

  commit="git commit -m '[Deploy] Update "$proj_id" "$proj_version"'"
  git pull && git add . && eval $commit && git push

  echo '[Deploy] Deployed to Akarin-repo successfully!'
)
