/**
 * Copyright (c) 2009 - 2012 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.trie;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

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
    
    public NodeContext getContext() {
        return this.ctx;
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
        Collections.sort(this.children);
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

