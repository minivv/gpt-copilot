package com.minivv.pilot.action;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.minivv.pilot.model.AppSettings;
import com.minivv.pilot.state.AppSettingsStorage;
import com.minivv.pilot.utils.GPTClient;
import com.minivv.pilot.utils.NotifyUtils;
import com.theokanning.openai.completion.CompletionChoice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class BasePilotPluginAction extends AnAction {
//    private final int index;
//    public BasePilotPluginAction(@Nullable @NlsActions.ActionText String text,int index) {
//        super(text);
//        this.index = index;
//    }
    public BasePilotPluginAction(@Nullable @NlsActions.ActionText String text) {
        super(text);
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        // 只有当编辑器中有选中的代码片段时才显示 GptCopilotAction 菜单选项
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        String code = editor.getSelectionModel().getSelectedText();
        e.getPresentation().setEnabled(code != null && !code.isEmpty());
    }
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        // 获取编辑器对象
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        // 获取用户选中的代码片段
        String code = editor.getSelectionModel().getSelectedText();
        if (code == null || code.isEmpty()) {
            return;
        }
        String statement = addStatement(code);
        NotifyUtils.notifyMessage(project,"sending code to gpt..", NotificationType.INFORMATION);
        doCommand(statement, editor, project);
    }

    private void doCommand(String statement, Editor editor, Project project) {
        AppSettings settings = AppSettingsStorage.getInstance().getState();
        if(settings == null){
            NotifyUtils.notifyMessage(project,"gpt-copilot settings is null, please check!", NotificationType.ERROR);
            return;
        }
        List<CompletionChoice> choices = GPTClient.callChatGPT(statement, settings);
        String optimizedCode;
        if(GPTClient.isSuccessful(choices)){
            optimizedCode = GPTClient.toString(choices);
        } else {
            NotifyUtils.notifyMessage(project,"gpt-copilot connection failed, please check!", NotificationType.ERROR);
            return;
        }
        //处理readable操作
        ApplicationManager.getApplication().runWriteAction(() -> {
            CommandProcessor.getInstance().executeCommand(
                    project,
                    () -> {
                        // 将优化后的代码作为注释添加到选中的代码块后面
                        insertCommentAfterSelectedText(editor, optimizedCode,settings.isReplace);
                        if(!settings.isReplace){
                            // 将选中的代码块注释掉
                            commentSelectedText(editor);
                        }
                        // 取消选中状态
                        clearSelection(editor);
                    },
                    "Insert Comment",
                    null
            );
        });
    }
    public abstract String addStatement(String code);
    private void commentSelectedText(Editor editor) {
        Document document = editor.getDocument();
        int selectionStartOffset = editor.getSelectionModel().getSelectionStart();
        int selectionEndOffset = editor.getSelectionModel().getSelectionEnd();
        String selectedText = document.getText().substring(selectionStartOffset, selectionEndOffset);
        document.replaceString(selectionStartOffset, selectionEndOffset, "/*" + selectedText + "*/");
    }
    private void insertCommentAfterSelectedText(Editor editor, String optimizedCode, boolean isReplace) {
        Document document = editor.getDocument();
        int selectionStartOffset = editor.getSelectionModel().getSelectionStart();
        int selectionEndOffset = editor.getSelectionModel().getSelectionEnd();
        if(isReplace){
            document.replaceString(selectionStartOffset, selectionEndOffset, optimizedCode);
            return;
        }
        document.insertString(selectionEndOffset, optimizedCode);
    }
    private void clearSelection(Editor editor) {
        editor.getSelectionModel().removeSelection();
    }
}