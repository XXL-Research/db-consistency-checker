package de.htwk.imn.consistencychecker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.htwk.imn.consistencychecker.database.DataBase;

public class MonotonicWriteConsistencyChecker {

	private DataBase db;
	private static final Logger logger = LogManager.getLogger(MonotonicWriteConsistencyChecker.class);

	public ConsistencyCheckResult checkMWC(DataBase database, int numberOfKeys, Boolean writeValues) {

		db = database;
		long totalChecks = 0;
		long violations = 0;

		if (writeValues) {
			long startTimeStamp = System.currentTimeMillis();
			for (int i = 1; i <= numberOfKeys; i++) {
				String key = "user" + String.valueOf(i);
				String value1 = String.valueOf(i);
				String value2 = String.valueOf(i + 1);

				db.writeData(key, "field0", value1);
				logger.info("Key: " + key + " Value 1: " + value1 + " geschrieben");
				db.writeData(key, "field0", value2);
				logger.info("Key: " + key + " Value 2: " + value2 + " geschrieben");
				totalChecks++;
			}
			logger.info("Dauer Schreiben MWC: " + (System.currentTimeMillis() - startTimeStamp));

		} else {
			long startTimeStamp = System.currentTimeMillis();
			for (int i = 1; i <= numberOfKeys; i++) {
				String key = "user" + String.valueOf(i);

				String value2 = String.valueOf(i + 1);
				String readvalue = db.readData(key, "field0");
				totalChecks++;

				if (readvalue.equals(value2)) {
					logger.info("Geschrieben: Key: " + key + " Value 2: " + value2 + " --- Gelesen: Key: " + key
							+ " Value: " + readvalue + " --- " + value2 + " = " + readvalue + " --- MWC eingehalten");
				} else {
					logger.info("Geschrieben: Key: " + key + " Value 2: " + value2 + " --- Gelesen: Key: " + key
							+ " Value: " + readvalue + " --- " + value2 + " != " + readvalue + " --- MWC verletzt");
					violations++;
				}
			}
			logger.info("Dauer Überprüfung MWC: " + (System.currentTimeMillis() - startTimeStamp));
		}
		return new ConsistencyCheckResult(violations, totalChecks);
	}

}
