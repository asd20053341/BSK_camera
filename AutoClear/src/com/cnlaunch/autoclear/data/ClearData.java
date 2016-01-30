package com.cnlaunch.autoclear.data;

public class ClearData {
	public final static String TYPE_APP = "app";
	public final static String TYPE_FILE = "file";
	public final static String TYPE_DIR = "dir";

	private String mType = "";
	private String mPath = "";

	public ClearData() {
	}

	public ClearData(String type, String info) {
		this.mType = type;
		this.mPath = info;
	}

	public String getType() {
		return mType;
	}

	public void setType(String type) {
		this.mType = type;
	}

	public String getPath() {
		return mPath;
	}

	public void setPath(String path) {
		this.mPath = path;
	}

	@Override
	public String toString() {
		return "[type=" + mType + ", path=" + mPath + "]";
	}
}
