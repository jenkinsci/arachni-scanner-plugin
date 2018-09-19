package org.jenkinsci.plugins.arachni;

import java.util.List;
import java.util.logging.Logger;

import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import hudson.tasks.Builder;

@Extension
public class ArachniRunListener extends RunListener<Run<?, ?>> {
    private static final Logger log = Logger.getLogger(ArachniRunListener.class.getName());

    @Override
    public void onFinalized(Run<?, ?> run) {
        if (run.getParent() instanceof FreeStyleProject) {
            FreeStyleProject project = (FreeStyleProject) run.getParent();
            ArachniScanner scanner = getArachniScannerBuilder(project);
            if (scanner != null) {
                scanner.shutdownScan();
            } else {
                log.warning("Builder for Arachni Scanner not found, shutdown not run.");
            }
        }
        super.onFinalized(run);
    }

    private ArachniScanner getArachniScannerBuilder(FreeStyleProject project) {
        List<Builder> builders = project.getBuilders();
        for (Builder builder : builders) {
            if (builder instanceof ArachniScanner) {
                return (ArachniScanner) builder;
            }
        }
        return null;
    }
}
