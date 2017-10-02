package ca.mcgill.ecse211.lab3;

import lejos.robotics.SampleProvider;

public class UltrasonicPoller extends Thread {
  private SampleProvider us;
  private UltrasonicController cont;
  private float[] usData;

  public UltrasonicPoller(SampleProvider us, float[] usData, UltrasonicController cont) {
    this.us = us;
    this.cont = cont;
    this.usData = usData;
  }

  public void run() {
    int distance;
    while (true) {
      us.fetchSample(usData, 0); // acquire data
      distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int
      if (!cont.avoiding()) {
    	  cont.processUSData(distance); // now take action depending on value
      }
      try {
        Thread.sleep(50);
      } catch (Exception e) {
      } // Poor man's timed sampling
    }
  }

}