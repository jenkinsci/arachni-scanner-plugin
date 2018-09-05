package org.jenkinsci.plugins.arachni;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

public class UserConfigProperty implements Describable<UserConfigProperty> {
    
    private String filename;

    @DataBoundConstructor
    public UserConfigProperty(String filename) {
        this.filename = filename;
    }
    
    public String getFilename() {
        return filename;
    }
    
    @Override
    public Descriptor<UserConfigProperty> getDescriptor() {
        return (DescriptorImpl) Jenkins.get().getDescriptor(UserConfigProperty.class);
    }
    
    @Extension
    public static class DescriptorImpl extends Descriptor<UserConfigProperty> {

        @Override
        public String getDisplayName() {
            return "User Configuration";
        }
    }
}
