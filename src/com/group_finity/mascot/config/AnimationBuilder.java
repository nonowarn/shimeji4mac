package com.group_finity.mascot.config;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.animation.Pose;
import com.group_finity.mascot.exception.AnimationInstantiationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.image.ImagePair;
import com.group_finity.mascot.image.ImagePairLoader;
import com.group_finity.mascot.script.Variable;

public class AnimationBuilder {

	private static final Logger log = Logger.getLogger(AnimationBuilder.class.getName());

	private final String condition;

	private final List<Pose> poses = new ArrayList<Pose>();

	public AnimationBuilder(final Entry animationNode) throws IOException {
		this.condition = animationNode.getAttribute("条件") == null ? "true" : animationNode.getAttribute("条件");

		log.log(Level.INFO, "アニメーション読み込み開始");

		for (final Entry frameNode : animationNode.getChildren()) {

			this.getPoses().add(loadPose(frameNode));
		}

		log.log(Level.INFO, "アニメーション読み込み完了");
	}

	private Pose loadPose(final Entry frameNode) throws IOException {

		final String imageText = frameNode.getAttribute("画像");
		final String anchorText = frameNode.getAttribute("基準座標");
		final String moveText = frameNode.getAttribute("移動速度");
		final String durationText = frameNode.getAttribute("長さ");

		final String[] anchorCoordinates = anchorText.split(",");
		final Point anchor = new Point(Integer.parseInt(anchorCoordinates[0]), Integer.parseInt(anchorCoordinates[1]));

		final ImagePair image = ImagePairLoader.load(imageText, anchor);

		final String[] moveCoordinates = moveText.split(",");
		final Point move = new Point(Integer.parseInt(moveCoordinates[0]), Integer.parseInt(moveCoordinates[1]));

		final int duration = Integer.parseInt(durationText);

		final Pose pose = new Pose(image, move.x, move.y, duration);

		log.log(Level.INFO, "姿勢読み込み({0})", pose);

		return pose;

	}

	public Animation buildAnimation() throws AnimationInstantiationException {
		try {
			return new Animation(Variable.parse(this.getCondition()), this.getPoses().toArray(new Pose[0]));
		} catch (final VariableException e) {
			throw new AnimationInstantiationException("条件の評価に失敗しました", e);
		}
	}

	private List<Pose> getPoses() {
		return this.poses;
	}

	private String getCondition() {
		return this.condition;
	}
}
