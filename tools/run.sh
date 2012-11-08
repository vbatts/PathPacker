#!/bin/sh

base_dir=$(readlink -f $(dirname $0)/..)

java \
	-cp $(find ${base_dir}/lib/ -type f -printf "%h/%f:")$(ls -rt ${base_dir}/target/*jar | tail -1) \
	com.hashbangbash.trie.App \
	${base_dir}/src/test/resources/test-certv3.pem

