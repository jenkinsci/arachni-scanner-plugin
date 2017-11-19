package org.jenkinsci.plugins.arachni;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import hudson.tasks.Builder;

@Extension
public class ArachniRunListener extends RunListener<Run<?,?>> {
    Logger log = LoggerFactory.getLogger(ArachniRunListener.class);
    
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
                        log.error(exception.getMessage(), exception);
                    }
                }
            }
        }
        super.onFinalized(run);
    }
}
