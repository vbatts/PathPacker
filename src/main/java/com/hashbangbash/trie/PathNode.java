package com.hashbangbash.trie;

import java.util.ArrayList;
import java.util.List;

public class PathNode {
    private long id = 0;
    private List<NodePair> children = new ArrayList<NodePair>();
    private List<PathNode> parents = new ArrayList<PathNode>();
    private NodeContext ctx = null;

    public PathNode() {
        this(new NodeContext());
    }

    public PathNode(NodeContext ctx) {
        this.ctx = ctx;
        this.id = this.ctx.nextId();
    }

    public long getId() {
        return this.id;
    }
    
    void addChild(NodePair cp) {
        this.children.add(cp);
    }

    void addParent(PathNode cp) {
        if (!parents.contains(cp)) {
            this.parents.add(cp);
        }
    }

    public List<NodePair> getChildren() {
        return this.children;
    }

    List<PathNode> getParents() {
        return this.parents;
    }

    void setParents(List<PathNode> parents) {
        this.parents = parents;
    }

    void addParents(List<PathNode> parents) {
        for (PathNode pn : parents) {
            addParent(pn);
        }
    }

    /*
     * same number of children with the same names for child nodes
     */
    boolean isEquivalentTo(PathNode that) {
        if (this.getChildren().size() != that.getChildren().size()) {
            return false;
        }

        for (NodePair thisnp : this.getChildren()) {
            boolean found = false;
            for (NodePair thatnp : that.getChildren()) {
                if (thisnp.getName().equals(thatnp.getName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        String parentList =  "";
        for (PathNode parent : parents) {
            parentList += ": " + parent.getId();
        }
        parentList += "";
        return "ID: " + id + ", Parents" + parentList + ", Children: " + children;
    }
}

