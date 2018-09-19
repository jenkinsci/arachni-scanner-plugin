package org.jenkinsci.plugins.arachni;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider.StoreImpl;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;

import hudson.Extension;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

@Extension
public class ArachniPluginConfiguration extends GlobalConfiguration implements Serializable {

    private static final long serialVersionUID = -2091111219470314999L;

    private String arachniServerUrl;

    /**
     * @deprecated Just for backward compatibility, use credentials plugin instead.
     */
    @Deprecated
    private transient boolean basicAuth;

    /**
     * @deprecated Just for backward compatibility, use credentials plugin instead.
     */
    @Deprecated
    private transient String user;

    /**
     * @deprecated Just for backward compatibility, use credentials plugin instead.
     */
    @Deprecated
    private transient String password;

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

    protected Object readResolve() throws IOException {
        if (basicAuth && StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) {
            StandardUsernamePasswordCredentials credentials = new UsernamePasswordCredentialsImpl(
                    CredentialsScope.GLOBAL, UUID.randomUUID().toString(),
                    "Credentials for Arachni Server, migrated from older version.", user, password);
            StoreImpl store = new StoreImpl();
            store.addCredentials(Domain.global(), credentials);
            credentialsId = credentials.getId();
            save();
        }
        return this;
    }

    public FormValidation doCheckArachniServerUrl(@QueryParameter String value) {
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
                        Collections.<DomainRequirement> emptyList(), CredentialsMatchers.withId(value))
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
