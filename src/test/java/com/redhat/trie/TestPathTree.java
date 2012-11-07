package com.redhat.trie;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestPathTree {

    @Test
    public void testNew0() {
        PathTree pt = new PathTree();
        assertNotNull(pt);
    }

    @Test
    public void testNew1() {
        PathTree pt = new PathTree();
        List<String> contents = loadContents("contents.list");
        try {
            pt = new PathTree(contents);
        } catch (PayloadException ex) {
            fail(ex.toString());
        }
        assertNotNull(pt);
    }

    @Test
    public void testNew2() {
        PathTree pt = new PathTree();
        List<String> contents = loadContents("contents.list");
        try {
            pt.setContentSets(contents);
        } catch (PayloadException ex) {
            fail(ex.toString());
        }
        assertNotNull(pt);
    }

    @Test
    public void testRootNode() {
        PathTree pt = new PathTree();
        List<String> contents = loadContents("contents.list");
        try {
            pt.setContentSets(contents);
        } catch (PayloadException ex) {
            fail(ex.toString());
        }

        // generate the root PathNode
        try {
            assertNotNull(pt.getRootPathNode());
        } catch (PayloadException ex) {
            fail(ex.toString());
        }

        // do it again, to make sure it was not a mistake
        try {
            assertNotNull(pt.getRootPathNode());
        } catch (PayloadException ex) {
            fail(ex.toString());
        }
    }

    @Test
    public void testThereAndBackAgainPayload() {
        byte[] bytes;
        PathTree pt0;
        PathTree pt1;
        List<String> contents0;
        List<String> contents1;

        bytes = loadBytes("test.bin");
        pt0 = new PathTree(bytes);
        contents0 = pt0.toList();
        for (String str : contents0) {
            System.out.println(str);
        }
        
        assertEquals(contents0.size(), 3);
        //printByteArray(bytes);

        try {
            pt1 = new PathTree(contents0);
            assertNotNull(pt1);
            //printByteArray(pt1.getPayload());
            contents1 = pt1.toList();
            assertTrue(cmpStrings(contents0, contents1));
            for (String str : contents1) {
                System.out.println(str);
            }
        } catch (Throwable ex) {
            fail(ex.toString());
        }

    }

    @Test
    public void testThereAndBackAgainCS() {
        PathTree pt0 = new PathTree();
        PathTree pt1;
        List<String> contents0 = loadContents("contents.list");
        List<String> contents1;
        byte[] bytes;


        try {
            pt0.setContentSets(contents0);
        } catch (PayloadException ex) {
            fail(ex.toString());
        }
        bytes = pt0.getPayload();
        assertNotNull(bytes);
        //printByteArray(bytes);

        pt1 = new PathTree(bytes);
        contents1 = pt1.toList();
        
        assertTrue(cmpStrings(contents0, contents1));
        assertEquals(contents0.size(), contents1.size());

        //System.out.println(contents0.size());
        //System.out.println(contents1.size());

        //Collections.sort(contents0);
        //Collections.sort(contents1);

        /*
        System.out.println("Originals, not in New");
        for (String thisS : contents0) {
            if (! contents1.contains(thisS)) {
                System.out.println(thisS);
            }
        }

        System.out.println("New, not in Original");
        for (String thisS : contents1) {
            if (! contents0.contains(thisS)) {
                System.out.println(thisS);
            }
        }
        */

    }


    // Helpers
    // 
    private boolean cmpStrings(List<String> thisList, List<String> thatList) {
        Collection<String> thisColl = new ArrayList(thisList);
        Collection<String> thatColl = new ArrayList(thatList);

        Collection<String> similar = new HashSet<String>( thisColl );
        Collection<String> different = new HashSet<String>();
        different.addAll( thisColl );
        different.addAll( thatColl );

        similar.retainAll( thatColl );
        different.removeAll( similar );
        
        if (different.size() > 0) {
            System.out.printf("Different:%s%n", different);
        }
        return (different.size() == 0);
    }

    private void printByteArray(byte[] bytes) {
        int width = 30;
        int counter = 0;

        for (byte b : bytes) {
            System.out.format("%02X ", b);
            counter++;
            if (counter > width) {
                counter = 0;
                System.out.println();
            }
        }
        System.out.println();
    }

    private InputStream resStream(String filename) {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }

    private byte[] loadBytes(String filename) {
        InputStream in = resStream(filename);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16834];

        try {
            while ((nRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException ex) {
            fail(ex.toString());
        }

        return buffer.toByteArray();
    }

    private List<String> loadContents(String filename) {
        String content;
        List<String> contentList = new ArrayList<String>();
        InputStream in = resStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        try {
            while ((content = br.readLine()) != null) {
                contentList.add(content);
            }
        } catch (IOException ex) {
            fail();
        }
        return contentList;
    }
}


