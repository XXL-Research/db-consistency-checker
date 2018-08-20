package de.htwk.imn.consistencychecker.utils;

import java.util.concurrent.TimeUnit;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;

public class SelectedMongoDBOptions extends SelectedOptions {

	public static final String WRITE_CONCERN_W0 = "0";
	public static final String WRITE_CONCERN_W1 = "1";
	public static final String WRITE_CONCERN_W2 = "2";
	public static final String WRITE_CONCERN_W3 = "3";
	public static final String WRITE_CONCERN_MAJORITY = "majority";

	public static final String READ_CONCERN_LOCAL = "local";
	public static final String READ_CONCERN_DEFAULT = "default";
	public static final String READ_CONCERN_MAJORITY = "majority";
	public static final String READ_CONCERN_LINEARIZABLE = "linearizable";

	public static final String READ_PREFERENCE_PRIMARY = "primary";
	public static final String READ_PREFERENCE_PRIMARY_PREFERRED = "primaryPreferred";
	public static final String READ_PREFERENCE_SECONDARY = "secondary";
	public static final String READ_PREFERENCE_SECONDARY_PREFERRED = "secondaryPreferred";
	public static final String READ_PREFERENCE_NEAREST = "nearest";

	private WriteConcern writeConcern;
	private ReadConcern readConcern;
	private ReadPreference readPreference;
	private Boolean causallyConsistent;

	public WriteConcern getWriteConcern() {
		return writeConcern;
	}

	public void setWriteConcern(String writeConcern, Boolean journal, Long wtimeout) {
		if (writeConcern.equals(WRITE_CONCERN_W0) || writeConcern.equals(WRITE_CONCERN_W1)
				|| writeConcern.equals(WRITE_CONCERN_W2) || writeConcern.equals(WRITE_CONCERN_W3)) {
			this.writeConcern = new WriteConcern(Integer.parseInt(writeConcern)).withJournal(journal)
					.withWTimeout(wtimeout, TimeUnit.MILLISECONDS);
		} else if (writeConcern.equals(WRITE_CONCERN_MAJORITY)) {
			this.writeConcern = new WriteConcern(writeConcern).withJournal(journal).withWTimeout(wtimeout,
					TimeUnit.MILLISECONDS);
		}
	}

	public ReadConcern getReadConcern() {
		return readConcern;
	}

	public void setReadConcern(String readConcern) {
		switch (readConcern) {
		case READ_CONCERN_LOCAL:
			this.readConcern = ReadConcern.LOCAL;
			break;
		case READ_CONCERN_DEFAULT:
			this.readConcern = ReadConcern.DEFAULT;
			break;
		case READ_CONCERN_MAJORITY:
			this.readConcern = ReadConcern.MAJORITY;
			break;
		case READ_CONCERN_LINEARIZABLE:
			this.readConcern = ReadConcern.LINEARIZABLE;
			break;
		default:
			this.readConcern = ReadConcern.DEFAULT;
			break;
		}
	}

	public ReadPreference getReadPreference() {
		return readPreference;
	}

	public void setReadPreference(String readPreference) {
		switch (readPreference) {
		case READ_PREFERENCE_PRIMARY:
			this.readPreference = ReadPreference.primary();
			break;
		case READ_PREFERENCE_PRIMARY_PREFERRED:
			this.readPreference = ReadPreference.primaryPreferred();
			break;
		case READ_PREFERENCE_SECONDARY:
			this.readPreference = ReadPreference.secondary();
			break;
		case READ_PREFERENCE_SECONDARY_PREFERRED:
			this.readPreference = ReadPreference.secondaryPreferred();
			break;
		case READ_PREFERENCE_NEAREST:
			this.readPreference = ReadPreference.nearest();
			break;
		default:
			this.readPreference = ReadPreference.primary();
			break;
		}
	}

	public void setReadPreferenceWithMaxStaleness(String readPreference, long maxStalenessSeconds) {
		switch (readPreference) {
		case READ_PREFERENCE_PRIMARY:
			this.readPreference = ReadPreference.primary();
			break;
		case READ_PREFERENCE_PRIMARY_PREFERRED:
			this.readPreference = ReadPreference.primaryPreferred(maxStalenessSeconds, TimeUnit.SECONDS);
			break;
		case READ_PREFERENCE_SECONDARY:
			this.readPreference = ReadPreference.secondary(maxStalenessSeconds, TimeUnit.SECONDS);
			break;
		case READ_PREFERENCE_SECONDARY_PREFERRED:
			this.readPreference = ReadPreference.secondaryPreferred(maxStalenessSeconds, TimeUnit.SECONDS);
			break;
		case READ_PREFERENCE_NEAREST:
			this.readPreference = ReadPreference.nearest(maxStalenessSeconds, TimeUnit.SECONDS);
			break;
		default:
			this.readPreference = ReadPreference.primary();
			break;
		}
	}

	public Boolean getCausallyConsistent() {
		return causallyConsistent;
	}

	public void setCausallyConsistent(Boolean causallyConsistent) {
		this.causallyConsistent = causallyConsistent;
	}

}
