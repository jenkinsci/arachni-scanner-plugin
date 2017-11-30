package org.jenkinsci.plugins.arachni;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

public class ArachniScannerTest {
    
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();
    
    @Test
    public void test() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new ArachniScanner("http://localhost:8080", null));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
    }

}
