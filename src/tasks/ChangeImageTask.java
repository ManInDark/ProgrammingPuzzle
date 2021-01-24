package tasks;


import abstractclasses.Entity;
import abstractclasses.Task;
import abstractclasses.Tile;
import logic.Animations;
import logic.Layers;


public class ChangeImageTask extends Task {

	private Tile tile;
	private Entity entity;
	private Layers layer;

	public ChangeImageTask(Integer tickDifference,Tile tile,Integer loop,Layers layer) {
		super(tickDifference,loop);
		this.tile = tile;
		this.layer = layer;
	}

	public ChangeImageTask(Integer tickDifference,Entity entity,Integer loop) {
		super(tickDifference,loop);
		this.entity = entity;
	}

	@Override
	public void runCode() { 
		if (tile != null) {
			tile.nextimage(layer);
			tile.triggerimageupdate();
			UpadateWorldTask.UpdateWorld(tile.getWorld().getWindow(),tile);
		} else {
			entity.nextimage(layer);
			entity.triggerimageupdate();
			//TODO
		}
	}

	@Override
	public void onEnd() {
		if (tile != null)
			tile.triggerAnimation(Animations.defaultanimation);
		else
			entity.triggerAnimation(Animations.defaultanimation);
	}

}
