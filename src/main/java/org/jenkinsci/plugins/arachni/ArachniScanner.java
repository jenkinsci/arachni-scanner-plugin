package org.jenkinsci.plugins.arachni;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.irissmann.arachni.client.ArachniClient;
import de.irissmann.arachni.client.Scan;
import de.irissmann.arachni.client.request.ScanRequest;
import de.irissmann.arachni.client.request.Scope;
import de.irissmann.arachni.client.response.ScanResponse;
import de.irissmann.arachni.client.rest.ArachniRestClientBuilder;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

public class ArachniScanner extends Builder {
    Logger log = LoggerFactory.getLogger(ArachniScanner.class);

    private String url;
    private ArachniScopeProperty scope;
    private Scan scan;
    private PrintStream console;
    private ArachniClient arachniClient;

    @DataBoundConstructor
    public ArachniScanner(String url, ArachniScopeProperty scope) {
        this.url = url;
        this.scope = scope;
    }

    public String getUrl() {
        return url;
    }

    public ArachniScopeProperty getScope() {
        return scope;
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

        public FormValidation doCheckUrl(@QueryParameter String value) throws IOException, ServletException {
            try {
                new URL(value);
                return FormValidation.ok();
            } catch (MalformedURLException excecption) {
                return FormValidation.error("URL is not valid.");
            }
        }
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) 
            throws InterruptedException {
        console = listener.getLogger();
        ArachniPluginConfiguration config = ArachniPluginConfiguration.get();
        String arachniUrl = config.getArachniServerUrl();
        console.println("Start Arachni Security Scan");
        console.println("Arachni server URL: " + arachniUrl);
        console.println("Site under scan: " + url);

        arachniClient = getArachniClient(config);

        Scope scannerScope = null;
        if (scope != null) {
            scannerScope = Scope.create().pageLimit(scope.getPageLimitAsInt())
                    .addExcludePathPatterns(scope.getExcludePathPattern()).build();
        }
        ScanRequest scanRequest = ScanRequest.create().url(url).scope(scannerScope).build();
        try {
            scan = arachniClient.performScan(scanRequest);
            console.println("Scan started with id: " + scan.getId());
            log.info("Scan started with id: {}", scan.getId());

            ScanResponse scanInfo;
            while (true) {
                Thread.sleep(5000);
                scanInfo = scan.monitor();
                console.println("Status: " + scanInfo.getStatus() + " - Pages found: "
                        + scanInfo.getStatistics().getFoundPages() + " - Pages audited: "
                        + scanInfo.getStatistics().getAuditedPages());
                if (!scanInfo.isBusy()) {
                    console.println("Scan finished for id: " + scan.getId());
                    log.info("Scan finished for id {}", scan.getId());
                    break;
                }
            }

            FilePath arachniPath = build.getWorkspace();
            log.debug("Path for arachni results: {}", arachniPath);

            if (arachniPath != null) {
                File reportFile = new File(arachniPath.getRemote(), "arachni-report-html.zip");
                if (!reportFile.exists()) {
                    reportFile.createNewFile();
                }
                OutputStream outstream = new FileOutputStream(reportFile);
                scan.getReportHtml(outstream);
            }
        } catch (Exception exception) {
            log.warn("Error when start Arachni Security Scan", exception);
            console.println(exception.getMessage());
            return false;
        }

        return true;
    }

    protected void shutdownScan() throws IOException {
        log.info("Shutdown scanner for id: {}", scan.getId());

        try {
            scan.shutdown();
            log.info("Shutdown successful.");
        } catch (Exception exception) {
            log.warn("Error when shutdown Arachni Security Scan", exception);
        } finally {
            arachniClient.close();
        }
    }

    private ArachniClient getArachniClient(ArachniPluginConfiguration config) {
        if (config.getBasicAuth()) {
            return ArachniRestClientBuilder.create(config.getArachniServerUrl())
                    .addCredentials(config.getUser(), config.getPassword()).build();
        } else {
            return ArachniRestClientBuilder.create(config.getArachniServerUrl()).build();
        }
    }
}
