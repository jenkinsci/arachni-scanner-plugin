package org.jenkinsci.plugins.arachni;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileReader;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.JsonParser;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;

public class ArachniScannerTest {
    
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8877);
    
    @Test
    public void performScan() throws Exception {
        stubFor(post(urlEqualTo("/scans"))
                .withHeader(HttpHeaders.CONTENT_TYPE, containing(ContentType.APPLICATION_JSON.toString()))
                .willReturn(aResponse().withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                        .withBody("{\"id\":\"919813cdb162af0c091c34fca3823b89\"}")));

        stubFor(get(urlEqualTo("/scans/919813cdb162af0c091c34fca3823b89"))
                .willReturn(aResponse().withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withBody(getJsonFromFile("responseMonitorScanDone.json"))));

        stubFor(get(urlEqualTo("/scans/919813cdb162af0c091c34fca3823b89/report.html.zip")).willReturn(aResponse().withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, "application/zip")));

        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new ArachniScanner("http://test-site:9090", null));
        ArachniPluginConfiguration config = new ArachniPluginConfiguration();
        config.setArachniServerUrl("http://localhost:8877");
        GlobalConfiguration.all().add(config);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("Scan finished for id: 919813cdb162af0c091c34fca3823b89"));
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

    private String getJsonFromFile(String filename) throws Exception {
        URL url = this.getClass().getResource(filename);
        File file = new File(url.toURI());
        
        return new JsonParser().parse(new FileReader(file)).toString();
    }
}
