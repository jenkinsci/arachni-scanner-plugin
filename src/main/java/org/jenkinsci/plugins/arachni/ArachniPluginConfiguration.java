package org.jenkinsci.plugins.arachni;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import javax.servlet.ServletException;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

import hudson.Extension;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

@Extension
public class ArachniPluginConfiguration extends GlobalConfiguration {

    private String arachniServerUrl;

    private String credentialsId;

    public ArachniPluginConfiguration() {
        load();
    }

    public static ArachniPluginConfiguration get() {
        return GlobalConfiguration.all().get(ArachniPluginConfiguration.class);
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        return true;
    }

    public FormValidation doCheckArachniServerUrl(@QueryParameter String value) throws IOException, ServletException {
        try {
            new URL(value);
            return FormValidation.ok();
        } catch (MalformedURLException excecption) {
            return FormValidation.error("URL is not valid.");
        }
    }

    public FormValidation doCheckCredentialsId(@AncestorInPath Item item, @QueryParameter String value) {
        if (item == null) {
            return FormValidation.ok();
        }
        if (CredentialsProvider
                .listCredentials(StandardUsernamePasswordCredentials.class, item, ACL.SYSTEM,
                        Collections.<DomainRequirement> emptyList(), CredentialsMatchers.withId(credentialsId))
                .isEmpty()) {
            return FormValidation.error("Cannot find currently selected credentials");
        }
        return FormValidation.ok();
    }

    public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
        StandardListBoxModel result = new StandardListBoxModel();
        if (item == null) {
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                return result.includeCurrentValue(credentialsId);
            }
        } else {
            if (!item.hasPermission(Item.EXTENDED_READ) && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                return result.includeCurrentValue(credentialsId);
            }
        }
        return result.includeEmptyValue().includeAs(ACL.SYSTEM, item, StandardUsernamePasswordCredentials.class,
                Collections.<DomainRequirement> emptyList()).includeCurrentValue(credentialsId);
    }

    public void setArachniServerUrl(String arachniServerUrl) {
        this.arachniServerUrl = arachniServerUrl;
    }

    public String getArachniServerUrl() {
        return arachniServerUrl;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }
}
