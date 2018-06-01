package org.jenkinsci.plugins.arachni;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import hudson.tasks.Builder;

@Extension
public class ArachniRunListener extends RunListener<Run<?,?>> {
    Logger log = Logger.getLogger(ArachniRunListener.class.getName());
    
    @Override
    public void onFinalized(Run<?, ?> run) {
        if (run.getParent() instanceof FreeStyleProject) {
            FreeStyleProject project = (FreeStyleProject) run.getParent();
            List<Builder> builders = project.getBuilders();
            for (Builder builder : builders) {
                if (builder instanceof ArachniScanner) {
                    try {
                        ((ArachniScanner) builder).shutdownScan();
                    } catch (IOException exception) {
                        log.severe(exception.getMessage());
                    }
                }
            }
        }
        super.onFinalized(run);
    }
}
