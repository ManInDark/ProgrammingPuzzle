package tiles;

import abstractclasses.Tile;
import world.Images;
import logic.Layers;

public class Default extends Tile {

	public Default() {
		super(FLOORHEIGHT, NOTANIMATED, 0, 0, null);
		setDescription("Default Tile (for testing purposes)");
	}

	@Override
	public void loadAnimation() {
		setImage(Layers.Floor, Images.loadLayerPicture("Default", Layers.Floor));
	}
}