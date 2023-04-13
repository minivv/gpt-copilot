package com.minivv.pilot;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;

public class AppProjectManagerListener implements ProjectManagerListener {
    static final Logger LOG = Logger.getInstance(AppProjectManagerListener.class);

    @Override
    public void projectOpened(Project project) {
        LOG.info("Project is opened: " + project.getName());
        AppConfigurable appConfigurable = new AppConfigurable();
        appConfigurable.getAppSettingsStorage().registerActions();
    }

    @Override
    public void projectClosed(Project project) {
        LOG.info("Project is closed: " + project.getName());
    }
}