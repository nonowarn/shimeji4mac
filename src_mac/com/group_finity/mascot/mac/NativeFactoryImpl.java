package com.group_finity.mascot.mac;

import java.awt.image.BufferedImage;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;

public class NativeFactoryImpl extends NativeFactory {

  private NativeFactory delegate =
    new com.group_finity.mascot.generic.NativeFactoryImpl();

	@Override
	public Environment getEnvironment() {
		return delegate.getEnvironment();
	}
	@Override
	public NativeImage newNativeImage(final BufferedImage src) {
		return delegate.newNativeImage(src);
	}
	@Override
	public TranslucentWindow newTransparentWindow() {
		return delegate.newTransparentWindow();
	}
}
