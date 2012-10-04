package com.hashbangbash.trie;

/*
 * for nodes like the HuffNode, that need
 * an external incrementor to the node id
 */
public class NodeContext {
    private long nodeId = 0;
    
    public NodeContext() {
    }

    public NodeContext(long startId) {
        this.nodeId = startId;
    }

    public long getId() {
        return this.nodeId;
    }

    public long nextId() {
        return this.nodeId++;
    }
}

