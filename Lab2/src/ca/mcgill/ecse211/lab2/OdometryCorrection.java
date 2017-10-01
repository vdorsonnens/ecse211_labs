/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.lab2;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

public class OdometryCorrection extends Thread {
  private static final long CORRECTION_PERIOD = 10;
  private static final double SENSOR_TO_WHEELS = 5.75;
  private static final int BOARD_COLOR = 6;
  private static final int LINE_COLOR = 13;
  private static final double THETA_WIDTH = 0.78540; //   PI/4
  private static final double ACCEPTED_ERROR = 0.1;
  
  private static final int FILTER = 100;
  private int count;
  private boolean recentLine;
  
  private Odometer odometer;
  private EV3ColorSensor sensor;
  
  public int color;
  public int numLines;
  public int direction;

  public double nextLinePosition;
  

  // constructor
  public OdometryCorrection(Odometer odometer, EV3ColorSensor sensor) {
    this.odometer = odometer;

    this.sensor = sensor;
    this.color = 0;
    this.numLines = 0;
    this.count = 0;
    this.recentLine = false;
    this.direction = 0;

    this.nextLinePosition = 0.0;
  }

  public void run() {
    long correctionStart, correctionEnd;
    int newColor;
    double error;
    
    // [x, y, theta]
    double[] position = new double[3];
    boolean[] update = new boolean[] {true, true, true};
    
    while (true) {
      correctionStart = System.currentTimeMillis();

      newColor = sensor.getColorID();
      
      // Dumb working filter
      if (this.recentLine) {
    	  this.count++;
    	  if (this.count > FILTER)  {
    		  this.count = 0;
    		  this.recentLine = false;
    	  }
      }
      
      // Line detected
      if (this.color == BOARD_COLOR && newColor == LINE_COLOR && !this.recentLine)  {
    	  this.numLines++;
    	  this.recentLine = true;
    	  this.count = 0;
    	  
    	  odometer.getPosition(position, update);
    	  // correction
    	  this.direction = getDirection(position[2]);
    	  switch (this.direction) {
    	  	case 0:
    	  		error = (position[0] + SENSOR_TO_WHEELS) - this.nextLinePosition;
    	  		if (Math.abs(error) > ACCEPTED_ERROR) {
    	  			odometer.setX(this.nextLinePosition);
    	  		}
    	  		break;
    	  	case 1:
    	  		error = (position[1] + SENSOR_TO_WHEELS) - this.nextLinePosition;
    	  		if (Math.abs(error) > ACCEPTED_ERROR) {
    	  			odometer.setY(this.nextLinePosition);
    	  		}
    	  		break;
    	  	case 2:
    	  		error = (position[0] - SENSOR_TO_WHEELS) - this.nextLinePosition;
    	  		if (Math.abs(error) > ACCEPTED_ERROR) {
    	  			odometer.setX(this.nextLinePosition);
    	  		}
    	  		break;
    	  	case 3:
    	  		error = (position[1] - SENSOR_TO_WHEELS) - this.nextLinePosition;
    	  		if (Math.abs(error) > ACCEPTED_ERROR) {
    	  			odometer.setY(this.nextLinePosition);
    	  		}
    	  		break;
    	  }	  
    	  setNextLine(this.direction);
      }
      
      this.color = newColor;

      // this ensure the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here because it is not
          // expected that the odometry correction will be
          // interrupted by another thread
        }
      }
    }
  }
  
  private int getDirection(double theta) {
	  // +X
	  if  ((6.28319 - THETA_WIDTH < theta && theta <= 6.28319) ||  theta >= 0 && theta < THETA_WIDTH)
		  return 0;
	  
	  //+Y
	  if (1.57080 + THETA_WIDTH > theta && theta > 1.57080 - THETA_WIDTH )
		  return 1;
	  
	  // -X
	  if (3.14159 + THETA_WIDTH > theta && theta > 3.14159 - THETA_WIDTH)
		  return 2;
	  
	  // -Y
	  if (4.71289 + THETA_WIDTH > theta && theta > 4.71289 - THETA_WIDTH)
		  return 3;
	  
	  return -1; // will not happen because the whole circle is already covered
  }
  
  // 0, 0 is at a cross
  private void setNextLine(int direction) {
	  switch (direction) {
	  	case 0:
	  		this.nextLinePosition += OdometryLab.TILE_SIZE;
	  		if (this.nextLinePosition >= OdometryLab.NUM_TILE_X * OdometryLab.TILE_SIZE - 5.0)  // will go to direction #3 (-Y){
	  		{
	  			this.nextLinePosition = 0.0;
	  		}
	  		break;
	  	case 3:
	  		this.nextLinePosition -= OdometryLab.TILE_SIZE;
	  		if (this.nextLinePosition <= -1*OdometryLab.NUM_TILE_Y * OdometryLab.TILE_SIZE + 5.0) // will go to #2 (-X)
	  		{
	  				this.nextLinePosition = ((OdometryLab.NUM_TILE_X-1) * OdometryLab.TILE_SIZE);
	  		}
	  		break;
	  	case 2:
	  		this.nextLinePosition -= OdometryLab.TILE_SIZE;
	  		if (this.nextLinePosition < 0) // will go to #1 (+Y)
	  		{
	  			this.nextLinePosition = -1 * ((OdometryLab.NUM_TILE_Y - 1) * OdometryLab.TILE_SIZE);
	  		}
	  		break;
	  	case 1: 
	  		this.nextLinePosition += OdometryLab.TILE_SIZE;
	  		if (this.nextLinePosition > 0) // will go to direction #0 (+X)
	  		{
	  			this.nextLinePosition = 0;
	  		}
	  		break;
	  }
  }
  
}
