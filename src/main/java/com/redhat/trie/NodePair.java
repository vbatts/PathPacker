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

public class NodePair implements Comparable {
    private String name;
    private PathNode connection;

    public NodePair(String name, PathNode connection) {
        this.name = name;
        this.connection = connection;
    }

    public String getName() {
        return this.name;
    }

    public long getId() {
        return this.getConnection().getId();
    }

    public PathNode getConnection() {
        return this.connection;
    }

    public void setConnection(PathNode connection) {
        this.connection = connection;
    }

    /**
     * pretty information
     */
    public String toString() {
        return "Name: " + name + ", Connection: " + connection.getId();
    }

    public int compareTo(Object other) {
        return this.name.compareTo(((NodePair) other).name);
    }
    
    /**
     * Does this <tt>NodePair</tt> have any children?
     * @return <tt>true</tt> if it has at least one connection and <tt>false</tt> otherwise.
     */
    public boolean hasNoChildren() {
      return getConnection().getChildren().isEmpty();
    }
}

