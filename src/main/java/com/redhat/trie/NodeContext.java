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

/*
 * for nodes like the HuffNode, that need
 * an external incrementor to the node id
 */
public class NodeContext {
    private long nodeId = 0;
    
    /**
     * Constructs with an id of 0
     */
    public NodeContext() {
    }

    /**
     * Constructs with provided startId
     *
     * @param startId   long of the id to start at
     */
    public NodeContext(long startId) {
        this.nodeId = startId;
    }

    /**
     * current context id
     *
     * @return      long of current id position
     */
    public long getId() {
        return this.nodeId;
    }

    /**
     * get current context id, and increment the id
     *
     * @return      long of current id position
     */
    public long nextId() {
        return this.nodeId++;
    }
}

