package com.minivv.pilot.state;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.minivv.pilot.action.BasePilotPluginAction;
import com.minivv.pilot.model.Prompt;
import com.minivv.pilot.model.AppSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@State(
        name = "chatGptPilot.idea.plugin",
        storages = {@Storage("setting.xml")}
)
public class AppSettingsStorage implements PersistentStateComponent<AppSettings> {
    private AppSettings settings = new AppSettings();
    private final String idPrefix = "chatGptPilot_";
    private final DefaultActionGroup actionGroup = new DefaultActionGroup("gpt pilot", true);

    @Nullable
    @Override
    public AppSettings getState() {
        if (settings == null) {
            settings = new AppSettings();
        }
        return settings;
    }

    @Override
    public void loadState(@NotNull AppSettings state) {
        settings = state;
        if(settings.prompts != null && settings.prompts.getPrompts().isEmpty()) {
            AppSettings.addDefaultPrompts(settings.prompts);
        }
    }

    public static AppSettingsStorage getInstance() {
        return ApplicationManager.getApplication().getService(AppSettingsStorage.class);
    }

    public static @NotNull Project getProject() {
        return ApplicationManager.getApplication().getService(ProjectManager.class).getOpenProjects()[0];
    }

//    public void registerActions() {
//        ActionManager actionManager = ActionManager.getInstance();
//        clear(actionGroup);
//        AnAction popupMenu = actionManager.getAction("EditorPopupMenu");
//        for (Prompt prompt : this.settings.prompts.getPrompts()) {
//            BasePilotPluginAction newAction = new BasePilotPluginAction(prompt.getOption(), prompt.getIndex()) {
//                @Override
//                public String addStatement(String code) {
//                    return prompt.getSnippet().replace("{query}", code);
//                }
//            };
//            actionManager.registerAction(idPrefix + prompt.getIndex(), newAction);
//            actionGroup.add(newAction);
//        }
//        ((DefaultActionGroup) popupMenu).add(actionGroup);
//    }

    public void registerActions() {
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup popupMenu = (DefaultActionGroup) actionManager.getAction("EditorPopupMenu");
        clear(popupMenu, actionGroup);
        for (Prompt prompt : this.settings.prompts.getPrompts()) {
            AnAction oldAction = actionManager.getAction(idPrefix + prompt.getOption());
            if (oldAction != null) {
                actionManager.unregisterAction(idPrefix + prompt.getOption());
            }
            oldAction = new BasePilotPluginAction(prompt.getOption()) {
                @Override
                public String addStatement(String code) {
                    return prompt.getSnippet().replace("{query}", code);
                }
            };
            actionManager.registerAction(idPrefix + prompt.getOption(), oldAction);
            actionGroup.add(oldAction);
        }
        popupMenu.add(actionGroup);
    }

    private static void clear(DefaultActionGroup popupMenu, DefaultActionGroup actionGroup) {
        popupMenu.remove(actionGroup);
        AnAction[] childActionsOrStubs = actionGroup.getChildActionsOrStubs();
        for (AnAction childActionsOrStub : childActionsOrStubs) {
            actionGroup.remove(childActionsOrStub);
        }
    }

//    public void unregisterActions() {
//        ActionManager actionManager = ActionManager.getInstance();
//        for (Prompt prompt : this.settings.prompts.getPrompts()) {
//            actionManager.unregisterAction(idPrefix + prompt.getIndex());
//        }
//    }

    public void unregisterActions() {
        ActionManager actionManager = ActionManager.getInstance();
        for (Prompt prompt : this.settings.prompts.getPrompts()) {
            actionManager.unregisterAction(idPrefix + prompt.getOption());
        }
    }
}