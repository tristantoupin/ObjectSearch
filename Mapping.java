package ObjectSearch;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.hardware.Sound;

public class Mapping {
	private float[] usData;
	private Navigation nav;
	private EV3LargeRegulatedMotor leftMotor, rightMotor,upperMotor;
	private Odometer odo;
	private ObjectColor obColor;

	final static int FAST = 200, SLOW = 100, SCANNING = 30, ACCELERATION = 200, GAP = 5;
	private SampleProvider usSensor;
	private SampleProvider colorValue;
	private float[] colorData;
	
	
	public Mapping(Odometer odo, SampleProvider colorValue, EV3LargeRegulatedMotor upperMotor,float[] colorData, SampleProvider usSensor, float[] usData) {
		this.odo = odo;
		this.nav = new Navigation(odo);
		this.colorValue = colorValue;
		this.colorData = colorData;
		this.usSensor = usSensor;
		this.usData = usData;
		EV3LargeRegulatedMotor[] motors = this.odo.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];
		this.upperMotor = upperMotor;
		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}
	
	
	public void doMap(){
		//begin spinning clockwise to scan for blocks
		leftMotor.setSpeed(SLOW);
		rightMotor.setSpeed(SLOW);
		leftMotor.backward();
		rightMotor.forward();
		//this variable makes sure we only do a close-adjustment once
		boolean haveAdjustedClose = false;
		while(true){
			//get the distance value
			double USDistance = getData();

			if(USDistance < 5){
				leftMotor.stop(true);
				rightMotor.stop(true);
				break;
			}else if(USDistance < 100){
				//move forward
				leftMotor.setSpeed(SLOW);
				rightMotor.setSpeed(SLOW);
				leftMotor.forward();
				rightMotor.forward();
			}else if(USDistance > 100){
				//spin
				leftMotor.setSpeed(SCANNING);
				rightMotor.setSpeed(SCANNING);
				leftMotor.backward();
				rightMotor.forward();
			}
			
		}
		//We should wait before reading the detector (so it settles)
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		/*
		 * Check to see what we are reading for an object.
		 * we beep once for a block and twice for the wooden block.
		 */
		
		if(isBlock()){
			Sound.beep();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//since it is a block, bring down the arms, and travel to the end position
			captureBlock();
			nav.travelTo(0, 0);
			//then beep thrice (as specified in document)
			Sound.beep();
			Sound.beep();
			Sound.beep();
		}else{
			Sound.beep();
			Sound.beep();
		}
		
	}
	
	private float getData() {
		usSensor.fetchSample(usData, 0);
		if (usData[0]*100 > 255)
			return 255;
		else
			return (usData[0]*100);
	}
	
	private void captureBlock(){
		upperMotor.setSpeed(SLOW);
		upperMotor.rotate(-180);
	}
	private void getColor() {
		colorValue.fetchSample(colorData, 0);
	}
	
	public boolean isBlock() {

		getColor();

		if (colorData[1] > colorData[0] && colorData[1] > colorData[2]) {
			return true;
		} else {
			return false;
		}
	}

}
