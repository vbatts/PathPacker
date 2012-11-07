#!/bin/sh

java \
	-cp $(find lib/ -type f -printf "./%h/%f:")$(ls -rt ./target/*jar | tail -1) \
	com.hashbangbash.trie.App \
	./src/test/resources/test-certv3.pem

