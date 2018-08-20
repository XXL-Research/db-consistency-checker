package de.htwk.imn.consistencychecker.utils;

public abstract class SelectedOptions {

	private String IP, database, modelToCheck;
	private int[] ports;
	private Boolean write;
	private int numberOfTests;

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public int[] getPorts() {
		return ports;
	}

	public void setPorts(int[] ports) {
		this.ports = ports;
	}

	public String getModelToCheck() {
		return modelToCheck;
	}

	public void setModelToCheck(String modelToCheck) {
		this.modelToCheck = modelToCheck;
	}

	public Boolean getWrite() {
		return write;
	}

	public void setWrite(Boolean write) {
		this.write = write;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public int getNumberOfTests() {
		return numberOfTests;
	}

	public void setNumberOfTests(int numberOfTests) {
		this.numberOfTests = numberOfTests;
	}

}
