package org.jenkinsci.tools.configcloner.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import hudson.model.Node;

import java.io.IOException;

import org.jenkinsci.tools.configcloner.CommandInvoker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class CloneNodeIntegrationTest {

    @Rule public JenkinsRule j = new JenkinsRule();
    private CommandInvoker command;

    @Before
    public void setUp() throws IOException {
        command = new CommandInvoker(j.getURL(), "node");
    }

    @Test
    public void createACopyOfNode() throws Exception {

        j.createSlave("SrcSlave", "src_label", null);

        assertTrue(command.invoke("computer/SrcSlave", "computer/DstSlave1", "computer/DstSlave2").succeeded());

        Node dstSlave1 = j.jenkins.getNode("DstSlave1");
        assertEquals("src_label", dstSlave1.getLabelString());
        Node dstSlave2 = j.jenkins.getNode("DstSlave2");
        assertEquals("src_label", dstSlave2.getLabelString());
    }

    @Test
    public void createACopyOfNodeOverwritingDestination() throws Exception {

        j.createSlave("SrcSlave", "src_label", null);
        j.createSlave("DstSlave1", "dst_label", null);
        j.createSlave("DstSlave2", "dst_label", null);

        assertTrue(command.opts("--force").invoke("computer/SrcSlave", "computer/DstSlave1", "computer/DstSlave2").succeeded());

        Node dstSlave1 = j.jenkins.getNode("DstSlave1");
        assertEquals("src_label", dstSlave1.getLabelString());
        Node dstSlave2 = j.jenkins.getNode("DstSlave2");
        assertEquals("src_label", dstSlave2.getLabelString());
    }

    @Test
    public void abortCloningSinceTheDestinationAlreadyExists() throws Exception {

        j.createSlave("SrcSlave", "src_label", null);
        j.createSlave("DstSlave", "dst_label", null);

        assertFalse(command.invoke("computer/SrcSlave", "computer/DstSlave").succeeded());

        Node dstSlave = j.jenkins.getNode("DstSlave");
        assertEquals("dst_label", dstSlave.getLabelString());
    }

    @Test
    public void abortCloningIfSourceDoesNotExist() throws Exception {

        j.createSlave("DstSlave", "dst_label", null);

        assertFalse(command.invoke("computer/SrcSlave", "computer/DstSlave").succeeded());

        Node dstSlave = j.jenkins.getNode("DstSlave");
        assertEquals("dst_label", dstSlave.getLabelString());
    }
}
