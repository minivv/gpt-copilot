package com.minivv.pilot.utils;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class Donate {
	private static final Logger LOG = Logger.getInstance(Donate.class);

	public static final Icon ICON = IconLoader.getIcon("com/minivv/pilot/ui/coins_in_hand.png", Donate.class);

	public static void initUrl(JButton donate, String url) {
		donate.setIcon(ICON);
		donate.addActionListener(e -> BrowserUtil.browse(url));
	}

	public static void initImage(JButton donate, String imgPath) {
		donate.setIcon(ICON);
		donate.addActionListener(e -> {
			// 加载图像文件
			BufferedImage img = null;
			URL url = Donate.class.getResource(imgPath);
			if (url == null) {
				LOG.error("Can't find image: " + imgPath);
				return;
			}
			ImageIcon icon = new ImageIcon(url);
			// 创建一个对话框来显示图像
			JFrame frame = new JFrame();
			frame.setSize(300,400);
			JDialog dialog = new JDialog(frame, "Thanks", true);
			JLabel label = new JLabel(icon);
			dialog.getContentPane().add(label);
			dialog.pack();
			dialog.setLocationRelativeTo(null);
			dialog.setVisible(true);
		});
	}
}
