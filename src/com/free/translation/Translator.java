package com.free.translation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.stream.StreamResult;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;


import java.net.*;
import com.free.searcher.*;
import java.text.*;
import android.util.*;
//import android.util.*;

public class Translator {

	private static LexicalizedParser LEXICALIZED_PARSER = null;
//	public static Dictionary FULL_DICTIONARY = new Dictionary();
	public static Dictionary ORI_DICT = null;
//	public static Dictionary CUSTOM_FIRST_DICTIONARY = new Dictionary();
//	public static Dictionary HND_FIRST_DICTIONARY = new Dictionary();
	public static Dictionary GEN_DICT = null;

	// public static enum DICT_TYPE {
	// 	FULL, CUSTOM_FIRST, HND_FIRST, CUSTOM_ONLY
	// };
	
	// public static DICT_TYPE CHOSEN_DICT = DICT_TYPE.CUSTOM_FIRST;

//	public static final Logger logger = Constants.LOGGER;

	static {
		try {
			DictionaryLoader dictionaryLoader = new DictionaryLoader();
			GEN_DICT = dictionaryLoader.readGenExcelDict(false);
			ORI_DICT = dictionaryLoader.readOriExcelDict();
//			Dictionary textDict = DictionaryLoader.readNewDictionary("dicts-parsers/HNDDict.txt");
//			GEN_DICT.addNewDictionary(textDict);
//			Log.i("ORI_DICT", ORI_DICT.toString());
//			Log.i("GEN_DICT", GEN_DICT.toString());
			
//			Constants.LOG.log(Level.INFO, "CUSTOM_ONLY_DICTIONARY freeMemory: {0}", Runtime.getRuntime().freeMemory());
//			Dictionary HND_DICTIONARY;
//			if (Constants.READ_DICT_FROM_PLAIN_FILE) {
//				HND_DICTIONARY = DictionaryLoader.readNewTextDictionary();
//			} else {
//				HND_DICTIONARY = Dictionary.restore(Constants.SERIALED_PLAIN_FILE_NAME);
//			}

//			Dictionary HND_DICTIONARY = DictionaryReader.readNewTextDictionary().getDictionary();
//			Constants.LOG.log(Level.INFO, "HND_DICTIONARY freeMemory: {0}", Runtime.getRuntime().freeMemory());
			
//			FULL_DICTIONARY.addNewDictionary(CUSTOM_ONLY_DICTIONARY);
//			FULL_DICTIONARY.appendDictionary(HND_DICTIONARY);
//			CHOSEN_DICTIONARY = FULL_DICTIONARY;
//			Constants.LOG.log(Level.INFO, "FULL_DICTIONARY freeMemory: {0}", Runtime.getRuntime().freeMemory());
			
//			CUSTOM_FIRST_DICTIONARY.addNewDictionary(CUSTOM_ONLY_DICTIONARY);
//			CUSTOM_FIRST_DICTIONARY.addNewDictionary(HND_DICTIONARY);
//		
//			HND_FIRST_DICTIONARY.addNewDictionary(HND_DICTIONARY);
//			HND_FIRST_DICTIONARY.addNewDictionary(CUSTOM_ONLY_DICTIONARY);
			
		} catch (Throwable e) {
			//Constants.LOGGER.throwing(Translator.class.getName(), "startup", e);
			e.printStackTrace();
			
		}
	}

	private Translator() {
	}
	
	static void initParser() {
		if (LEXICALIZED_PARSER == null) {
			LEXICALIZED_PARSER = new LexicalizedParser(Constants.PARSER_FILE_NAME_VALUE);

			LEXICALIZED_PARSER.setOptionFlags(new String[] { "-maxLength",
					Constants.MAX_PARSE_LENGTH_VALUE, "-retainTmpSubcategories" });
			// LEXICALIZED_PARSER.setOptionFlags(
			//		new String[]{"-maxLength", MAX_PARSE_LENGTH_VALUE, 
			//						"-outputFormatOptions", "treeDependencies", "-retainTmpSubcategories"});
			// LEXICALIZED_PARSER.setOptionFlags(
			// 		new String[]{"-maxLength", MAX_PARSE_LENGTH_VALUE, 
			//						"-outputFormatOptions", "basicDependencies", "-retainTmpSubcategories"});
			// LEXICALIZED_PARSER.setOptionFlags(
			//		new String[]{"-maxLength", MAX_PARSE_LENGTH_VALUE, 
			//						"-outputFormatOptions", "CCPropagatedDependencies", "-retainTmpSubcategories"});
		}
	}

	private static final TreebankLanguagePack tlp = new PennTreebankLanguagePack();
	private static final GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();

	private static Collection<TypedDependency> initGrammarStructure(Tree parse) {
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		Collection<TypedDependency> tdl = gs.typedDependenciesCollapsed();
		return tdl;
	}

	public static void main(String[] args) throws IOException {

		List<String> sentenceList = new LinkedList<String>();
		sentenceList.add("These four meditation subjects serve as \"sabbatthaka kammaṭṭhānas\"; that is, the meditation subjects generally desireable in all matters.");
		sentenceList.add("Citta visuddhi nāma sa upacāra aṭṭha–samāpattiyo");
		sentenceList.add("Citta visuddhi nāma sa upacāra aṭṭha- samāpattiyo");

//		sentenceList.add("The papers actually represent the essential summary "
//				+ "of Tranquillity Meditation and Insight Meditation which are being conducted "
//				+ "in minute detail strictly in accordance with the teachings of the Buddha in the "
//				+ "International Pa-auk Forest Buddha Sāsana Centres throughout Myanmar. "
//				+ "Two papers entitled “Breakthrough in Tranquillity Meditation "
//				+ "(Samatha Bhavana)” and “Breakthrough in Insight Meditation (Vipassanā)”, "
//				+ "which were read and discussed with great interest in the World Buddhist Summit "
//				+ "held in Yangon, Myanmar, on December 9 to 11, 2004, are printed and published in "
//				+ "the form of the present book for the benefit of the private who like to know "
//				+ "a brief and concise account of the practical aspects of Buddhism called "
//				+ "Samatha-Vipassanā.");
//		sentenceList.add(
//				"This book will introduce the readers to the proper way of undertaking Samatha-Vipassanā " +
//				"as taught by the Buddha, developing the right concentration and penetrating into the ultimate realities " +
//				"called \"Paramatthas\" defining each ultimate reality precisely by " +
//				"its characteristic, function, manifestation and nearest cause, investigating " +
//				"the causal relations of Dependent Arising (Paṭiccasamuppāda) that beautifully describes " +
//				"the round of rebirth, and contemplating all mentality and materiality (nāma-rūpa), " +
//				"causes and effects, internal and external, pertaining to the past, the present and the future, " +
//				"as impermanent (anicca), painful (dukkha) and not-self (anatta) in various ways.");
//		sentenceList.add("The contents and the scope of the two papers will demonstrate clearly that " +
//				"the Samatha-Vipassanā meditation is being maintained and practised fully in its original pure form " +
//				"in the Union of Myanmar. In fact Myanmar preserves and maintains all the teachings of the Buddha " +
//				"both in words and practice (Pariyatti and Paṭipatti) and can share them with the world for " +
//				"the welfare of all mankind.");
//		sentenceList.add("Undertaking Tranquillity Meditation to develop the right concentration and " +
//				"practising Insight Meditation to develop wisdom to the highest level means walking on " +
//				"the noblest Path that leads to ever-lasting peace and eternal happiness called Nibbana. " +
//				"Everyone is welcome to take part in this delightful noble task which will surely bring great benefits " +
//				"in this very life.");
		sentenceList.add("Bell, based in Los Angeles, makes and distributes electronic, computer and building products.");
		sentenceList.add("She looks very beautiful");
		sentenceList.add("The accident happened as the night was falling");
		sentenceList.add("If you know who did it, you should tell the teacher");
		sentenceList.add("Genetically modified food");
		sentenceList.add("less often");
		sentenceList.add("The man has been killed by the police");
		sentenceList.add("Effects caused by the protein are important");
		sentenceList.add("Sam eats red meat");
		sentenceList.add("Sam, my brother");
		sentenceList.add("Bill (John's cousin and John's cousin)");
		sentenceList.add("What is that?");
		sentenceList.add("Reagan has died");
		sentenceList.add("He should leave");
		sentenceList.add("Kennedy has been killed");
		sentenceList.add("He says that you like to swim");
		sentenceList.add("I am certain that he did it");
		sentenceList.add("I admire the fact that you are honest");
		sentenceList.add("He says that you like to swim");
		sentenceList.add("Bill is big and honest");
		sentenceList.add("They either ski or snowboard");
		sentenceList.add("Bill is big");
		sentenceList.add("Bill is an honest man");
		sentenceList.add("What she said makes sense");
		sentenceList.add("What she said is not true");
		sentenceList.add("That she lied was suspected by everyone");
		sentenceList.add("Then, as if to show that he could, ...");
		sentenceList.add("The man is here");
		sentenceList.add("Which book do you prefer?");
		sentenceList.add("She gave me a raise");
		sentenceList.add("They win the lottery");
		sentenceList.add("There is a ghost in the room");
		sentenceList.add("Points to establish are . . .");
		sentenceList.add("I don't have anything to say");
		sentenceList.add("She gave me a raise");
		sentenceList.add("Forces engaged in fighting after insurgents attacked");
		sentenceList.add("I like dogs as well as cats");
		sentenceList.add("He cried because of you");
		sentenceList.add("Bill is not a scientist");
		sentenceList.add("Bill doesn't drive");
		sentenceList.add("Oil price futures");
		sentenceList.add("The director is 65 years old");
		sentenceList.add("6 feet long");
		sentenceList.add("Shares eased a fraction");
		sentenceList.add("IBM earned $ 5 a share");
		sentenceList.add("The silence is itself significant");
		sentenceList.add("90% of Australians like him, the most of any country");
		sentenceList.add("Clinton defeated Dole");
		sentenceList.add("The baby is cute");
		sentenceList.add("Dole was defeated by Clinton");
		sentenceList.add("Sam eats 3 sheep");
		sentenceList.add("I lost $ 3.2 billion");
		sentenceList.add("The guy, John said, left early in the morning");
		sentenceList.add("Truffes picked during the spring are tasty");
		sentenceList.add("Bill tried to shoot demonstrating his incompetence");
		sentenceList.add("We have no information on whether users are at risk");
		sentenceList.add("They heard about you missing classes");
		sentenceList.add("It is warmer in Greece than in Italy");
		sentenceList.add("I sat on the chair");
		sentenceList.add("their offices");
		sentenceList.add("Bill's clothes");
		sentenceList.add("Both the boys and the girls are here");
		sentenceList.add("All the boys are here");
		sentenceList.add("I saw a cat in a hat");
		sentenceList.add("I saw a cat with a telescope");
		sentenceList.add("He is responsible for meals");
		sentenceList.add("He purchased it without paying a premium");
		sentenceList.add("They shut down the station");
		sentenceList.add("Go home!");
		sentenceList.add("He talked to him in order to secure the account");
		sentenceList.add("About 200 people came to the party");
		sentenceList.add("I saw the man you love");
		sentenceList.add("I saw the book which you bought");
		sentenceList.add("I saw the man whose wife you love");
		sentenceList.add("Last night, I swam in the pool");
		sentenceList.add("He says that you like to swim");
		sentenceList.add("I am ready to leave");
		sentenceList.add("Tom likes to eat fish");
		sentenceList.add("Bill went over the river and through the woods");
//		sentenceList.add(
//				"3.11: Before we attain the fourth jhàna, and eradicate ignorance (avijjà), " +
//				"many unwholesome thoughts still arise due to bad habits. For example, in our daily life " +
//				"(outside a meditation retreat) we know that greed or hatred arises. " +
//				"Can we use foulness meditation (asubha), " +
//				"or lovingkindness meditation (mettà bhàvanà) to remove them? " +
//				"Or should we ignore them and just concentrate on our meditation subject, " +
//				"and let them disappear automatically?         126" +
//				""
//				);
		sentenceList.add("This good book is cheap");
		sentenceList.add("why is the sky blue");
		sentenceList.add("The man was killed by a police");
		sentenceList.add("The man has been killed by a police");
		sentenceList.add("This means hello world");
		sentenceList.add("This means \"hello world\"");
		sentenceList.add("That means \"hello world\"");
		
		
		sentenceList.add("The boy climbed a tree.");
		sentenceList.add("The young boy climbed a tall tree.");
		sentenceList.add("The young boy quickly climbed a tall tree.");
		sentenceList.add("Jill reads.");
		sentenceList.add("The brown dog with the red collar always barks loudly.");
		sentenceList.add("The dog barked and growled loudly.");
		sentenceList.add("The sun was setting in the west and the moon was just rising.");
		sentenceList.add("The sun was setting in the west. The moon was just rising.");
		sentenceList.add("I walked to the shops, but my husband drove.");
		sentenceList.add("I might watch the film, or I might visit my friends.");
		sentenceList.add("My friend enjoyed the film, but she didn't like the actor.");
		sentenceList.add("My mother likes dogs that don't bark.");
		sentenceList.add("Beer and wine are my favourite drinks.");
		sentenceList.add("I play football twice a week.");
		sentenceList.add("I've got a car.");
		sentenceList.add("David's working in the bank.");
		sentenceList.add("David works in a bank.");
		sentenceList.add("Working at the computer all day made David's head ache.");
		sentenceList.add("Michael Schumaker drove the race car.");
		sentenceList.add("He opened the door.");
		sentenceList.add("I gave him the book.");
		sentenceList.add("David disagreed.");
		sentenceList.add("David gave her a present.");
		sentenceList.add("I sometimes have trouble with adverbs.");
		sentenceList.add("He spoke very quietly.");
		sentenceList.add("I've read that book three times.");
		sentenceList.add("She's gone to the bank.");
		sentenceList.add("He is Spanish.");
		sentenceList.add("She became an engineer.");
		sentenceList.add("That man looks like John.");
		sentenceList.add("They painted the house red.");
		sentenceList.add("She called him an idiot!");
		sentenceList.add("I saw her standing there.");
		sentenceList.add("My ache is very big.");
		sentenceList.add("My aches are very big.");
		sentenceList.add("My ear ached.");
		sentenceList.add("My ears ached.");
		sentenceList.add("My ear was ached.");
		sentenceList.add("My ears were ached.");
		sentenceList.add("My ear has been ached.");
		sentenceList.add("My ears have been ached.");
		sentenceList.add("I went to school yesterday.");
		sentenceList.add("These books are so nice.");
		sentenceList.add("I don't like these.");
		sentenceList.add("I don't like this.");
		sentenceList.add("Each ultimate reality lives so short.");
		sentenceList.add("I was ill and so I could not come.");
		sentenceList.add("She spends 3 days in Malaysia.");
		sentenceList.add("She's spending 3 days in Malaysia.");
		sentenceList.add("She's spent 3 days in Malaysia.");
		sentenceList.add("I've spent 3 days in Malaysia.");
		sentenceList.add("I have spent 3 days in Malaysia.");
		sentenceList.add("I spent 3 days in Malaysia.");
		sentenceList.add("I am spending 3 days in Malaysia.");
		sentenceList.add("I have been being spent 3 days in Malaysia.");
		sentenceList.add("Mary is taller than Max.");
		sentenceList.add("Mary is the tallest of all the students.");
		sentenceList.add("Of the three students, Max is the oldest.");
		sentenceList.add("My hair is longer than your hair.");
		sentenceList.add("Max's story is the longest story I've ever heard.");
		sentenceList.add("It is getting hotter and hotter.");
		sentenceList.add("The storm became more and more violent.");
		sentenceList.add("The sooner this is done, the better it is.");
		sentenceList.add("John is more generous than Jack.");
		sentenceList.add("John is the most generous of all the people I know.");
		sentenceList.add("Health is more important than money.");
		sentenceList.add("Of all the people I know, Max is the most important.");
		sentenceList.add("Women are more intelligent than men.");
		sentenceList.add("Mary is the most intelligent person I've ever met.");
		sentenceList.add("What kind of rose?");
		sentenceList.add("What are you doing?");
		sentenceList.add("Who are you?");
		sentenceList.add("The method most often used involves in steps");
		sentenceList.add("The method most often used");
		sentenceList.add("method most often used");

//		translateFromPlainTextFile(new File("/storage/emulated/0/backups/Living and dying new.doc.converted.txt"), 
//		"/storage/emulated/0/backups/Living and dying.doc.html", null);
		
		
//		LOG.info(translateParagraphs(sentenceList));

//		trans.translateFromSimpleWordFile(new File(ORI_WORD_FILE_NAME), NEW_HTML_FILE_NAME);
//		LOG.log(Level.INFO, "ORI_TEXT_FILE_NAME: {0}", Constants.ORI_TEXT_FILE_NAME);
//		LOG.log(Level.INFO, "NEW_HTML_FILE_NAME: {0}", Constants.NEW_HTML_FILE_NAME);
//		translateFromPlainTextFile(new File(Constants.ORI_TEXT_FILE_NAME), Constants.NEW_HTML_FILE_NAME);
		
		//translateFromParagraphList(sentenceList, "/storage/emulated/0/.com.free.translation/data/translateResult.html");
//		openOfficeConvertFailed(new File("i:/dest/Breakthrought - Copy (3).docx"), "I:/dest/Breakthrought - Copy (3).docx.html");
//		openOfficeConvertFailed(new File("i:/dest/simpledoc.doc"), "I:/dest/simpledoc.doc.html");
	}

