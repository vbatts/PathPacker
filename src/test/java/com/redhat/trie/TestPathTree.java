package com.redhat.trie;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
        printByteArray(bytes);

        pt1 = new PathTree(bytes);
        contents1 = pt1.toList();
        
        System.out.println(contents0.size());
        System.out.println(contents1.size());

        Collections.sort(contents0);
        Collections.sort(contents1);

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

        assertEquals(contents0.size(), contents1.size());
    }



    // Helpers
    // 
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


