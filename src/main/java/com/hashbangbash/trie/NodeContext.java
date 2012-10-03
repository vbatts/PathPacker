package com.hashbangbash.trie;

/*
 * for nodes like the HuffNode, that need
 * an external incrementor to the node id
 */
public class NodeContext {
    private long huffNodeId = 0;
    
    public NodeContext() {
    }

    public NodeContext(long startId) {
        this.huffNodeId = startId;
    }

    public long getId() {
        return this.huffNodeId;
    }

    public long nextId() {
        return this.huffNodeId++;
    }
}