	// amod -> det
	// amod -> amod -> amod
	public static String reorderPhrases(List<WordInfo> wordList) {
		if (wordList.size() == 0) {
			return "";
		}
		initParser();
		//Log.i("before wordList", wordList.toString());
		Tree parse = null;
		StringBuilder sb = new StringBuilder();

		try {
			
//			Constants.LOG.log(Level.INFO, "LEXICALIZED_PARSER1 freeMemory: {0}", Runtime.getRuntime().freeMemory());
			LEXICALIZED_PARSER.reset();
			parse = (Tree) LEXICALIZED_PARSER.apply(wordList);
			
//			Constants.LOG.log(Level.INFO, "LEXICALIZED_PARSER2 freeMemory: {0}", Runtime.getRuntime().freeMemory());
		} catch (UnsupportedOperationException uoe) {
			Log.e(Translator.class.getName(), "reorderPhrases", uoe);
			buildWordInfoToSentence(wordList, sb);
			return sb.toString();
		} catch (OutOfMemoryError t) {
			LEXICALIZED_PARSER = null;
			
			initParser();
			Log.i("LEXICALIZED_PARSER1 freeMemory:", ":" + Runtime.getRuntime().freeMemory());
			
			parse = (Tree) LEXICALIZED_PARSER.apply(wordList);
			
			Log.i("LEXICALIZED_PARSER2 freeMemory:", ":" + Runtime.getRuntime().freeMemory());
		}
		Collection<TypedDependency> tdl = null;
		try {
			tdl = initGrammarStructure(parse);
		} catch (RuntimeException e) {
			Log.e(Translator.class.getName(), "reorderPhrases", e);
			Log.i("wordList cause error: {0}", wordList.toString());
			return wordList.toString();
		}

		List<GrammarTypedDependency> typeList = new ArrayList<GrammarTypedDependency>(tdl.size());
		for (TypedDependency typedDependency : tdl) {
			typeList.add(new GrammarTypedDependency(typedDependency));
		}

		Map<WordInfo, Integer> wiChangedMap = new TreeMap<WordInfo, Integer>();

		GrammarTypedDependency typeDep1;
		GrammarTypedDependency typeDep2;
		GrammarTypedDependency typeDep3;
		GrammarTypedDependency typeDep4;
		WordInfo wordInfo1;
		Stack<Entry<Integer, WordInfo>> stack = new Stack<Entry<Integer, WordInfo>>();
		int skip = 0;
		boolean whichQuestion = false;
		int comparativePos = -1;
		int lastWordListIndex = wordList.size() - 1;
		for (int i = 0; i < wordList.size(); i++) {
			wordInfo1 = wordList.get(i);

			int wordInfo1Order = wordInfo1.getOrder();
			String wordInfo1Name = wordInfo1.getName();
			if ("The".equals(wordInfo1Name) || "THE".equals(wordInfo1Name)) {
				toTitleCase(wordList.get(wordInfo1Order));	// the Summit
				wordInfo1.clear();
				if (i < lastWordListIndex
					&& wordList.get(i + 1).hasType(Dictionary.ADJECTIVE_COMPARATIVE)) {	// Adj. comparative
					comparativePos = i + 1;
				}
				continue;
			} else if ("the".equals(wordInfo1Name)) {
				wordInfo1.clear();
				if (i < lastWordListIndex
					&& wordList.get(i + 1).hasType(Dictionary.ADJECTIVE_COMPARATIVE)) {	// Adj. comparative
					comparativePos = i + 1;
				}
				continue;
			}
			skip = checkCompoundWords(wordList, wordInfo1, i);
			if (skip != i) {
				i = skip;
				continue;
			}
			
//			LOG.log(Level.INFO, "before wordInfo: [{0}]", wordInfo1.toString());
			int typeListSize = typeList.size();
			for (int j = 0; j < typeListSize; j++) {
				typeDep1 = typeList.get(j);
				//Log.i("current wordInfo:", wordInfo1 + " typeDep1: " + typeDep1 + " with j = " + j + "/" + typeListSize);
				String grammarRelation1 = typeDep1.getGrammarRelation();
				
				int typeDep1GovPos = typeDep1.getGovPos();
				WordInfo typeDep1GovWI = wordList.get(typeDep1GovPos - 1);
				
				int typeDep1DepPos = typeDep1.getDepPos();
				WordInfo typeDep1DepWI = wordList.get(typeDep1DepPos - 1);
				
				String typeDep1DepWord = typeDep1.getDepWord();
				
				if (j < typeListSize - 3) {
					typeDep2 = typeList.get(j + 1);
					String grammarRelation2 = typeDep2.getGrammarRelation();
					int typeDep2GovPos = typeDep2.getGovPos();

					typeDep3 = typeList.get(j + 2);
					String grammarRelation3 = typeDep3.getGrammarRelation();
					int typeDep3GovPos = typeDep3.getGovPos();

					typeDep4 = typeList.get(j + 3);
					String grammarRelation4 = typeDep4.getGrammarRelation();
					int typeDep4GovPos = typeDep4.getGovPos();
					int typeDep4DepPos = typeDep4.getDepPos();
					
					//Log.i("current wordInfo:", wordInfo1 + " typeDep2:" + typeDep2);
					//Log.i("current wordInfo:", wordInfo1 + " typeDep3:" + typeDep3);
					//Log.i("current wordInfo:", wordInfo1 + " typeDep4:" + typeDep4);

					if ("nsubj".equals(grammarRelation1)
							&& "advmod".equals(grammarRelation2)
							&& "advmod".equals(grammarRelation3)
							&& "amod".equals(grammarRelation4)
							&& wordInfo1Order == typeDep1DepPos
							&& wordInfo1Order == typeDep4GovPos
							&& typeDep2GovPos == typeDep3GovPos
							&& typeDep3GovPos == typeDep4DepPos
							) {
						// The method most often used involves in steps
						// typeDep1: nsubj(involves-6, method-2)
						// typeDep2: advmod(used-5, most-3)
						// typeDep3: advmod(used-5, often-4)
						// typeDep4: amod(method-2, used-5)
						putToLast(typeDep2, wordList, wiChangedMap);
						i += typeDep1GovPos - wordInfo1Order;
						break;
					}
				}
				if (j < typeListSize - 2) {

					typeDep2 = typeList.get(j + 1);
					String grammarRelation2 = typeDep2.getGrammarRelation();
					int typeDep2GovPos = typeDep2.getGovPos();

					typeDep3 = typeList.get(j + 2);
					String grammarRelation3 = typeDep3.getGrammarRelation();
					int typeDep3GovPos = typeDep3.getGovPos();
					int typeDep3DepPos = typeDep3.getDepPos();

//					LOG.log(Level.INFO, "current wordInfo: [{0}], typeDep2: {1}", new Object[]{wordInfo1, typeDep2.toString()});
//					LOG.log(Level.INFO, "current wordInfo: [{0}], typeDep3: {1}", new Object[]{wordInfo1, typeDep3.toString()});

					if ("dobj".equals(grammarRelation1)
							&& wordInfo1Order == typeDep1DepPos
							&& typeDep1GovPos == typeDep2GovPos
							&& "aux".equals(grammarRelation2)
							&& "what".equalsIgnoreCase(wordInfo1Name)
							&& j < typeList.size() - 2
							) {
						if (typeDep3GovPos == typeDep2.getGovPos()
								&& "nsubj".equals(grammarRelation3)) {
							// What are you doing?
							// typeDep1: dobj(doing-4, What-1)
							// typeDep2: aux(doing-4, are-2)
							// typeDep3: nsubj(doing-4, you-3)
							putToLast(typeDep1, wordList, wiChangedMap);
							putToBeforeLast(typeDep2, wordList, wiChangedMap);
							i += typeDep1GovPos - typeDep1DepPos;
							break;
						}
					} else if ("advmod".equals(grammarRelation1)
							&& "advmod".equals(grammarRelation2)
							&& "amod".equals(grammarRelation3)
							&& wordInfo1Order == typeDep3GovPos
							&& typeDep1GovPos == typeDep2GovPos
							&& typeDep2GovPos == typeDep3DepPos
							) {
						// The method most often used involves in steps
						// typeDep1: advmod(used-5, most-3)
						// typeDep2: advmod(used-5, often-4)
						// typeDep3: amod(method-2, used-5)
						putToLast(typeDep1, wordList, wiChangedMap);
						i += typeDep1GovPos - wordInfo1Order;
						break;
					}
				}
				
				if (j < typeListSize - 1) {
					typeDep2 = typeList.get(j + 1);
//					LOG.log(Level.INFO, "current wordInfo: [{0}], typeDep2: {1}", new Object[]{wordInfo1, typeDep2.toString()});
					String grammarRelation2 = typeDep2.getGrammarRelation();
					int typeDep2GovPos = typeDep2.getGovPos();
					WordInfo typeDep2GovWI = wordList.get(typeDep2GovPos - 1);
					int typeDep2DepPos = typeDep2.getDepPos();
					if (("advmod".equals(grammarRelation1)
//							&& wordInfo1.getName().equals(typeDep1.getGovWord())
							&& wordInfo1Order == typeDep1GovPos
							&& "amod".equals(grammarRelation2)
//							&& wordInfo1.getName().equals(typeDep2.getDepWord())
							&& wordInfo1Order == typeDep2DepPos)) {
						// Genetically modified food
						// typeDep1: advmod(modified-2, Genetically-1)
						// typeDep2: amod(food-3, modified-2)
							typeDep1DepWI.setType(Dictionary.ADVERB);
							wordInfo1.setType(Dictionary.ADJECTIVE);
							typeDep2GovWI.setType(Dictionary.NOUN);
						insertBefore(typeDep1, wordList, wiChangedMap);
						insertBefore(typeDep2, wordList, wiChangedMap);
						i += 2;
						break;
					} else if (("advmod".equals(grammarRelation1)
							&& "npadvmod".equals(grammarRelation2)
//							&& wordInfo1.getName().equals(typeDep2.getGovWord())
							&& wordInfo1Order == typeDep1DepPos)
							&& typeDep1GovPos - typeDep1DepPos == 1) {
						// The brown dog with the red collar always barks loudly.
						// typeDep1: advmod(barks-9, always-8) with j = 5/8
						// typeDep2: npadvmod(loudly-10, barks-9)
						i += 2;
						break;
					} else if (("advmod".equals(grammarRelation1)
							&& "acomp".equals(grammarRelation2)
//							&& wordInfo1.getName().equals(typeDep2.getGovWord())
							&& wordInfo1Order == typeDep2GovPos)
							&& typeDep1GovPos - typeDep1DepPos == 1
							&& typeDep1GovPos == typeDep2DepPos) {
						// Each ultimate reality lives so short.
						// typeDep1: advmod(short-6, so-5) with j = 3/5
						// typeDep2: acomp(lives-4, short-6)
						
						// many unwholesome thoughts still arise due to bad habits
						// typeDep1: advmod(arise-5, still-4) with j = 3/7
						// typeDep2: acomp(arise-5, due-6)
						i += 2;
						break;
					} else if (("advmod".equals(grammarRelation1)
							&& "advmod".equals(grammarRelation2)
//							&& wordInfo1.getName().equals(typeDep2.getGovWord())
							&& wordInfo1Order == typeDep2GovPos)
							&& typeDep1GovPos - typeDep1DepPos == 1) {
						// He spoke very quietly.
						// typeDep1: advmod(quietly-4, very-3) with j = 1/3
						// typeDep2: advmod(spoke-2, quietly-4)
						i += 2;
						break;
					} else if (("nsubj".equals(grammarRelation1)
							&& "advmod".equals(grammarRelation2)
//							&& wordInfo1.getName().equals(typeDep2.getGovWord())
							&& wordInfo1Order == typeDep1DepPos)
							&& typeDep1GovPos - typeDep1DepPos == 2) {
						// I sometimes have trouble with adverbs.
						// typeDep1: nsubj(have-3, I-1)
						// typeDep2: advmod(have-3, sometimes-2)
						i += 2;
						break;
					} else if ("nsubj".equals(grammarRelation1)
							&& "rcmod".equals(grammarRelation2)
//							&& wordInfo1.getName().equals(typeDep2.getGovWord())
							&& wordInfo1Order == typeDep1DepPos
							&& typeDep1GovPos == typeDep2DepPos
							&& "that".equalsIgnoreCase(wordInfo1Name)) {
						// that leads to ever-lasting peace and eternal happiness called Nibbana
						// typeDep1: nsubj(leads-27, that-26)
						// typeDep2: rcmod(Path-25, leads-27)
						typeDep1DepWI.setDefinition("mà/rằng");
						i += 1;
						break;
					} else if ("attr".equals(grammarRelation1)
							&& "nsubj".equals(grammarRelation2)
//							&& wordInfo1.getName().equals(typeDep2.getGovWord())
							&& wordInfo1Order == typeDep1DepPos
							&& typeDep1GovPos == typeDep2GovPos
							&& ("who".equalsIgnoreCase(wordInfo1Name)
									|| "what".equalsIgnoreCase(wordInfo1Name)
									|| "how".equalsIgnoreCase(wordInfo1Name)
							)) {
						// Who are you?
						// typeDep1: attr(are-2, Who-1) with j = 0/2
						// typeDep2: nsubj(are-2, you-3)
						putToLast(typeDep1, wordList, wiChangedMap);
						// [Thì/là] [là những người mà/là những ai/ai] [quý vị]?
						moveToBefore(wordList.get(typeDep2.getDepPos() - 1), typeDep1.getDepPos() - 1, wordList, wiChangedMap);
						i += typeDep2DepPos - typeDep1DepPos;
						break;
					} else if ("det".equals(grammarRelation1)
							&& "nsubj".equals(grammarRelation2)
//							&& wordInfo1.getName().equals(typeDep2.getGovWord())
							&& wordInfo1Order == typeDep1DepPos
							&& typeDep1GovPos == typeDep2DepPos
							&& "What".equalsIgnoreCase(wordInfo1Name)) {
						// What kind of rose?
						// typeDep1: det(kind-2, What-1)
						// typeDep2: nsubj(rose-4, kind-2)
						
						moveToAfter(wordInfo1, wordList.size() - 1, wordList, wiChangedMap);
//						WordInfo removeAdjComp = wordList.remove(typeDep1DepPos - 1);
//						if (isUpperCase(removeAdjComp)) {
//							toTitleCase(wordList.get(typeDep1DepPos - 1));
//						}
//						lastWordListIndex = wordList.size() - 1;
//						if (isLowerCase(wordList.get(lastWordListIndex))) {
//							toLowerCase(removeAdjComp);
//						}
//						switchStartTagSign(removeAdjComp, wordList.get(typeDep1DepPos - 1));
//						switchEndTagSign(wordList.get(lastWordListIndex), removeAdjComp);
//						wordList.add(wordList.size(), removeAdjComp);
						
						i += typeDep2GovPos - typeDep1DepPos;
						break;
					} else if (("cop".equals(grammarRelation1)
							&& "advmod".equals(grammarRelation2)
//							&& wordInfo1.getName().equals(typeDep2.getGovWord())
							&& wordInfo1Order == typeDep2DepPos)
							&& typeDep1GovPos - typeDep1DepPos == 2) {
						// These books are so nice.
						// typeDep1: cop(nice-5, are-3) with j = 2/4
						// typeDep2: advmod(nice-5, so-4)
						if ("more".equalsIgnoreCase(wordInfo1.getName())) {
							wordInfo1.clear();
						}
						i += 1;
						break;
					} else if (("aux".equals(grammarRelation1)
							&& "advmod".equals(grammarRelation2)
//							&& wordInfo1.getName().equals(typeDep2.getGovWord())
							&& wordInfo1Order == typeDep1DepPos)
							&& typeDep1GovPos - typeDep1DepPos == 2) {
						// The moon was just rising.
						// typeDep1: aux(rising-13, was-11) with j = 8/10
						// typeDep2: advmod(rising-13, just-12)
						if ("just".equalsIgnoreCase(wordList.get(typeDep2DepPos).getName())) {
							i += 2;
							break;
						}
					} else if (("aux".equals(grammarRelation1)		// aux(know-21, to-20)
							&& "to".equals(typeDep1DepWord)	// like to know 
							&& "xcomp".equals(grammarRelation2)		// xcomp(like-19, know-21)
//							&& wordInfo1.getName().equals(typeDep2.getGovWord())
							&& wordInfo1Order == typeDep2GovPos)
							&& typeDep1DepPos - typeDep2GovPos == 1) {
					   wordInfo1.setType(Dictionary.VERB);
						wordList.get(wordInfo1Order).clear();
					   wordList.get(i + 2).setType(Dictionary.VERB);
						break;
					} else if (("cop".equals(grammarRelation1)
							&& "neg".equals(grammarRelation2)
//							&& wordInfo1.getName().equals(typeDep1.getDepWord())
							&& wordInfo1Order == typeDep1DepPos)
							&& typeDep1DepPos == typeDep2DepPos - 1
							&& typeDep1GovPos == typeDep2GovPos) {
						// Bill is not a scientist
						// typeDep1: cop(scientist-5, is-2) with j = 1/4
						// typeDep2: neg(scientist-5, not-3)
					   typeDep1GovWI.setType(Dictionary.NOUN);
						WordInfo removed = wordList.remove(typeDep2DepPos - 1);
						wordList.add(typeDep1DepPos - 1, removed);
						i++;
						break;
					} else if (("advmod".equals(grammarRelation1)
							&& "why".equals(typeDep1DepWord)
							&& "aux".equals(grammarRelation2)
//							&& wordInfo1.getName().equals(typeDep2.getGovWord())
							&& wordInfo1Order == typeDep2GovPos)) {
						// why is the sky blue
						// typeDep1: advmod(blue-5, why-1) with j = 0/4
						// typeDep2: aux(blue-5, is-2)
					   wordInfo1.setType(Dictionary.ADJECTIVE);
						putToBeforeLast(typeDep2, wordList, wiChangedMap);
						break;
					} else {
						String typeDep2DepWord = typeDep2.getDepWord();
						if ("nn".equals(grammarRelation1) // typeDep1: nn(Samatha-Vipassanā-13, undertaking-12)
//							&& wordInfo1.getName().equals(typeDep1.getDepWord()) 
								&& wordInfo1Order == typeDep1DepPos
								&& wordInfo1Name.toLowerCase().endsWith("ing")
								&& "prep_of".equals(grammarRelation2) // typeDep2: prep_of(way-10, Samatha-Vipassanā-13)
								&& typeDep1.getGovWord().equals(typeDep2DepWord)
								&& typeDep1GovPos == typeDep2DepPos) {
							// the proper way of undertaking Samatha-Vipassanā
							// typeDep1: nn(Samatha-Vipassanā-13, undertaking-12)
							// typeDep2: prep_of(way-10, Samatha-Vipassanā-13)
								typeDep1GovWI.setType(Dictionary.NOUN);
							typeDep2GovWI.setType(Dictionary.NOUN);
							i++;
							break;
						} else {
							WordInfo typeDep2DepWI = wordList.get(typeDep2DepPos - 1);
							if ("amod".equals(grammarRelation1) // typeDep1: amod(account-26, brief-23)??
//							&& wordInfo1.getName().equals(typeDep1.getDepWord()) 
									&& wordInfo1Order == typeDep1DepPos
									&& typeDep1DepWord.equals(typeDep2.getGovWord())
									&& typeDep1DepPos == typeDep2GovPos
									&& "conj_and".equals(grammarRelation2)
									&& typeDep2DepPos > typeDep2GovPos) { //typeDep2: conj_and(brief-23, concise-25)??
								// the present and the future
								// typeDep1: amod(future-5, present-2)
								// typeDep2: conj_and(present-2, the-4)
								typeDep1GovWI.setType(Dictionary.NOUN);
								wordInfo1.setType(Dictionary.ADJECTIVE);
								typeDep2DepWI.setType(Dictionary.ADJECTIVE);
								break;	// no need to change
							} else if ("poss".equals(grammarRelation1)
//							&& wordInfo1.getName().equals(typeDep1.getDepWord()) 
									&& wordInfo1Order == typeDep1DepPos
									&& "nn".equals(grammarRelation2)
									&& typeDep1.getGovWord().equals(typeDep2.getGovWord())
									&& typeDep1GovPos == typeDep2GovPos) {
								// our meditation subject
								// typeDep1: poss(subject-12, our-10)
								// typeDep2: nn(subject-12, meditation-11)
								typeDep1GovWI.setType(Dictionary.NOUN);
								typeDep2DepWI.setType(Dictionary.NOUN);
								putToLast(typeDep1, wordList, wiChangedMap);
								//Log.i("putToLast poss & nn: ", ":" + wordList);
								insertBefore(typeDep2, wordList, wiChangedMap);
								//Log.i("insertBefore poss & nn: ", ":" + wordList);
								i += 2;
								break;
							} else if (("det".equals(grammarRelation1) || "poss".endsWith(grammarRelation1))
//							&& wordInfo1.getName().equals(typeDep1.getDepWord())
									&& wordInfo1Order == typeDep1DepPos
									&& "amod".equals(grammarRelation2)
									&& typeDep1.getGovWord().equals(typeDep2.getGovWord())
									&& typeDep1GovPos == typeDep2GovPos) {

								// a brief and concise account 
								// typeDep1: det(account-26, a-22) with j = 15/25
								// typeDep2: amod(account-26, brief-23)

								// each ultimate reality
								// typeDep1: det(reality-4, each-2) 
								// typeDep2: amod(reality-4, ultimate-3)

								// this very life 
								// typeDep1: det(life-21, this-19)
								// typeDep2: amod(life-21, very-20)
								
								// the right concentration
								// typeDep1: det(concentration-4, the-2) with j = 0/8
								// typeDep2: amod(concentration-4, right-3)
								
								// the causal relations
								// typeDep1: det(relations-4, the-2) with j = 0/5
								// typeDep2: amod(relations-4, causal-3)
								
								// this delightful noble task
								// typeDep1: det(task-11, this-8)
								// typeDep2: amod(task-11, delightful-9)
								// typeDep2: amod(task-11, noble-10)
								
								typeDep2GovWI.setType(Dictionary.NOUN);
								typeDep2DepWI.setType(Dictionary.ADJECTIVE);
								if (!"a".equalsIgnoreCase(typeDep1DepWord)
										&& !"an".equalsIgnoreCase(typeDep1DepWord)
										&& !"each".equalsIgnoreCase(typeDep1DepWord)
										&& !"the".equalsIgnoreCase(typeDep1DepWord)
										&& !"very".equalsIgnoreCase(typeDep2DepWord)
										&& !"named".equalsIgnoreCase(typeDep2DepWord)
										&& !"called".equalsIgnoreCase(typeDep2DepWord)) {
									if ("this".equalsIgnoreCase(typeDep1DepWord)) {
										wordInfo1.setDefinition("này");
									} else if ("that".equalsIgnoreCase(typeDep1DepWord)) {
										wordInfo1.setDefinition("đó");
									}
									insertFirstToLastLastToFirst(typeDep1, wordList, wiChangedMap);
									i += typeDep1GovPos - wordInfo1Order;
									break;
								} else if ("a".equalsIgnoreCase(typeDep1DepWord)
										|| "an".equalsIgnoreCase(typeDep1DepWord)
										|| "each".equalsIgnoreCase(typeDep1DepWord)) {
									insertBefore(typeDep2, wordList, wiChangedMap);
									i += typeDep2GovPos - typeDep1DepPos;
									break;
								}
							} else if ("aux".equals(grammarRelation1)
									&& wordInfo1Order == typeDep1DepPos
									&& typeDep1GovPos == typeDep2GovPos
									&& "auxpass".equals(grammarRelation2)
									) {
								// typeDep1: aux(spent-5, been-3) with j = 2/7
								// typeDep2: auxpass(spent-5, being-4)
								// I have been being spent 3 days in Malaysia.
								typeDep1DepWI.setDefinition("đang");
								typeDep2DepWI.setDefinition("được/bị");
								break;
							} else if ("nsubj".equals(grammarRelation1)
									&& wordInfo1Order == typeDep1DepPos
									&& typeDep1GovPos == typeDep2GovPos
									&& "aux".equals(grammarRelation2)
									&& typeDep1DepPos - typeDep2GovPos == -2
									) {
								// typeDep1: nsubj(spending-3, She-1) with j = 0/5
								// typeDep2: aux(spending-3, is-2)
								if ("am".equals(typeDep2DepWord)
										|| "is".equals(typeDep2DepWord)
										|| "are".equals(typeDep2DepWord)
										|| "has".equals(typeDep2DepWord)
										|| "have".equals(typeDep2DepWord)
										) {
								typeDep2DepWI.clear();
								} else if ("was".equals(typeDep2DepWord)
										|| "were".equals(typeDep2DepWord)
										|| "has".equals(typeDep2DepWord)
										|| "have".equals(typeDep2DepWord)
										) {
									typeDep2DepWI.setDefinition("đã");
								}
								typeDep1GovWI.setType(Dictionary.VERB);
								i += 1;
								break;
							} else if ("nsubjpass".equals(grammarRelation1)
//							&& wordInfo1.getName().equalsIgnoreCase(typeDep1.getDepWord())
									&& wordInfo1Order == typeDep1DepPos
									&& "auxpass".equals(grammarRelation2)
									&& typeDep1.getGovWord().equals(typeDep2.getGovWord())
									&& typeDep1GovPos == typeDep2GovPos) {
								// typeDep1: nsubjpass(killed-4, man-2) with j = 1/5
								// typeDep2: auxpass(killed-4, was-3)
								typeDep2GovWI.setType(Dictionary.VERB);

								if ("was".equalsIgnoreCase(typeDep2DepWord)
										|| "were".equalsIgnoreCase(typeDep2DepWord)) {
									typeDep2DepWI.setDefinition("đã bị/được");
								} else if ("is".equalsIgnoreCase(typeDep2DepWord)
										|| "are".equalsIgnoreCase(typeDep2DepWord)) {
									typeDep2DepWI.setDefinition("bị/được");
								}
								i += typeDep2GovPos - typeDep1DepPos;
								break;
							} else if ("aux".equals(grammarRelation1)
//							&& wordInfo1.getName().equals(typeDep1.getDepWord())
									&& wordInfo1Order == typeDep1DepPos
									&& "auxpass".equals(grammarRelation2)
									&& typeDep1.getGovWord().equals(typeDep2.getGovWord())
									&& typeDep1GovPos == typeDep2GovPos) {
								typeDep2GovWI.setType(Dictionary.VERB);

//						typeDep1: aux(killed-5, has-3) with j = 2/6
//						typeDep2: auxpass(killed-5, been-4)
								
								if ("has".equalsIgnoreCase(wordInfo1Name)
										|| "have".equalsIgnoreCase(wordInfo1Name)) {
									wordInfo1.clear();
								} else if ("had".equalsIgnoreCase(wordInfo1Name)) {
									wordInfo1.setDefinition("đã");
								}
								if ("been".equalsIgnoreCase(typeDep2DepWord)
										|| "was".equalsIgnoreCase(typeDep2DepWord)
										|| "were".equalsIgnoreCase(typeDep2DepWord)) {
									wordInfo1.clear();
									typeDep2DepWI.setDefinition("đã bị/được");
								} else if ("being".equalsIgnoreCase(typeDep2DepWord)) {
									wordInfo1.clear();
									typeDep2DepWI.setDefinition("đang bị/được");
								}

								// are being conducted
								// typeDep1: aux(conducted-17, are-15) with j = 11/31
								// typeDep2: auxpass(conducted-17, being-16)
								i += typeDep2GovPos - typeDep1DepPos;
								break;
							} else if ("aux".equals(grammarRelation1)
//							&& wordInfo1.getName().equals(typeDep1.getDepWord())
									&& wordInfo1Order == typeDep1DepPos
									&& !"to".equals(wordInfo1Name)
									&& ("nsubj".equals(grammarRelation2) || "dobj".equals(grammarRelation2))
//									&& typeDep1.getGovWord().equals(typeDep2.getGovWord())
									&& typeDep1GovPos == typeDep2GovPos) {
								// Can we use
								// typeDep1: aux(use-3, Can-1) with j = 0/4
								// typeDep2: nsubj(use-3, we-2)
								
								// should we ignore
								// typeDep1: aux(ignore-4, should-2) with j = 1/9
								// typeDep2: dobj(ignore-4, we-3)

								// to remove them
								// typeDep1: aux(remove-2, to-1) with j = 0/2
								// typeDep2: dobj(remove-2, them-3)
								
								// and can share them with the world for the welfare of all mankind
								// typeDep1: nsubj(share-3, and-1)
								// typeDep2: aux(share-3, can-2)
								// typeDep2: dobj(share-3, them-4)
								
								// It is getting hotter and hotter.
								// typeDep1: aux(getting-3, is-2) with j = 1/4
								// typeDep2: dobj(getting-3, hotter-4)
								
								if (!whichQuestion
										&& (j - 1 >= 0) && !"nsubj".equals(typeList.get(j - 1).getGrammarRelation())) {
									typeDep1DepWI.setType(Dictionary.VERB);
									typeDep2GovWI.setType(Dictionary.VERB);

									wordInfo1.setName(wordInfo1Name.toLowerCase());
									putToBeforeLast(typeDep1, wordList, wiChangedMap);
									//Log.i("wordInfo1:", ":" + wordInfo1);
									//Log.i("typeList.get(j - 1):", ":" + typeList.get(j - 1));
									stack.push(new Entry<Integer, WordInfo>(wordInfo1Order - 1, 
											new WordInfo(wordInfo1Order, wordInfo1Name, "liệu", " ", " ")));
								}
								i += 2;
							}
						}
					}
				}
				
				// its original pure form
				// a brief and concise account
				// can share them
				// all mankind
				// Which book do you prefer?
				// det(book-2, Which-1)
				// dobj(prefer-5, book-2)
				
				
				
				
				// typeDep1: amod(interest-8, great-7)
				if (("amod".equals(grammarRelation1)
						|| "poss".equals(grammarRelation1))	// called Samatha-Vipassanā
//						&& wordInfo1.getName().equals(typeDep1.getDepWord())
						&& wordInfo1Order == typeDep1DepPos
						&& !"called".equalsIgnoreCase(typeDep1DepWord)
						&& !"very".equalsIgnoreCase(typeDep1DepWord)
						&& !"many".equalsIgnoreCase(typeDep1DepWord)
						&& !"just".equalsIgnoreCase(typeDep1DepWord)
						&& !"named".equalsIgnoreCase(typeDep1DepWord)) {
					// our daily life
					// typeDep1: poss(life-4, our-2)
					// typeDep2: amod(life-4, daily-3)
					
					// typeDep1: amod(book-11, present-10)
					typeDep1GovWI.setType(Dictionary.NOUN);
					typeDep1DepWI.setType(Dictionary.ADJECTIVE);

					putToLast(typeDep1, wordList, wiChangedMap);
					// John's cousin
					// typeDep1: amod(cousin-2, John's-1)
					if (typeDep1DepWord.endsWith("'s")) {
						stack.push(new Entry<Integer, WordInfo>(wordInfo1Order, 
								new WordInfo(wordInfo1Order + 1, "", "của", " ", " ")));
						removeApos(wordList.get(typeDep1DepPos));
					}
					if (wordInfo1Order == typeDep1GovPos - 1) {
						i++;
					}
				} else if (("auxpass".equals(grammarRelation1))	// are printed and published
//						&& wordInfo1.getName().equals(typeDep1.getDepWord())
						&& wordInfo1Order == typeDep1DepPos) {
					// typeDep1: auxpass(printed-2, are-1)
					// typeDep2: conj_and(printed-2, published-4)					
					typeDep1GovWI.setType(Dictionary.VERB);
					if ("are".equalsIgnoreCase(wordInfo1Name)
							|| "is".equalsIgnoreCase(wordInfo1Name)) {
						wordInfo1.setDefinition("được/bị");
					} else if ("were".equalsIgnoreCase(wordInfo1Name)
							|| "was".equalsIgnoreCase(wordInfo1Name)) {
//						WordInfo govWI = wordList.get(typeDep1.getGovPos() - 1);
//						govWI.setDefinition(govWI.getDefinition().substring("đã ".length()));
						wordInfo1.setDefinition("đã bị/được");
					}
					break;
				} else if (("aux".equals(grammarRelation1))	// The man has been killed by the police
//						&& wordInfo1.getName().equals(typeDep1.getDepWord())
						&& wordInfo1Order == typeDep1DepPos
						&& ("has".equalsIgnoreCase(wordInfo1Name)
						|| "have".equalsIgnoreCase(wordInfo1Name))
						) {
					// Reagan has died
					// typeDep1: aux(died-3, has-2)
					wordInfo1.clear();
					break;
				} else if (("advmod".equals(grammarRelation1))	// Max's story is the longest story I've ever heard.
//						&& wordInfo1.getName().equals(typeDep1.getDepWord())
						&& wordInfo1Order == typeDep1GovPos
						&& typeDep1GovPos - typeDep1DepPos == 1
						) {
//					LOG.info("grammarRelation1: " + grammarRelation1);
					// Max's story is the longest story I've ever heard.
					// typeDep1: advmod(heard-10, ever-9)
					
					// many unwholesome thoughts still arise due to bad habits
					// typeDep1: advmod(arise-5, still-4)
					// typeDep1: advmod(bring-15, surely-14)
					if (!"still".equalsIgnoreCase(typeDep1DepWord)
							&& !"surely".equalsIgnoreCase(typeDep1DepWord)
							) {
						insertBefore(typeDep1, wordList, wiChangedMap);
					}
					break;
				} else if (("conj_and".equals(grammarRelation1))	// It is getting hotter and hotter.
//						&& wordInfo1.getName().equals(typeDep1.getDepWord())
						&& wordInfo1Order == typeDep1GovPos
						&& typeDep1GovWI.getName().equals(typeDep1DepWI.getName())
						&& typeDep1GovWI.hasType(Dictionary.ADJECTIVE_COMPARATIVE)
						) {
					// It is getting hotter and hotter.
					// typeDep1: conj_and(hotter-4, hotter-6)
				   typeDep1GovWI.setType(Dictionary.OTHER);
					wordList.get(i + 1).setType(Dictionary.OTHER);
					wordInfo1.setType(Dictionary.OTHER);
					wordList.get(i + 2).setType(Dictionary.OTHER);
//					LOG.info("typeDep1GovWI.getType(): " + typeDep1GovWI.getType());
					wordList.get(i + 1).setDefinition(wordInfo1.getDefinition());
					wordInfo1.setDefinition("ngày càng");
					wordList.get(i + 2).setDefinition("hơn");
					i += 2;
					break;
				} else if (("cop".equals(grammarRelation1))
//						&& wordInfo1.getName().equals(typeDep1.getDepWord())
						&& wordInfo1Order == typeDep1DepPos
//						&& typeDep1.getGovPos() - typeDep1.getDepPos() == 1
						) {
					// I was ill
					// typeDep1: cop(ill-3, was-2)
					// Bill is an honest man
					// typeDep1: cop(man-5, is-2)
//					LOG.info("govWordInfo: " + typeDep1GovWI);
					if ("was".equalsIgnoreCase(typeDep1DepWord)) {
						wordInfo1.setDefinition("đã");
					}
					if (typeDep1GovWI.hasType(Dictionary.ADJECTIVE)) {
						typeDep1GovWI.setType(Dictionary.ADJECTIVE);
						wordInfo1.setDefinition("thì");
						if ("was".equalsIgnoreCase(typeDep1DepWord)) {
							wordInfo1.setDefinition("thì đã");
						}
					} else if (typeDep1GovWI.hasType(Dictionary.NOUN)
							&& "is".equalsIgnoreCase(typeDep1DepWord)
							&& "are".equalsIgnoreCase(typeDep1DepWord)
							&& "was".equalsIgnoreCase(typeDep1DepWord)
							&& "were".equalsIgnoreCase(typeDep1DepWord)) {	// typeDep1: cop(engineer-4, became-2)
						typeDep1GovWI.setType(Dictionary.NOUN);						// She became an engineer.
						wordInfo1.setDefinition("là");
					}
					break;
				} else if (("det".equals(grammarRelation1)
						&& (!"a".equalsIgnoreCase(typeDep1DepWord))
						&& (!"an".equalsIgnoreCase(typeDep1DepWord))
						&& (!"all".equalsIgnoreCase(typeDep1DepWord))
						&& (!"the".equalsIgnoreCase(typeDep1DepWord))
						&& (!"each".equalsIgnoreCase(typeDep1DepWord)))
						// && wordInfo1.getName().equals(typeDep1.getDepWord())
						&& wordInfo1Order == typeDep1DepPos) {
					// Which book do you prefer?
					// This book
					if ("this".equalsIgnoreCase(typeDep1DepWord)
							|| "these".equalsIgnoreCase(typeDep1DepWord)) {	// These books are so nice
						wordInfo1.setDefinition("này");
						typeDep1GovWI.setType(Dictionary.NOUN);		// typeDep1: det(books-2, These-1)
					} else if ("that".equalsIgnoreCase(typeDep1DepWord)
							|| "these".equalsIgnoreCase(typeDep1DepWord)) {
						typeDep1GovWI.setType(Dictionary.NOUN);
						wordInfo1.setDefinition("đó");
					}
					putToLast(typeDep1, wordList, wiChangedMap);
					i += typeDep1GovPos - typeDep1DepPos;
					if ("what".equalsIgnoreCase(typeDep1DepWord)
							|| "which".equalsIgnoreCase(typeDep1DepWord)) {
						whichQuestion = true;
					}
//				} else if (("det".equals(grammarRelation1))	// The man has been killed by the police
////						&& wordInfo1.getName().equals(typeDep1.getDepWord())
//						&& wordInfo1.getOrder() == typeDep1.getDepPos()
//				) {
//					// These books are so nice
//					// typeDep1: det(books-2, These-1)
//					wordInfo1.setType("Adjective");
//					putToLast(typeDep1, wordList, wiChangedMap);
//					i += typeDep1.getGovPos() - typeDep1.getDepPos();
					break;
				} else if (("agent".equals(grammarRelation1))	// The man has been killed by the police
						&& "by".equalsIgnoreCase(wordInfo1Name)) {
					// typeDep1: agent(killed-4, police-7)
					wordInfo1.setDefinition("bởi/bằng");
					break;
				} else if (("dobj".equals(grammarRelation1))	// The man has been killed by the police
						&& wordInfo1Name.equals(typeDep1DepWord)
						) {
					// I don't like these.
					// typeDep1: dobj(like-4, these-5)
					if ("this".equalsIgnoreCase(typeDep1DepWord)) {
						wordInfo1.setDefinition("điều này/cái này");
					} else if ("these".equalsIgnoreCase(typeDep1DepWord)) {
						wordInfo1.setDefinition("những điều này/cái này");
					} else {
						typeDep1DepWI.setType(Dictionary.NOUN);
					}
					typeDep1GovWI.setType(Dictionary.VERB);
					break;
				} else if (("complm".equals(grammarRelation1))	// The man has been killed by the police
//						&& wordInfo1.getName().equals(typeDep1.getDepWord())
						&& wordInfo1Order == typeDep1DepPos
						&& "that".equalsIgnoreCase(wordInfo1Name)
						) {
					// He says that you like to swim
					// typeDep1: complm(like-5, that-3)
					wordInfo1.setDefinition("mà/rằng/vốn");
					break;
//				} else if (("rcmod".equals(grammarRelation1))	// The man has been killed by the police
////						&& wordInfo1.getName().equals(typeDep1.getGovWord())
//						&& wordInfo1.getOrder() == typeDep1.getGovPos()
//						&& (j - 1 >= 0) && !"nsubj".equals(typeList.get(j - 1).getGrammarRelation())) {
//					// I saw the man you love
//					stack.push(new Entry<Integer, WordInfo>(wordInfo1.getOrder(), new WordInfo(wordInfo1
//							.getOrder() + 1, "", "mà/rằng/vốn", "", " ")));
				} else if (("nsubj".equals(grammarRelation1))
						&& wordInfo1Order == typeDep1DepPos
						) {
					if ("this".equalsIgnoreCase(wordInfo1Name)) {
						// This means "hello world"
						// typeDep1: nsubj(means-2, This-1)
						wordInfo1.setDefinition("Điều này");
					} else if ("that".equalsIgnoreCase(wordInfo1Name)
							&& typeDep1GovPos - typeDep1DepPos > 1) {
						// That means "hello world"
						// typeDep1: nsubj(bark-8, that-5)
						// typeDep1: nsubj(leads-27, that-26)
						wordInfo1.setDefinition("mà/rằng/vốn");
					} else if ("that".equalsIgnoreCase(wordInfo1Name)) {
						// That means "hello world"
						// typeDep1: nsubj(means-2, That-1)
						wordInfo1.setDefinition("Điều đó");
					}
					typeDep1GovWI.setType(Dictionary.VERB);
					break;
					//cnew comment/
					// if (typeDep1.getDepWord().endsWith("'s")) {	//John's cousin
					// stack.push(new Entry<Integer, WordInfo>(wordInfo1.getOrder(), 
					// new WordInfo(wordInfo1.getOrder(), "", "của", "", " ")));
					// removeApos(wordList.get(typeDep1.getDepPos()));	// đã đổi vị trí nên lấy depPos
					// }
				} else if ("nn".equals(grammarRelation1) // typeDep1: nn(Samatha-Vipassanā-13, undertaking-12)
//						&& wordInfo1.getName().equals(typeDep1.getDepWord()) 
						&& wordInfo1Order == typeDep1DepPos
						&& wordInfo1Name.toLowerCase().endsWith("ing")) {
					i++;
					break;
				} else if ("nn".equals(grammarRelation1)
//						&& wordInfo1.getName().equals(typeDep1.getDepWord())
						&& wordInfo1Order == typeDep1DepPos
						&& !"doesn't".equalsIgnoreCase(typeDep1.getGovWord())	// Bill doesn't drive // nn(doesn't-2, Bill-1)
						&& !"don't".equalsIgnoreCase(typeDep1.getGovWord())
						&& !"didn't".equalsIgnoreCase(typeDep1.getGovWord())
						&& !"and".equalsIgnoreCase(typeDep1DepWord)) {	// and not-self // nn(not-self-2, and-1)
						// International Pa-auk Forest Buddha Sāsana Centres
						int counter = typeDep1GovPos - wordInfo1Order; // typeDep1: nn(Meditation-3, Tranquillity-2)
						boolean isInverseAllNN = true;	// typeDep1: nn(Centres-37, International-32)
				   		//Log.i("counter > 1", ":" + (counter > 1));
						for (int checkNN = 1; checkNN < counter; checkNN++) {
							if (!"nn".equals(typeList.get(j + checkNN).getGrammarRelation())) {
								isInverseAllNN = false;
								//Log.i("isInverseAllNN", " = false");
								break;
							}
						}
						//Log.i("counter", ":" + counter);
						//Log.i("isInverseAllNN", ":" + isInverseAllNN);
						if (counter >= 0) {
						
						if (isInverseAllNN) {
							for (int k = 0; k < counter; k++) {
								// trùng tên và định nghĩa thì không change vị trí
//								if (!wordList.get(wordInfo1.getOrder() + counter - 1).getName().equals(
//										wordList.get(wordInfo1.getOrder() + counter - 1).getDefinition())) {
									// xóa từ cuối
								if (wordList.get(wordInfo1Order - 1 + counter).isDefinitionEmpty()) {
									continue;
								}
								typeDep1GovWI.setType(Dictionary.NOUN);
								typeDep1DepWI.setType(Dictionary.NOUN);

									WordInfo removeWI = wordList.remove(wordInfo1Order + counter - 1);
									// thêm vào ở k
									int index = wordInfo1Order + k - 1;
									if (index == 0 || (index == 1 && wordList.get(0).isDefinitionEmpty())) {
										if (isUpperCase(wordList.get(index))) {
											toTitleCase(removeWI);
											toLowerCase(wordList.get(index));
										}
										switchStartTagSign(removeWI, wordList.get(index));
									}
									switchEndTagSign(removeWI, wordList.get(wordInfo1Order + counter - 1 - 1));
									wordList.add(index, removeWI);
									//Log.i("1. current wordInfo:", wordInfo1 + " changed order between " + wordInfo1 + " and " + removeWI);
									//Log.i("2. current wordList:", wordList.toString());
//								}
							}
							i += counter;
						} else {
							typeDep1GovWI.setType(Dictionary.NOUN);
							typeDep1DepWI.setType(Dictionary.NOUN);
							WordInfo removeWI = wordList.remove(wordInfo1Order + counter - 1);
							wordList.add(wordInfo1Order - 1, removeWI);
							switchStartTagSign(removeWI, wordList.get(wordInfo1Order));
							switchEndTagSign(removeWI, wordList.get(wordInfo1Order + counter - 1));
							// ?
//							changeSign(wordList.get(wordInfo1.getOrder() + counter - 1), removeWI, false);
							//Log.i("3. current wordInfo:", wordInfo1 +" changed order between " + wordInfo1 + " and " + removeWI);
							//Log.i("4. current wordList: [{0}]", wordList.toString());
							i += counter;
						}
						if (typeDep1DepWord.endsWith("'s")) {		// John's cousin // typeDep1: nn(cousin-5, John's-4)
							// typeDep1: nn(ache-10, David's-8)
							// typeDep2: nn(ache-10, head-9)
							int indexOfWI = wordList.indexOf(wordInfo1);
							stack.push(new Entry<Integer, WordInfo>(indexOfWI, new WordInfo(indexOfWI + 1, 
									"", "của", "", " ")));
							removeApos(wordInfo1);
						}
						}
					}
			}
			//Log.i("after wordInfo: [{0}]", wordInfo1.toString());
		}
		insertToWordList(wordList, stack);
		if (comparativePos > -1) {
			// The sooner this is done, the better it is.
			WordInfo removeAdjComp = wordList.remove(comparativePos);
			if (isUpperCase(removeAdjComp)) {
				toTitleCase(wordList.get(comparativePos));
			}
			lastWordListIndex = wordList.size() - 1;
			if (isLowerCase(wordList.get(lastWordListIndex))) {
				toLowerCase(removeAdjComp);
			}
			removeAdjComp.setDefinition("càng " + removeAdjComp.getDefinition());
			switchStartTagSign(removeAdjComp, wordList.get(comparativePos));
			switchEndTagSign(wordList.get(lastWordListIndex), removeAdjComp);
			wordList.add(wordList.size(), removeAdjComp);
		}
		//Log.i("after wordList: {0}", wordList.toString());
		buildWordInfoToSentence(wordList, sb);
		// Util.gc();
		return sb.toString();
	}

