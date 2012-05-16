package com.group_finity.mascot.mac;

import javax.swing.JRootPane;
import javax.swing.JWindow;

import com.group_finity.mascot.image.TranslucentWindow;
import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.image.NativeImage;

class MacTranslucentWindow implements TranslucentWindow {
  private TranslucentWindow delegate;
	private boolean imageChanged = false;
	private NativeImage oldImage = null;

	MacTranslucentWindow(NativeFactory factory) {
		delegate = factory.newTransparentWindow();
    JRootPane rootPane = delegate.asJWindow().getRootPane();

    // ウィンドウの影がずれるので、影を描画しないようにする
    rootPane.putClientProperty("Window.shadow", Boolean.FALSE);

    // 実行時の warning を消す
    rootPane.putClientProperty("apple.awt.draggableWindowBackground", Boolean.TRUE);
	}

	@Override
	public JWindow asJWindow() {
		return delegate.asJWindow();
	}

	@Override
	public void setImage(NativeImage image) {
		this.imageChanged = (this.oldImage != null && image != oldImage);
		this.oldImage = image;
		delegate.setImage(image);
	}

	@Override
	public void updateImage() {
		if (this.imageChanged) {
			delegate.updateImage();
			this.imageChanged = false;
		}
	}
}
