package ca.mcgill.ecse211.lab4;

import lejos.robotics.SampleProvider;

public class UltrasonicPoller extends Thread {
  private SampleProvider us;
  private UltrasonicHandler handler;
  private float[] usData;

  public UltrasonicPoller(SampleProvider us, float[] usData, UltrasonicHandler handler) {
    this.us = us;
    this.handler = handler;
    this.usData = usData;
  }

  public void run() {
    int distance;
    while (true) {
      us.fetchSample(usData, 0); // acquire data
      distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int
      if (this.handler != null)
    	  this.handler.processUSData(distance); // now take action depending on value
      else
    	  return;
      try {
        Thread.sleep(50);
      } catch (Exception e) {
      } // Poor man's timed sampling
    }
  }
  
  public void setHandler(UltrasonicHandler handler) {
	  this.handler = handler;
  }

}