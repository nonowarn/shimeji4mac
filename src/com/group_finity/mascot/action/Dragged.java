package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.environment.Location;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.awt.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * ドラッグされてるアクション.
 */
public class Dragged extends ActionBase {

	private static final Logger log = Logger.getLogger(Dragged.class.getName());

	private static final String VARIABLE_FOOTX = "footX";

	private double footX;

	private double footDx;

	private int timeToRegist;

    //
	private double displacementX = 0.0;

    private double displacementY = 0.0;

	public Dragged(final List<Animation> animations, final VariableMap params) {
		super(animations, params);
	}

	@Override
	public void init(final Mascot mascot) throws VariableException {
		super.init(mascot);

		setFootX(getEnvironment().getCursor().getX());
		setTimeToRegist(250);

	}

	@Override
	public boolean hasNext() throws VariableException {

		final boolean intime = this.getTime() < this.getTimeToRegist();
		final boolean lukewarm = Math.random() >= 0.1;

		return super.hasNext() && (intime || lukewarm);

	}

	@Override
	protected void tick() throws LostGroundException, VariableException {

		getMascot().setLookRight(false);

		final Location cursor = getEnvironment().getCursor();

		if (Math.abs(cursor.getX() - getMascot().getAnchor().x) >= 5) {
			this.setTime(0);
		}

		final int newX = cursor.getX();

		this.setFootDx((this.getFootDx() + ((newX - this.getFootX()) * 0.1)) * 0.8);
		this.setFootX(this.getFootX() + this.getFootDx());

		// アニメーションの条件に足の位置が含まれる可能性があるので変数に入れておく
		putVariable(VARIABLE_FOOTX, this.getFootX());

		// アニメーションさせる
		getAnimation().next(getMascot(), getTime());

		// マスコットの位置をマウスカーソルに合わせる
        //	getMascot().setAnchor(new Point(cursor.getX(), cursor.getY() + 120));

        // if the displacement is 0, that means you need to initialize it or recalculate it
        // you will keep this value until a drag action finished
        if (this.displacementX == 0 || this.displacementY == 0) {
            this.displacementX = cursor.getX() - getMascot().getAnchor().x;
            this.displacementY = cursor.getY() - getMascot().getAnchor().y;
        }

        // the anchor should be the cursor's position subtract the distance between cursor and original anchor
        getMascot().setAnchor(new Point((int)(cursor.getX() - this.displacementX), (int)(cursor.getY() - this.displacementY)));
	}

	public void setTimeToRegist(final int timeToRegist) {
		this.timeToRegist = timeToRegist;
	}

	private int getTimeToRegist() {
		return this.timeToRegist;
	}

	private void setFootX(final double footX) {
		this.footX = footX;
	}

	private double getFootX() {
		return this.footX;
	}

	private void setFootDx(final double footDx) {
		this.footDx = footDx;
	}

	private double getFootDx() {
		return this.footDx;
	}
}
