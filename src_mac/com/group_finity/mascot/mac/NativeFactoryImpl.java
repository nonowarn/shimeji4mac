package com.group_finity.mascot.mac;

import java.awt.image.BufferedImage;

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

    // Don't draw shadow
    transcluentWindow.
      asJWindow().
      getRootPane().
      putClientProperty("Window.shadow", Boolean.FALSE);

    return transcluentWindow;
	}
}
