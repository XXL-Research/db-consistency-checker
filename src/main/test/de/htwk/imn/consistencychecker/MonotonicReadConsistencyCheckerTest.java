package de.htwk.imn.consistencychecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.htwk.imn.consistencychecker.database.DataBase;

public class MonotonicReadConsistencyCheckerTest {

	@Mock
	private DataBase db;

	@Mock
	private CountDownLatch latch;

	private MonotonicReadConsistencyChecker mrc;
	private ConsistencyCheckResult result;

	@Before
	public void setUp() throws Exception {
		LogManager.getLogger(ReadYourWritesConsistencyChecker.class).setLevel(Level.OFF);
		MockitoAnnotations.initMocks(this);
		Mockito.doNothing().when(db).writeData(any(String.class), any(String.class), any(String.class));
	}

	@Test
	public void mwcPass() {
		mrc = new MonotonicReadConsistencyChecker(db, 3, false, latch);
		when(db.readData(any(String.class), any(String.class))).thenAnswer(new Answer() {
			private int count = 0;

			@Override
			public String answer(InvocationOnMock arg0) throws Throwable {
				if (++count == 1)
					return "1";
				return "2";
			}
		});

		result = mrc.checkMRC();
		assertThat(result.getTotalChecks(), equalTo(3L));
		assertThat(result.getViolations(), equalTo(0L));
	}

	@Test
	public void mwcFail() {
		mrc = new MonotonicReadConsistencyChecker(db, 3, false, latch);
		Answer<String> answer = new Answer<String>() {
			private int count = 0;

			@Override
			public String answer(InvocationOnMock arg0) throws Throwable {
				if (++count == 1)
					return "5";
				return "2";
			}
		};
		when(db.readData(any(String.class), any(String.class))).thenAnswer(answer);

		result = mrc.checkMRC();
		assertThat(result.getTotalChecks(), equalTo(3L));
		assertThat(result.getViolations(), equalTo(3L));
	}

}
