package com.bsk.music.entity;

public class Music {
	private String name;
	private String englishName;
	private String url;
	private Integer type;

	public Music(String name, String englishName, String url, Integer type) {
		super();
		this.name = name;
		this.englishName = englishName;
		this.url = url;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getEnglishName() {
		return englishName;
	}

	public void setEnglishName(String englishName) {
		this.englishName = englishName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "name: " + getName() + ",englishName: " + getEnglishName() + ",url: " + getUrl() + ", type: " + getType()
				+ "\n";
	}
}
