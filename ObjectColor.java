package ObjectSearch;

import lejos.hardware.lcd.TextLCD;
import lejos.robotics.SampleProvider;

public class ObjectColor {
	private SampleProvider colorValue;
	private float[] colorData;
	private TextLCD t;

	public ObjectColor(SampleProvider colorValue, float[] colorData, TextLCD t) { // TextLCD
																					// t){
		this.colorValue = colorValue;
		this.colorData = colorData;
		this.t = t;
	}

	public void isBlock() {

		getColor();

		if (colorData[1] > colorData[0] && colorData[1] > colorData[2]) {
			System.out.println("Block");
		} else {
			System.out.println("Not a block");

		}
		// }
	}

	private void getColor() {
		colorValue.fetchSample(colorData, 0);
	}
	


}
