package bean;

import java.util.Date;

public class Bug {

	private Date openVersion;
	private Date injVersion;
	private Date fixVersion;
	private String key;
	private Release release;
	
	public Bug() {
		super();
	}

	public Date getOpenVersion() {
		return openVersion;
	}

	public void setOpenVersion(Date openVersion) {
		this.openVersion = openVersion;
	}

	public Date getInjVersion() {
		return injVersion;
	}

	public void setInjVersion(Date injVersion) {
		this.injVersion = injVersion;
	}

	public Date getFixVersion() {
		return fixVersion;
	}

	public void setFixVersion(Date fixVersion) {
		this.fixVersion = fixVersion;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Release getRelease() {
		return release;
	}

	public void setRelease(Release release) {
		this.release = release;
	}

	@Override
	public String toString() {
		return "Bug [openVersion=" + openVersion + ", injVersion=" + injVersion + ", fixVersion=" + fixVersion
				+ ", key=" + key + ", release=" + release + "]";
	}
	
}
