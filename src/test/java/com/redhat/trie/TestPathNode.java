package com.redhat.trie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestPathNode {

    @Test
    public void testNew0() {
        PathNode pn = new PathNode();
        assertNotNull(pn);
    }

    @Test
    public void testNew1() {
        PathNode pn = new PathNode(new NodeContext());
        assertNotNull(pn);
    }

    @Test
    public void testEquivalent() {
        NodeContext ctx0 = new NodeContext();
        NodeContext ctx1 = new NodeContext();
        PathNode endMarker = new PathNode();
        PathNode pn0 = new PathNode(ctx0);
        PathNode pn1 = new PathNode(ctx1);

        assertTrue(pn0.isEquivalentTo(pn1));


        NodePair np0a = new NodePair("foo",endMarker);
        pn0.addChild(np0a);
        NodePair np1a = new NodePair("foo",endMarker);
        pn1.addChild(np1a);

        assertTrue(pn0.isEquivalentTo(pn1));


        PathNode pn2 = new PathNode(pn0.getContext());
        PathNode pn3 = new PathNode(pn1.getContext());
        NodePair np0b = new NodePair("bar",endMarker);
        pn0.addChild(np0b);
        NodePair np1b = new NodePair("baz",endMarker);
        pn1.addChild(np1b);

        // XXX finish this test !!
        //assertTrue(pn0.isEquivalentTo(pn1));
    }
}

