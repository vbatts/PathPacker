package com.hashbangbash.trie;

public class HuffNode {
    private long id = 0;
    private Object value = null;
    private int weight = 0;
    private HuffNode left = null;
    private HuffNode right = null;
    private NodeContext ctx = null;

    public HuffNode(Object value, int weight, HuffNode left, HuffNode right) {
        this(new NodeContext(), value, weight, left, right);
    }

    public HuffNode(NodeContext ctx, Object value, int weight, HuffNode left, HuffNode right) {
        this.ctx = ctx;
        this.value = value;
        this.weight = weight;
        this.left = left;
        this.right = right;
        this.id = this.ctx.nextId();
    }

    public HuffNode(Object value, int weight) {
        this(new NodeContext(), value, weight);
    }

    public HuffNode(NodeContext ctx, Object value, int weight) {
        this.ctx = ctx;
        this.value = value;
        this.weight = weight;
        this.id = this.ctx.nextId();
    }
    
    public NodeContext getContext() {
        return this.ctx;
    }

    public long getId() {
        return this.id;
    }

    public Object getObject() {
        return this.value;
    }

    public int getWeight() {
        return this.weight;
    }

    public HuffNode getLeft() {
        return this.left;
    }

    public HuffNode getRight() {
        return this.right;
    }

    public String toString() {
        return "ID: " + id +
            ", Value " + value +
            ", Weight: " + weight +
            ", Left: " + left +
            ", Right: " + right;
    }
}

