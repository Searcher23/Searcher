package com.free.translation;

import java.util.TreeSet;
import java.util.logging.Logger;

import edu.stanford.nlp.ling.HasWord;
import com.free.translation.*;
import java.util.*;
import android.util.*;


public class WordInfo implements Comparable<WordInfo>, HasWord {
	private static final long serialVersionUID = -8715402241741145825L;
	private String startSign = "";
	private String endSign = "";
	private String startTag = "";
	private String endTag = "";
	private int order = 0;
	private String name = "";
	private String definition = "";
	private int type = Dictionary.OTHER;
	//private static final Logger log = Constants.LOGGER;	// Logger.getLogger(WordInfo.class.getName());
    private TreeSet<WordClass> definitions = new TreeSet<WordClass>();
	
	static TreeSet<String> notYetDefined = new TreeSet<String>();
	
	public WordInfo(int order, String name, String definition, String startSign, String endSign) {
		if (name == null) {
			//name = "";
			throw new NullPointerException("Name cannot be null");
		}
		if (startSign == null) {
			startSign = "";
		}
		if (endSign == null) {
			endSign = "";
		}
		this.order = order;
		this.name = name.trim();
		this.definition = definition;
		//this.startTag = "";
		this.startSign = startSign;
		//this.endTag = "";
		this.endSign = endSign;
	}

	public WordInfo(int order, String name, TreeSet<WordClass> definitions, String startSign, String endSign) {
		if (name == null) {
			throw new NullPointerException("Name cannot be null");
			//name = "";
		}
		if (startSign == null) {
			startSign = "";
		}
		if (endSign == null) {
			endSign = "";
		}
		this.order = order;
		this.name = name.trim();
		this.definitions = definitions;
		this.definition = getDefinition(Dictionary.OTHER);
		//this.startTag = "";
		this.startSign = startSign;
		//this.endTag = "";
		this.endSign = endSign;
	}
	
	public WordInfo(int order, String startTag, String startSign, String name,
			String definition, String endSign, String endTag) {
		if (name == null) {
			//name = "";
			throw new NullPointerException("Name cannot be null");
		}
		if (startTag == null) {
			startTag = "";
		}
		if (endTag == null) {
			endTag = "";
		}
		if (startSign == null) {
			startSign = "";
		}
		if (endSign == null) {
			endSign = "";
		}
		this.order = order;
		this.startTag = startTag;
		this.startSign = startSign;
		this.name = name.trim();
		this.definition = definition;
		this.endTag = endTag;
		this.endSign = endSign;
	}
	
