package ObjectSearch;

import org.jfree.chart.block.Block;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import ObjectSearch.Odometer;
import ObjectSearch.USLocalizer;

public class Lab5 {
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	
	private static final Port usPort = LocalEV3.get().getPort("S3");
	private static final Port colorPort = LocalEV3.get().getPort("S4");
	private TextLCD t;

	public static void main(String[] args) {
		int buttonChoice;

		@SuppressWarnings("resource")
		// Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance"); // colorValue
																// provides
																// samples from
																// this instance
		float[] usData = new float[usValue.sampleSize()]; // colorData is the
															// buffer in which
															// data are returned

		@SuppressWarnings("resource")
		EV3ColorSensor colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorValue = colorSensor.getRGBMode(); // colorValue
																// provides
																// samples from
																// this instance
		float[] colorData = new float[colorValue.sampleSize()]; // colorData is
																// the buffer in
																// which data
																// are returned

		final TextLCD t = LocalEV3.get().getTextLCD();
		ObjectDistance objectDistance = new ObjectDistance(usSensor, usData);
		ObjectColor objectColor = new ObjectColor(colorValue, colorData, t);

		do {
			// clear the display
			t.clear();

			t.drawString(" < Left | Right > ", 0, 0);
			t.drawString("        |         ", 0, 1);
			t.drawString(" Detect | Object  ", 0, 2);
			t.drawString(" blocks | search  ", 0, 3);
			t.drawString("        |         ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			t.clear();
			while (true) {
				if (objectDistance.isClose()) {
					objectColor.isBlock();
				} else {
					System.out.println("Object is too far");
				}
			}

		} else {
			Odometer odo = new Odometer(leftMotor, rightMotor, 30, true);

			USLocalizer usl = new USLocalizer(odo, usValue, usData, USLocalizer.LocalizationType.RISING_EDGE);
			//usl.doLocalization();
			
			//while (Button.waitForAnyPress() != Button.ID_ESCAPE);
			Mapping map = new Mapping(odo, usSensor, usData);
			map.doMap();


		}
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);

	}
}
