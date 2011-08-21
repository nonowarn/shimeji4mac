package com.group_finity.mascot.mac;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashSet;
import java.lang.management.ManagementFactory;

import com.sun.jna.Pointer;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.mac.jna.Carbon;
import com.group_finity.mascot.mac.jna.ProcessSerialNumber;
import com.group_finity.mascot.mac.jna.AXValueRef;
import com.group_finity.mascot.mac.jna.AXUIElementRef;
import com.group_finity.mascot.mac.jna.CFTypeRef;
import com.group_finity.mascot.mac.jna.CGPoint;
import com.group_finity.mascot.mac.jna.CGSize;
import com.group_finity.mascot.mac.jna.CFStringRef;
import com.group_finity.mascot.mac.jna.CFNumberRef;
import com.group_finity.mascot.mac.jna.CFArrayRef;

/**
 * Java では取得が難しい環境情報をAppleScriptを使用して取得する.
 */
class MacEnvironment extends Environment {

  /**
    In mac environment, I think getting the frontmost window is easier
    than specific applications' window (such as Chrome).

    So, In this class, I implement getting the frontmost window, and I
    use "frontmostWindow" for alias of "activeIE".
   */
	private static Area activeIE = new Area();
  private static Area frontmostWindow = activeIE;

	private static final int screenWidth =
		(int) Math.round(Toolkit.getDefaultToolkit().getScreenSize().getWidth());
	private static final int screenHeight =
		(int) Math.round(Toolkit.getDefaultToolkit().getScreenSize().getHeight());

	private static Carbon carbon = Carbon.INSTANCE;

	// Mac では、ManagementFactory.getRuntimeMXBean().getName()で
	// PID@マシン名 の文字列が返ってくる
	private static long myPID =
		Long.parseLong(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);

	private static long currentPID = myPID;

	private static HashSet<Long> touchedProcesses = new HashSet<Long>();

	static final CFStringRef
  	kAXPosition = createCFString("AXPosition"),
		kAXSize = createCFString("AXSize"),
		kAXFocusedWindow = createCFString("AXFocusedWindow"),
		kDock = createCFString("com.apple.Dock"),
		kTileSize = createCFString("tilesize"),
		kOrientation = createCFString("orientation"),
		kAXChildren = createCFString("AXChildren");

  private static Rectangle getFrontmostAppRect() {
		Rectangle ret;
		long pid = getFrontmostAppsPID();

		AXUIElementRef application =
			carbon.AXUIElementCreateApplication(pid);

		PointerByReference windowp = new PointerByReference();

		// XXX: ここ以外でもエラーチェックは必要?
		if (carbon.AXUIElementCopyAttributeValue(
					application, kAXFocusedWindow, windowp) == carbon.kAXErrorSuccess) {
			AXUIElementRef window = new AXUIElementRef();
			window.setPointer(windowp.getValue());

			CGPoint position = getPositionOfWindow(window);
			CGSize size = getSizeOfWindow(window);

			ret = new Rectangle(
				position.getX(), position.getY(), size.getWidth(), size.getHeight());
		} else {
			ret = null;
		}

		carbon.CFRelease(application);
		return ret;
  }

	private static long getFrontmostAppsPID() {
		ProcessSerialNumber front_process_psn = new ProcessSerialNumber();
		LongByReference front_process_pidp = new LongByReference();

		carbon.GetFrontProcess(front_process_psn);
		carbon.GetProcessPID(front_process_psn, front_process_pidp);

		long newPID = front_process_pidp.getValue();

		if (newPID != myPID) {
			currentPID = newPID;
			touchedProcesses.add(newPID);
		}

		return currentPID;
	}

	private static CGPoint getPositionOfWindow(AXUIElementRef window) {
		CGPoint position = new CGPoint();
		AXValueRef axvalue = new AXValueRef();
		PointerByReference valuep = new PointerByReference();

		carbon.AXUIElementCopyAttributeValue(window, kAXPosition, valuep);
		axvalue.setPointer(valuep.getValue());
		carbon.AXValueGetValue(axvalue, carbon.kAXValueCGPointType, position.getPointer());
		position.read();

		return position;
	}

