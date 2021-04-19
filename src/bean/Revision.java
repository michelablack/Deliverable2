package bean;

import java.util.Date;
import java.util.List;


public class Revision {
	
	private String commit;
	private List<File> files;
	private Date date;
	private int version;
	private Release release;
	
	public Revision() {
		super();
	} 
	public String getCommit() {
		return commit;
	}
	public void setCommit(String commit) {
		this.commit = commit;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public List<File> getFiles() {
		return files;
	}
	public void setFiles(List<File> files) {
		this.files = files;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public Release getRelease() {
		return release;
	}
	public void setRelease(Release release) {
		this.release = release;
	}
}
