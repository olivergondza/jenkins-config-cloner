package org.jenkinsci.tools.configcloner.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.jenkinsci.tools.configcloner.handler.Helper.stdoutContains;
import static org.jenkinsci.tools.configcloner.handler.Helper.succeeded;
import hudson.model.Node;

import java.io.IOException;

import org.jenkinsci.tools.configcloner.CommandInvoker;
import org.jenkinsci.tools.configcloner.CommandResponse.Accumulator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class CloneNodeIntegrationTest {

    @Rule public JenkinsRule j = new JenkinsRule();
    private CommandInvoker command;

    @Before
    public void setUp() throws IOException {
        command = new CommandInvoker("node").url(j.getURL());
    }

    @Test
    public void createACopyOfNode() throws Exception {

        j.createSlave("SrcSlave", "src_label", null);

        assertThat(command.invoke("computer/SrcSlave", "computer/DstSlave1", "computer/DstSlave2"), succeeded());

        Node dstSlave1 = j.jenkins.getNode("DstSlave1");
        assertThat(dstSlave1.getLabelString(), equalTo("src_label"));
        Node dstSlave2 = j.jenkins.getNode("DstSlave2");
        assertThat(dstSlave2.getLabelString(), equalTo("src_label"));
    }

    @Test
    public void createACopyOfNodeOverwritingDestination() throws Exception {

        j.createSlave("SrcSlave", "src_label", null);
        j.createSlave("DstSlave1", "dst_label", null);
        j.createSlave("DstSlave2", "dst_label", null);

        assertThat(command.opts("--force").invoke("computer/SrcSlave", "computer/DstSlave1", "computer/DstSlave2"), succeeded());

        Node dstSlave1 = j.jenkins.getNode("DstSlave1");
        assertThat(dstSlave1.getLabelString(), equalTo("src_label"));
        Node dstSlave2 = j.jenkins.getNode("DstSlave2");
        assertThat(dstSlave2.getLabelString(), equalTo("src_label"));
    }

    @Test
    public void abortCloningSinceTheDestinationAlreadyExists() throws Exception {

        j.createSlave("SrcSlave", "src_label", null);
        j.createSlave("DstSlave", "dst_label", null);

        assertThat(command.invoke("computer/SrcSlave", "computer/DstSlave"), not(succeeded()));

        Node dstSlave = j.jenkins.getNode("DstSlave");
        assertThat(dstSlave.getLabelString(), equalTo("dst_label"));
    }

    @Test
    public void abortCloningIfSourceDoesNotExist() throws Exception {

        j.createSlave("DstSlave", "dst_label", null);

        assertThat(command.invoke("computer/SrcSlave", "computer/DstSlave"), not(succeeded()));

        Node dstSlave = j.jenkins.getNode("DstSlave");
        assertThat(dstSlave.getLabelString(), equalTo("dst_label"));
    }

    @Test
    public void performTransformation() throws Exception {

        j.createSlave("SrcSlave", "a_label b_label", null);

        assertThat(command.opts("--expression", "s/_label//g").invoke("computer/SrcSlave", "computer/DstSlave"), succeeded());

        Node dstSlave = j.jenkins.getNode("DstSlave");
        assertThat(dstSlave.getLabelString(), equalTo("a b"));
    }

    @Test
    public void cloneDryRun() throws Exception {

        j.createSlave("SrcSlave", "src_label", null);

        Accumulator result = command.opts("--dry-run", "-e", "s/_label//")
                .invoke("computer/SrcSlave", "computer/DstSlave")
        ;

        assertThat(result, succeeded());
        assertThat(result, stdoutContains("-  <label>src_label</label>"));
        assertThat(result, stdoutContains("+  <label>src</label>"));
        assertThat(j.jenkins.getNode("DstSlave"), nullValue());
    }

    @Test
    public void overwriteDryRun() throws Exception {

        j.createSlave("SrcSlave", "src_label", null);
        j.createSlave("DstSlave", "dst_label", null);

        assertThat(command.opts("-n").invoke("computer/SrcSlave", "computer/DstSlave"), succeeded());
        assertThat(j.jenkins.getNode("DstSlave").getLabelString(), equalTo("dst_label"));
    }
}