	// this delightful noble task
	// typeDep1: det(task-11, this-8)
	// typeDep2: amod(task-11, delightful-9)
	// typeDep2: amod(task-11, noble-10)
	private static void insertFirstToLastLastToFirst(
			GrammarTypedDependency typeDep1, List<WordInfo> wordList,
			Map<WordInfo, Integer> wiChangedMap) {
		//Log.i("before insertFirstToLastLastToFirst: ", Util.collectionToSlashString(wordList));

		putToLast(typeDep1, wordList, wiChangedMap);
		int last = typeDep1.getGovPos() - 1 - 1;
		int first = typeDep1.getDepPos() - 1;
		if ((first == 0  || (first == 1 && wordList.get(0).isDefinitionEmpty())) && isUpperCase(wordList.get(first))) {
			toTitleCase(wordList.get(last));
			toLowerCase(wordList.get(first));
		}
		WordInfo remove = wordList.remove(last);
		wordList.add(first, remove);
		wiChangedMap.put(remove, first + 1);
		//Log.i("after insertFirstToLastLastToFirst: ", Util.collectionToSlashString(wordList));
	}

	// this very life 
	// typeDep1: det(life-21, this-19)
	// typeDep2: amod(life-21, very-20)
	private static void putToLast(GrammarTypedDependency typeDep,
			List<WordInfo> wordList, Map<WordInfo, Integer> wiChangedMap) {
//		LOG.info(new StringBuilder("typeDep: ").append(typeDep).toString());
		// typeDep1: poss(life-4, our-2)
		//Log.i("before putToLast: ", Util.collectionToSlashString(wordList));
		Integer temp = 0;
		Integer depWordInfoPos = 
			(((temp = wiChangedMap.get(new WordInfo(typeDep.getDepPos(), typeDep.getDepWord(), "", "", ""))) == null) ?
				(typeDep.getDepPos()) : (temp)) - 1;
		Integer govWordInfoPos = 
			(((temp = wiChangedMap.get(new WordInfo(typeDep.getGovPos(), typeDep.getGovWord(), "", "", ""))) == null) ?
				typeDep.getGovPos() : temp) - 1;
		WordInfo decreaseIndexWI = wordList.get(govWordInfoPos.intValue());
		WordInfo depWIRemoved = wordList.remove(depWordInfoPos.intValue());
		if (govWordInfoPos.intValue() == wordList.size()) {
			wordList.add(depWIRemoved);
		} else {
			wordList.add(govWordInfoPos.intValue(), depWIRemoved);
		}
		for (int i = depWordInfoPos.intValue(); i <= govWordInfoPos.intValue(); i++) {
			wiChangedMap.put(wordList.get(i), Integer.valueOf(i + 1));
		}
		if ((depWordInfoPos.intValue() == 0 || (depWordInfoPos.intValue() == 1 && wordList.get(0).isDefinitionEmpty())) && isUpperCase(depWIRemoved)) {
			toTitleCase(wordList.get(depWordInfoPos.intValue()));
			toLowerCase(depWIRemoved);
		}
		switchStartTagSign(depWIRemoved, wordList.get(depWordInfoPos.intValue()));
		switchEndTagSign(decreaseIndexWI, depWIRemoved);
		//Log.i("after putToLast: ", Util.collectionToSlashString(wordList));
	}
	
