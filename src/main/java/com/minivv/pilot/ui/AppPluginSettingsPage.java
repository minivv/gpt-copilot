package com.minivv.pilot.ui;


import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.AnActionLink;
import com.minivv.pilot.constants.SysConstants;
import com.minivv.pilot.model.AppSettings;
import com.minivv.pilot.model.Prompt;
import com.minivv.pilot.model.Prompts;
import com.minivv.pilot.utils.ActionLinkUtils;
import com.minivv.pilot.utils.Donate;
import com.minivv.pilot.utils.GPTClient;
import com.minivv.pilot.utils.NotifyUtils;
import com.theokanning.openai.completion.CompletionChoice;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

/**
 * todo 超时时间配置
 * todo 更换NotifyUtils
 * todo 限制代码量
 */
public class AppPluginSettingsPage {


    private JPanel rootPane;
    private JTextField gptKey;
    private JTextField gptModel;
    private JSpinner gptMaxToken;
    private JCheckBox isReplace;
    private JRadioButton enableProxy;
    private JRadioButton httpProxy;
    private JRadioButton socketProxy;
    private JTextField proxyHost;
    private JSpinner proxyPort;
    private AnActionLink gptKeyLink;
    private AnActionLink gptModelsLink;
    private AnActionLink gptUsageLink;
    private JTextField testConnMsg;
    private JButton testConnButton;
    private JPanel promptsPane;
    private JSpinner maxWaitSeconds;
    private JButton donatePaypal;
    private JButton donateWx;
    //    private JPanel locale;
    private AppSettings settings;
    private final PromptsTable promptsTable;

    public AppPluginSettingsPage(AppSettings original) {
        this.settings = original.clone();
        this.promptsTable = new PromptsTable();
        promptsPane.add(ToolbarDecorator.createDecorator(promptsTable)
                .setRemoveAction(button -> promptsTable.removeSelectedPrompts())
                .setAddAction(button -> promptsTable.addPrompt(Prompt.of("Option" + promptsTable.getRowCount(), "Snippet:{query}")))
                .addExtraAction(new AnActionButton("Reset Default Prompts", AllIcons.Actions.Rollback) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        promptsTable.resetDefaultAliases();
                    }
                })
                .createPanel());

        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(MouseEvent e) {
                return promptsTable.editPrompt();
            }
        }.installOn(promptsTable);
//        ComboBox<Locale> comboBox = new ComboBox<>();
//        Locale[] locales = Locale.getAvailableLocales();
//        for (Locale locale : locales) {
//            comboBox.addItem(locale);
//        }
//        locale.add(comboBox);
        Donate.initUrl(donatePaypal, "https://www.paypal.me/kuweiguge");
