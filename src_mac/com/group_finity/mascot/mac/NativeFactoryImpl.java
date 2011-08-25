package com.group_finity.mascot.mac;

import java.awt.image.BufferedImage;

import javax.swing.JRootPane;
import javax.swing.JWindow;

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
		final TranslucentWindow transcluentWindow = delegate.newTransparentWindow();

    JRootPane rootPane = transcluentWindow.asJWindow().getRootPane();

    // ウィンドウの影がずれるので、影を描画しないようにする
    rootPane.putClientProperty("Window.shadow", Boolean.FALSE);

    // 実行時の warning を消す
    rootPane.putClientProperty("apple.awt.draggableWindowBackground", Boolean.TRUE);

    return new TranslucentWindow() {
			// パフォーマンスのためにラップしたクラスを使う
			// updateImage() がボトルネックになっていたので、
			// image が変わった時だけ呼ぶようにする

			private boolean imageChanged = false;
			private NativeImage oldImage = null;

			@Override
			public JWindow asJWindow() {
				return transcluentWindow.asJWindow();
			}

			@Override
			public void setImage(NativeImage image) {
				this.imageChanged = (this.oldImage != null && image != oldImage);
				this.oldImage = image;
				transcluentWindow.setImage(image);
			}

			@Override
			public void updateImage() {
				if (this.imageChanged) {
					transcluentWindow.updateImage();
					this.imageChanged = false;
				}
			}
		};
	}
}