	private static CGSize getSizeOfWindow(AXUIElementRef window) {
		CGSize size = new CGSize();
		AXValueRef axvalue = new AXValueRef();
		PointerByReference valuep = new PointerByReference();

		carbon.AXUIElementCopyAttributeValue(window, kAXSize, valuep);
		axvalue.setPointer(valuep.getValue());
		carbon.AXValueGetValue(axvalue, carbon.kAXValueCGSizeType, size.getPointer());
		size.read();

		return size;
	}

	private static void moveFrontmostWindow(final Point point) {
		AXUIElementRef application =
			carbon.AXUIElementCreateApplication(currentPID);

		PointerByReference windowp = new PointerByReference();

		if (carbon.AXUIElementCopyAttributeValue(
					application, kAXFocusedWindow, windowp) == carbon.kAXErrorSuccess) {
			AXUIElementRef window = new AXUIElementRef();
			window.setPointer(windowp.getValue());

			CGPoint position = new CGPoint((double) point.x, (double) point.y);
			position.write();
			AXValueRef axvalue = carbon.AXValueCreate(carbon.kAXValueCGPointType, position.getPointer());
			carbon.AXUIElementSetAttributeValue(
				window, kAXPosition, axvalue);
		}

		carbon.CFRelease(application);
	}

	private static void restoreWindowsNotIn(final Rectangle rect) {
		Rectangle visibleArea = getWindowVisibleArea();
		for (long pid : touchedProcesses) {
			AXUIElementRef application =
				carbon.AXUIElementCreateApplication(pid);

			for (AXUIElementRef window : getWindowsOf(application)) {
				Rectangle windowRect = getWindowRect(window);
				if (!visibleArea.intersects(windowRect)) {
					moveWindow(window, 0, 0);
				}
			}

			carbon.CFRelease(application);
		}
	}

	private static ArrayList<AXUIElementRef> getWindowsOf(AXUIElementRef application) {
		PointerByReference axWindowsp = new PointerByReference();
		ArrayList<AXUIElementRef> ret = new ArrayList<AXUIElementRef>();

		carbon.AXUIElementCopyAttributeValue(application, kAXChildren, axWindowsp);

		if (axWindowsp.getValue() == Pointer.NULL) {
			return ret;
		}

		CFArrayRef cfWindows = new CFArrayRef();
		cfWindows.setPointer(axWindowsp.getValue());

		for (long i = 0, l = carbon.CFArrayGetCount(cfWindows); i < l; ++i) {
			Pointer p = carbon.CFArrayGetValueAtIndex(cfWindows, i);
			AXUIElementRef el = new AXUIElementRef();
			el.setPointer(p);
			ret.add(el);
		}

		return ret;
	}

	private static Rectangle getWindowRect(AXUIElementRef window) {
		CGPoint pos = getPositionOfWindow(window);
		CGSize size = getSizeOfWindow(window);
		int x = pos.getX(), y = pos.getY();
		return new Rectangle(x, y, x + size.getWidth(), y + size.getHeight());
	}

	private static void moveWindow(AXUIElementRef window, int x, int y) {
		CGPoint position = new CGPoint((double) x, (double) y);
		position.write();
		AXValueRef axvalue = carbon.AXValueCreate(carbon.kAXValueCGPointType, position.getPointer());
		carbon.AXUIElementSetAttributeValue(
			window, kAXPosition, axvalue);
	}

  private static Rectangle rectangleFromBounds(ArrayList<Long> bounds) {
    int
      leftTopX     = bounds.get(0).intValue(), leftTopY     = bounds.get(1).intValue(),
      rightBottomX = bounds.get(2).intValue(), rightBottomY = bounds.get(3).intValue();
    return new Rectangle(
      leftTopX,
      leftTopY,
      rightBottomX - leftTopX,
      rightBottomY - leftTopY);
  }

	private static CFStringRef createCFString(String s) {
		return Carbon
			.INSTANCE
			.CFStringCreateWithCharacters(null, s.toCharArray(), s.length());
	}

	private static int getScreenWidth() {
		return screenWidth;
	}

	private static int getScreenHeight() {
		return screenHeight;
	}