	private static void toLowerCase(WordInfo wordInfo) {
		if (!wordInfo.isDefinitionEmpty()) {
			String name = wordInfo.getName().trim();
			if (name.length() > 1) {
				StringBuilder sb = new StringBuilder(name
						.substring(0, 1).toLowerCase()).append(name, 1, name.length());
				wordInfo.setName(sb.toString());
			} else {
				wordInfo.setName(name.toLowerCase());
			}
		}
	}

	private static void toTitleCase(WordInfo wordInfo) {
		String name = wordInfo.getName().trim();
		if (name.length() > 1) {
			wordInfo.setName(new StringBuilder(name.substring(0, 1).toUpperCase()).append(name, 1, name.length()).toString());
		} else {
			wordInfo.setName(name.toUpperCase());
		}
	}

	private static void switchStartTagSign(WordInfo smallOrderWI, WordInfo bigOrderWI) {
		String strTemp = smallOrderWI.getStartSign();
		smallOrderWI.setStartSign(bigOrderWI.getStartSign());
		bigOrderWI.setStartSign(strTemp);
		
		strTemp = smallOrderWI.getStartTag();
		smallOrderWI.setStartTag(bigOrderWI.getStartTag());
		bigOrderWI.setStartTag(strTemp);
	}

	private static void switchEndTagSign(WordInfo smallOrderWI, WordInfo bigOrderWI) {
		String strTemp = smallOrderWI.getEndSign();
		smallOrderWI.setEndSign(bigOrderWI.getEndSign());
		bigOrderWI.setEndSign(strTemp);
		
		strTemp = smallOrderWI.getEndTag();
		smallOrderWI.setEndTag(bigOrderWI.getEndTag());
		bigOrderWI.setEndTag(strTemp);
	}
	
