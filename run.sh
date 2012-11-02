#!/bin/sh

java \
	-cp ./lib/bcprov-jdk16-1.46.jar:$(ls -rt ./target/*jar | tail -1) \
	com.hashbangbash.trie.App \
	./src/test/resources/test-certv3.pem

