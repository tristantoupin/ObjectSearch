package ObjectSearch;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.hardware.Sound;

public class Mapping {
	private float[] usData;
	private Navigation nav;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private Odometer odo;

	final static int FAST = 200, SLOW = 100, SCANNING = 30, ACCELERATION = 200, GAP = 5;
	private double firstHit[] = new double[2];
	private double secondHit[]= new double[2];
	private double thirdHit[]= new double[2];
	private double fourthHit[]= new double[2];
	private SampleProvider usSensor;
	
	public Mapping(Odometer odo, SampleProvider usSensor, float[] usData) {
		this.odo = odo;
		this.nav = new Navigation(odo);
		this.usSensor = usSensor;
		this.usData = usData;
		EV3LargeRegulatedMotor[] motors = this.odo.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];

		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}
	
	
	public void doMap(){

		double firstAngle = odo.getAng();
		//set motor speed
		leftMotor.setSpeed(SCANNING);
		rightMotor.setSpeed(SCANNING);
		firstHit[0] = getData();
		System.out.println(firstHit[0]);
		System.out.println(firstHit[0]);
		System.out.println(firstHit[0]);
		System.out.println(firstHit[0]);

		while (firstHit[0] > 100){
			leftMotor.backward();
			rightMotor.forward();
			firstHit[0] = getData();
			System.out.println(firstHit[0]);

		}
		System.out.println(firstHit[0]);

		Sound.beep();
		firstHit[1] = odo.getAng();

		while (secondHit[0] < firstHit[0]+GAP){
			leftMotor.backward();
			rightMotor.forward();
			secondHit[0] = getData();
			System.out.println(secondHit[0]);
		}
		
		Sound.beep();
		secondHit[1] = odo.getAng();	
		/*
		System.out.println(secondHit[0]);
		double angleA = firstHit[1];
		double angleB = secondHit[1];
		
		double north = (angleA+angleB)/2;
		double distanceToTravel = (firstHit[0]+secondHit[0])/2;
		nav.travelTo(Math.cos(north)*distanceToTravel, Math.sin(north)*distanceToTravel);
		*/
		nav.setSpeeds(0, 0);
		
	}
	
	private float getData() {
		usSensor.fetchSample(usData, 0);
		if (usData[0]*100 > 255)
			return 255;
		else
			return (usData[0]*100);
	}

}
