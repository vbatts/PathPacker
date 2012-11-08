package com.redhat.trie;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

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

        NodePair np0b = new NodePair("bar",endMarker);
        pn0.addChild(np0b);

        assertFalse(pn0.isEquivalentTo(pn1));
    }
    
    @Test
    public void testIncludes() {
        PathNode pn0 = new PathNode();
        PathNode pn1 = new PathNode();
        PathNode pn2 = new PathNode();

        PathTree pt0 = new PathTree();
        PathTree pt1 = new PathTree();
        PathTree pt2 = new PathTree();

        List<String> contents0 = TestHelpers.loadContents(this, "contents.list");
        List<String> contents1 = TestHelpers.loadContents(this, "contents_small.list");

        try {
            pt0.setContentSets(contents0);
            pn0 = pt0.getRootPathNode(); // setup the larger PathNode

            pt1.setContentSets(contents1);
            pn1 = pt1.getRootPathNode(); // setup the small PathNode

            contents1.add(new String("/this/is/not/in/the/list"));
            pt2.setContentSets(contents1);
            pn2 = pt2.getRootPathNode(); // setup the small PathNode
        } catch (PayloadException ex) {
            fail(ex.toString());
        }

        assertNotNull(pn0);
        assertNotNull(pn1);
        assertNotNull(pn2);

        // FIXME - Finish the test first
        //assertTrue(pn0.includes(pn1));
        //assertFalse(pn0.includes(pn2));
    }
}

