package ca.mcgill.ecse211.lab4;

import lejos.robotics.SampleProvider;

public class LightPoller extends Thread {
  private SampleProvider lightSensor;
  private LightHandler handler;
  private float[] lightData;

  public LightPoller(SampleProvider lightSensor, float[] lightData, LightHandler handler) {
    this.lightSensor = lightSensor;
    this.handler = handler;
    this.lightData = lightData;
  }

  public void run() {
    int intensity;
    while (true) {
    	if (this.handler != null) {
    		lightSensor.fetchSample(lightData, 0); // acquire data
    		intensity =  (int) (lightData[0]*1000);
    		this.handler.processLightData(intensity); // now take action depending on value
    	} else {
    		return;
    	}
    	try {
    		Thread.sleep(50);
    	} catch (Exception e) {
    	} // Poor man's timed sampling
    }
  }
  
  public void setHandler(LightHandler handler) {
	  this.handler = handler;
  }

}