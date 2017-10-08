package ca.mcgill.ecse211.lab4;

public interface UltrasonicHandler {
	public void processUSData(int distance);
	public int readUSDistance();
}
