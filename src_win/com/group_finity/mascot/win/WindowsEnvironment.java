package com.group_finity.mascot.win;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.environment.Location;
import com.group_finity.mascot.win.jna.Gdi32;
import com.group_finity.mascot.win.jna.RECT;
import com.group_finity.mascot.win.jna.User32;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Java では取得が難しい環境情報をJNIを使用して取得する.
 */
class WindowsEnvironment extends Environment {

	/**
	 * 作業領域を取得する. この領域はディスプレイ領域からタスクバーをのぞいたものになる.
	 *
	 * @return 作業領域.
	 */
	private static Rectangle getWorkAreaRect() {
		final RECT rect = new RECT();
		User32.INSTANCE.SystemParametersInfoW(User32.SPI_GETWORKAREA, 0, rect, 0);
		return new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
	}

	private static HashMap<Pointer, Boolean> ieCache = new LinkedHashMap<Pointer, Boolean>();

	private static boolean isIE(final Pointer ie) {

		final Boolean cache = ieCache.get(ie);
		if (cache != null) {
			return cache;
		}

		// ウィンドウタイトルで IE かどうか判断する
		final char[] title = new char[1024];

		final int titleLength = User32.INSTANCE.GetWindowTextW(ie, title, 1024);

		if (new String(title, 0, titleLength).contains("Internet Explorer")) {
			ieCache.put(ie, true);
			return true;
		}

		if (new String(title, 0, titleLength).contains("Google Chrome")) {
			ieCache.put(ie, true);
			return true;
		}

		if (new String(title, 0, titleLength).contains("Mozilla Firefox")) {
			ieCache.put(ie, true);
			return true;
		}


		// ウィンドウクラウスで MSNメッセンジャか判断する
		final char[] className = new char[1024];

		final int classNameLength = User32.INSTANCE.GetClassNameW(ie, className, 1024);

		if (new String(className, 0, classNameLength).contains("IMWindowClass")) {
			ieCache.put(ie, true);
			return true;
		}
		if (new String(className, 0, classNameLength).contains("SleipnirMainWindow")) {
			ieCache.put(ie, true);
			return true;
		}
		if (new String(className, 0, classNameLength).contains("OperaWindowClass")) {
			ieCache.put(ie, true);
			return true;
		}

		ieCache.put(ie, false);
		return false;
	}

	static int f;

	private static Pointer findActiveIE() {

		Pointer ie = User32.INSTANCE.GetWindow(User32.INSTANCE.GetForegroundWindow(), User32.GW_HWNDFIRST);

		while (User32.INSTANCE.IsWindow(ie) != 0) {

			if ( User32.INSTANCE.IsWindowVisible(ie)!=0) {
				if ((User32.INSTANCE.GetWindowLongW(ie, User32.GWL_STYLE) & User32.WS_MAXIMIZE) != 0) {
					// 最大化されているウィンドウが見つかったので中止
					return null;
				}

				if (isIE(ie) && (User32.INSTANCE.IsIconic(ie) == 0)) {
					// IE が見つかった
					break;
				}
			}

			ie = User32.INSTANCE.GetWindow(ie, User32.GW_HWNDNEXT);

		}

		if (User32.INSTANCE.IsWindow(ie) == 0) {
			// 見つからなかった
			return null;
		}

		return ie;
	}

	/**
	 * アクティブなIEの領域を取得する.
	 *
	 * @return アクティブなIEの領域. 見つけられなかったときは null.
	 */
	private static Rectangle getActiveIERect() {

		final Pointer ie = findActiveIE();

		// IE の矩形を取得
		final RECT out = new RECT();
		User32.INSTANCE.GetWindowRect(ie, out);
		final RECT in = new RECT();
		if (getWindowRgnBox(ie, in) == User32.ERROR) {
			in.left = 0;
			in.top = 0;
			in.right = out.right - out.left;
			in.bottom = out.bottom - out.top;
		}

		// Rectangle オブジェクトを作って返す
		return new Rectangle(out.left + in.left, out.top + in.top, in.Width(), in.Height());
	}

	private static int getWindowRgnBox(final Pointer window, final RECT rect) {

		Pointer hRgn = Gdi32.INSTANCE.CreateRectRgn(0, 0, 0, 0);
		try {
			if ( User32.INSTANCE.GetWindowRgn(window, hRgn)==User32.ERROR ) {
				return User32.ERROR;
			}
			Gdi32.INSTANCE.GetRgnBox(hRgn, rect);
			return 1;
		}finally {
			Gdi32.INSTANCE.DeleteObject(hRgn);
		}
	}

	private static boolean moveIE(final Pointer ie, final Rectangle rect) {

		if (ie == null) {
			return false;
		}

		// IE の矩形を取得
		final RECT out = new RECT();
		User32.INSTANCE.GetWindowRect(ie, out);
		final RECT in = new RECT();
		if (getWindowRgnBox(ie, in) == User32.ERROR) {
			in.left = 0;
			in.top = 0;
			in.right = out.right - out.left;
			in.bottom = out.bottom - out.top;
		}

		User32.INSTANCE.MoveWindow(ie, rect.x - in.left, rect.y - in.top, rect.width + out.Width() - in.Width(),
				rect.height + out.Height() - in.Height(), 1);

		return true;
	}

	private static void restoreAllIEs() {

		// ワークエリアの矩形を取得
		final RECT workArea = new RECT();
		User32.INSTANCE.SystemParametersInfoW(User32.SPI_GETWORKAREA, 0, workArea, 0);

		Pointer ie = User32.INSTANCE.GetWindow(User32.INSTANCE.GetForegroundWindow(), User32.GW_HWNDFIRST);

		while (User32.INSTANCE.IsWindow(ie) != 0) {
			if (isIE(ie)) {
				// IE が見つかった

				// IE の矩形を取得
				final RECT rect = new RECT();
				User32.INSTANCE.GetWindowRect(ie, rect);
				if ((rect.right <= workArea.left + 100) || (rect.bottom <= workArea.top + 100)
						|| (rect.left >= workArea.right - 100) || (rect.top >= workArea.bottom - 100)) {
					// 位置がおかしいような気がする

					rect.OffsetRect(workArea.left + 100 - rect.left, workArea.top + 100 - rect.top);
					User32.INSTANCE.MoveWindow(ie, rect.left, rect.top, rect.Width(), rect.Height(), 1);
					User32.INSTANCE.BringWindowToTop(ie);
				}

				break;
			}

			ie = User32.INSTANCE.GetWindow(ie, User32.GW_HWNDNEXT);
		}
	}

	public static Area workArea = new Area();

	public static Area activeIE = new Area();

	@Override
	public void tick() {
		super.tick();
		workArea.set(getWorkAreaRect());

		final Rectangle ieRect = getActiveIERect();
		activeIE.setVisible((ieRect != null) && ieRect.intersects(getScreen().toRectangle()));
		activeIE.set(ieRect == null ? new Rectangle(-1, -1, 0, 0) : ieRect);

	}

	@Override
	public void moveActiveIE(final Point point) {
		moveIE(findActiveIE(), new Rectangle(point.x, point.y, activeIE.getWidth(), activeIE.getHeight()));
	}

	@Override
	public void restoreIE() {
		restoreAllIEs();
	}

	@Override
	public Area getWorkArea() {
		return workArea;
	}

	@Override
	public Area getActiveIE() {
		return activeIE;
	}

}
