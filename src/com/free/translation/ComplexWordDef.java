package com.free.translation;

import java.io.Serializable;
import java.util.Iterator;
import java.util.TreeSet;
import com.free.searcher.*;


/**
 * Mục từ và các định nghĩa cùng với loại từ
 *
 */
public class ComplexWordDef implements Comparable<ComplexWordDef>, Serializable {

    private static final long serialVersionUID = 2904216776319456126L;
    private String name = "";
    private TreeSet<WordClass> definitions = new TreeSet<WordClass>();

    public ComplexWordDef() {
    }

    public ComplexWordDef(String name) {
        if (Util.isNotEmpty(name)) {
            this.name = name.trim().toLowerCase();
        } else {
            //Log.i("error", "name cannot be null");
			throw new NullPointerException("name cannot be null");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (Util.isNotEmpty(name)) {
            this.name = name.trim().toLowerCase();
        } else {
            throw new NullPointerException("name cannot be null");
        }
    }

    public ComplexWordDef add(WordClass wc) {
        if (wc != null) {
			if (definitions.contains(wc)) {
				WordClass item = definitions.floor(wc);
				item.addDefinition(wc.getDefinition());
			} else {
				definitions.add(wc);
			}
		} 
        return this;
    }

    public ComplexWordDef add(int type, String def) { //, String profession
        add(new WordClass(type).addDefinition(def));//, profession
        return this;
    }

    public TreeSet<WordClass> getDefinitions() {
//        WordClass[] arr = definitions.toArray(new WordClass[0]);
//        definitions.clear();
//        for (int i = 0; i < arr.length; i++) {
//            if (arr[i].getDefinition().length() > 0) {
//                definitions.add(arr[i]);
//            }
//        }
        return definitions;
    }

    public String getDefinition() {
        return getCompactDefinitions();
    }

    public String getCompactDefinitions() {
        StringBuilder sb = new StringBuilder();
        Iterator<WordClass> iter = definitions.iterator();
		WordClass curWC;
        while (iter.hasNext()) {
            curWC = iter.next();
            if (curWC.getDefinition().length() > 0) {
                sb.append(curWC.getDefinition());
                if (iter.hasNext()) {
                    sb.append("/");
                }
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object dicWord) {
        if (dicWord == null || !(dicWord instanceof ComplexWordDef)) {
//            throw new ClassCastException("not WordDefinition Class");
        	return false;
        } else {
//			Translator.LOG.info("this.name: " + this.name);
//			Translator.LOG.info("((ComplexWordDef) dicWord).getName(): " + ((ComplexWordDef) dicWord).getName());
            return this.name.equalsIgnoreCase(((ComplexWordDef) dicWord).getName());
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + this.name.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "[" + name + "] " + "[" + getDefinition() + "]";
    }

    @Override
    public int compareTo(ComplexWordDef o) {
        return o == null ? -1 : this.name.compareToIgnoreCase(o.getName());
    }

    public static void main(String[] args) {
        ComplexWordDef dw = new ComplexWordDef();
        dw.setName("abash");
        dw.add(new WordClass(Dictionary.VERB).addDefinition("làm bối rối, làm lúng túng, làm luống cuống"));
        System.out.println(dw);
    }
}
