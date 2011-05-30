package com.group_finity.mascot.mac;

import java.awt.image.BufferedImage;

import javax.swing.JRootPane;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.group_finity.mascot.mac.MacEnvironment;

public class NativeFactoryImpl extends NativeFactory {

  private NativeFactory delegate =
    new com.group_finity.mascot.generic.NativeFactoryImpl();
  private Environment environment = new MacEnvironment();

	@Override
	public Environment getEnvironment() {
		return this.environment;
	}

	@Override
	public NativeImage newNativeImage(final BufferedImage src) {
		return delegate.newNativeImage(src);
	}

	@Override
	public TranslucentWindow newTransparentWindow() {
		TranslucentWindow transcluentWindow = delegate.newTransparentWindow();

    JRootPane rootPane = transcluentWindow.asJWindow().getRootPane();

    // Don't draw shadow
    rootPane.putClientProperty("Window.shadow", Boolean.FALSE);

    // Suppress warning
    rootPane.putClientProperty("apple.awt.draggableWindowBackground", Boolean.TRUE);

    return transcluentWindow;
	}
}
