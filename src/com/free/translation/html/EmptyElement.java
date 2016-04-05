package com.free.translation.html;

import java.util.Map;
import java.util.TreeMap;
import com.free.translation.util.*;
import com.free.searcher.*;

public class EmptyElement {

	private String name = "";

	private Map<String, String> attrs = new TreeMap<String, String>();
	
	public EmptyElement(String elementName) {
		if (Util.isNotEmpty(elementName)) {
			this.name = elementName;
		} else {
			throw new RuntimeException("element name cannot be empty");
		}
	}
	
	public String getName() {
		return name;
	}
	
	public EmptyElement addAttr(String key, String value) {
		if (Util.isNotEmpty(key) && Util.isNotEmpty(value)) {
			attrs.put(key, value);
		}
		return this;
	}
	
	public String getAttrs() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : attrs.entrySet()) {
			if (entry.getValue().indexOf("'") < 0) {
			sb.append(" ").append(entry.getKey()).append("='").append(entry.getValue()).append("'");
			} else {
				sb.append(" ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
			}
		}
		return sb.toString();
	}
	
	public String getStartTag() {
		return "<" + name + getAttrs() + ">";
	}

	public String getEndTag() {
		return "</" + name + ">";
	}
	
	@Override
	public String toString() {
		return getStartTag() + getEndTag();
	}
}
