package com.hashbangbash.trie;

public class NodePair {
    private String name;
    private PathNode connection;

    public NodePair(String name, PathNode connection) {
        this.name = name;
        this.connection = connection;
    }

    public String getName() {
        return this.name;
    }

    public PathNode getConnection() {
        return this.connection;
    }

    public void setConnection(PathNode connection) {
        this.connection = connection;
    }

    public String toString() {
        return "Name: " + name + ", Connection: " + connection.getId();
    }
}

