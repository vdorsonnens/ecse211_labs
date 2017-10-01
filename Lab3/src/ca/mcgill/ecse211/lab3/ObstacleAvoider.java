package ca.mcgill.ecse211.lab3;

import lejos.robotics.SampleProvider;

public class ObstacleAvoider implements UltrasonicController {
	private static final int FILTER_OUT = 2;
	private static final int THRESHOLD = 5;
	
	private boolean avoiding;
	
	private SampleProvider usSensor;
	private float[] usData;
	private WheelsController wheelsController;
	private int filterControl;
	
	private int distance;
	
	public ObstacleAvoider(WheelsController wheelsController) {
		this.usSensor = usSensor;
		this.usData = usData;
		this.wheelsController = wheelsController;
		this.distance = 20;
		this.filterControl = 0;
		this.avoiding = false;
	}
	
	@Override
	 public void processUSData(int distance) {
		// Filtering
	    if (distance >= 100 && filterControl < FILTER_OUT) {
	      filterControl++;
	    } else if (distance >= 100) {
	      this.distance = 100;
	    } else {
	      filterControl = 0;
	      this.distance = distance;
	    }
	    
	    if (this.distance < THRESHOLD) {
	    	this.avoiding = true;
	    	this.wheelsController.stop();
	    	this.wheelsController.turnTo(-90);
	    	this.wheelsController.travelTo(20, true);
	    	this.wheelsController.turnTo(90);
	    	this.wheelsController.travelTo(30, true);
	    	this.avoiding = false;
	    	this.distance = 20;
	    }
	    
	 }
	
	@Override
	 public int readUSDistance() {
		 return this.distance;
	 }
	
	public boolean avoiding() {
		return this.avoiding;
	}
	
}
