package com.minivv.pilot;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.minivv.pilot.model.AppSettings;
import com.minivv.pilot.state.AppSettingsStorage;
import com.minivv.pilot.ui.AppPluginSettingsPage;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AppConfigurable implements Configurable {

    private AppPluginSettingsPage form;
    private AppSettings state;

    private AppSettingsStorage appSettingsStorage;

    public AppConfigurable() {
        appSettingsStorage = AppSettingsStorage.getInstance();
        state = appSettingsStorage.getState();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "gpt-copilot";
    }

    @Override
    public @Nullable JComponent createComponent() {
        form = new AppPluginSettingsPage(state);
        return form.getRootPane();
    }

    @Override
    public boolean isModified() {
        return form != null && form.isSettingsModified(state);
    }

    @Override
    public void apply() throws ConfigurationException {
        appSettingsStorage.unregisterActions();
        state = form.getSettings().clone();
        appSettingsStorage.loadState(state);
        appSettingsStorage.registerActions();
    }

    @Override
    public void reset() {
        if (form != null) {
            form.importForm(state);
        }
    }

    @Override
    public void disposeUIResources() {
        form = null;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return form.getGptKey();
    }

    public AppSettingsStorage getAppSettingsStorage() {
        return appSettingsStorage;
    }
}
