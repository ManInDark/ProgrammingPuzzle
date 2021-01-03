package start;

import frame.DebuggingWindow;
import frame.Frame;
import logic.Debugger;
import logic.MainControl;

public class start {
	/**
	 * Die Tats�chliche main Methode
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		MainControl.initialize();
		Frame.init();
		Debugger.initialize();

		MainControl.start();
		Frame.setVisible();

		MainControl.createWorld();
		//new DebuggingWindow();
	}
}