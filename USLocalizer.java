package ObjectSearch;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static int ROTATION_SPEED = 80, ACCELERATION = 400, DATACONSIDERED = 5;
	public static final double DIST_WALL = 30, NOISE_MARGIN = 3, DIST_WHEELSENSOR = 10.0;
	public static float[] usStored = new float[10];
	
	private double smallestX = 255.0, smallestY = 255.0;
	private Odometer odo;
	private SampleProvider usSensor;

	private float[] usData;
	private LocalizationType locType;
	private Navigation nav;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		EV3LargeRegulatedMotor[] motors = this.odo.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];
		leftMotor.setAcceleration(ACCELERATION);
		rightMotor.setAcceleration(ACCELERATION);
		
		this.nav = new Navigation(odo);

	}
	
	public void doLocalization() {
		double angleA, angleB;
		//set motor speed
		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);
		
		if (locType == LocalizationType.FALLING_EDGE) {
			// rotate the robot until it sees no wall
			while(getFilteredData() < DIST_WALL + NOISE_MARGIN){
				leftMotor.forward();
				rightMotor.backward();
			}

			// keep rotating until the robot sees a wall, then latch the angle
			while(getFilteredData() > DIST_WALL){
				leftMotor.forward();
				rightMotor.backward();
			}
			//get the angle from the odometer
			angleA = odo.getAng();
			
			// switch direction and wait until it sees no wall
			while(getFilteredData() < DIST_WALL + NOISE_MARGIN){
				leftMotor.backward();
				rightMotor.forward();
			}
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(getFilteredData() > DIST_WALL){
				leftMotor.backward();
				rightMotor.forward();
			}
			rightMotor.stop(true);
			leftMotor.stop(true);
			
			//get the angle from the odometer
			angleB = odo.getAng();
			Sound.beep();
			
			double north = 0;
			//if our angle A is larger than B, it means we passed the 0 point, and that angle A is "negative".
			if(angleA > angleB){
				north = 225 - (angleA + angleB)/2.0;
			} else {
				north = 45 - (angleA + angleB)/2.0;
			}
			
			
			odo.setPosition(new double [] {0.0, 0.0, odo.getAng()+north}, new boolean [] {false, false, true});
			nav.turnTo(0,true);	
			
	} else {
		/*
		 * The robot should turn until it sees the wall, then look for the
		 * "rising edges:" the points where it no longer sees the wall.
		 * This is very similar to the FALLING_EDGE routine, but the robot
		 * will face toward the wall for most of it.
		 */
		// rotate the robot until it sees a wall
		while(getFilteredData() > DIST_WALL - NOISE_MARGIN){
			leftMotor.backward();
			rightMotor.forward();
			
		}
		// keep rotating until the robot no longer sees the wall, then latch the angle
		while(getFilteredData() < DIST_WALL){
			leftMotor.backward();
			rightMotor.forward();
			if (smallestY > getFilteredData()){
				smallestY = getFilteredData();
			}

		}

		angleA = odo.getAng();
		
		//switch directions and rotate until the robot sees the wall.
		while(getFilteredData() > DIST_WALL - NOISE_MARGIN){
			leftMotor.forward();
			rightMotor.backward();
		}

		// rotate until the robot no longer sees the wall and latch the angle.
		while(getFilteredData() < DIST_WALL){
			leftMotor.forward();
			rightMotor.backward();
			if (smallestX > getFilteredData()){
				smallestX = getFilteredData();
			}
		}
		leftMotor.stop(true);
		rightMotor.stop(true);
		angleB = odo.getAng();
		
		Sound.beep();
		
		double north = 0;
		//if our angle A is larger than B, it means we passed the 0 point, and that angle A is "negative".
		if(angleA > angleB){
			north = 225 - (angleA + angleB)/2.0;
		} else {
			north = 45 - (angleA + angleB)/2.0;
		}
		
		odo.setPosition(new double [] {smallestX-30.48+DIST_WHEELSENSOR, smallestY-30.48+DIST_WHEELSENSOR, odo.getAng()+north}, new boolean [] {true, true, true});
		
		nav.travelTo(0, 0);
		
		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);
		nav.turnTo(0,true);
			
		}
	}
	
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		return usData[0]*100;
	}
}
