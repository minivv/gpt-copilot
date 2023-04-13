package com.minivv.pilot.model;

import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Transient;
import com.minivv.pilot.state.AppSettingsStorage;
import com.minivv.pilot.constants.SysConstants;
import com.rits.cloning.Cloner;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;


public class AppSettings extends DomainObject implements Cloneable {
    public boolean enableProxy = false;
    public String proxyHost = "127.0.0.1";
    public int proxyPort = 1087;
    public String proxyType = SysConstants.httpProxyType;
    public String gptKey;
    public String gptModel = "text-davinci-003";
    public int gptMaxTokens = 2048;
    public int maxWaitSeconds = 60;
    public boolean isReplace = false;
    public String testConnMsg = SysConstants.testConnMsg;
    public Prompts prompts = new Prompts();
    public AppSettings() {
        this.addDefaultPrompts(this.prompts);
    }

    @NotNull
    public static Project getProject() {
        return AppSettingsStorage.getProject();
    }

    @NotNull
    public static AppSettings get() {
        AppSettingsStorage instance = AppSettingsStorage.getInstance();
        return instance.getState();
    }

    @Override
    public AppSettings clone() {
        Cloner cloner = new Cloner();
        cloner.nullInsteadOfClone();
        return cloner.deepClone(this);
    }

    public static void resetDefaultPrompts(List<Prompt> _prompts) {
        Prompts prompts = addDefaultPrompts(new Prompts());
        Map<String, String> stringStringMap = prompts.asMap();
        _prompts.removeIf(next -> stringStringMap.containsKey(next.getOption()));
        _prompts.addAll(prompts.getPrompts());
    }

    @Transient
    public static Prompts addDefaultPrompts(Prompts prompts) {
        prompts.add(Prompt.of("Readable", "help me enhance the readability of the following code snippet, without adding any additional information except for the optimized code. Here is the code snippet:{query}"));
        prompts.add(Prompt.of("List Steps", "help me add comments to the key steps of the following code snippet and return the optimized code with comments. without adding any additional information except for the optimized code. Here is the code snippet:{query}"));
        prompts.add(Prompt.of("Explain", "帮我增强下面一段代码的可读性吧，除了优化后的代码，不要添加任何其他信息，这是代码片段：{query}"));
        prompts.add(Prompt.of("步骤注释", "帮我给下面一段代码的关键步骤添加注释，返回优化后的完整代码，除了优化后的代码，不要添加任何其他信息，这是代码片段：{query}"));
        prompts.add(Prompt.of("emptyForYou", "balabala{query}"));
        return prompts;
    }
}