	public void clear() {
		this.name = "";
		this.definition = "";
		this.definitions = new TreeSet<WordClass>();
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null) {
			throw new NullPointerException("Name cannot be null");
		}
		this.name = name.trim();
	}

	public String getDefinition(int type) {
		StringBuilder sb = new StringBuilder();
		for (WordClass wc : definitions) {
			if (wc.getType() == type) {
				return wc.getDefinition();
			}
			if (wc.getDefinition().length() > 0) {
				sb.append(wc.getDefinition());
				sb.append("/");
			}
		}
		int length = sb.length();
		if (length > 0) {
			return sb.substring(0, --length);
		} else {
			return sb.toString();
		}
	}
	
	public String getDefinition() {
		if (definitions.size() > 0 && type > Dictionary.OTHER) {
			definition = getDefinition(type);
		}
		if (definition != null && definition.trim().length() > 0) {
			if (name.toLowerCase().equals(name)) {
				return definition.toLowerCase();
			} else if (name.length() > 1 && name.toUpperCase().equals(name)) {
				return definition.toUpperCase();
			} else {
				String nameSubstring = name.substring(0, 1);
				if (nameSubstring.toUpperCase().equals(nameSubstring)) {
					return new StringBuilder(definition.substring(0, 1)
											 .toUpperCase()).append(definition, 1, definition.length())
							.toString();
				} else {
					return definition;
				}
			}
		} else if (definition != null) {
			return definition;
		} else {
			return name;
		}
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getStartSign() {
		return startSign;
	}

	public void setStartSign(String startSign) {
		this.startSign = startSign;
	}

	public String getEndSign() {
		return endSign;
	}

	public void setEndSign(String endSign) {
		this.endSign = endSign;
	}

	public String getStartTag() {
		return startTag;
	}

	public void setStartTag(String startTag) {
		this.startTag = startTag;
	}

	public String getEndTag() {
		return endTag;
	}

	public void setEndTag(String endTag) {
		this.endTag = endTag;
	}

	public int getType() {
		return type;
	}

	public boolean hasType(int checkType) {
		for (WordClass wc : definitions) {
			if (wc.getType() == checkType) {
				return true;
			}
		}
		return false;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public int compareTo(WordInfo wi) {
		if (wi == null) {
			throw new NullPointerException("wordInfo cannot be null");
		}
		return (this.name.toLowerCase() + this.order).compareTo(wi
				.getName().toLowerCase() + wi.getOrder());
	}

	@Override
	public boolean equals(Object wordInfo) {
		if (wordInfo == null || !(wordInfo instanceof WordInfo)) {
			return false;
			//throw new ClassCastException("not WordInfo class");
		} else {
			WordInfo wi = (WordInfo) wordInfo;
			return (this.name.toLowerCase() + this.order).equals(
					wi.getName().toLowerCase() + wi.getOrder());
		}
	}

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + this.order;
        hash = 37 * hash + this.name.hashCode();
        return hash;
    }

	public boolean isTheSame(WordInfo otherWord) {
		return otherWord != null ? 
			this.name.equalsIgnoreCase(otherWord.name)
			&& this.getDefinition().equalsIgnoreCase(otherWord.getDefinition())
			&& this.order == otherWord.order
				: false;
	}

	@Override
	public String toString() {
//		return new StringBuilder(order).append(": ").append(startTag)
//				.append(startSign).append("[").append(name).append("]")
//				.append(": ").append(definition).append(endSign).append(endTag)
//				.toString();
		return translated();
	}
	
	public String signTranslated() {
		return new StringBuilder(startTag).append(startSign).append(name).append(endSign).append(endTag).toString();
	}
	
	private static final String SPAN_TITLE_START = "<span title=\"";
	private static final String SPAN_TITLE_END = "</span>";
	
	public String translated() {
		if (definitions.size() > 0 && type > Dictionary.OTHER) {
			definition = getDefinition(type);
		}
		if (definition != null && definition.length() > 0) {
			if (definition.indexOf("/") > 0) {
				return new StringBuilder(startTag).append(startSign)
					.append(SPAN_TITLE_START).append(name).append("\">")
					.append("[").append(getDefinition()).append("]")
					.append(SPAN_TITLE_END)
					.append(endSign).append(endTag)
					.toString();
			} else {
				return new StringBuilder(startTag).append(startSign)
					.append(SPAN_TITLE_START).append(name).append("\">")
					.append(getDefinition())
					.append(SPAN_TITLE_END)
					.append(endSign).append(endTag)
					.toString();
			}
		} else if (name.trim().length() > 0) {
			notYetDefined.add(name);
			return new StringBuilder(startTag).append(startSign)
					//.append(SPAN_TITLE_START).append(getName()).append("\">")
					.append(name)
					//.append(SPAN_TITLE_END)
					.append(endSign).append(endTag).toString();
		} else if (name.length() > 0) {
			return new StringBuilder(startTag).append(startSign).append(name).append(endSign).append(endTag).toString();
		} else {
			return new StringBuilder(startTag).append(startSign).append(endSign).append(endTag).toString();
		}
	}
	
	public boolean isDefinitionEmpty() {
		return definition == null || definition.trim().length() == 0;
	}

	public void printContent() {
		Log.i("WordInfo", this.toString());
	}

	@Override
	public String word() {
		return name;
	}

	@Override
	public void setWord(String word) {
		this.name = word;
	}

}
