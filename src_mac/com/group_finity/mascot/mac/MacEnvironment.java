package com.group_finity.mascot.mac;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;

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

  private static ScriptEngine engine = new ScriptEngineManager().getEngineByName("AppleScript");

  private static Rectangle getFrontmostAppRect() {
    ArrayList<Long> bounds = null;

    try {
      bounds = (ArrayList<Long>) engine.eval(getFrontmostAppRectScript());
    } catch (ScriptException e) {}

    if (bounds != null && bounds.size() == 4) {
      return rectangleFromBounds(bounds);
    } else {
      return null;
    }
  }

	private static void moveFrontmostWindow(final Point point) {
		try {
			engine.eval(moveFrontmostWindowScript(point));
		} catch (ScriptException e) {}
	}

	private static ArrayList<Long> getWindowVisibleBounds() {
		return null;
	}

  private static String getFrontmostAppRectScript() {
    return "tell application \"System Events\"\n" +
           "  set appName to name of first item of (processes whose frontmost is true)\n" +
           "end tell\n" +
           "return bounds of first window of application appName";
  }

	private static String moveFrontmostWindowScript(final Point point) {
    return
			"tell application \"System Events\"\n" +
			"  set appName to name of first item of (processes whose frontmost is true)\n" +
			"end tell\n" +
			"set w to first window of application appName\n" +
			"set x to " + Double.toString(point.getX()) + "\n" +
			"set y to " + Double.toString(point.getY()) + "\n" +
			"set {x1, y1, x2, y2} to bounds of w\n" +
			"set bounds of w to {x, y, x+(x2-x1), y+(y2-y1)}";
	}

	private static String getWindowVisibleBoundsScript() {
		return
			"tell application \"Finder\" to set {x1, y1, x2, y2} to bounds of window of Desktop\n" +
			"tell application \"System Events\"\n" +
			"  tell process \"Dock\"\n" +
			"    set {dw, dh} to size in list 1\n" +
			"  end tell\n" +
			"  tell dock preferences\n" +
			"    set edge to screen edge as string\n" +
			"  end tell\n" +
			"end tell\n" +
			"if edge = \"bottom\" then\n" +
			"  set y2 to y2 - dh\n" +
			"else if edge = \"right\" then\n" +
			"  set x2 to x2 - dw\n" +
			"else if edge = \"left\" then\n" +
			"  set x1 to x1 + dw\n" +
			"end if\n" +
			"{x1+1, y1+22, x2-1, y2-22}";
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

	/**
		画面内でウィンドウを移動しても押し返されない範囲を Rectangle で返す。
		Mac では、ウィンドウを完全に画面外に移動させようとすると、
		ウィンドウが画面内に押し返されてしまう。
	 */
	private static Rectangle getWindowVisibleArea() {
		ArrayList<Long> bounds = null;

		try {
			bounds = (ArrayList<Long>) engine.eval(getWindowVisibleBoundsScript());
		} catch (ScriptException e) {}

		if (bounds != null && bounds.size() == 4) {
			return rectangleFromBounds(bounds);
		} else {
			return null;
		}
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
		if (pX < minX) {
			pX = minX;
		} else if (pX > maxX) {
			pX = maxX;
		}

		// Y方向の折り返し
		if (pY < minY) {
			pY = minY;
		} else if (pY > maxY) {
			pY = maxY;
		}

		point.setLocation(pX, pY);
		moveFrontmostWindow(point);
	}

	@Override
	public void restoreIE() {
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
