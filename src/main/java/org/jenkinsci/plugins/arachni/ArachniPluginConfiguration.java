package org.jenkinsci.plugins.arachni;

import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.Extension;
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
