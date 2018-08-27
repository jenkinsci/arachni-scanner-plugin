package org.jenkinsci.plugins.arachni;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.util.FormValidation;

public class ArachniPluginConfigurationTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void validateArachniServerUrlWithOk() throws Exception {
        ArachniPluginConfiguration config = new ArachniPluginConfiguration();
        FormValidation validation = config.doCheckArachniServerUrl("http://localhost:8080");
        assertEquals(FormValidation.ok(), validation);
    }

    @Test
    public void validateArachniServerUrlWithError() throws Exception {
        ArachniPluginConfiguration config = new ArachniPluginConfiguration();
        FormValidation validation = config.doCheckArachniServerUrl("qwert");
        assertEquals("URL is not valid.", validation.getMessage());
    }

    @Test
    public void validateEmptyCredentials() throws Exception {
        ArachniPluginConfiguration config = new ArachniPluginConfiguration();
        FormValidation validation = config.doCheckCredentialsId(null, null);
        assertEquals(FormValidation.ok(), validation);
    }
}
