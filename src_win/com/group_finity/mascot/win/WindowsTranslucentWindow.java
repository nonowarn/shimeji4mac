package com.group_finity.mascot.win;

import java.awt.Graphics;

import javax.swing.JWindow;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.group_finity.mascot.win.jna.BLENDFUNCTION;
import com.group_finity.mascot.win.jna.Gdi32;
import com.group_finity.mascot.win.jna.POINT;
import com.group_finity.mascot.win.jna.RECT;
import com.group_finity.mascot.win.jna.SIZE;
import com.group_finity.mascot.win.jna.User32;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * α値つき画像ウィンドウ.
 * {@link #setImage(WindowsNativeImage)} で設定した {@link WindowsNativeImage} をデスクトップに表示できる.
 *
 * {@link #setAlpha(int)} で表示するときの濃度も指定できる.
 */
class WindowsTranslucentWindow extends JWindow implements TranslucentWindow {

	private static final long serialVersionUID = 1L;

	@Override
	public JWindow asJWindow() {
		return this;
	}

	/**
	 * α値つき画像を描画する.
	 * @param imageHandle ビットマップのハンドル.
	 * @param alpha 表示濃度. 0 = まったく表示しない、255 = 完全に表示する.
	 */
	private void paint(final Pointer imageHandle, final int alpha) {

		final Pointer hWnd = Native.getComponentPointer(this);

		if ( User32.INSTANCE.IsWindow(hWnd)!=0 ) {

			final int exStyle = User32.INSTANCE.GetWindowLongW(hWnd, User32.GWL_EXSTYLE);
			if ( (exStyle&User32.WS_EX_LAYERED)==0 ) {
				User32.INSTANCE.SetWindowLongW(hWnd, User32.GWL_EXSTYLE, exStyle | User32.WS_EX_LAYERED);
			}

			// 画像の転送元DCを作成
			final Pointer clientDC= User32.INSTANCE.GetDC(hWnd);
			final Pointer memDC = Gdi32.INSTANCE.CreateCompatibleDC(clientDC);
			final Pointer oldBmp = Gdi32.INSTANCE.SelectObject(memDC, imageHandle );

			User32.INSTANCE.ReleaseDC(hWnd, clientDC);

			// 転送先領域
			final RECT windowRect = new RECT();
			User32.INSTANCE.GetWindowRect(hWnd, windowRect);

			// 転送
			final BLENDFUNCTION bf = new BLENDFUNCTION();
			bf.BlendOp = BLENDFUNCTION.AC_SRC_OVER;
			bf.BlendFlags = 0;
			bf.SourceConstantAlpha = (byte)alpha; // 濃度を設定
			bf.AlphaFormat = BLENDFUNCTION.AC_SRC_ALPHA;

			final POINT lt = new POINT();
			lt.x = windowRect.left;
			lt.y = windowRect.top;
			final SIZE size = new SIZE();
			size.cx = windowRect.Width();
			size.cy = windowRect.Height();
			final POINT zero = new POINT();
			User32.INSTANCE.UpdateLayeredWindow(
					hWnd, Pointer.NULL,
					lt, size,
					memDC, zero, 0, bf, User32.ULW_ALPHA );

			// ビットマップは元に戻しておく
			Gdi32.INSTANCE.SelectObject(memDC, oldBmp);

			Gdi32.INSTANCE.DeleteDC(memDC);
		}

	}

	/**
	 * 表示する画像.
	 */
	private WindowsNativeImage image;

	/**
	 * 表示濃度. 0 = まったく表示しない、255 = 完全に表示する.
	 */
	private int alpha = 255;

	@Override
	public String toString() {
		return "LayeredWindow[hashCode="+hashCode()+",bounds="+getBounds()+"]";
	}

	@Override
	public void paint(final Graphics g) {
		if (getImage() != null) {
			// JNI を使用してα値つき画像を描画する.
			paint(getImage().getHandle(), getAlpha());
		}
	}

	private WindowsNativeImage getImage() {
		return this.image;
	}

	public void setImage(final NativeImage image) {
		this.image = (WindowsNativeImage)image;
	}

	public int getAlpha() {
		return this.alpha;
	}

	public void setAlpha(final int alpha) {
		this.alpha = alpha;
	}

	public void updateImage() {
		repaint();
	}

}
