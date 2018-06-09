package org.jenkinsci.plugins.arachni;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import de.irissmann.arachni.client.ArachniClient;
import de.irissmann.arachni.client.Scan;
import de.irissmann.arachni.client.request.ScanRequest;
import de.irissmann.arachni.client.request.ScanRequestBuilder;
import de.irissmann.arachni.client.request.Scope;
import de.irissmann.arachni.client.response.ScanResponse;
import de.irissmann.arachni.client.rest.ArachniRestClientBuilder;
import de.irissmann.arachni.client.rest.ArachniUtils.MergeConflictStrategy;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;

public class ArachniScanner extends Builder implements SimpleBuildStep {
    transient private static final Logger log = Logger.getLogger(ArachniScanner.class.getName());

    protected static final String FORMAT_HTML = "html";
    protected static final String FORMAT_JSON = "json";
    protected static final String FORMAT_XML = "xml";
    protected static final String FORMAT_YAML = "yaml";

    private String url;
    private String checks;
    private UserConfigProperty userConfig;
    private ArachniScopeProperty scope;
    private String format;
    private Scan scan;
    private PrintStream console;
    private ArachniClient arachniClient;

    @DataBoundConstructor
    public ArachniScanner(String url, String checks, ArachniScopeProperty scope, UserConfigProperty userConfig,
            String format) {
        this.url = url;
        this.checks = checks;
        this.scope = scope;
        this.userConfig = userConfig;
        this.format = format;
    }

    public String getUrl() {
        return url;
    }

    public String getChecks() {
        return checks;
    }

    public ArachniScopeProperty getScope() {
        return scope;
    }

    public UserConfigProperty getUserConfig() {
        return userConfig;
    }

    public String getFormat() {
        if (StringUtils.isEmpty(format)) {
            return FORMAT_HTML;
        }
        return format;
    }

    @Symbol("arachniScanner")
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

        public FormValidation doCheckUrl(@QueryParameter String value) {
            try {
                new URL(value);
                return FormValidation.ok();
            } catch (MalformedURLException excecption) {
                return FormValidation.error("URL is not valid.");
            }
        }
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
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

        ScanRequestBuilder requestBuilder = ScanRequest.create().url(url).scope(scannerScope);
        if (StringUtils.isNotBlank(checks)) {
            for (String check : checks.split(",")) {
                requestBuilder.addCheck(check.trim());
            }
        } else {
            requestBuilder.addCheck("*");
        }
        ScanRequest scanRequest = requestBuilder.build();

        // reads user configuration file if exists
        String configuration = null;
        if (userConfig != null && StringUtils.isNotBlank(userConfig.getFilename())) {
            FilePath configFile = workspace.child(userConfig.getFilename());
            if (!configFile.exists()) {
                String message = String.format("Configuration file %s does not exists", userConfig.getFilename());
                log.warning(message);
                throw new AbortException(message);
            }
            configuration = configFile.readToString();
        }

        OutputStream outstream = null;
        try {
            scan = arachniClient.performScan(scanRequest, configuration);
            console.println("Scan started with id: " + scan.getId());
            log.info(String.format("Scan started with id: %s", scan.getId()));

            ScanResponse scanInfo;
            while (true) {
                Thread.sleep(5000);
                scanInfo = scan.monitor();
                console.println("Status: " + scanInfo.getStatus() + " - Pages found: "
                        + scanInfo.getStatistics().getFoundPages() + " - Pages audited: "
                        + scanInfo.getStatistics().getAuditedPages());
                if (!scanInfo.isBusy()) {
                    console.println("Scan finished for id: " + scan.getId());
                    log.info(String.format("Scan finished for id %s", scan.getId()));
                    break;
                }
            }

            String filename = String.format("arachni-report-%s.zip", getFormat());
            File reportFile = new File(workspace.getRemote(), filename);
            if (!reportFile.exists()) {
                if (!reportFile.createNewFile()) {
                    throw new AbortException("Could not create file " + reportFile.toString());
                }
            }
            outstream = new FileOutputStream(reportFile);
            switch (getFormat()) {
            case FORMAT_HTML:
                scan.getReportHtml(outstream);
                break;
            case FORMAT_JSON:
                writeZipFile(scan.getReportJson().getBytes(), "arachni-report.json", outstream);
                break;
            case FORMAT_XML:
                writeZipFile(scan.getReportXml().getBytes(), "arachni-report.xml", outstream);
                break;
            case FORMAT_YAML:
                writeZipFile(scan.getReportYaml().getBytes(), "arachni-report.yml", outstream);
                break;
            default:
                throw new AbortException("Report format not supported");
            }
        } catch (FileNotFoundException exception) {
            log.warning("Error when start Arachni Security Scan");
            console.println(exception.getMessage());
            throw new AbortException();
        } finally {
            if (outstream != null) {
                outstream.close();
            }
        }
    }

    protected void shutdownScan() throws IOException {
        if (scan == null) {
            return;
        }

        log.info(String.format("Shutdown scanner for id: %s", scan.getId()));

        try {
            scan.shutdown();
            log.info("Shutdown successful.");
        } catch (Exception exception) {
            log.warning("Error when shutdown Arachni Security Scan");
        } finally {
            arachniClient.close();
        }
    }
    
    private void writeZipFile(byte[] content, String entryName, OutputStream outstream) throws IOException {
        ZipOutputStream zip = (new ZipOutputStream(outstream));
        zip.putNextEntry(new ZipEntry(entryName));
        zip.write(content);
        zip.closeEntry();
        zip.close();
    }

    private ArachniClient getArachniClient(ArachniPluginConfiguration config) {
        ArachniRestClientBuilder builder = ArachniRestClientBuilder.create(config.getArachniServerUrl());
        if (config.getBasicAuth()) {
            builder.addCredentials(config.getUser(), config.getPassword());
        }
        builder.setMergeConflictStratey(MergeConflictStrategy.PREFER_STRING);

        return builder.build();
    }
}
