package com.free.translation;

class Word {
	private String name;
	private int type;
	private String profession;
	
	public String getProfession() {
		return profession;
	}

	public void setProfession(String profession) {
		this.profession = profession;
	}

	private String definitions;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getDefinitions() {
		return definitions;
	}

	public void setDefinitions(String definition) {
		this.definitions = definition;
	}

}
