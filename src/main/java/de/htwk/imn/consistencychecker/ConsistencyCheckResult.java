package de.htwk.imn.consistencychecker;

public class ConsistencyCheckResult {

	private long violations;
	private long totalChecks;

	public ConsistencyCheckResult(long violations, long totalChecks) {
		super();
		this.violations = violations;
		this.totalChecks = totalChecks;
	}

	public long getViolations() {
		return violations;
	}

	public void setViolations(long violations) {
		this.violations = violations;
	}

	public long getTotalChecks() {
		return totalChecks;
	}

	public void setTotalChecks(long totalChecks) {
		this.totalChecks = totalChecks;
	}

}
