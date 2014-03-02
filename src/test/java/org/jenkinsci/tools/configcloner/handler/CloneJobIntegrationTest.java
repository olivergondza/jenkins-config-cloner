package org.jenkinsci.tools.configcloner.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.tools.configcloner.handler.Helper.stdoutContains;
import static org.jenkinsci.tools.configcloner.handler.Helper.succeeded;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import hudson.model.TopLevelItem;
import hudson.model.FreeStyleProject;

import java.io.IOException;

import org.jenkinsci.tools.configcloner.CommandInvoker;
import org.jenkinsci.tools.configcloner.CommandResponse.Accumulator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class CloneJobIntegrationTest {

    @Rule public JenkinsRule j = new JenkinsRule();
    private CommandInvoker command;

    @Before
    public void setUp() throws IOException {
        command = new CommandInvoker("job").url(j.getURL());
    }

    @Test
    public void createACopyOfJob() {

        createFreeStyle("sourceJob", "Job Description");

        assertThat(command.invoke("job/sourceJob/", "job/destJob1/", "job/destJob2/"), succeeded());

        assertHasDescription("destJob1", "Job Description");
        assertHasDescription("destJob2", "Job Description");
    }

    @Test
    public void createACopyOfJobFromView() {

        createFreeStyle("sourceJob", "Job Description");

        assertThat(command.invoke("view/AView/job/sourceJob/", "job/destJob/"), succeeded());

        assertHasDescription("destJob", "Job Description");
    }

    @Test
    public void createACopyOfJobOverwritingDestination() throws IOException {

        createFreeStyle("sourceJob", "Job Description");
        j.createFreeStyleProject("destJob").setDescription("Description to be overwriten");

        assertThat(command.opts("--force").invoke("job/sourceJob/", "job/destJob/"), succeeded());

        assertHasDescription("destJob", "Job Description");
    }

    @Test
    public void abortCloningSinceTheDestinationAlreadyExists() {

        createFreeStyle("sourceJob", "Job Description");
        createFreeStyle("destJob", "Description not to be overwriten");

        assertThat(command.invoke("job/sourceJob/", "job/destJob/"), not(succeeded()));

        assertHasDescription("destJob", "Description not to be overwriten");
    }

    @Test
    public void abortCloningIfSourceDoesNotExist() {

        createFreeStyle("destJob", "Description not to be overwriten");

        assertThat(command.invoke("job/sourceJob/", "job/destJob/"), not(succeeded()));

        assertHasDescription("destJob", "Description not to be overwriten");
    }

    @Test
    public void performTransformation() {

        createFreeStyle("sourceJob", "Job Description");

        String[] opts = new String[] {
                "-e", "s/Job\\sDescription/asdfghjkl/",
                "-e", "s/asdf.hjkl/Project Description/"
        };

        assertThat(command.opts(opts).invoke("job/sourceJob/", "job/destJob/"), succeeded());

        assertHasDescription("destJob", "Project Description");
    }

    @Test
    public void cloneDryRun() {

        createFreeStyle("sourceJob", "Job Description");

        Accumulator result = command
                .opts("--dry-run", "-e", "s/Description/DSCR/")
                .invoke("job/sourceJob", "job/destJob")
        ;

        assertThat(result, succeeded());
        assertThat(result, stdoutContains("-  <description>Job Description</description>"));
        assertThat(result, stdoutContains("+  <description>Job DSCR</description>"));
        assertNull("Destination should not be created", j.jenkins.getItem("DstJob"));
    }

    @Test
    public void overwriteDryRun() {

        createFreeStyle("sourceJob", "Job Description");
        createFreeStyle("destJob", "Dest Job Description");

        assertThat(command.opts("-n").invoke("job/sourceJob", "job/destJob"), succeeded());

        assertHasDescription("destJob", "Dest Job Description");
    }

    @Test
    public void failEvenIfLastTrasferSucceeds() {

        createFreeStyle("sourceJob", "Job Description");
        createFreeStyle("existingJob", "");

        assertThat(command.invoke("job/sourceJob", "job/existingJob", "job/destJob"), not(succeeded()));

        assertHasDescription("destJob", "Job Description");
    }

    @Test
    public void useLongAndShortOptionAtTheSameTime() {

        createFreeStyle("sourceJob", "ORIGINAL Description");

        final Accumulator rsp = command.opts(
                "-e", "s/ORIGINAL/MODIFIED/", "--expression", "s/MODIFIED/FINAL/"
        ).invoke("job/sourceJob", "job/destJob");

        assertThat(rsp, succeeded());
        assertHasDescription("destJob", "FINAL Description");
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
