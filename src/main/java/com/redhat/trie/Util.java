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

import java.util.List;
import java.util.ArrayList;

import java.util.StringTokenizer;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/*
 * Util
 *
 */
public class Util {
    private NodeContext ctx;

    public Util() {
        this.ctx = new NodeContext();
    }

    public PathNode makePathTree(List<String> contents, PathNode parent) {
        PathNode endMarker = new PathNode(this.ctx);
        for (String path : contents) {
            StringTokenizer st = new StringTokenizer(path, "/");
            makePathForURL(st, parent, endMarker);
        }
        //condenseSubTreeNodes(endMarker);
        return parent;
    }

    public static void printTree(PathNode pn, int tab) {
        StringBuffer nodeRep = new StringBuffer();
        for (int i = 0; i <= tab; i++) {
            nodeRep.append("  ");
        }
        nodeRep.append("Node [");
        nodeRep.append(pn.getId());
        nodeRep.append("]");

        for (PathNode parent : pn.getParents()) {
            nodeRep.append(" ^ [");
            nodeRep.append(parent.getId());
            nodeRep.append("]");
        }
        for (NodePair cp : pn.getChildren()) {
            nodeRep.append(" v [");
            nodeRep.append(cp.getName());
            nodeRep.append(" {");
            nodeRep.append(cp.getConnection().getId());
            nodeRep.append("} ]");
        }
        System.out.println(nodeRep);
        for (NodePair cp : pn.getChildren()) {
            printTree(cp.getConnection(), tab + 1);
        }
    }

    public static void printTrie(HuffNode hn, int tab) {
        StringBuffer nodeRep = new StringBuffer();
        for (int i = 0; i <= tab; i++) {
            nodeRep.append("  ");
        }
        nodeRep.append("Node [");
        nodeRep.append(hn.getId());
        nodeRep.append("]");

        nodeRep.append(", Weight [");
        nodeRep.append(hn.getWeight());
        nodeRep.append("]");

        nodeRep.append(", Value = [");
        nodeRep.append(hn.getValue());
        nodeRep.append("]");

        System.out.println(nodeRep);
        if (hn.getLeft() != null) {
            printTrie(hn.getLeft(), tab + 1);
        }
        if (hn.getRight() != null) {
            printTrie(hn.getRight(), tab + 1);
        }
    }

    /*
     * given a tokenized URL path, build out the PathNode parent,
     * and append endMarker to terminal nodes.
     */
    private void makePathForURL(StringTokenizer st, PathNode parent, PathNode endMarker) {
        if (st.hasMoreTokens()) {
            String childVal = st.nextToken();
            if (childVal.equals("")) {
                return;
            }

            boolean isNew = true;
            for (NodePair child : parent.getChildren()) {
                if (child.getName().equals(childVal) &&
                        !child.getConnection().equals(endMarker)) {
                    makePathForURL(st, child.getConnection(), endMarker);
                    isNew = false;
                }
            }
            if (isNew) {
                PathNode next = null;
                if (st.hasMoreTokens()) {
                    next = new PathNode(parent.getContext());
                    parent.addChild(new NodePair(childVal, next));
                    next.addParent(parent);
                    makePathForURL(st, next, endMarker);
                } else {
                    parent.addChild(new NodePair(childVal, endMarker));
                    if (!endMarker.getParents().contains(parent)) {
                        endMarker.addParent(parent);
                    }
                }
            }
        }
    }

    public void condenseSubTreeNodes(PathNode location) {
        // "equivalent" parents are merged
        List<PathNode> parentResult = new ArrayList<PathNode>();
        parentResult.addAll(location.getParents());
        for (PathNode parent1 : location.getParents()) {
            if (!parentResult.contains(parent1)) {
                continue;
            }
            for (PathNode parent2 : location.getParents()) {
                if (!parentResult.contains(parent2) ||
                    parent2.getId() == parent1.getId()) {
                    continue;
                }
                if (parent1.isEquivalentTo(parent2)) {
                    // we merge them into smaller Id
                    PathNode merged = parent1.getId() < parent2.getId() ?
                        parent1 : parent2;
                    PathNode toRemove = parent1.getId() < parent2.getId() ?
                        parent2 : parent1;

                    // track down the name of the string in the grandparent
                    //  that points to parent
                    String name = "";
                    PathNode oneParent = toRemove.getParents().get(0);
                    for (NodePair child : oneParent.getChildren()) {
                        if (child.getConnection().getId() == toRemove.getId()) {
                            name = child.getName();
                            break;
                        }
                    }

                    // copy grandparents to merged parent node.
                    List<PathNode> movingParents = toRemove.getParents();
                    merged.addParents(movingParents);

                    // all grandparents with name now point to merged node
                    for (PathNode pn : toRemove.getParents()) { 
                        for (NodePair child : pn.getChildren()) {
                            if (child.getName().equals(name)) {
                                child.setConnection(merged);
                            }
                        }
                    }
                    parentResult.remove(toRemove);
                }
            }
        }
        location.setParents(parentResult);
        for (PathNode pn : location.getParents()) {
            condenseSubTreeNodes(pn);
        }
    }

    public List<String> orderStrings(PathNode parent) throws IOException {     
        List<String> parts = new ArrayList<String>();                          
        // walk tree to make string map  
        Map<String, Integer> segments =  new HashMap<String, Integer>();       
        Set<PathNode> nodes =  new HashSet<PathNode>();                        
        buildSegments(segments, nodes, parent);                                
        for (String part : segments.keySet()) {                                
            if (!part.equals("")) {
                int count = segments.get(part);                                
                if (parts.size() == 0) {                                       
                    parts.add(part);                                           
                }
                else {
                    int pos = parts.size();
                    for (int i = 0; i < parts.size(); i++) {
                        if (count < segments.get(parts.get(i))) {              
                            pos = i;                                           
                            break;                                             
                        }                                                      
                    }
                    parts.add(pos, part);                                      
                }                                                              
            }                                                                  
        }
        return parts;                                                          
    }                                                                          

    private void buildSegments(Map<String, Integer> segments,
        Set<PathNode> nodes, PathNode parent) {
        if (!nodes.contains(parent)) {
            nodes.add(parent);
            for (NodePair np : parent.getChildren()) {
                Integer count = segments.get(np.getName());
                if (count == null) {
                    count = new Integer(0);
                }
                segments.put(np.getName(), ++count);
                buildSegments(segments, nodes, np.getConnection());
            }
        }
    }

    private List<PathNode> orderNodes(PathNode treeRoot) {
        List<PathNode> result = new ArrayList<PathNode>();

        // walk tree to make string map
        Set<PathNode> nodes =  getPathNodes(treeRoot);
        for (PathNode pn : nodes) {
            int count = pn.getParents().size();
            if (nodes.size() == 0) {
                nodes.add(pn);
            }
            else {
                int pos = result.size();
                for (int i = 0; i < result.size(); i++) {
                    if (count <= result.get(i).getParents().size()) {
                        pos = i;
                        break;
                    }
                }
                result.add(pos, pn);
            }
        }
        return result; 
    }

    private Set<PathNode> getPathNodes(PathNode treeRoot) {
        Set<PathNode> nodes = new HashSet<PathNode>();
        nodes.add(treeRoot);
        for (NodePair np : treeRoot.getChildren()) {
            nodes.addAll(getPathNodes(np.getConnection()));
        }
        return nodes;
    }


}

