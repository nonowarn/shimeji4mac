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
 * Java Ç≈ÇÕéÊìæÇ™ìÔÇµÇ¢ä¬ã´èÓïÒÇAppleScriptÇégópÇµÇƒéÊìæÇ∑ÇÈ.
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

  private static String getFrontmostAppRectScript() {
    return "tell application \"System Events\"\n" +
           "  set appName to name of first item of (processes whose frontmost is true)\n" +
           "end tell\n" +
           "return bounds of first window of application appName";
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

  private void updateFrontmostWindow() {
    final Rectangle frontmostWindowRect = getFrontmostAppRect();

    frontmostWindow.setVisible(
      (frontmostWindowRect != null)
      && frontmostWindowRect.intersects(getScreen().toRectangle()));
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
