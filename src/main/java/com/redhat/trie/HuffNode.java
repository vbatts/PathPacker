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

public class HuffNode {
    /**
     * Empty node to be referenced as the authority on what is an "end node"
     */
    public static final Object END_NODE = new Object();

    private long id = 0;
    private Object value = null;
    private int weight = 0;
    private HuffNode left = null;
    private HuffNode right = null;
    private NodeContext ctx = null;

    /**
     * storage of Object value, it's weighting, and children, assumes new NodeContext
     */
    public HuffNode(Object value, int weight, HuffNode left, HuffNode right) {
        this(new NodeContext(), value, weight, left, right);
    }

    /**
     * storage of Object value, it's weighting, and children, and existing NodeContext.
     *
     * This will increment the NodeContext
     */
    public HuffNode(NodeContext ctx, Object value, int weight, HuffNode left, HuffNode right) {
        this.ctx = ctx;
        this.value = value;
        this.weight = weight;
        this.left = left;
        this.right = right;
        this.id = this.ctx.nextId();
    }

    /**
     * storage of Object's value, and its weighting, assumes new NodeContext
     */
    public HuffNode(Object value, int weight) {
        this(new NodeContext(), value, weight);
    }

    /**
     * storage of Object's value, and its weighting, and existing NodeContext.
     *
     * This will increment the NodeContext
     */
    public HuffNode(NodeContext ctx, Object value, int weight) {
        this.ctx = ctx;
        this.value = value;
        this.weight = weight;
        this.id = this.ctx.nextId();
    }
    
    /**
     * return the NodeContext incrementor
     */
    public NodeContext getContext() {
        return this.ctx;
    }

    /**
     * return this node's id, per the NodeContext incrementor
     */
    public long getId() {
        return this.id;
    }

    /**
     * return the stored Object
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * return the node's weight
     */
    public int getWeight() {
        return this.weight;
    }

    /**
     * node on the left
     */
    public HuffNode getLeft() {
        return this.left;
    }

    /**
     * node on the right
     */
    public HuffNode getRight() {
        return this.right;
    }

    /**
     * search down the tree, for the node,
     * at address of String bits
     */
    public HuffNode findByBits(String bits) {
        return this.findByBits(this, bits);
    }

    /**
     * search down the tree, for the node,
     * at address of String bits, on HuffNode trie
     */
    public HuffNode findByBits(HuffNode trie, String bits) {
        if (bits.length() == 0) {
            return trie;
        }
        char bit = bits.charAt(0);
        if (bit == '0') {
            if (getLeft() == null) { throw new RuntimeException("Encoded path not in trie"); }
            return getLeft().findByBits(bits.substring(1));
        }
        else if (bit == '1') {
            if (getRight() == null) { throw new RuntimeException("Encoded path not in trie"); }
            return getRight().findByBits(bits.substring(1));
        }
        return null;
    }

    /**
     * pretty information
     */
    public String toString() {
        return "ID: " + id +
            ", Value " + value +
            ", Weight: " + weight +
            ", Left: " + left +
            ", Right: " + right;
    }
}

