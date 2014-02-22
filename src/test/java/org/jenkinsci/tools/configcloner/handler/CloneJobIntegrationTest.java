package org.jenkinsci.tools.configcloner.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import hudson.model.TopLevelItem;
import hudson.model.FreeStyleProject;

import java.io.IOException;

import org.jenkinsci.tools.configcloner.CommandInvoker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class CloneJobIntegrationTest {

    @Rule public JenkinsRule j = new JenkinsRule();
    private CommandInvoker command;

    @Before
    public void setUp() throws IOException {
        command = new CommandInvoker(j.getURL(), "job");
    }

    @Test
    public void createACopyOfJob() {

        createFreeStyle("sourceJob", "Job Description");

        assertTrue(command.invoke("job/sourceJob/", "job/destJob1/", "job/destJob2/").succeeded());

        assertHasDescription("destJob1", "Job Description");
        assertHasDescription("destJob2", "Job Description");
    }

    @Test
    public void createACopyOfJobFromView() {

        createFreeStyle("sourceJob", "Job Description");

        assertTrue(command.invoke("view/AView/job/sourceJob/", "job/destJob/").succeeded());

        assertHasDescription("destJob", "Job Description");
    }

    @Test
    public void createACopyOfJobOverwritingDestination() throws IOException {

        createFreeStyle("sourceJob", "Job Description");
        j.createFreeStyleProject("destJob").setDescription("Description to be overwriten");

        assertTrue(command.opts("--force").invoke("job/sourceJob/", "job/destJob/").succeeded());

        assertHasDescription("destJob", "Job Description");
    }

    @Test
    public void abortCloningSinceTheDestinationAlreadyExists() {

        createFreeStyle("sourceJob", "Job Description");
        createFreeStyle("destJob", "Description not to be overwriten");

        assertFalse(command.invoke("job/sourceJob/", "job/destJob/").succeeded());

        assertHasDescription("destJob", "Description not to be overwriten");
    }

    @Test
    public void abortCloningIfSourceDoesNotExist() {

        createFreeStyle("destJob", "Description not to be overwriten");

        assertFalse(command.invoke("job/sourceJob/", "job/destJob/").succeeded());

        assertHasDescription("destJob", "Description not to be overwriten");
    }

    @Test
    public void performTransformation() {

        createFreeStyle("sourceJob", "Job Description");

        String[] opts = new String[] {
                "-e", "s/Job\\sDescription/asdfghjkl/",
                "-e", "s/asdf.hjkl/Project Description/"
        };

        assertTrue(command.opts(opts).invoke("job/sourceJob/", "job/destJob/").succeeded());

        assertHasDescription("destJob", "Project Description");
    }

    @Test
    public void cloneDryRun() {

        createFreeStyle("sourceJob", "Job Description");

        assertTrue(command.opts("--dry-run").invoke("job/sourceJob", "job/destJob").succeeded());

        assertNull("Destination should not be created", j.jenkins.getNode("DstSlave"));
    }

    @Test
    public void overwriteDryRun() {

        createFreeStyle("sourceJob", "Job Description");
        createFreeStyle("destJob", "Dest Job Description");

        assertTrue(command.opts("-n").invoke("job/sourceJob", "job/destJob").succeeded());

        assertHasDescription("destJob", "Dest Job Description");
    }

    private FreeStyleProject createFreeStyle(String name, String description) {

        try {
            FreeStyleProject job = j.createFreeStyleProject(name);
            job.setDescription(description);
            return job;
        } catch (IOException ex) {

            throw new AssertionError(ex);
        }
    }

    private <T extends TopLevelItem> T getProject(String name, Class<T> type) {
        return j.jenkins.getItem(name, j.jenkins, type);
    }

    private void assertHasDescription(String name, String description) {
        FreeStyleProject dstJob = getProject(name, FreeStyleProject.class);
        assertEquals(name, dstJob.getName());
        assertEquals(description, dstJob.getDescription());
    }
}
