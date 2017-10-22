package lab5;

import java.util.*;

import lab5.main.Global;

public class UltraSonicSensor extends Thread{
	
	public static final int COUNT_MAX = 3;
	public static final int MID = 1;
	
	public UltraSonicSensor() {
	}
	
	public void run() {
		float arr[] = new float[COUNT_MAX];
		while(true) {
			if (Global.usSwitch) {
				for (int i = 0; i < COUNT_MAX; i++) {
					Global.usDistance.fetchSample(Global.usData, 0);
					arr[i] = Global.usData[0];
				}
				Arrays.sort(arr);
				Global.ObstacleDistance = arr[MID]*100;
			}else {
				try {
					Thread.sleep(Global.THREAD_SLEEP_TIME);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	
	
	
	
}
