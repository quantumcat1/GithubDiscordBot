package stuff;

import java.util.Date;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHUser;

public class RepoTS
{
	private GHRelease release;
	private GHCommit commit;
	private GHUser author;
	private Date timestamp;

	public RepoTS(GHRelease release, GHCommit commit, GHUser author, Date timestamp)
	{
		this.release = release;
		this.commit = commit;
		this.author = author;
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

	public GHUser getAuthor() {
		return author;
	}

	public void setAuthor(GHUser author) {
		this.author = author;
	}

}
