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
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import org.apache.log4j.Logger;

/**
 * PathNode is the relationship to an item in the path tree.
 *
 * It holds the relationships to children, as well as all parents that regard it as a child.
 *
 * The Name of a given PathNode, is inferred by the NodePair that regards this PathNode as its "connection"
 */
public class PathNode {
    private static org.apache.log4j.Logger log = Logger.getLogger(PathTree.class);
    private long id = 0;
    private List<NodePair> children = new ArrayList<NodePair>();
    private List<PathNode> parents = new ArrayList<PathNode>();
    private NodeContext ctx = null;

    /**
     * New node, with 0 id.
     */
    public PathNode() {
        this(new NodeContext());
    }

    /**
     * New node, with id determined by provided ctx
     *
     * @param   ctx     NodeContext, for id increments
     */
    public PathNode(NodeContext ctx) {
        this.ctx = ctx;
        this.id = this.ctx.nextId();
    }

    /**
     * This node's id
     */
    public long getId() {
        return this.id;
    }
    
    /**
     * The NodeContext used by this node
     */
    public NodeContext getContext() {
        return this.ctx;
    }
    
    /**
     * Get the nodes, from here down.
     *
     * @return      A unique list of all PathNode nodes, from this node down
     */
    public Set<PathNode> getAllNodes() {
        return getAllNodes(this);
    }

    /**
     * return the unique set of PathNodes in a given node.
     *
     * @param node    a "root" PathNode. Which can all be a matter of perspective.
     * @return        the unique Set of Nodes
     */
    public Set<PathNode> getAllNodes(PathNode node) {
        Set<PathNode> nodes = new HashSet<PathNode>();
        nodes.add(node);
        for (NodePair np : node.getChildren()) {
            nodes.addAll(getAllNodes(np.getConnection()));
        }
        return nodes;
    }

    public List<NodePair> getChildren() {
        Collections.sort(this.children);
        return this.children;
    }

    public List<PathNode> getParents() {
        return this.parents;
    }

    /**
     * A NodePair cp, as a child.
     *
     * TODO - determine uniqueness?
     */
    public void addChild(NodePair cp) {
        this.children.add(cp);
    }

    /**
     * A PathNode cp, as a parent.
     *
     * Checks whether this parent is already a parent.
     */
    public void addParent(PathNode cp) {
        if (!parents.contains(cp)) {
            this.parents.add(cp);
        }
    }

    /**
     * Set parents as the new List collection.
     */
    public void setParents(List<PathNode> parents) {
        this.parents = parents;
    }

    /**
     * add entire List of parents
     */
    public void addParents(List<PathNode> parents) {
        for (PathNode pn : parents) {
            addParent(pn);
        }
    }

    /**
     * get the inferred name of this node, through the referring NodePair.
     */
    public String getName() {
        String name = "";
        for (NodePair child : this.getParents().get(0).getChildren()) {
            if (child.getConnection().getId() == this.getId()) {
                return child.getName();
            }
        }
        return name;
    }

    /**
     * Traverse up the tree, and get the highest ancestor PathNode.
     */
    public PathNode getStartNode() {
        return getStartNode(this);
    }

    /**
     * Traverse up the tree, and get the highest ancestor PathNode, for node.
     */
    public PathNode getStartNode(PathNode node) {
        if (node.getParents().size() == 0) {
            return node; // this is the end!
        }

        for (PathNode parent : node.getParents()) {
            return node.getStartNode(parent);
        }
        return node; // when in doubt, return yourself
    }

    /**
     * Traverse down the tree, and get the "endMarker" child.
     */
    public PathNode getEndNode() {
        return getEndNode(this);
    }

    /**
     * Traverse down the tree, and get the "endMarker" child, for node.
     */
    public PathNode getEndNode(PathNode node) {
        if (node.getChildren().size() == 0) {
            return node; // this is the end!
        }
        for (NodePair child : node.getChildren()) {
            return node.getEndNode(child.getConnection());
        }
        return node; // when in doubt, return yourself
    }

    /**
     * same number of children with the same names for child nodes
     */
    boolean isEquivalentTo(PathNode that) {
        // same number of children with the same names for child nodes
        if (this.getChildren().size() != that.getChildren().size()) {
            return false;
        }

        if (this.getId() == that.getId()) {
            return true;
        }

        for (NodePair thisnp : this.getChildren()) {
            boolean found = false;
            for (NodePair thatnp : that.getChildren()) {
                if (thisnp.getName().equals(thatnp.getName())) {
                    if(thisnp.getConnection().isEquivalentTo(thatnp.getConnection())) {
                        found = true;
                        break;
                    }
                    else {
                        return false;
                    }   
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

        for (NodePair thatnp : that.getChildren()) {
            for (NodePair thisnp : this.getChildren()) {
                // keep checking, even if we hit a variablized value
                if ( thatnp.getName().startsWith("$") || 
                     thisnp.getName().startsWith("$") ||
                     thisnp.getName().equals(thatnp.getName()) ) {
                    result = thisnp.getConnection().includes(thatnp.getConnection());
                    found.add(new Boolean(result).booleanValue());
                    log.debug("includes: this: " + thisnp.getName() + " == that:" + thatnp.getName());
                }
            }
        }

        // return true, if we've found all of thats children
        return (found.size() == that.getChildren().size());
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
        return "ID: " + id + ", Name: " + this.getName() + ", Parents" + parentList + ", Children: " + children;
    }
}

