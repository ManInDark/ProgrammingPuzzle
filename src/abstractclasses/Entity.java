
package abstractclasses;


import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import Enums.Animations;
import Enums.Heights;
import Enums.Rotations;
import logic.Constants;
import logic.Layers;
import tasks.ChangeEntityImageTask;
import tasks.MoveEntityTask;
import world.Images;
import world.World;


/**
 * Die Grundklasse aller Entities. Um als ein Entity klassifiziert zu werden, muss das
 * Objekt / Lebewesen sich bewegen k�nnen.
 */

public abstract class Entity implements Constants {

	private Heights height;
	private int relativedrawX;
	private int relativedrawY;

	private Point pixelposition;
	protected Rotations rotation;

	private int ticksperimagechange;

	public HashMap<Rotations,HashMap<Animations,ArrayList<String>>> animations = new HashMap<>();

	protected Animations actualanimation = null;
	protected int actualanimationcounter = 0;
	protected ChangeEntityImageTask task = null;

	protected MoveEntityTask movetask = null;

	private World world;

	protected Entity(Point position) {
		this(position,0,0);
	}

	protected Entity(Point position,int relativedrawX,int relativedrawY) {
		this(position,relativedrawX,relativedrawY,Rotations.norotation,DEFAULTIMAGECHANGETICKDELAY);
	}

	protected Entity(Point position,int relativedrawX,int relativedrawY,Rotations rotation,int ticksperimagechange) {
		setHeight(Heights.UNPASSABLE);
		this.relativedrawX = relativedrawX;
		this.relativedrawY = relativedrawY;
		setPosition(position);
		setRotation(rotation);
		this.ticksperimagechange = ticksperimagechange;

		loadAnimations();
		triggerAnimation(Animations.deactivatedanimation);

	}

	public abstract void loadAnimations();

	public abstract void getdata(LinkedHashMap<String,String> List);

	public void setPixelPosition(Point position) {
		this.pixelposition = position;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public void setHeight(Heights height) {
		this.height = height;
	}

	public void setRotation(Rotations rotation) {
		this.rotation = rotation;
	}

	public Point getPixelPosition() {
		return pixelposition;
	}

	public void setPosition(Point position) {
		this.pixelposition = new Point(position.x * DEFAULTIMAGEWIDHTHEIGHT,position.y * DEFAULTIMAGEWIDHTHEIGHT);
	}

	public Point getPosition() {
		return new Point(pixelposition.x / DEFAULTIMAGEWIDHTHEIGHT,pixelposition.y / DEFAULTIMAGEWIDHTHEIGHT);
	}

	public int getRelativedrawX() {
		return relativedrawX;
	}

	public int getRelativedrawY() {
		return relativedrawY;
	}

	public World getWorld() {
		return world;
	}

	public void nextimage(Layers layer) {
		actualanimationcounter++;
	}

	public BufferedImage getImage() {
		try {
			return Images.getImage(animations.get(rotation).get(actualanimation).get(actualanimationcounter));
		} catch (NullPointerException e) {
		}
		return null;
	}

	public Rotations getRotation() {
		return rotation;
	}

	public Heights getHeight() {
		return height;
	}

	public void onInteract(Entity entity) {
	}

	public void nextimage() {
		actualanimationcounter++;
	}

	public void startmove(int ticksperimagechange,Rotations rotation) {
		if (movetask == null || movetask.getEnded())
			movetask = new MoveEntityTask(ticksperimagechange,this,rotation);
	}

	public void move(Rotations direction) {
		switch (direction) {
			case down:
				pixelposition.y++;
			break;
			case left:
				pixelposition.x--;
			break;
			case right:
				pixelposition.x++;
			break;
			case up:
				pixelposition.y--;
			break;
			case norotation:
			break;
		}
	}

	public void triggerAnimation(Animations animation) {
		actualanimation = animation;
		actualanimationcounter = 0;
		try {
			task.end();
		} catch (NullPointerException e) {
		}
		task = new ChangeEntityImageTask(ticksperimagechange,this,animations.get(rotation).get(animation).size() - 1,
				animation);
	}

}
