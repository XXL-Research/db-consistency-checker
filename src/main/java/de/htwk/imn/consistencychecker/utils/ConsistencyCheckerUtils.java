package de.htwk.imn.consistencychecker.utils;

import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.htwk.imn.consistencychecker.ConsistencyCheckResult;
import de.htwk.imn.consistencychecker.MonotonicReadConsistencyChecker;
import de.htwk.imn.consistencychecker.MonotonicWriteConsistencyChecker;
import de.htwk.imn.consistencychecker.ReadYourWritesConsistencyChecker;
import de.htwk.imn.consistencychecker.database.Cassandra;
import de.htwk.imn.consistencychecker.database.DataBase;
import de.htwk.imn.consistencychecker.database.DataBases;
import de.htwk.imn.consistencychecker.database.MongoDB;
import de.htwk.imn.consistencychecker.database.Redis;

public class ConsistencyCheckerUtils {

	private DataBase database;
	private SelectedOptions selectedOptions;
	private static final String RYWC = "RYWC";
	private static final String MWC = "MWC";
	private static final String MRC = "MRC";

	private Logger logger;

	public ConsistencyCheckerUtils(SelectedOptions selectedOptions) {
		super();
		this.selectedOptions = selectedOptions;
	}

	public void setUpDatabase() {

		String ip = selectedOptions.getIP();
		Boolean newDataBase = true;
		if (selectedOptions.getModelToCheck().equals(MWC)) {
			newDataBase = selectedOptions.getWrite();
		}
		int[] ports = selectedOptions.getPorts();

		if (selectedOptions.getDatabase().equals(DataBases.Cassandra.toString())) {
			try {
				database = new Cassandra(ip, ports, (SelectedCassandraOptions) selectedOptions, newDataBase);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}

		} else if (selectedOptions.getDatabase().equals(DataBases.MongoDB.toString())) {
			try {
				database = new MongoDB(ip, ports, (SelectedMongoDBOptions) selectedOptions, newDataBase);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		} else if (selectedOptions.getDatabase().equals(DataBases.Redis.toString())) {
			database = new Redis(ip, ports, (SelectedRedisOptions) selectedOptions, newDataBase);
		}
	}

	public void runConsistencyCheck() {

		String database = selectedOptions.getDatabase();
		String modelToCheck = selectedOptions.getModelToCheck();
		int numberOfTests = selectedOptions.getNumberOfTests();

		if (modelToCheck.equals(RYWC)) {

			logger = LogManager.getLogger(ReadYourWritesConsistencyChecker.class);
			logger.info("Start Überprüfung RYWC, Anzahl Testläufe: " + numberOfTests + ", Datenbank: " + database);

			ReadYourWritesConsistencyChecker rywc = new ReadYourWritesConsistencyChecker();
			ConsistencyCheckResult result = rywc.checkRYWC(this.database, numberOfTests, 3);

			logger.info("Ende Überprüfung RYWC, Datenbank: " + database + ", Anzahl Testläufe: "
					+ result.getTotalChecks() + ", Konsistenz Modell Verletzungen: " + result.getViolations());

			this.cleanUpDataBase();

		} else if (modelToCheck.equals(MWC)) {

			logger = LogManager.getLogger(MonotonicWriteConsistencyChecker.class);

			MonotonicWriteConsistencyChecker mwc = new MonotonicWriteConsistencyChecker();

			logger.info("Start Überprüfung MWC, Anzahl Testläufe: " + numberOfTests + ", Datenbank: " + database);

			ConsistencyCheckResult result = mwc.checkMWC(this.database, numberOfTests, selectedOptions.getWrite());

			logger.info("Ende Überprüfung MWC, Datenbank: " + database + ", Anzahl Testläufe: "
					+ result.getTotalChecks() + ", Konsistenz Modell Verletzungen: " + result.getViolations());

			this.cleanUpDataBase();

		} else if (modelToCheck.equals(MRC)) {

			logger = LogManager.getLogger(MonotonicReadConsistencyChecker.class);

			CountDownLatch latch = new CountDownLatch(2);
			MonotonicReadConsistencyChecker mrcWriter = new MonotonicReadConsistencyChecker(this.database,
					numberOfTests, true, latch);
			MonotonicReadConsistencyChecker mrcChecker = new MonotonicReadConsistencyChecker(this.database,
					numberOfTests, false, latch);

			logger.info("Start Überprüfung MRC, Anzahl Testläufe: " + numberOfTests + ", Datenbank: " + database);

			mrcWriter.start();
			mrcChecker.start();

			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			logger.info("Ende Überprüfung MRC, Datenbank: " + database);
			logger.info("Reader Anzahl Testläufe: " + mrcChecker.getResult().getTotalChecks()
					+ ", Konsistenz Modell Verletzungen: " + mrcChecker.getResult().getViolations());

			this.cleanUpDataBase();

		}

	}

	private void cleanUpDataBase() {
		this.database.closeConnection();
	}
}
