package stuff;

import java.util.Date;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRelease;

public class RepoTS
{
	private GHRelease release;
	private GHCommit commit;
	private Date timestamp;

	public RepoTS(GHRelease release, GHCommit commit, Date timestamp)
	{
		this.release = release;
		this.commit = commit;
		this.timestamp = timestamp;
	}

	public GHRelease getRelease() {
		return release;
	}
	public void setRelease(GHRelease release) {
		this.release = release;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public GHCommit getCommit() {
		return commit;
	}
	public void setCommit(GHCommit commit) {
		this.commit = commit;
	}

}