//        Donate.initImage(donateWx,"images/wechat_donate.png");
    }

    private void createUIComponents() {
        gptMaxToken = new JSpinner(new SpinnerNumberModel(2048, 128, 2048, 128));
        proxyPort = new JSpinner(new SpinnerNumberModel(1087, 1, 65535, 1));
        maxWaitSeconds = new JSpinner(new SpinnerNumberModel(60, 5, 600, 5));
        gptKeyLink = ActionLinkUtils.newActionLink("https://platform.openai.com/account/api-keys");
        gptModelsLink = ActionLinkUtils.newActionLink("https://platform.openai.com/docs/models/overview");
        gptUsageLink = ActionLinkUtils.newActionLink("https://platform.openai.com/account/usage");
    }

    public AppSettings getSettings() {
        promptsTable.commit(settings);
        getData(settings);
        return settings;
    }

    private void getData(AppSettings settings) {
        settings.gptKey = gptKey.getText();
        settings.gptModel = gptModel.getText();
        settings.gptMaxTokens = (int) gptMaxToken.getValue();
        settings.isReplace = isReplace.isSelected();
        settings.enableProxy = enableProxy.isSelected();
        settings.proxyHost = proxyHost.getText();
        settings.proxyPort = (int) proxyPort.getValue();
        settings.maxWaitSeconds = (int) maxWaitSeconds.getValue();
        settings.proxyType = httpProxy.isSelected() ? SysConstants.httpProxyType : SysConstants.socketProxyType;
        settings.testConnMsg = testConnMsg.getText();
        settings.prompts = new Prompts(promptsTable.prompts);
    }

    public void importForm(AppSettings state) {
        this.settings = state.clone();
        setData(settings);
        promptsTable.reset(settings);
    }

    private void setData(AppSettings settings) {
        gptKey.setText(settings.gptKey);
        gptModel.setText(settings.gptModel);
        gptMaxToken.setValue(settings.gptMaxTokens);
        isReplace.setSelected(settings.isReplace);
        testConnMsg.setText(settings.testConnMsg);
        httpProxy.setSelected(Objects.equals(settings.proxyType, SysConstants.httpProxyType));
        socketProxy.setSelected(Objects.equals(settings.proxyType, SysConstants.socketProxyType));
        proxyHost.setText(settings.proxyHost);
        proxyPort.setValue(settings.proxyPort);
        maxWaitSeconds.setValue(settings.maxWaitSeconds);
        enableProxy.addChangeListener(e -> {
            if (enableProxy.isSelected()) {
                httpProxy.setEnabled(true);
                socketProxy.setEnabled(true);
                proxyHost.setEnabled(true);
                proxyPort.setEnabled(true);
            } else {
                httpProxy.setEnabled(false);
                socketProxy.setEnabled(false);
                proxyHost.setEnabled(false);
                proxyPort.setEnabled(false);
            }
        });
        httpProxy.addChangeListener(e -> {
            socketProxy.setSelected(!httpProxy.isSelected());
        });
        socketProxy.addChangeListener(e -> {
            httpProxy.setSelected(!socketProxy.isSelected());
        });
        enableProxy.setSelected(settings.enableProxy);

        testConnButton.addActionListener(e -> {
            String msg = StringUtils.isBlank(testConnMsg.getText()) ? SysConstants.testConnMsg : testConnMsg.getText();
            boolean hasError = checkSettings();
            if (hasError) {
                return;
            }
            List<CompletionChoice> choices = GPTClient.callChatGPT(msg, settings);
            if (GPTClient.isSuccessful(choices)) {
                NotifyUtils.notifyMessage(AppSettings.getProject(), "Test connection successfully!ChatGPT answer:" + GPTClient.toString(choices), NotificationType.INFORMATION);
            } else {
                NotifyUtils.notifyMessage(AppSettings.getProject(), "Test connection failed!", NotificationType.ERROR);
            }
        });
    }

    /**
     * 保存设置
     *
     * @return 是否有错误
     */
    private boolean checkSettings() {
        StringBuilder error = new StringBuilder();
        if (StringUtils.isBlank(gptKey.getText())) {
            error.append("GPT Key is required.").append("\n");
        }
        if (StringUtils.isBlank(gptModel.getText())) {
            error.append("GPT Model is required.").append("\n");
        }
        if (gptMaxToken.getValue() == null || (int) gptMaxToken.getValue() <= 0 || (int) gptMaxToken.getValue() > 2048) {
            error.append("GPT Max Token is required and should be between 1 and 2048.").append("\n");
        }
        if (enableProxy.isSelected()) {
            if (StringUtils.isBlank(proxyHost.getText())) {
                error.append("Proxy Host is required.").append("\n");
            }
            if (proxyPort.getValue() == null || (int) proxyPort.getValue() <= 0 || (int) proxyPort.getValue() > 65535) {
                error.append("Proxy Port is required and should be between 1 and 65535.").append("\n");
            }
            if (maxWaitSeconds.getValue() == null || (int) maxWaitSeconds.getValue() <= 0 || (int) maxWaitSeconds.getValue() > 600) {
                error.append("Max Wait Seconds is required and should be between 5 and 600.").append("\n");
            }
        }
        if (promptsTable.getRowCount() <= 0) {
            error.append("Prompts is required.").append("\n");
        }
        if (promptsTable.prompts.stream().anyMatch(p -> !StringUtils.contains(p.getSnippet(), "{query}"))) {
            error.append("Prompts should contain {query}.").append("\n");
        }
        if (StringUtils.isNotBlank(error)) {
            NotifyUtils.notifyMessage(AppSettings.getProject(), error.toString(), NotificationType.ERROR);
            return true;
        } else {
            return false;
        }
    }


    public boolean isSettingsModified(AppSettings state) {
        if (promptsTable.isModified(state)) return true;
        return !this.settings.equals(state) || isModified(state);
    }

    private boolean isModified(AppSettings state) {
        return !gptKey.getText().equals(state.gptKey) ||
                !gptModel.getText().equals(state.gptModel) ||
                !gptMaxToken.getValue().equals(state.gptMaxTokens) ||
                isReplace.isSelected() != state.isReplace ||
                enableProxy.isSelected() != state.enableProxy ||
                !proxyHost.getText().equals(state.proxyHost) ||
                !proxyPort.getValue().equals(state.proxyPort) ||
                !maxWaitSeconds.getValue().equals(state.maxWaitSeconds) ||
                !httpProxy.isSelected() == Objects.equals(state.proxyType, SysConstants.httpProxyType) ||
                !socketProxy.isSelected() == Objects.equals(state.proxyType, SysConstants.socketProxyType) ||
                !testConnMsg.getText().equals(state.testConnMsg);
    }

    public JPanel getRootPane() {
        return rootPane;
    }

    public JTextField getGptKey() {
        return gptKey;
    }
}
