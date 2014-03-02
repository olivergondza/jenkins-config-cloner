package org.jenkinsci.tools.configcloner.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.tools.configcloner.handler.Helper.stdoutContains;
import static org.jenkinsci.tools.configcloner.handler.Helper.succeeded;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import hudson.model.TopLevelItem;
import hudson.model.ListView;
import hudson.model.View;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jenkinsci.tools.configcloner.CommandInvoker;
import org.jenkinsci.tools.configcloner.CommandResponse.Accumulator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class CloneViewIntegrationTest {

    @Rule public JenkinsRule j = new JenkinsRule();
    private CommandInvoker command;
    private TopLevelItem project;

    @Before
    public void setUp() throws IOException {
        command = new CommandInvoker("view").url(j.getURL());
        project = j.createFreeStyleProject("project");
    }

    @Test
    public void createACopyOfView() throws Exception {

        view("SrcView").add(project);

        assertThat(command.invoke("view/SrcView", "view/DstView1", "view/DstView2"), succeeded());

        View dstView1 = j.jenkins.getView("DstView1");
        assertTrue(dstView1.contains(project));
        View dstView2 = j.jenkins.getView("DstView2");
        assertTrue(dstView2.contains(project));
    }

    @Test
    public void createACopyOfViewOverwritingDestination() throws Exception {

        view("SrcView").add(project);
        view("DstView1");
        view("DstView2");

        assertThat(command.opts("-f").invoke("view/SrcView", "view/DstView1", "view/DstView2"), succeeded());

        View dstView1 = j.jenkins.getView("DstView1");
        assertTrue(dstView1.contains(project));
        View dstView2 = j.jenkins.getView("DstView2");
        assertTrue(dstView2.contains(project));
    }

    @Test
    public void abortCloningSinceTheDestinationAlreadyExists() throws Exception {

        view("SrcView").add(project);
        view("DstView");

        assertThat(command.invoke("view/SrcView", "view/DstView"), not(succeeded()));

        View dstView = j.jenkins.getView("DstView");
        assertFalse(dstView.contains(project));
    }

    @Test
    public void abortCloningIfSourceDoesNotExist() throws Exception {

        view("DstView").add(project);

        assertThat(command.invoke("view/SrcView", "view/DstView"), not(succeeded()));

        View dstView = j.jenkins.getView("DstView");
        assertTrue(dstView.contains(project));
    }

    @Test
    @Ignore(value="TODO: Update CreateViewCommand to create view in arbitrary ViewGroup")
    public void cloneNestedView() throws IOException {

        NestedView group = viewGroup("group");
        ListView srcView = view("SrcView", group);
        srcView.add(project);

        assertThat(command.invoke("view/group/view/SrcView", "view/TopLevelView", "view/group/view/NestedView"), succeeded());

        View topLevelView = j.jenkins.getView("TopLevelView");
        assertTrue(topLevelView.contains(project));
        View nestedView = ((NestedView) j.jenkins.getView("group")).getView("NostedView");
        assertTrue(nestedView.contains(project));
    }

    @Test
    public void performTransformation() throws Exception {

        ListView view = view("SrcView");
        view.setIncludeRegex("originalRegex");
        view.add(project);

        assertThat(command.opts("-e", "s/original/replaced/").invoke("view/SrcView", "view/DstView"), succeeded());

        View dstView = j.jenkins.getView("DstView");
        assertTrue(dstView.contains(project));
    }

    @Test
    public void cloneDryRun() throws Exception {

        view("SrcView").setIncludeRegex("^src-");

        Accumulator result = command.opts("-n", "-e", "s/src-/dst-/")
                .invoke("view/SrcView", "view/DstView")
        ;

        assertThat(result, succeeded());
        assertThat(result, stdoutContains("-  <includeRegex>^src-</includeRegex>"));
        assertThat(result, stdoutContains("+  <includeRegex>^dst-</includeRegex>"));
        assertNull("Destination should not be created", j.jenkins.getView("DstView"));
    }

    @Test
    public void overwriteDryRun() throws Exception {

        view("SrcView").add(project);
        view("DstView");

        assertThat(command.opts("--force", "--dry-run").invoke("view/SrcView", "view/DstView"), succeeded());

        View dstView = j.jenkins.getView("DstView");
        assertFalse("Destination should not be overwritten", dstView.contains(project));
    }

    private ListView view(String name) throws IOException {
        final ListView view = new ListView(name, j.jenkins);
        j.jenkins.addView(view);
        return view;
    }

    private ListView view(String name, NestedView group) throws IOException {
        final ListView view = new ListView(name, group);
        group.addView(view);
        return view;
    }

    private NestedView viewGroup(String name) throws IOException {
        final NestedView group = new NestedView(name);
        j.jenkins.addView(group);
        return group;
    }

    private static final class NestedView extends hudson.plugins.nested_view.NestedView {
        private NestedView(String name) {
            super(name);
        }

        // Add public addView(View) method
        public void addView(View view) {
            try {
                Method method = hudson.plugins.nested_view.NestedView.class.getDeclaredMethod("addView", View.class);
                method.setAccessible(true);
                method.invoke(this, view);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            } catch (SecurityException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
