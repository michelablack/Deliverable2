package bean;

import java.util.Date;

public class File{
	
	private String name;
	private Date fileAdded;
	private Date fileDeleted;
	private String isBuggy = "NO";
	private int[] buggy;
	private int[] version;
	private int lastIndex;
	private float num;
	private float sumLocAdded;
	private float sumLocDeleted;
	private float maxLocAdded;
	private float avgLocAdded;
	private float churn;
	private float maxChurn;
	private float avgChurn;
	private float chgSetSize;
	private float sumChgSetSize;
	private float maxChgSet;
	private float avgChgSet;
	private float locAdded;
	private float locDeleted;
	
	public File() {
		super();
	}
	
	public File(File original) {
	    this.name = original.name;
		this.fileAdded = original.fileAdded;
		this.fileDeleted = original.fileDeleted;
		this.isBuggy = original.isBuggy;
		this.buggy = original.buggy;
		this.version = original.version;
		this.lastIndex = original.lastIndex;
		this.num = original.num;
		this.sumLocAdded = original.sumLocAdded;
		this.sumLocDeleted = original.sumLocDeleted;
		this.maxLocAdded = original.maxLocAdded;
		this.avgLocAdded = original.avgLocAdded;
		this.churn = original.churn;
		this.maxChurn = original.maxChurn;
		this.avgChurn = original.avgChurn;
		this.chgSetSize = original.chgSetSize;
		this.sumChgSetSize = original.sumChgSetSize;
		this.maxChgSet = original.maxChgSet;
		this.avgChgSet = original.avgChgSet;
		this.locAdded = original.locAdded;
		this.locDeleted = original.locDeleted;
	}
	
	public Date getFileAdded() {
		return fileAdded;
	}

	public void setFileAdded(Date fileAdded) {
		this.fileAdded = fileAdded;
	}

	public Date getFileDeleted() {
		return fileDeleted;
	}

	public void setFileDeleted(Date fileDeleted) {
		this.fileDeleted = fileDeleted;
	}

	public int[] getVersion() {
		return version;
	}

	public void setVersion(int[] version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		File other = (File) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public int[] getBuggy() {
		return buggy;
	}

	public void setBuggy(int[] buggy) {
		this.buggy = buggy;
	}

	public String getIsBuggy() {
		return isBuggy;
	}

	public void setIsBuggy(String isBuggy) {
		this.isBuggy = isBuggy;
	}
	public float getLocAdded() {
		return locAdded;
	}

	public void setLocAdded(float locAdded) {
		this.locAdded = locAdded;
	}

	public float getLocDeleted() {
		return locDeleted;
	}

	public void setLocDeleted(float locDeleted) {
		this.locDeleted = locDeleted;
	}

	public float getSumLocAdded() {
		return sumLocAdded;
	}

	public void setSumLocAdded(float sumLocAdded) {
		this.sumLocAdded = sumLocAdded;
	}

	public float getMaxLocAdded() {
		return maxLocAdded;
	}

	public void setMaxLocAdded(float maxLocAdded) {
		this.maxLocAdded = maxLocAdded;
	}

	public float getAvgLocAdded() {
		return avgLocAdded;
	}

	public void setAvgLocAdded(float avgLocAdded) {
		this.avgLocAdded = avgLocAdded;
	}

	public float getChurn() {
		return churn;
	}

	public void setChurn(float churn) {
		this.churn = churn;
	}

	public float getMaxChurn() {
		return maxChurn;
	}

	public void setMaxChurn(float maxChurn) {
		this.maxChurn = maxChurn;
	}

	public float getAvgChurn() {
		return avgChurn;
	}

	public void setAvgChurn(float avgChurn) {
		this.avgChurn = avgChurn;
	}

	public float getChgSetSize() {
		return chgSetSize;
	}

	public void setChgSetSize(float chgSetSize) {
		this.chgSetSize = chgSetSize;
	}

	public float getMaxChgSet() {
		return maxChgSet;
	}

	public void setMaxChgSet(float maxChgSet) {
		this.maxChgSet = maxChgSet;
	}

	public float getAvgChgSet() {
		return avgChgSet;
	}

	public void setAvgChgSet(float avgChgSet) {
		this.avgChgSet = avgChgSet;
	}

	public float getNum() {
		return num;
	}

	public void setNum(float num) {
		this.num = num;
	}

	public int getLastIndex() {
		return lastIndex;
	}

	public void setLastIndex(int lastIndex) {
		this.lastIndex = lastIndex;
	}

	public float getSumChgSetSize() {
		return sumChgSetSize;
	}

	public void setSumChgSetSize(float sumChgSetSize) {
		this.sumChgSetSize = sumChgSetSize;
	}

	@Override
	public String toString() {
		return ""+name+"";
	}

	public float getSumLocDeleted() {
		return sumLocDeleted;
	}

	public void setSumLocDeleted(float sumLocDeleted) {
		this.sumLocDeleted = sumLocDeleted;
	}

	
	


	
	
}
