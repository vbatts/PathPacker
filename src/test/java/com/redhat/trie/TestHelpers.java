package com.redhat.trie;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.apache.log4j.Logger;

/**
 * This class is just to provide helpers for the other tests
 */
public class TestHelpers {
  private static org.apache.log4j.Logger log = Logger.getLogger(TestHelpers.class);

    /**
     * junit requires at least one runnable test
     */
    @Test
    public void testDeadVicker() {
        assertNotNull(new String("What's its diocese?"));
    }

    // Helpers
    // 
    public static boolean cmpStrings(List<String> thisList, List<String> thatList) {
        Collection<String> thisColl = new ArrayList<String>(thisList);
        Collection<String> thatColl = new ArrayList<String>(thatList);

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

    public static void printByteArray(byte[] bytes) {
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

    public static InputStream resStream(Object klass, String filename) {
        return klass.getClass().getClassLoader().getResourceAsStream(filename);
    }

    public static byte[] loadBytes(Object klass, String filename) {
        InputStream in = resStream(klass, filename);
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

    public static List<String> loadContents(Object klass, String filename) {
        String content;
        List<String> contentList = new ArrayList<String>();
        InputStream in = resStream(klass, filename);
        BufferedReader br = null;
        try {
          br = new BufferedReader(new InputStreamReader(in));
        } catch(NullPointerException npe) {
          // can happen in the case of an empty content set list
          log.warn(">>>>>>> Empty content set <<<<<");
          return contentList;
        }

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


