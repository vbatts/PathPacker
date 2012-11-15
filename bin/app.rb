#!/usr/bin/env jruby

BASE_DIR = File.expand_path(File.dirname(__FILE__) + "/..")
require 'java'
Dir[File.join(BASE_DIR,'lib/*jar')].each {|j| require j }
Dir[File.join(BASE_DIR,'target/*jar')].each {|j| require j }

require 'openssl'
require 'stringio'
require 'optparse'

import java.io.BufferedInputStream
import java.io.FileInputStream
import java.security.cert.X509Certificate
import java.security.cert.CertificateFactory

import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.x509.extension.X509ExtensionUtil

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

# PrettyPrint a PathNode or HuffNode tree
def printTree(node, tab, opts = {})
  io = opts[:io] ? opts[:io] : STDOUT
  nodeRep = " " * tab

  nodeRep += "Node [#{node.getId}]"
  nodeRep += ", Weight [#{node.getWeight}]" if node.respond_to? :getWeight
  nodeRep += ", Value = [#{node.getValue}]" if node.respond_to? :getValue
  node.getParents.each do |parent|
    nodeRep += " ^ [#{parent.getId}]"
  end if node.respond_to? :getParents
  node.getChildren.each do |child|
    nodeRep += " v [#{child.getName} {#{child.getId}}]"
  end if node.respond_to? :getChildren

  io.puts nodeRep

  node.getChildren.each do |child|
    printTree(child.getConnection, tab+1, opts)
  end if node.respond_to? :getChildren
  if node.respond_to?(:getLeft) and node.respond_to?(:getRight)
    printTree(node.getLeft, tab+1, opts) if node.getLeft != nil
    printTree(node.getRight, tab+1, opts) if node.getRight != nil
  end
end

# ick, using java to do SSL
def object_from_oid(cert, oid)
  return unless cert
  cert.getNonCriticalExtensionOIDs.each do |o|
    if o == oid
      return X509ExtensionUtil.fromExtensionValue(cert.getExtensionValue(o))
    end
  end 
  return nil
end

# ick, using java to do SSL
def value_from_oid(filename, oid)
  bis = BufferedInputStream.new(FileInputStream.new(filename))
  cf = CertificateFactory.getInstance("X.509")
  cert = cf.generateCertificate(bis) # this is an X509Certificate

  object_from_oid(cert, oid)
end

# not working on jRuby. :-(
# https://github.com/jruby/jruby/issues/389
def value_from_oid_bunk(filename, oid)
  cert = OpenSSL::X509::Certificate.new(File.read(filename))
  ext = cert.extensions.detect {|ext| ext.oid == oid }
  return if ext.nil?

  return OpenSSL::ASN1.decode(OpenSSL::ASN1.decode(ext.to_der).value[1].value).value
end

def _puts(opts, str)
  if opts[:output_file]
    begin
      count = File.open(opts[:output_file],'w') {|f| f.write(str) }
      puts "wrote #{count} to #{opts[:output_file]} ..."
    rescue => ex
      STDERR.puts(ex)
    end
  else
    puts str
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
    opts.on('--cert FILE', 'read contents from certificate FILE') do |o|
      options[:certificate] = o
    end
    opts.on('--binary FILE', 'read contents from packed binary FILE') do |o|
      options[:binary_file] = o
    end
    opts.on('--test PATH', "validate PATH, instead of [#{options[:test_url]}]") do |o|
      options[:test_url] = o
    end
    opts.on('--print', 'print the tree of contents') do |o|
      options[:printTree] = o
    end
    opts.on('--gen-payload', 'generate the binary payload from the content sets') do |o|
      options[:payload] = o
    end
    opts.on('-o FILE','put output to FILE, instead of STDOUT') do |o|
      options[:output_file] = o
    end
  end

  opts.parse!(args)

  return options
end

def main(args)
  options = parse_args(args)

  if options[:dot]
    _puts(options, print_dot(pt(options[:content_list]).getRootPathNode()).read())
    return
  end
  if options[:printTree]
    pn = pt.getRootPathNode
    io = options[:output_file] ? File.open(options[:output_file], 'w') : STDOUT
    printTree(pn, 0, :io => io)
    if options[:output_file]
      puts "wrote #{io.pos} to #{options[:output_file]} ..."
      io.close
    end
    return
  end
  if options[:certificate]
    data = value_from_oid(options[:certificate], '1.3.6.1.4.1.2312.9.7')
    pt = Trie::PathTree.new(data.getOctets)
    _puts(options, pt.toList().join("\n"))
    return
  end
  if options[:binary_file]
    pt = Trie::PathTree.new(File.read(options[:binary_file]).to_java_bytes)
    _puts(options, pt.toList().join("\n"))
    return
  end
  if options[:payload]
    _puts(options, pt(options[:content_list]).getPayload())
    return
  end

  # Default behaviour (no other flags provided)
  puts "[%s] %s" % [options[:test_url], pt.validate(options[:test_url])]

end

main(ARGV) if $0 == __FILE__

