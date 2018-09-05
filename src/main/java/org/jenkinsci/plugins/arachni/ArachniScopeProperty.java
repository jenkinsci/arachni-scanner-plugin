package org.jenkinsci.plugins.arachni;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
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
        return (DescriptorImpl) Jenkins.get().getDescriptor(ArachniScopeProperty.class);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ArachniScopeProperty> {

        @Override
        public String getDisplayName() {
            return "Scope";
        }

        public FormValidation doCheckPageLimit(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.ok();
            }
            try {
                Integer.parseInt(value);
                return FormValidation.ok();
            } catch (NumberFormatException excecption) {
                return FormValidation.error("Not valid.");
            }
        }
    }
}
