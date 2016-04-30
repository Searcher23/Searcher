package com.free.translation.html;

import java.util.ArrayList;
import java.util.List;
import com.free.searcher.*;

public class Element extends EmptyElement {
	
	private List<Object> data = new ArrayList<Object>();
	
	public Element(String name) {
		super(name);
	}
	
	public Element addData(Object obj) {
		data.add(obj);
		return this;
	}
	
	public Object remove(int index) {
		return data.remove(index);
	}
	
	public void clear() {
		data.clear();
	}
	
	public Object get(int index) {
		return data.get(index);
	}
	
	public Object getLast() {
		int size = data.size();
		if (size > 0) {
			return data.get(size - 1);
		} else {
			return null;
		}
	}
	
	public int size() {
		return data.size();
	}
	
	@Override
	public String toString() {
		return getStartTag() + Util.collectionToStr(data) + getEndTag();
	}
}