	private static void putToBeforeLast(GrammarTypedDependency typeDep,
			List<WordInfo> wordList, Map<WordInfo, Integer> wiChangedMap) {
		//Log.i("before putToBeforeLast: ", Util.collectionToSlashString(wordList));
		Integer temp = 0;
		Integer depWordInfoPos = 
			(((temp = wiChangedMap.get(new WordInfo(typeDep.getDepPos(), typeDep.getDepWord(), "", "", ""))) == null) ?
					(typeDep.getDepPos()) : (temp)) - 1;
		Integer govWordInfoPos = 
			(((temp = wiChangedMap.get(new WordInfo(typeDep.getGovPos(), typeDep.getGovWord(), "", "", ""))) == null) ?
					typeDep.getGovPos() : temp) - 1;
		WordInfo depWIRemoved = wordList.remove(depWordInfoPos.intValue());
		wordList.add(govWordInfoPos.intValue() - 1, depWIRemoved);
		for (int i = depWordInfoPos.intValue(); i < govWordInfoPos.intValue(); i++) {
			wiChangedMap.put(wordList.get(i), Integer.valueOf(i + 1));
		}
		int smallOrder = depWordInfoPos.intValue();
		int bigOrder = govWordInfoPos - 1;
		WordInfo smallOrderWI = wordList.get(smallOrder);
		WordInfo bigOrderWI = wordList.get(bigOrder);
		if (smallOrder < bigOrder - 1) {
			switchStartTagSign(smallOrderWI, wordList.get(smallOrder + 1));
			switchEndTagSign(bigOrderWI, smallOrderWI);
		} else {
			switchStartTagSign(smallOrderWI, bigOrderWI);
			switchEndTagSign(smallOrderWI, bigOrderWI);
		}
		if ((depWordInfoPos.intValue() == 0 || (depWordInfoPos.intValue() == 1 && wordList.get(0).isDefinitionEmpty())) 
				&& isUpperCase(depWIRemoved)) {
			toTitleCase(wordList.get(depWordInfoPos.intValue()));
			toLowerCase(depWIRemoved);
		}
		//Log.i("after putToBeforeLast: ", Util.collectionToSlashString(wordList));
	}

