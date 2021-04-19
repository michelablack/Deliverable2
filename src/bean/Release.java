package bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Release {
		
	private int index;
	private String id;
	private String name;
	private Date date;
	private int iv;
	private int ov;
	private int fv;
	private List<File> files = new ArrayList<>();
	public Release() {
		super();
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getIv() {
		return iv;
	}

	public void setIv(int iv) {
		this.iv = iv;
	}

	public int getOv() {
		return ov;
	}

	public void setOv(int ov) {
		this.ov = ov;
	}

	public int getFv() {
		return fv;
	}

	public void setFv(int fv) {
		this.fv = fv;
	}

	public List<File> getList() {
		return files;
	}

	public void setList(List<File> files) {
		this.files = files;
	}

	@Override
	public String toString() {
		return "Release [index=" + index + ", files=" + files + "]";
	}
	

}
