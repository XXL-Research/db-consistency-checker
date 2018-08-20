package de.htwk.imn.consistencychecker.database;

public interface DataBase {

	public void writeData(String y_id, String field, String newValue);

	public String readData(String y_id, String field);

	public void closeConnection();

}
