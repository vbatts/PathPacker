#!/usr/bin/env jruby

BASE_DIR = File.expand_path(File.dirname(__FILE__) + "/..")
require 'java'
Dir[File.join(BASE_DIR,'lib/*jar')].each {|j| require j }
Dir[File.join(BASE_DIR,'target/*jar')].each {|j| require j }

require 'stringio'
require 'optparse'

module Trie
  include_package 'com.redhat.trie'
end

def pt(path = './src/test/resources/contents.list')
  content_sets = File.open(path).read.split("\n")
  Trie::PathTree.new(content_sets)
end

def print_dot(root)
  sio = StringIO.new
  sio.write("digraph pathnode_tree {\n")

  write_children(sio, root)

  sio.write("\n}\n")

  sio.seek(0)
  sio
end

def write_children(io, node)
  return unless (node.getChildren().length > 0)
  node.getChildren.each do |child|
    id = (child.getConnection().getId().to_s + child.getName()).hash
    children_ids = child.getConnection().getChildren().map {|c|
      (c.getConnection().getId().to_s + c.getName()).hash
    }

    io.write("  %d [label=\"%s\"];\n" % [id, child.getName()])
    io.write("  %d -> { %s };\n" % [id, children_ids.join(" ")]) if children_ids.length > 0

    write_children(io, child.getConnection())
  end
end

def get_end(node)
  return node if node.getChildren().length == 0
  node.getChildren.each do |child|
    return get_end(child.getConnection())
  end
end
def parse_args(args)
  options = {
    :content_list => './src/test/resources/contents.list',
    :dot          => false,
    :test_url     => '/content/beta/rhel/server/5/5server/x86_64/sap/os/repomd.xml',
  }
  opts = OptionParser.new do |opts|
    opts.on('--dot',"output the dot digraph of content listing (defaults to #{options[:content_list]}") do |o|
      options[:dot] = o
    end
    opts.on('--contents FILE', "use FILE instead of #{options[:content_list]}") do |o|
      options[:content_list] = o
    end
    opts.on('--test PATH', "validate PATH, instead of [#{options[:test_url]}]") do |o|
      options[:test_url] = o
    end
  end

  opts.parse!(args)

  return options
end

def main(args)
  options = parse_args(args)

  STDERR.puts("[%s] %s" % [options[:test_url], pt.validate(options[:test_url])])

  puts print_dot(pt(options[:content_list]).getRootPathNode()).read() if options[:dot]

  #pn = pt.getRootPathNode
  #Trie::Util.printTree(pn, 0)
end

main(ARGV) if $0 == __FILE__

