#!/bin/sh

java -cp ./lib/bcprov-jdk16-1.46.jar:$(ls -rt ./dist/*jar | tail -1) com.hashbangbash.trie.App ./data/test-certv3.pem

