package ObjectSearch;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.hardware.Sound;

public class Mapping {
	private float[] usData;
	@SuppressWarnings("unused")
	private Navigation nav;
	private EV3LargeRegulatedMotor leftMotor, rightMotor,upperMotor;
	private Odometer odo;

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
		
		if(isBlock()){		//if we see block
			Sound.beep();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//capture the block
			captureBlock();
			//the method travelTo doesnt work well so we skip this part for the demo
			//nav.travelTo(0, 0);
			//then beep thrice (as specified in document)
			Sound.beep();
			Sound.beep();
			Sound.beep();
		}else{
			Sound.beep();						//beep twice because it is brown
			Sound.beep();
			
			//go backward a little bit and the turn left
			leftMotor.setSpeed(SLOW);
			rightMotor.setSpeed(SLOW);
			leftMotor.backward();
			rightMotor.backward();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			leftMotor.stop();
			rightMotor.stop();
			
			Sound.beep();
			Sound.beep();
			leftMotor.setSpeed(SLOW);
			rightMotor.setSpeed(SLOW);
			leftMotor.backward();
			rightMotor.forward();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			leftMotor.stop();
			rightMotor.stop();
			//use recursion to find the blue block
			doMap();
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
