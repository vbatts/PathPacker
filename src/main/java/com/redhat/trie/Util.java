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

import java.io.IOException;

import java.security.cert.X509Certificate;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

/*
 * Util
 *
 * All the misc dirty work
 *
 */
public class Util {
    /* 
     * PrettyPrint a PathNode tree
     */
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

    /* 
     * PrettyPrint a HuffNode tree
     */
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
     * From the deflated payload, produce the content set lists
     *
     *
     * FIXME - break this apart, so that the hydrated payload
     *         can be structure to more quickly search, and use less memory
     *
     *      Rename it for tracking, and to be clear about what is happening
     */
    public static List<String> hydrateContentPackage(byte[] compressedBlob)
        throws PayloadException {
        try {
            PathTree pt = new PathTree(compressedBlob);
            return pt.toList();
        } catch (Throwable t) {
            throw t;
        }
    }


    public static ASN1Encodable objectFromOid(X509Certificate cert, String oid) {
        if (cert == null) { return null; }

        try {
            for (String thisOid : cert.getNonCriticalExtensionOIDs()) {
                if (thisOid.equals(oid)) {
                    return X509ExtensionUtil.fromExtensionValue(cert.getExtensionValue(oid));
                }
            }
        } catch (IOException ex) { }
        return null;
    }
}

