Overview
========

This is a simple utility, with the intention of reading from a packed
payload, into a searchable tree.

The tree is comprised of url path bases.

Also, to be able to take a list of these path bases, and render a packed payload.


Algorithm
=========

For more information on the algorith used to pack the payload, see 
https://github.com/jbowes/content-set-packer


Compiling
=========

You can use either ant, or maven. I included the only two jar deps, for local 
tools and ant builds.
But the unit tests are currently run with maven.


Tools
=====

The CLI class was done away with, so as not to ship in the jar, anything uneeded for 
the library. It's been rewritten in jRuby (see http://jruby.org/ ).
The script ./tools/handy.rb, has plenty of functionality, and feel free to add more.

** Usage
  $> jruby tools/handy.rb --help
  Usage: handy [options]
        --dot                        output the dot digraph of content listing (defaults to ./src/test/resources/contents.list
        --contents FILE              use FILE instead of ./src/test/resources/contents.list
        --cert FILE                  read contents from certificate FILE
        --test PATH                  validate PATH, instead of [/content/beta/rhel/server/5/5server/x86_64/sap/os/repomd.xml]
        --print                      print the tree of contents

  $> jruby ./tools/handy.rb --dot > contents.dot && dot -Tpng contents.dot -o contents.png && display contents.png

  $> jruby ./tools/handy.rb --cert ./src/test/resources/test-certv3.pem 
  [/content/beta/rhel/server/5/5server/x86_64/sap/os/repomd.xml] true
  [/foo/path, /foo/path/always/$releasever, /foo/path/never]

