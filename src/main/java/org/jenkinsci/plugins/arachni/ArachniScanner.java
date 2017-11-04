package org.jenkinsci.plugins.arachni;

import java.io.IOException;
import java.io.PrintStream;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

public class ArachniScanner extends Builder implements SimpleBuildStep{
    Logger log = LoggerFactory.getLogger(ArachniScanner.class);
    
    private String url;
    
    @DataBoundConstructor
    public ArachniScanner(String url) {
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
        private String arachniServerUrl;
        
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
        
        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            json = json.getJSONObject("arachni");
            arachniServerUrl = json.getString("arachniServerUrl");
            save();
            return true;
        }
        
        public String getArachniServerUrl() {
            return arachniServerUrl;
        }
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        PrintStream consoleStream = listener.getLogger();
        consoleStream.println("Start Arachni Security Scan");
        consoleStream.println("Arachni server URL: " + url);
        consoleStream.println("Site under scan: ");
        
    }
}
