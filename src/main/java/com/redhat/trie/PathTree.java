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

/*
 * PathTree
 *
 * The efficient means by which to check the content sets
 *
 * TODO - this is a prototype stub
 */
public class PathTree {
    private HuffNode dict;
    private PathNode tree;

    public PathTree() {
    }

    public PathTree(byte[] payload) {
    }

    public boolean validate(String contentPath) {
        return false;
    }

    public List<String> toList() {
        return null;
    }
}

