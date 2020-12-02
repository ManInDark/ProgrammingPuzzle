package frame;

import java.util.ArrayList;

import javax.swing.JLayeredPane;

import abstractclasses.CustomWindow;
import logic.Constants;

public class CustomWindowManager extends JLayeredPane implements Constants {

	CustomWindow fullscreen;
	ArrayList<CustomWindow> windows;

	public CustomWindowManager() {
		windows = new ArrayList<>();
	}

	public void addWindow(CustomWindow window) {
		windows.add(window);
		window.setLayer(nextHighestLayer());
		clean();
	}

	public void removeWindow(CustomWindow newWindow) {
		windows.remove(newWindow);
		clean();
	}

	public void windowToFront(CustomWindow window) {
		if (isFullscreen(window)) {
			window.setLayer(nextHighestLayer());
			clean();
		}
	}

	public void setFullscreen(CustomWindow fullscreen) {
		if (this.fullscreen != null) {
			this.fullscreen.setBounds(DEFAULTX, DEFAULTY, DEFAULTWITH, DEFAULTHEIGHT);
		}
		this.fullscreen = fullscreen;
		if (fullscreen != null)
			fullscreen.setLayer(nextLowestLayer());
		clean();
	}

	public Boolean isFullscreen(CustomWindow test) {
		return fullscreen == null ? false : fullscreen.equals(test);
	}

	private void clean() {
		windows.sort(null);
		Integer actlayer = 0;
		for (CustomWindow window : windows) {
			window.setLayer(actlayer);
			actlayer++;
		}
		addComponents();
	}

	private void addComponents() {
		removeAll();
		for (CustomWindow window : windows) {
			add(window, window.getLayer());
		}
		Frame.repaint();
	}

	private int nextHighestLayer() {
		Integer layer = 0;
		for (CustomWindow window : windows) {
			if (window.getLayer() > layer)
				layer = window.getLayer();
		}
		return ++layer;
	}

	private int nextLowestLayer() {
		Integer layer = nextHighestLayer();
		for (CustomWindow window : windows) {
			if (window.getLayer() < layer)
				layer = window.getLayer();
		}
		return --layer;
	}
}