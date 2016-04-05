package com.free.translation;

import edu.stanford.nlp.trees.TypedDependency;

public class GrammarTypedDependency {
	private String grammarRelation;
	private String governorWord;
	private String dependentWord;
	private int governorWordPosition;
	private int dependentWordPosition;
	private TypedDependency typedDependency;
//	private String governorDefinition = "";
//	private String dependentDefinition = "";

//	public static final Pattern WORD_ORDER_PATTERN = Pattern.compile("([^ -]+)([-])([\\d]+)");

//	static Dictionary DICTIONARY = null;

//	static {
//		try {
//			DICTIONARY = DictionaryLoader.readDictionary();
//			DICTIONARY = Translator.DICTIONARY;
//			
//		} catch (FileNotFoundException e) {
//			throw new RuntimeException(e);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}
	
	public GrammarTypedDependency(TypedDependency typedDependency) {
		this.typedDependency = typedDependency;

		this.grammarRelation = typedDependency.reln().toString();

//		Matcher mat = WORD_ORDER_PATTERN.matcher(typedDependency.gov().toString());
		String gov = typedDependency.gov().toString();
		int lastMinus = gov.lastIndexOf("-");
//		if (mat.find()) {
//			this.governorWord = mat.group(1);
			this.governorWord = gov.substring(0, lastMinus);
			this.governorWordPosition = Integer.parseInt(gov.substring(lastMinus + 1));
//		}
		
		String dep = typedDependency.dep().toString();
		lastMinus = dep.lastIndexOf("-");
//		mat = WORD_ORDER_PATTERN.matcher(dep);
//		if (mat.find()) {
//			this.dependentWord = mat.group(1);
			this.dependentWord = dep.substring(0, lastMinus);
			this.dependentWordPosition = Integer.parseInt(dep.substring(lastMinus + 1));
//		}

//		ComplexWordDef govDefinition = DICTIONARY.getDefinition(governorWord);
//		if (govDefinition != null) {
//			governorDefinition = govDefinition.getDefinition();
//		}
//		ComplexWordDef depDefinition = DICTIONARY.getDefinition(dependentWord);
//		if (depDefinition != null) {
//			dependentDefinition = depDefinition.getDefinition();
//		}
	}

	public String getGrammarRelation() {
		return grammarRelation;
	}

	public String getGovWord() {
		return governorWord;
	}

	public void setGovWord(String governorWord) {
		this.governorWord = governorWord;
	}

	public String getDepWord() {
		return dependentWord;
	}

	public void setDepWord(String dependentWord) {
		this.dependentWord = dependentWord;
	}

	public int getGovPos() {
		return governorWordPosition;
	}

	public int getDepPos() {
		return dependentWordPosition;
	}

//	public String getGovDefinition() {
//		return governorDefinition;
//	}
//
//	public String getDepDefinition() {
//		return dependentDefinition;
//	}

	@Override
	public boolean equals(Object o) {
	    if (this == o) {
	        return true;
	      }
	      if (!(o instanceof TypedDependency)) {
	        return false;
	      }
	      final TypedDependency typedDep = (TypedDependency) o;

	      if (grammarRelation != null ? !grammarRelation.equals(typedDep.reln().toString()) : typedDep.reln() != null) {
	        return false;
	      }
	      if (governorWord != null ? !governorWord.equals(typedDep.gov().toString()) : typedDep.gov() != null) {
	        return false;
	      }
	      if (dependentWord != null ? !dependentWord.equals(typedDep.dep().toString()) : typedDep.dep() != null) {
	        return false;
	      }

	      return true;
	}

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.grammarRelation != null ? this.grammarRelation.hashCode() : 0);
        hash = 29 * hash + (this.governorWord != null ? this.governorWord.hashCode() : 0);
        hash = 29 * hash + (this.dependentWord != null ? this.dependentWord.hashCode() : 0);
        hash = 29 * hash + this.governorWordPosition;
        hash = 29 * hash + this.dependentWordPosition;
        return hash;
    }

	@Override
	public String toString() {
		return typedDependency.reln().toString() + "("
				+ typedDependency.gov().toString()
				+ typedDependency.gov().toPrimes().toString()
//				+ ": " + governorDefinition
				+ ", "
				+ typedDependency.dep().toString()
				+ typedDependency.dep().toPrimes().toString()
//				+ ": " + dependentDefinition
				+ ")";

	}

}
