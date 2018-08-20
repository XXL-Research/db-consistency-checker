package de.htwk.imn.consistencychecker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.htwk.imn.consistencychecker.database.DataBase;

public class ReadYourWritesConsistencyChecker {

	private DataBase db;
	private static final Logger logger = LogManager.getLogger(ReadYourWritesConsistencyChecker.class);

	public ConsistencyCheckResult checkRYWC(DataBase database, int numberOfTestRun, int numberOfMinRead) {
		db = database;
		String writevalue, readvalue;
		long totalChecks = 0;
		long violations = 0;
		long startTimeStamp = System.currentTimeMillis();
		for (int i = 1; i <= numberOfTestRun; i++) {
			writevalue = String.valueOf(i);
			db.writeData("user1", "field0", writevalue);
			for (int j = 1; j <= numberOfMinRead; j++) {
				readvalue = db.readData("user1", "field0");
				totalChecks++;
				if (Integer.parseInt(readvalue) >= Integer.parseInt(writevalue)) {
					logger.info("gelesen: " + readvalue + " geschrieben: " + writevalue + " --- " + readvalue + " >= "
							+ writevalue + " --- RYWC eingehalten");
				} else {
					logger.info("gelesen: " + readvalue + " geschrieben: " + writevalue + " --- " + readvalue + " < "
							+ writevalue + " --- RYWC verletzt");
					violations++;
					break;
				}
			}
		}
		logger.info("Dauer Überprüfung RYWC: " + (System.currentTimeMillis() - startTimeStamp));
		return new ConsistencyCheckResult(violations, totalChecks);
	}

}
