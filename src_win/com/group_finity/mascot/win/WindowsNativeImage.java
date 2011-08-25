package com.group_finity.mascot.win;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.win.jna.BITMAP;
import com.group_finity.mascot.win.jna.BITMAPINFOHEADER;
import com.group_finity.mascot.win.jna.Gdi32;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * {@link WindowsTranslucentWindow} に使用可能なα値つき画像.
 * 
 * {@link WindowsTranslucentWindow} に使用できるのは Windows ビットマップだけなので、
 * 既存の {@link BufferedImage} から Windows ビットマップにピクセルをコピーする.
 * 
 */
class WindowsNativeImage implements NativeImage {

	/**
	 * Windows ビットマップを作成する.
	 * @param width ビットマップの横幅.
	 * @param height ビットマップの高さ.
	 * @return 作成したビットマップのハンドル.
	 */
	private static Pointer createNative(final int width, final int height) {

		final BITMAPINFOHEADER bmi = new BITMAPINFOHEADER();
		bmi.biSize = 40;
		bmi.biWidth = width;
		bmi.biHeight = height;
		bmi.biPlanes = 1;
		bmi.biBitCount = 32;

		final Pointer hBitmap = Gdi32.INSTANCE.CreateDIBSection(
				Pointer.NULL, bmi, Gdi32.DIB_RGB_COLORS, Pointer.NULL, Pointer.NULL, 0 );

		return hBitmap;
	}

	/**
	 * {@link BufferedImage} の内容をビットマップに反映させる.
	 * @param nativeHandle ビットマップのハンドル.
	 * @param rgb 画像のARGB値.
	 */
	private static void flushNative(final Pointer nativeHandle, final int[] rgb) {

		final BITMAP bmp = new BITMAP();
		Gdi32.INSTANCE.GetObjectW(nativeHandle, 20+Native.POINTER_SIZE, bmp);

		// ピクセルレベルでコピーする.
		final int width = bmp.bmWidth;
		final int height = bmp.bmHeight;
		final int destPitch = ((bmp.bmWidth*bmp.bmBitsPixel)+31)/32*4;
		int destIndex = destPitch*(height-1);
		int srcIndex = 0;
		for( int y = 0; y < height; ++y )
		{
			for( int x = 0; x<width; ++x )
			{
				// UpdateLayeredWindow と Photoshop は相性が悪いらしい
				// UpdateLayeredWindow はRGB値が FFFFFF だとα値を無視してしまうバグがあり、
				// Photoshop はα値が0なところはRGB値を 0 にする特性がある.

				bmp.bmBits.setInt(destIndex + x*4,
					(rgb[srcIndex]&0xFF000000)==0 ? 0 : rgb[srcIndex] );

				++srcIndex;
			}

			destIndex -= destPitch;
		}

	}

	/**
	 * Windows ビットマップを開放する.
	 * @param nativeHandle ビットマップのハンドル.
	 */
	private static void freeNative(final Pointer nativeHandle) {
		Gdi32.INSTANCE.DeleteObject(nativeHandle);
	}

	/**
	 * Java イメージオブジェクト.
	 */
	private final BufferedImage managedImage;

	/**
	 * Windows ビットマップハンドル.
	 */
	private final Pointer nativeHandle;

	/**
	 * ARGB値の転送に使用するバッファ.
	 */
	private final int[] rgb;

	public WindowsNativeImage(final BufferedImage image) {
		this.managedImage = image;
		this.nativeHandle = createNative(this.getManagedImage().getWidth(), this.getManagedImage().getHeight());
		this.rgb = new int[this.getManagedImage().getWidth() * this.getManagedImage().getHeight()];
		update();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		freeNative(this.getNativeHandle());
	}

	/**
	 * 画像への変更を Windows ビットマップに反映させる.
	 */
	public void update() {

		this.getManagedImage().getRGB(0, 0, this.getManagedImage().getWidth(), this.getManagedImage().getHeight(), this.getRgb(), 0,
				this.getManagedImage().getWidth());

		flushNative(this.getNativeHandle(), this.getRgb());

	}

	public void flush() {
		this.getManagedImage().flush();
	}

	public Pointer getHandle() {
		return this.getNativeHandle();
	}

	public Graphics getGraphics() {
		return this.getManagedImage().createGraphics();
	}

	public int getHeight() {
		return this.getManagedImage().getHeight();
	}

	public int getWidth() {
		return this.getManagedImage().getWidth();
	}

	public int getHeight(final ImageObserver observer) {
		return this.getManagedImage().getHeight(observer);
	}

	public Object getProperty(final String name, final ImageObserver observer) {
		return this.getManagedImage().getProperty(name, observer);
	}

	public ImageProducer getSource() {
		return this.getManagedImage().getSource();
	}

	public int getWidth(final ImageObserver observer) {
		return this.getManagedImage().getWidth(observer);
	}

	private BufferedImage getManagedImage() {
		return this.managedImage;
	}

	private Pointer getNativeHandle() {
		return this.nativeHandle;
	}

	private int[] getRgb() {
		return this.rgb;
	}

}
