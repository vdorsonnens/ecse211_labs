package ca.mcgill.ecse211.lab3;

import lejos.robotics.SampleProvider;

public class ObstacleAvoider extends Thread {
	
	private SampleProvider usSensor;
	private float[] usData;
	private WheelsController wheelsController;
	
	public ObstacleAvoider(SampleProvider usSensor, float[] usData, WheelsController wheelsController) {
		this.usSensor = usSensor;
		this.usData = usData;
		this.wheelsController = wheelsController;
	}
	
	public void run() {
	}
	
}