	private static void insertBefore(GrammarTypedDependency typeDep,
			List<WordInfo> wordList, Map<WordInfo, Integer> wiChangedMap) {
		//Log.i("before insertBefore: ", Util.collectionToSlashString(wordList));
		Integer temp = 0;
		Integer depWordInfoPos = 
			((temp = wiChangedMap.get(new WordInfo(typeDep.getDepPos(), typeDep.getDepWord(), "", "", ""))) == null) ?
				(typeDep.getDepPos() - 1) : (temp - 1);
		//Log.e("depWordInfoPos", depWordInfoPos + ".");
		Integer govWordInfoPos = 
			((temp = wiChangedMap.get(new WordInfo(typeDep.getGovPos(), typeDep.getGovWord(), "", "", ""))) == null) ?
				(typeDep.getGovPos() - 1) : (temp - 1);
		// typeDep2: nn(subject-12, meditation-11)
		WordInfo increaseIndexWI = wordList.get(depWordInfoPos.intValue());
		WordInfo removed = wordList.remove(govWordInfoPos.intValue());
		wordList.add(depWordInfoPos.intValue(), removed);
		for (int i = depWordInfoPos.intValue(); i <= govWordInfoPos.intValue(); i++) {
			wiChangedMap.put(wordList.get(i), Integer.valueOf(i + 1));
		}
		switchStartTagSign(removed, increaseIndexWI);
		switchEndTagSign(removed, wordList.get(govWordInfoPos.intValue()));
		if ((1 == wiChangedMap.get(removed)
				|| (depWordInfoPos.intValue() == 1 && wordList.get(0).isDefinitionEmpty())) 
				&& isUpperCase(increaseIndexWI)) {
			toTitleCase(removed);
			toLowerCase(increaseIndexWI);
		}
		//Log.i("after insertBefore: ", Util.collectionToSlashString(wordList));
	}
	
	private static void moveToBefore(WordInfo wordInfo, int destPos,
			List<WordInfo> wordList, Map<WordInfo, Integer> wiChangedMap) {
		//Log.i("before moveTo: ", Util.collectionToSlashString(wordList));
		Integer temp = 0;
		Integer wordInfoPos = ((temp = wiChangedMap.get(wordInfo)) == null) ? (wordInfo.getOrder() - 1) : (temp - 1);
		// typeDep2: nn(subject-12, meditation-11)
		if (destPos > wordInfoPos) {
			return;
		}
		
		WordInfo increaseIndexWI = wordList.get(destPos);
		WordInfo removed = wordList.remove(wordInfoPos.intValue());
		
		wordList.add(destPos, removed);
		for (int i = destPos; i <= wordInfoPos.intValue(); i++) {
			wiChangedMap.put(wordList.get(i), Integer.valueOf(i + 1));
		}
		switchStartTagSign(removed, increaseIndexWI);
		switchEndTagSign(removed, wordList.get(wordInfoPos));
		if (destPos == 0 && isUpperCase(increaseIndexWI)) {
			toTitleCase(removed);
		}
		if (destPos == 0 && !increaseIndexWI.isDefinitionEmpty()) {
			toLowerCase(increaseIndexWI);
		}
		//Log.i("after moveTo: ", Util.collectionToSlashString(wordList));
	}
	
	private static void moveToAfter(WordInfo wordInfo, int destPos,
			List<WordInfo> wordList, Map<WordInfo, Integer> wiChangedMap) {
		//Log.i("before moveTo: ", Util.collectionToSlashString(wordList));
		Integer temp = 0;
		Integer wordInfoPos = ((temp = wiChangedMap.get(wordInfo)) == null) ? (wordInfo.getOrder() - 1) : (temp - 1);
		// typeDep2: nn(subject-12, meditation-11)
		if (destPos < wordInfoPos) {
			return;
		}
		
		WordInfo decreaseIndexWI = wordList.get(destPos);
		WordInfo removed = wordList.remove(wordInfoPos.intValue());
		if (destPos == wordList.size()) {
			wordList.add(removed);
		} else {
			wordList.add(destPos, removed);
		}
		for (int i = wordInfoPos.intValue(); i <= destPos; i++) {
			wiChangedMap.put(wordList.get(i), Integer.valueOf(i + 1));
		}
		switchStartTagSign(removed, wordList.get(wordInfoPos));
		switchEndTagSign(removed, decreaseIndexWI);
		if (wordInfoPos.intValue() == 0 && isUpperCase(removed) && !removed.isDefinitionEmpty()) {
			toTitleCase(wordList.get(0));
			toLowerCase(removed);
		}
		//Log.i("after moveTo: ", Util.collectionToSlashString(wordList));
	}

