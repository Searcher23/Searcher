package com.free.translation;

import java.io.Serializable;
import com.free.searcher.*;

/**
 * Loại từ và các định nghĩa
 *
 */
public class WordClass implements Serializable, Comparable<WordClass> {
	
	private static final long serialVersionUID = 3179655625844316015L;
	
	private int type = Dictionary.OTHER;
	//private String profession = "";
	private String definitions = "";

	public WordClass() {
	}
	
	public WordClass(int type) {
		super();
		this.type = type;
	}

//	public WordClass(int type, String profession) {
//		super();
//		this.type = type;
//		//this.profession = Util.trim(profession);
//	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

//	public String getProfession() {
//		return profession;
//	}
//
//	public void setProfession(String profession) {
//		this.profession = Util.trim(profession);
//	}
	
	public String getDefinition() {
		return definitions;
	}

	public WordClass addDefinition(String def) {
		
		//Log.d("addDefinition", def);
		String[] arr = def.split("[,;/]");
		StringBuilder sb = new StringBuilder(definitions);
		for (String trimed : arr) {
			trimed = trimed.trim().toLowerCase();
//			System.out.println("trimed: " + trimed);
			if (trimed.length() > 0 && sb.indexOf(trimed) < 0) {
				//Log.d("addDefinition.trimed", trimed);
//				System.out.println("added " + trimed);
				if (sb.length() > 0) {
					sb.append("/");
				}
				sb.append(trimed);
			}
		}
		definitions = sb.toString();
		return this;
	}
	
	public WordClass setDefinition(String def) {
		StringBuilder sb = new StringBuilder();
		String[] arr = def.split("[,;/]");
		for (String trimed : arr) {
			trimed = trimed.trim().toLowerCase();
			if (trimed.length() > 0 && !sb.toString().contains(trimed)) {
				if (sb.length() > 0) {
					sb.append("/");
				}
				sb.append(trimed);
			}
		}
		definitions = sb.toString();
		return this;
	}
	
	@Override
	public String toString() {
		return type + ": [" + getDefinition() + "]"; // + ": " + profession
	}
	
	@Override
	public int compareTo(WordClass o) {
		return (this.type)-(o.type); // + this.profession + compareToIgnoreCase(o.getProfession()
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof WordClass)) {
			return false;
		} else {
			return (this.type) == ((WordClass)obj).type;
		}
	}

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.definitions != null ? this.definitions.hashCode() : 0);
        return hash;
    }

	public static void main(String[] args) {
//		WordClass wc = new WordClass(Dictionary.NOUN, "math");
//		wc.addDefinition(",plus, Divide;");
//		wc.addDefinition("minus").addDefinition("multiply# test");
//		wc.addDefinition(",plus, Divide;");
//		
//		System.out.println(wc);
	}
}
