#!/bin/sh
openssl aes-256-cbc -K $encrypted_210a330fcca4_key -iv $encrypted_210a330fcca4_iv -in key.txt.enc -out key.txt -d
gpg --import key.txt
chmod +x ./git-crypt
./git-crypt unlock
