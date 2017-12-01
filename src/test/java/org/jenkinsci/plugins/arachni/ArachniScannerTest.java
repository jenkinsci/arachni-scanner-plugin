package org.jenkinsci.plugins.arachni;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.util.FormValidation;

public class ArachniScannerTest {
    
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();
    
    @Test
    public void test() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new ArachniScanner("http://localhost:8080", null));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
    }
    
    @Test
    public void validateUrlIsOk() throws Exception {
        FormValidation validation = new ArachniScanner
                .DescriptorImpl()
                .doCheckUrl("http://localhost:8080");
        assertEquals(FormValidation.ok(), validation);
    }
    
    @Test
    public void validateUrlError() throws Exception {
        FormValidation validation = new ArachniScanner
                .DescriptorImpl()
                .doCheckUrl("http://localhost:8d080");
        assertEquals("URL is not valid.", validation.getMessage());
    }
}