	static void insertAfter(GrammarTypedDependency typeDep,
			List<WordInfo> wordList, Map<WordInfo, Integer> wiChangedMap) {
		Integer temp = -1;
		Integer depWordInfoPos = 
			((temp = wiChangedMap.get(new WordInfo(typeDep.getDepPos(), typeDep.getDepWord(), "", "", ""))) == null) ?
				(typeDep.getDepPos() - 1) : (temp - 1);
		Integer govWordInfoPos = ((temp = wiChangedMap.get(new WordInfo(typeDep.getGovPos(), typeDep.getGovWord(), "", "", ""))) == null) ?
				(typeDep.getGovPos() - 1) : (temp - 1);
		WordInfo removed = wordList.remove(govWordInfoPos.intValue());
		wordList.add(depWordInfoPos.intValue() + 1, removed);
		for (int i = depWordInfoPos.intValue(); i <= govWordInfoPos.intValue(); i++) {
			wiChangedMap.put(wordList.get(i), Integer.valueOf(i + 1));
		}
	}

	private static int checkCompoundWords(List<WordInfo> wordList, WordInfo wordInfo1, int i) {
		int compoundNum = 0;
		WordInfo wordInfo2 = null;
		WordInfo wordInfo3 = null;
		StringBuilder wordInfoCombined2 = new StringBuilder();
		StringBuilder wordInfoCombined3 = new StringBuilder();
//		LOG.info("i = " + i + ", wordList.size(): " + wordList.size());
		if (i < wordList.size() - 1) {
			wordInfo2 = wordList.get(i + 1);
			wordInfoCombined2.append(wordInfo1.getName()).append(" ").append(wordInfo2.getName());
			compoundNum++;
		}
		if (i < wordList.size() - 2) {
			wordInfo3 = wordList.get(i + 2);
			wordInfoCombined3.append(wordInfoCombined2).append(" ").append(wordInfo3.getName());
			compoundNum++;
		}
		
		if (compoundNum > 0) {
			String combinedStr2 = wordInfoCombined2.toString();
			String combinedStr3 = wordInfoCombined3.toString();

			//Log.i("combinedStr2:", combinedStr2 + " wordDefinition2: " + wordInfoCombined2);
			ComplexWordDef wordDefinition2 = GEN_DICT.getComplexWordDef(combinedStr2);

			ComplexWordDef wordDefinition3 = null;
			if (combinedStr3.length() > 0) {
				//Log.i("combinedStr3:", combinedStr3 + " wordDefinition3: " + wordInfoCombined3);
				wordDefinition3 = GEN_DICT.getComplexWordDef(combinedStr3);
			}

			if (wordDefinition3 != null) {
				wordInfo1.clear();
				wordInfo1.setName(combinedStr3);
				wordInfo1.setDefinition(wordDefinition3.getDefinition());
				wordList.get(++i).clear();
				wordList.get(++i).clear();
				wordInfo1.setEndSign(wordList.get(i).getEndSign());
				wordList.get(i).setEndSign("");
				wordInfo1.setEndTag(wordList.get(i).getEndTag());
				wordList.get(i).setEndTag("");
			} else if (wordDefinition2 != null) {
				wordInfo1.clear();
				wordInfo1.setName(combinedStr2);
				wordInfo1.setDefinition(wordDefinition2.getDefinition());
				wordList.get(++i).clear();
				wordInfo1.setEndSign(wordList.get(i).getEndSign());
				wordList.get(i).setEndSign("");
				wordInfo1.setEndTag(wordList.get(i).getEndTag());
				wordList.get(i).setEndTag("");
			} else if (compoundNum == 2 && "whose".equalsIgnoreCase(wordInfo2.getName())) {
				wordInfo3.setDefinition(wordInfo3.getDefinition() + " của [người ấy / nó] là [người / vật / cái] mà");
				i += 2;
			}
		}
		return i;
	}

	private static void removeApos(WordInfo wordInfo) {
		String name = wordInfo.getName();
		wordInfo.setName(name.substring(0, name.indexOf("'")));
	}

	private static void insertToWordList(List<WordInfo> wordList,
			Stack<Entry<Integer, WordInfo>> stack) {
		Entry<Integer, WordInfo> entry = null;
		while (!stack.isEmpty()) {
			entry = stack.pop();
			wordList.add(entry.getKey().intValue(), entry.getValue());
		}
	}

	private static boolean isUpperCase(WordInfo wordInfo) {
		String firstChar = wordInfo.getName().substring(0, 1);
		return firstChar.toUpperCase().equals(firstChar);
	}
	
	private static boolean isLowerCase(WordInfo wordInfo) {
		String firstChar = wordInfo.getName().substring(0, 1);
		return firstChar.toLowerCase().equals(firstChar);
	}

//	private static void changeSign(WordInfo wordInfo, WordInfo depWordInfo, boolean isChangeCase) {
//		String signTemp = depWordInfo.getEndSign();
//		depWordInfo.setEndSign(wordInfo.getEndSign());
//		wordInfo.setEndSign(signTemp);
//
//		signTemp = depWordInfo.getEndTag();
//		depWordInfo.setEndTag(wordInfo.getEndTag());
//		wordInfo.setEndTag(signTemp);
//		
//		signTemp = depWordInfo.getStartSign();
//		depWordInfo.setStartSign(wordInfo.getStartSign());
//		wordInfo.setStartSign(signTemp);
//
//		signTemp = depWordInfo.getStartTag();
//		depWordInfo.setStartTag(wordInfo.getStartTag());
//		wordInfo.setStartTag(signTemp);
//		
//		if (isChangeCase) {
//			String firstCharWI = wordInfo.getName().substring(0, 1);
//			String firstCharDep = depWordInfo.getName().substring(0, 1);
//			boolean isUpperCase = firstCharWI.toUpperCase().equals(firstCharWI)
//					&& !wordInfo.getDefinition().equals(wordInfo.getName());
//			if (isUpperCase) {
//				if (!firstCharDep.toUpperCase().equals(firstCharDep)) {
//					upperCase(depWordInfo);
//					lowerCase(wordInfo);
//				}
//			} else {
//				if (!firstCharDep.toLowerCase().equals(firstCharDep)) {
//					lowerCase(depWordInfo);
//					upperCase(wordInfo);
//				}
//			}
//		}
//	}

	// private static int WORD_COUNTER = 0;
	private static void buildWordInfoToSentence(List<WordInfo> wordList, StringBuilder sb) {
//		String[] wordColor = {"<span class=\"odd\">", "<span class=\"even\">"};
//		String endSpan = "</span>";
		for (WordInfo wordInfo2 : wordList) {
//			sb.append(wordColor[WORD_COUNTER++%2]).append(wordInfo2.translated()).append(endSpan);
			sb.append(wordInfo2.translated());
		}
	}

