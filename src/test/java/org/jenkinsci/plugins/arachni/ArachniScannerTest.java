package org.jenkinsci.plugins.arachni;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.irissmann.arachni.client.Scan;
import de.irissmann.arachni.client.request.ScanRequest;
import de.irissmann.arachni.client.response.ScanResponse;
import de.irissmann.arachni.client.response.Statistics;
import de.irissmann.arachni.client.rest.ArachniRestClient;
import de.irissmann.arachni.client.rest.ArachniRestClientBuilder;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.util.FormValidation;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ArachniPluginConfiguration.class, ArachniRestClient.class, ArachniRestClientBuilder.class, Scan.class,
    Statistics.class, ScanResponse.class})
@PowerMockIgnore({"javax.crypto.*" })
public class ArachniScannerTest {
    
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void performScan() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new ArachniScanner("http://test-site:9090", null, null));
        ArachniPluginConfiguration config = new ArachniPluginConfiguration();
        config.setArachniServerUrl("http://localhost:8877");
        
        // init mock objects
        PowerMockito.mockStatic(ArachniPluginConfiguration.class);
        when(ArachniPluginConfiguration.get()).thenReturn(config);
        
        Statistics statistics = mock(Statistics.class);
        when(statistics.getFoundPages()).thenReturn(3);
        when(statistics.getAuditedPages()).thenReturn(3);
        
        ScanResponse scanInfo = mock(ScanResponse.class);
        when(scanInfo.getStatus()).thenReturn("done");
        when(scanInfo.getStatistics()).thenReturn(statistics);
        when(scanInfo.isBusy()).thenReturn(false);
        
        Scan scan = mock(Scan.class);
        when(scan.getId()).thenReturn("919813cdb162af0c091c34fca3823b89");
        when(scan.monitor()).thenReturn(scanInfo);
        
        ArachniRestClient arachniClient = mock(ArachniRestClient.class);
        when(arachniClient.performScan(any(ScanRequest.class))).thenReturn(scan);
        
        ArachniRestClientBuilder clientBuilder = PowerMockito.mock(ArachniRestClientBuilder.class);
        when(clientBuilder.build()).thenReturn(arachniClient);

        PowerMockito.mockStatic(ArachniRestClientBuilder.class);
        when(ArachniRestClientBuilder.create("http://localhost:8877")).thenReturn(clientBuilder);
        
        // start builder
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        
        // validate result
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
}
