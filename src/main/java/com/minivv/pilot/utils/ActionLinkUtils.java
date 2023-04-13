package com.minivv.pilot.utils;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.components.AnActionLink;
import org.jetbrains.annotations.NotNull;

/**
 * 构造 链接
 */
public class ActionLinkUtils {

    /**
     * 构造链接工具类
     */
    public static AnActionLink newActionLink(String linkUrl) {
        return new AnActionLink("", new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                BrowserUtil.browse(linkUrl);
            }
        });
    }
}