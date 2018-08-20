package de.htwk.imn.consistencychecker;

import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.htwk.imn.consistencychecker.database.DataBase;

public class MonotonicReadConsistencyChecker extends Thread {

	private DataBase db;
	private int numberOfValues;
	private Boolean writer;
	private CountDownLatch latch;
	private ConsistencyCheckResult result;

	private static final Logger logger = LogManager.getLogger(MonotonicReadConsistencyChecker.class);

	public MonotonicReadConsistencyChecker(DataBase db, int numberOfValues, Boolean writer, CountDownLatch latch) {
		this.db = db;
		this.numberOfValues = numberOfValues;
		this.writer = writer;
		this.latch = latch;
	}

	public void run() {

		if (writer) {
			this.write();
		} else {
			setResult(this.checkMRC());
		}
	}

	private void write() {
		long startTimeStamp = System.currentTimeMillis();
		for (int i = 1; i <= (numberOfValues + 100); i++) {
			db.writeData("user1", "field0", String.valueOf(i));
			logger.info("Thread " + this.getId() + " geschrieben: " + String.valueOf(i));
		}
		latch.countDown();
		logger.info("Dauer Schreiben MRC: " + (System.currentTimeMillis() - startTimeStamp));
	}

	public ConsistencyCheckResult checkMRC() {
		String firstReadValue, newReadValue;
		long totalChecks = 0;
		int checksCounter = 0;
		long violations = 0;
		long startTimeStamp = System.currentTimeMillis();
		firstReadValue = db.readData("user1", "field0");
		for (int i = 1; i <= numberOfValues; i++) {
			if (checksCounter == 10) {
				firstReadValue = db.readData("user1", "field0");
				checksCounter = 0;
			}
			newReadValue = db.readData("user1", "field0");
			totalChecks++;
			if (Integer.parseInt(newReadValue) >= Integer.parseInt(firstReadValue)) {
				logger.info("Thread " + this.getId() + " 1. gelesener Wert: " + firstReadValue
						+ " neuer gelesener Wert: " + newReadValue + " --- " + newReadValue + " >= " + firstReadValue
						+ " --- MRC eingehalten");
			} else {
				logger.info(
						"Thread " + this.getId() + " 1. gelesener Wert: " + firstReadValue + " neuer gelesener Wert: "
								+ newReadValue + " --- " + newReadValue + " < " + firstReadValue + " --- MRC verletzt");
				violations++;
			}
			checksCounter++;
		}
		latch.countDown();
		logger.info("Dauer Überprüfung MRC: " + (System.currentTimeMillis() - startTimeStamp));
		return new ConsistencyCheckResult(violations, totalChecks);
	}

	public ConsistencyCheckResult getResult() {
		return result;
	}

	public void setResult(ConsistencyCheckResult result) {
		this.result = result;
	}

}
