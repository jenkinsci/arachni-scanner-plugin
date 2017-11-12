package org.jenkinsci.plugins.arachni;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.irissmann.arachni.api.ArachniApi;
import de.irissmann.arachni.api.ArachniApiException;
import de.irissmann.arachni.client.rest.ArachniApiRestBuilder;
import de.irissmann.arachni.client.rest.request.RequestScan;
import de.irissmann.arachni.client.rest.response.ResponseScan;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

public class ArachniScanner extends Builder {
    Logger log = LoggerFactory.getLogger(ArachniScanner.class);

    private String url;
    private String scanId;
    private PrintStream console;

    @DataBoundConstructor
    public ArachniScanner(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Arachni Scanner";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        console = listener.getLogger();
        ArachniPluginConfiguration config = ArachniPluginConfiguration.get();
        String arachniUrl = config.getArachniServerUrl();
        console.println("Start Arachni Security Scan");
        console.println("Arachni server URL: " + arachniUrl);
        console.println("Site under scan: " + url);

        ArachniApi api = getApi(config);

        RequestScan scan = new RequestScan(url);
        try {
            scanId = api.performScan(scan);
            console.println("Scan started with id: " + scanId);
            log.info("Scan started with id: {}", scanId);
            
            ResponseScan scanInfo;
            while (true) {
                Thread.sleep(5000);
                scanInfo = api.monitorScan(scanId);
                console.println("Status: " + scanInfo.getStatus()
                + " - Pages found: " + scanInfo.getStatistics().getFoundPages()
                + " - Pages audited: " + scanInfo.getStatistics().getAuditedPages());
                if (! scanInfo.isBusy()) {
                    console.println("Scan finished for id: " + scanId);
                    log.info("Scan finished for id {}", scanId);
                    break;
                }
            }
        } catch (ArachniApiException exception) {
            log.warn("Error when start Arachni Security Scan");
            log.debug(exception.getMessage());
            console.println(exception.getMessage());
            return false;
        }
        return true;
    }
    
    protected void shutdownScan() {
        try {
            log.info("Shutdown scanner for id: {}", scanId);
            ArachniApi api = getApi(ArachniPluginConfiguration.get());
            api.shutdownScan(scanId);
            log.info("Shutdown successful.");
        } catch (Exception exception) {
            
        }
    }
    
    private ArachniApi getApi(ArachniPluginConfiguration config) throws IOException {
        if (config.getBasicAuth()) {
            return ArachniApiRestBuilder.create(new URL(config.getArachniServerUrl()))
                    .addCredentials(config.getUser(), config.getPassword()).build();
        } else {
            return ArachniApiRestBuilder.create(new URL(config.getArachniServerUrl())).build();
        }
    }
}
