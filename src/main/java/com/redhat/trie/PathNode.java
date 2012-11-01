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
    public boolean isEquivalentTo(PathNode that) {
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

    /** 
     * check whether current PathNode, includes the paths in PathNode that, like a mask.
     * 
     * TODO - this is a stub
     *
     * @param that  PathNode to check for
     * @return      boolean of truth!
     */
    public boolean includes(PathNode that) {
        // if we are at the end of the tree we're checking against,
        // then it includes everything up to this point.
        if (this.getChildren().size() == 0 || that.getChildren().size() == 0) {
            return true;
        }

        // why can java allow a list of primitives ...
        List<Boolean> found = new ArrayList<Boolean>();
        boolean result;

        for (NodePair thisnp : this.getChildren()) {
            for (NodePair thatnp : that.getChildren()) {
                // keep checking, even if we hist a variablized value
                if (thisnp.getName().startsWith("$") || thisnp.getName().equals(thatnp.getName())) {
                    result = thisnp.getConnection().includes(thatnp.getConnection());
                    found.add(new Boolean(result).booleanValue());
                    break;
                }
                found.add(Boolean.FALSE);
            }
        }

        if (found.contains(Boolean.FALSE)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * pretty information
     */
    public String toString() {
        String parentList =  "";
        for (PathNode parent : parents) {
            parentList += ": " + parent.getId();
        }
        parentList += "";
        return "ID: " + id + ", Parents" + parentList + ", Children: " + children;
    }
}

