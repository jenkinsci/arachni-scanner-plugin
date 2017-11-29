package org.jenkinsci.plugins.arachni;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

public class ArachniScopeProperty implements Describable<ArachniScopeProperty> {
    private int pageLimit;
    private String excludePathPattern;

    @DataBoundConstructor
    public ArachniScopeProperty(int pageLimit, String excludePathPattern) {
        this.pageLimit = pageLimit;
        this.excludePathPattern = excludePathPattern;
    }
    
    public String getPageLimit() {
        if (pageLimit > 0) {
            return Integer.toString(pageLimit);
        }
        return "";
    }

    public int getPageLimitAsInt() {
        return pageLimit;
    }

    public String getExcludePathPattern() {
        return excludePathPattern;
    }
    
    @Override
    public Descriptor<ArachniScopeProperty> getDescriptor() {
        return (DescriptorImpl) Jenkins.getInstance().getDescriptor(ArachniScopeProperty.class);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ArachniScopeProperty> {

        @Override
        public String getDisplayName() {
            return "Scope";
        }
    }
}
