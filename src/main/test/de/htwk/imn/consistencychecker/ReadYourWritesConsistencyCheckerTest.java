package de.htwk.imn.consistencychecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.htwk.imn.consistencychecker.database.DataBase;

public class ReadYourWritesConsistencyCheckerTest {

	@Mock
	private DataBase db;

	@InjectMocks
	private ReadYourWritesConsistencyChecker rywc = new ReadYourWritesConsistencyChecker();
	private ConsistencyCheckResult result;

	@Before
	public void setUp() throws Exception {
		LogManager.getLogger(ReadYourWritesConsistencyChecker.class).setLevel(Level.OFF);
		MockitoAnnotations.initMocks(this);
		Mockito.doNothing().when(db).writeData(any(String.class), any(String.class), any(String.class));
	}

	@Test
	public void rywcPass() {
		LogManager.getRootLogger().setLevel(Level.OFF);
		when(db.readData(any(String.class), any(String.class))).thenReturn("1");
		result = rywc.checkRYWC(db, 1, 3);
		assertThat(result.getTotalChecks(), equalTo(3L));
		assertThat(result.getViolations(), equalTo(0L));
	}

	@Test
	public void rywcFail() {
		LogManager.getRootLogger().setLevel(Level.OFF);
		when(db.readData(any(String.class), any(String.class))).thenReturn("1");
		result = rywc.checkRYWC(db, 3, 2);
		assertThat(result.getTotalChecks(), equalTo(4L));
		assertThat(result.getViolations(), equalTo(2L));
	}

}
