package com.redhat.trie;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests of whether a URL should be granted access based on a given content set
 * (as would be specified in the requester's entitlement certificate).
 *
 */
@RunWith(value = Parameterized.class)
public class TestValidater {
  private String contentSetFileName;
  private String requestedPath;
  private boolean expectedResult;
  private PathTree pathTree;

  @Parameters
  public static Collection<Object[]> data() {
    /*
     * Data for each run of the test.
     * [0] = The name of the file containing content set paths
     * [1] = The requested resource
     * [3] = The expected result.
     */
    Object[][] data = new Object[][] {
      /*
       * WARNING -- 
       *   Do not uncomment empty.contents.list cases unless
       *   PathTree has been updated to handle that case.
       *
      { "empty.contents.list", "/content/rc/rhel/7/vt", false},
      { "empty.contents.list", "/content/rc/rhel/7", false},
       */
      { "contents.list", "/content/beta/rhel/server/5/5server/x86_64/sap/os/repomd.xml", true},
      { "contents.list", "/fart/face/mcjones", false},
      { "contents.list", "/content/dist/rhel/server/6/$releasever/$basearch/vt/os", false},

      { "variables-last-contents.list", "/content/rc/rhel/server/7/x86_64/debug", true},
      { "variables-last-contents.list", "/content/rc/rhel/server/7/x86_64/debug/tools/firefox", true},
      { "variables-last-contents.list", "/content/rc/rhel/server/7/x86_64/Debug", false},
      { "variables-last-contents.list", "/content/rc/rhel/server/7/Debug", false},
      { "variables-last-contents.list", "/content/rc/rhel/server/7/x86_64/os", true},

      { "variables-first-contents.list", "/content/rc/rhel/server/7/x86_64/debug", true},
      { "variables-first-contents.list", "/content/rc/rhel/server/7/x86_64/debug/tools/firefox", true},
      { "variables-first-contents.list", "/content/rc/rhel/server/7/x86_64/Debug", false},
      { "variables-first-contents.list", "/content/rc/rhel/server/7/Debug", false},
      { "variables-first-contents.list", "/content/rc/rhel/server/7/x86_64/os", true},

      { "variables-mixed-contents.list", "/content/rc/rhel/server/7/x86_64/debug", true},
      { "variables-mixed-contents.list", "/content/rc/rhel/server/7/x86_64/debug/tools/firefox", true},
      { "variables-mixed-contents.list", "/content/rc/rhel/server/7/x86_64/Debug", false},
      { "variables-mixed-contents.list", "/content/rc/rhel/server/7/Debug", false},
      { "variables-mixed-contents.list", "/content/rc/rhel/server/7/x86_64/os", true},

      { "another-contents.list", "/content/rc/rhel/server/7/x86_64/os", true},
      { "another-contents.list", "/content/rc/rhel/server/7/x86_64/debug/os", false}
    };
    return Arrays.asList(data);
  }

  /**
   * Instatiate a test class with the given parameters.
   * @param contentSetFileName The name of the file containing the allowed content sets.
   * @param requestedPath The resource being requested
   * @param expectedResult The correct response.
   */
  public TestValidater(final String contentSetFileName, final String requestedPath, final boolean expectedResult) {
    this.contentSetFileName = contentSetFileName;
    this.requestedPath = requestedPath;
    this.expectedResult = expectedResult;
  }

  /**
   * Prepare for a test run.
   */
  @Before
  public void setup() throws Exception {
    List<String> validContentPaths = TestHelpers.loadContents(this, contentSetFileName);
    pathTree = new PathTree();
    pathTree.setContentSets(validContentPaths);
    assertTrue(TestHelpers.cmpStrings(validContentPaths, pathTree.toList()));
  }

  /**
   * Run the test.
   */
  @Test
  public void justDoIt() throws Exception {
    assertEquals("[" + requestedPath + "] " + (expectedResult?"allowed ":"not allowed ") + "using " + contentSetFileName, 
        expectedResult, pathTree.validate(requestedPath));
  }
}