	static NumberFormat nf = NumberFormat.getInstance();
	public static int currentPercent = 0;
	private static boolean translateParsedWords(Object[] paragraphs, String htmlFileName, final MainFragment searchFragment) {
		List<String> sentencesList;
		List<WordInfo> wordInfoList;
		StringBuilder translatedBuffer = new StringBuilder();
		createHTMLHeader(translatedBuffer);
		createHead(translatedBuffer);
		createBodyStart(translatedBuffer);
		createTableHeader(translatedBuffer);
		final File file = new File(htmlFileName);
		file.delete();
		int read = 0;
		for (Object paragraph : paragraphs) {
			//System.out.println(paragraph);
			if (TranslationSession.stopTranslate) {
				TranslationSession.stopTranslate = false;
				return CANCEL;
			}
			read += paragraph.toString().length();
			currentPercent = (100 * read) / fileSize;
			sentencesList = readSentences((String)paragraph);
			createRowHeader(translatedBuffer);
			createColumnHeader(translatedBuffer);
			translatedBuffer.append(paragraph);
			createColumnEnder(translatedBuffer);
			createColumnHeader(translatedBuffer);
			for (String sentence : sentencesList) {
				//System.out.println(sentence);
				if (searchFragment.translateTask.isCancelled()) {
					return CANCEL;
				} else {
					TranslationSession.bytesRead += sentence.getBytes().length;
//				final String sen = "File " + TranslationSession.curFileNo + "/" + searchFragment.getSourceFileTask.convertedFileList.size() + ": " + nf.format((double)TranslationSession.bytesRead * 100/searchFragment.getSourceFileTask.totalSelectedSize) + "% (" + (TranslationSession.bytesRead + "/" + searchFragment.getSourceFileTask.totalSelectedSize) + "): " + sentence;
//				searchFragment.statusView.postDelayed(new  Runnable() {
//														  @Override
//														  public void run() {
//															  searchFragment.statusView.setText(sen);
//														  }
//				}, 1);
					List<String> sentenceParts = readSentenceParts(sentence);
					for (String sentencePart : sentenceParts) {
						wordInfoList = readWordsInSentencePart(sentencePart);//sentence
						try {
							translatedBuffer.append(reorderPhrases(wordInfoList));
						} catch (Throwable e) {
							Log.e("translateParsedWords", e.getMessage(), e);
							translatedBuffer.append(wordInfoList.toString());
						}
					}
				}
			}
			createColumnEnder(translatedBuffer);
			createRowEnder(translatedBuffer);
			if (translatedBuffer.length() > 4096) {
//				System.out.println("translatedBuffer:" + translatedBuffer);
				try {
					FileUtil.writeAppendFileAsCharset(file, translatedBuffer.toString(), "utf-8");
					translatedBuffer = new StringBuilder();
					
					Log.d("file.toURL().toString()", file.toURL().toString() + ", " + file.toURI().toURL().toString());
//					if (searchFragment.webView != null) {
//						searchFragment.webView.postDelayed(new  Runnable() {
//										   @Override
//										   public void run() {
//											   try {
//												   searchFragment.locX = searchFragment.webView.getScrollX();
//												   searchFragment.locY = searchFragment.webView.getScrollY();
//												   searchFragment.webView.loadUrl(file.toURL().toString());
//											   } catch (MalformedURLException e) {
//												   e.printStackTrace();
//											   }
//										   }
//										   
//							
//						}, 1);
//					}
				} catch (IOException e) {}
//				Thread.sleep(10000);
			}
		}
		try {
			FileUtil.writeAppendFileAsCharset(file, translatedBuffer.toString(), "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		createTableEnder(translatedBuffer);
		createHTMLEnder(translatedBuffer);
		return OK;
	}

	private static final boolean OK = true;
	private static final boolean CANCEL = false;
	
	private static String translateWordByWord(String[] paragraphs) {
		List<String> sentenceList;
		List<WordInfo> wordsList;
		String definition;
		StringBuilder targetTranslated = new StringBuilder();
		createHTMLHeader(targetTranslated);
		createHead(targetTranslated);
		createBodyStart(targetTranslated);
		createTableHeader(targetTranslated);
		for (String paragraph : paragraphs) {
			sentenceList = readSentences(paragraph);
			createRowHeader(targetTranslated);
			createColumnHeader(targetTranslated);
			targetTranslated.append(paragraph);
			createColumnEnder(targetTranslated);
			createColumnHeader(targetTranslated);
			for (String sentence : sentenceList){
				wordsList = readWordsInSentencePart(sentence);
				for (WordInfo wordInfo : wordsList) {
					definition = wordInfo.getDefinition();
					if (definition != null) {
						if (definition.length() > 0) {
							targetTranslated.append(wordInfo.getStartSign()).append("[").append(definition).append("]").append(wordInfo.getEndSign());
						} else {
							targetTranslated.append(wordInfo.getStartSign()).append(wordInfo.getEndSign());
						}
					} else {
						targetTranslated.append(wordInfo.getStartSign()).append("[").append(wordInfo.getName()).append("]").append(wordInfo.getEndSign());
					}
				}
			}
			createColumnEnder(targetTranslated);
			createRowEnder(targetTranslated);
		}
		createTableEnder(targetTranslated);
		createHTMLEnder(targetTranslated);
		return targetTranslated.toString();
	}

	private static void createRowHeader(StringBuilder targetTranslated) {
		targetTranslated.append("<tr>");
	}

	private static void createRowEnder(StringBuilder targetTranslated) {
		targetTranslated.append("</tr>");
	}

	private static void createColumnHeader(StringBuilder targetTranslated) {
		targetTranslated.append("<td style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>");
	}

	private static void createColumnEnder(StringBuilder targetTranslated) {
		targetTranslated.append("</td>");
	}

	private static void createTableHeader(StringBuilder targetTranslated) {
		targetTranslated.append("<table border='0' cellspacing='0' cellpadding='0' width='100%' style='width:100%;border-collapse:collapse'>");
	}

	private static void createTableEnder(StringBuilder targetTranslated) {
		targetTranslated.append("</table>");
	}
	
	private static void createHTMLHeader(StringBuilder targetTranslated) {
		targetTranslated.append("<html>\r\n");
	}

	private static void createHead(StringBuilder targetTranslated) {
		targetTranslated.append("\t\t<head>\r\n");
		targetTranslated.append("\t\t\t<meta http-equiv=Content-Type content=\"text/html; charset=utf-8\">\r\n");
		targetTranslated.append("\t\t\t<style>\r\n");
		targetTranslated.append("BODY { background: #FFFFFF; " +
				"margin: 0; " +
				"padding: 10; " +
				"font-size:12.0pt; " +
                        	"font-family:'Times New Roman', Times, serif;}\r\n");
		targetTranslated.append("TABLE { border-style: solid; " +
				"border-color: black; " +
				"border-width: 1; " +
				"border-right-width: 1; " +
				"border-bottom-width: 1; " +
				"padding: 0; " +
				"font-family:'Times New Roman', Times, serif; " +
				"font-size:12.0pt; " +
				"text-align: justify; " +
				"margin: 15; " +
				"margin-left: 10; " +
				"margin-right: 10; }" +
                        	Constants.NEW_LINE);
		targetTranslated.append("TD, P { border-style: solid; padding: 0; border-width: 1; margin: 3; text-align: justify; vertical-align: top; }\r\n");
		targetTranslated.append("TR { background: #FFFFFF; vertical-align: top; }\r\n");
		
		targetTranslated.append(".odd { color:#000000; }\r\n");
		targetTranslated.append(".even { color:#000000; }\r\n");
		
		targetTranslated.append("\t\t\t</style>\r\n");
		targetTranslated.append("\t\t</head>\r\n");
	}
	
	private static void createBodyStart(StringBuilder targetTranslated) {
		targetTranslated.append("\t<body>\r\n");
	}
	
	private static void createHTMLEnder(StringBuilder targetTranslated) {
		targetTranslated.append("\r\n\t</body>\r\n</html>");
	}

	//	amod -> advmod
	private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("([\\r\\n]*)([^\\r\\n]+)([\\r\\n]*)");
	private static List<String> readParagraphs(String paragraphs) {
		Matcher mat = PARAGRAPH_PATTERN.matcher(paragraphs);
		List<String> paragraphList = new LinkedList<String>();
		String paragraph = "";
		while (mat.find()) {
			paragraph = mat.group(2).toString();
			if (paragraph != null && paragraph.trim().length() > 0) {
				paragraphList.add(paragraph);
//				LOG.log(Level.INFO, "paragraph: {0}", paragraph);
			}
		}
		return paragraphList;
	}

	private static final Pattern SENTENCE_PATTERN = Pattern.compile("([\"“]*)([^\\.?!]+)([\\.?!\"”]*)");
	private static List<String> readSentences(String sentences) {
		Matcher mat = SENTENCE_PATTERN.matcher(sentences);
		List<String> sentenceList = new LinkedList<String>();
		String sentence = "";
		while (mat.find()) {
			sentence = new StringBuilder(mat.group(1))
					.append(mat.group(2)).append(mat.group(3)).toString();
			if (sentence != null && sentence.trim().length() > 0) {
				sentenceList.add(sentence);
//				LOG.log(Level.INFO, "sentence: {0}", sentence);
			}
		}
		return sentenceList;
	}

	private static final Pattern SENTENCE_PART_PATTERN = Pattern.compile(
			"([\"“]*)([^\\.?!,:;\"“”()\\r\\n\\f]+)([\\.?!,:;\"“”()\\r\\n\\f]*)");
	private static List<String> readSentenceParts(String sentence) {
		Matcher mat = SENTENCE_PART_PATTERN.matcher(sentence);
		List<String> sentencePartsList = new LinkedList<String>();
		//Log.i("readSentenceParts is processing", sentence);
		
		StringBuilder sb = null;
		while (mat.find()) {
			sb = new StringBuilder(mat.group(1)).append(mat.group(2)).append(mat.group(3));
			sentencePartsList.add(sb.toString());
//			LOG.log(Level.INFO, "sentencePartsList: {0}", sb.toString());
		}
		return sentencePartsList;
	}

	private static final Pattern WORDS_PATTERN = Pattern.compile(//"");
			"([ \\.?!,:;\"“”()\\r\\n\\t\\f]*)([^ \\.?!,:;\"“”()\\r\\n\\t\\f]+)([ \\.?!,:;\"“”()\\r\\n\\t\\f]*)([^ \\.?!,:;\"“”()\\r\\n\\t\\f]*)");
	private static List<WordInfo> readWordsInSentencePart(String sentence) {
		Matcher mat = WORDS_PATTERN.matcher(sentence);
		List<WordInfo> wordInfoList = new ArrayList<WordInfo>();
		int order = 0;
		//Log.i("readWordsInSentencePart is processing", sentence);
		int indexOfApos = 0;
		String tailAdded = "";
		int pos4 = 0;
		while (mat.find(pos4)) {
			String group2 = mat.group(2);
			pos4 = mat.end(3);
//			LOG.info("group2: " + group2);
			if (group2.length() > 0) {
				tailAdded = "";
				if ((indexOfApos = group2.indexOf("'ve")) > 0) {
					group2 = group2.substring(0, indexOfApos);
					tailAdded = "have";
				} else if ((indexOfApos = group2.indexOf("'ll")) > 0) {
					group2 = group2.substring(0, indexOfApos);
					tailAdded = "will";
				} else if ((indexOfApos = group2.indexOf("n't")) > 0) {
					group2 = group2.substring(0, indexOfApos);
					tailAdded = "not";
				} else if ((indexOfApos = group2.indexOf("'s")) > 0) {
					String group4 = mat.group(4);
					if (group4.toLowerCase().endsWith("ing")) {
						group2 = group2.substring(0, indexOfApos);
						tailAdded = "is";
					} else if (group4.toLowerCase().endsWith("ed")) {
						group2 = group2.substring(0, indexOfApos);
						tailAdded = "has";
					} else if (GEN_DICT.getComplexWordDef(group4) != null) {
						TreeSet<WordClass> definitions = GEN_DICT.getComplexWordDef(group4).getDefinitions();
						for (WordClass wordClass : definitions) {
							if (Dictionary.VERB == wordClass.getType()) {
								group2 = group2.substring(0, indexOfApos);
								tailAdded = "has";
								break;
							}
						}
					}
				}
				ComplexWordDef wordDefinition = GEN_DICT.getComplexWordDef(group2);
//				LOG.info("wordDefinition: " + wordDefinition);
				if (wordDefinition != null) {
					if (tailAdded.length() > 0) {
						wordInfoList.add(new WordInfo(++order, group2, wordDefinition.getDefinitions(), mat.group(1), ""));
						wordInfoList.add(new WordInfo(++order, tailAdded, GEN_DICT.getComplexWordDef(tailAdded).getDefinitions(), "", mat.group(3)));
					} else {
						wordInfoList.add(new WordInfo(++order, group2, wordDefinition.getDefinitions(), mat.group(1), mat.group(3)));
					}
				} else {
//					newWords.add(group2);
					if (tailAdded.length() > 0) {
						wordInfoList.add(new WordInfo(++order, group2, "", mat.group(1), ""));
						wordInfoList.add(new WordInfo(++order, tailAdded, GEN_DICT.getComplexWordDef(tailAdded).getDefinitions(), "", mat.group(3)));
					} else {
						wordInfoList.add(new WordInfo(++order, group2, "", mat.group(1), mat.group(3)));
					}
				}
			} else {
				wordInfoList.add(new WordInfo(++order, group2, "", mat.group(1), mat.group(3)));
			}
		}
//		LOG.log(Level.INFO, "wordInfoList: {0}", wordInfoList);
		return wordInfoList;
	}
	
//	static List<String> newWords;
	private static int fileSize = 0;

	public static boolean translateFromSimpleWordFile(File wordFileName, String htmlFileName, MainFragment searchFragment) throws IOException {
		long start = System.currentTimeMillis();
//		newWords = new LinkedList<String>();
		String[] paragraphs = FileUtil.readWordFileToParagraphs(wordFileName.getAbsolutePath());
		for (String st : paragraphs) {
			fileSize += st.length();
		}
		Log.i("Translated file ", wordFileName + ": " + (System.currentTimeMillis() - start) + " milliseconds");
		boolean translateParseWords = translateParsedWords(paragraphs, htmlFileName, searchFragment);
		//FileUtil.writeContentToFile(htmlFileName, translateParseWords);
		return translateParseWords;
	}

	public static boolean translateFromPlainTextFile(File fileName, String htmlFileName, MainFragment searchFragment) throws IOException {
		Log.i("translateFromPlainTextFile", fileName + ": " + fileName + ", " + htmlFileName);
		long start = System.currentTimeMillis();
//		newWords = new LinkedList<String>();
		String fileContent = FileUtil.readFileWithCheckEncode(fileName.getAbsolutePath());
		fileSize += fileContent.length();
		List<String> paragraphs = readParagraphs(fileContent);
		
//		FileUtil.writeContentToFile(htmlFileName, translateParseWords);
		Log.i("Translated file ", fileName + ": " + (System.currentTimeMillis() - start) + " milliseconds");
		boolean translateParseWords = translateParsedWords(paragraphs.toArray(), htmlFileName, searchFragment);
		return translateParseWords;
	}

	public static boolean translateFromParagraphList(List<String> paragraphs, String htmlFileName, MainFragment searchFragment) throws IOException {
		long start = System.currentTimeMillis();
//		newWords = new LinkedList<String>();
		for (String st : paragraphs) {
			fileSize += st.length();
		}
		new  File(htmlFileName).getParentFile().mkdirs();
		
		//FileUtil.writeContentToFile(htmlFileName, translateParseWords);
		Log.i("Translated took: ", (System.currentTimeMillis() - start) + " milliseconds");
		boolean translateParseWords = translateParsedWords(paragraphs.toArray(), htmlFileName, searchFragment);
		return translateParseWords;
	}

	public static String translateWordByWordFromWordFile(File wordFileName, String htmlFileName) throws IOException {
		long start = System.currentTimeMillis();
//		newWords = new LinkedList<String>();
		String[] paragraphs = FileUtil.readWordFileToParagraphs(wordFileName.getAbsolutePath());
		String translateWordByWord = translateWordByWord(paragraphs);
		FileUtil.writeContentToFile(htmlFileName, translateWordByWord);
		Log.i("Translated file ", wordFileName + ": " + (System.currentTimeMillis() - start) + " milliseconds");
		return translateWordByWord;
	}
	
	public static String translateParagraphs(List<String> paragraphs) {
		long start = System.currentTimeMillis();
//		newWords = new LinkedList<String>();
		List<String> sentencesList;
		List<WordInfo> wordsList;
		StringBuilder translatedSentencePart = new StringBuilder();
		for (String paragraph : paragraphs) {
			sentencesList = readSentences(paragraph);
			for (String sentence : sentencesList) {
				List<String> sentenceParts = readSentenceParts(sentence);
				for (String sentencePart : sentenceParts) {
					wordsList = readWordsInSentencePart(sentencePart);
					try {
						translatedSentencePart.append(reorderPhrases(wordsList));
					} catch (RuntimeException e) {
						translatedSentencePart.append(wordsList.toString());
					}
				}
			}
			translatedSentencePart.append(Constants.NEW_LINE);
		}
		Log.i("Translated took: ", (System.currentTimeMillis() - start) + " milliseconds");
		return translatedSentencePart.toString();
	}

	public static String translateParagraphs(String paragraphs) {
		List<String> paragraphList = readParagraphs(paragraphs);
		return translateParagraphs(paragraphList);
	}

	public static void openOfficeConvertFailed(File sourceFile,
			String newFullFileName) {
		try {
			if (sourceFile.getAbsolutePath().toLowerCase().endsWith("docx")
					|| sourceFile.getAbsolutePath().toLowerCase().endsWith("xlsx")
					|| sourceFile.getAbsolutePath().toLowerCase().endsWith("pptx")) {
//				AbstractHtmlExporter exporter = new HtmlExporterNG2();
//				OutputStream os = new java.io.FileOutputStream(newFullFileName);
//				StreamResult result = new StreamResult(os);
//				WordprocessingMLPackage wordMLPackage;
//				wordMLPackage = WordprocessingMLPackage.load(sourceFile);
//				exporter.html(wordMLPackage, result, newFullFileName + "_files");
			} else if (sourceFile.getAbsolutePath().toLowerCase().endsWith("doc")) {
				Translator.translateFromSimpleWordFile(sourceFile, newFullFileName, null);
			}
		} catch (IOException e) {
//			JOptionPane.showMessageDialog(null, "I/O Error");
			Log.e("I/O Error", e.getMessage(), e);
//		} catch (Docx4JException e) {
//			JOptionPane.showMessageDialog(null, "I/O Error");
//			Constants.Log.i("Convert to html error", e);
		} catch (Exception e) {
//			JOptionPane.showMessageDialog(null, "I/O Error");
			Log.e("Misc Error", e.getMessage(), e);
		}
	}
}
