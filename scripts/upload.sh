#!/bin/sh

- git clone --depth=1 https://$Github_token@github.com/Ghost-chu/BinaryRepository.git BinaryRepository
  - mkdir -p ./BinaryRepository/$PROJECT_NAME/$TRAVIS_BUILD_NUMBER
  - mkdir -p ./BinaryRepository/$PROJECT_NAME/$TRAVIS_BUILD_NUMBER/target
  - cp -r ./target/* ./BinaryRepository/$PROJECT_NAME/$TRAVIS_BUILD_NUMBER/target
  - echo -e "== Built by Travis-CI ==\nBuild Number [$TRAVIS_BUILD_NUMBER]\nBuild Type
    [$TRAVIS_EVENT_TYPE]\nTag [$TRAVIS_TAG]\nCommit [$TRAVIS_COMMIT] - [$TRAVIS_COMMIT_MESSAGE]"
    > ./BinaryRepository/$PROJECT_NAME/$TRAVIS_BUILD_NUMBER/BUILDINFO
  - cd BinaryRepository
  - git add .
  - git commit -m "Auto update by Bot"
  - git push --force
  - cd .. #Back root
  - echo "Finish upload to BinaryRepository"