	/**
		 min < max のとき、
		 min <= x <= max ならば x を返す
		 x < min ならば min を返す
		 x > max ならば max を返す
	 */
	private static double betweenOrLimit(double x, double min, double max) {
		return Math.min(Math.max(x, min), max);
	}

	/**
		画面内でウィンドウを移動しても押し返されない範囲を Rectangle で返す。
		Mac では、ウィンドウを完全に画面外に移動させようとすると、
		ウィンドウが画面内に押し返されてしまう。
	 */
	private static Rectangle getWindowVisibleArea() {
		final int menuBarHeight = 22;
		int x = 1, y = menuBarHeight,
			width = getScreenWidth() - 2, // 0-origin なので
			height = getScreenHeight() - menuBarHeight;

		refreshDockState();
		final String orientation = getDockOrientation();
		final int tilesize = getDockTileSize();

		if ("bottom".equals(orientation)) {
			height -= tilesize;
		} else if ("right".equals(orientation)) {
			width -= tilesize;
		}	else /* if ("left".equals(orientation)) */ {
			x += tilesize;
		}

		Rectangle r = new Rectangle(x, y, width, height);
		return r;
	}

	private static String getDockOrientation() {
		CFTypeRef orientationRef =
			carbon.CFPreferencesCopyValue(kOrientation, kDock, carbon.kCurrentUser, carbon.kAnyHost);
		final int bufsize = 64;
		Memory buf = new Memory(64);
		carbon.CFStringGetCString(orientationRef, buf, bufsize, carbon.CFStringGetSystemEncoding());
		carbon.CFRelease(orientationRef);
		String ret = buf.getString(0, false);
		buf.clear();
		return ret;
	}

	private static int getDockTileSize() {
		CFTypeRef tilesizeRef =
			carbon.CFPreferencesCopyValue(kTileSize, kDock, carbon.kCurrentUser, carbon.kAnyHost);
		IntByReference intRef = new IntByReference();
		carbon.CFNumberGetValue(tilesizeRef, carbon.kCFNumberInt32Type, intRef);
		carbon.CFRelease(tilesizeRef);
		return intRef.getValue();
	}

	private static void refreshDockState() {
		carbon.CFPreferencesAppSynchronize(kDock);
	}

  private void updateFrontmostWindow() {
    final Rectangle frontmostWindowRect = getFrontmostAppRect();

    frontmostWindow.setVisible(
      (frontmostWindowRect != null)
      && frontmostWindowRect.intersects(getWindowVisibleArea()));
    frontmostWindow.set(
      frontmostWindowRect == null ? new Rectangle(-1, -1, 0, 0) : frontmostWindowRect);
  }

	@Override
	public void tick() {
		super.tick();
    this.updateFrontmostWindow();
	}

	@Override
	public void moveActiveIE(final Point point) {
		/**
			前述のとおり、完全に画面外へ移動しようとすると押し返されるため、
			そのような位置の指定に対しては可能なかぎりの移動に切り替える。
		 */
		final Rectangle
			visibleRect = getWindowVisibleArea(),
			windowRect  = getFrontmostAppRect();

		final double
			minX = visibleRect.getMinX() - windowRect.getWidth(), // 左方向の折り返し座標
			maxX = visibleRect.getMaxX(),													// 右方向の折り返し座標
			minY = visibleRect.getMinY(),													// 上方向の折り返し座標
																														// (メニューバーより上へは移動できない)
			maxY = visibleRect.getMaxY();													// 下方向の折り返し座標

		double
			pX   = point.getX(),
			pY   = point.getY();

		// X方向の折り返し
		pX = betweenOrLimit(pX, minX, maxX);

		// Y方向の折り返し
		pY = betweenOrLimit(pY, minY, maxY);

		point.setLocation(pX, pY);
		moveFrontmostWindow(point);
	}

	@Override
	public void restoreIE() {
		final Rectangle visibleRect = getWindowVisibleArea();
		restoreWindowsNotIn(visibleRect);
	}

	@Override
	public Area getWorkArea() {
		return getScreen();
	}

	@Override
	public Area getActiveIE() {
		return this.activeIE;
	}

}
