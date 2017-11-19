package org.jenkinsci.plugins.arachni;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;

@Extension
public class ArachniPluginConfiguration extends GlobalConfiguration {
    Logger log = LoggerFactory.getLogger(ArachniPluginConfiguration.class);

    private String arachniServerUrl;
    
    private boolean basicAuth;
    
    private String user;
    
    private String password;

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
    
    public FormValidation doCheckUser(@QueryParameter String value, @QueryParameter boolean basicAuth) throws IOException, ServletException {
        if (basicAuth) {
            if ((value == null) || value.isEmpty()) {
                return FormValidation.error("User should not be empty for basic authentication.");
            }
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckPassword(@QueryParameter String value, @QueryParameter boolean basicAuth) throws IOException, ServletException {
        if (basicAuth) {
            if ((value == null) || value.isEmpty()) {
                return FormValidation.error("Password should not be empty for basic authentication.");
            }
        }
        return FormValidation.ok();
    }

    public void setArachniServerUrl(String arachniServerUrl) {
        this.arachniServerUrl = arachniServerUrl;
    }
    
    public String getArachniServerUrl() {
        return arachniServerUrl;
    }
    
    public void setBasicAuth(boolean basicAuth) {
        this.basicAuth = basicAuth;
    }
    
    public boolean getBasicAuth() {
        return basicAuth;
    }
    
    public void setUser(String user) {
        this.user = user;
    }
    
    public String getUser() {
        return user;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return password;
    }
}
