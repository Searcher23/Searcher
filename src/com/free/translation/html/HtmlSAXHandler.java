package com.free.translation.html;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import com.free.translation.*;
import com.free.translation.util.*;
import com.free.searcher.*;

/**
 * 0) check encoding 1) fix meta 2) fix html encoded characters: &quot; &amp;
 * &gt; &lt; &nbsp; 3) build data structure
 */
public class HtmlSAXHandler extends DefaultHandler implements LexicalHandler {

    private Stack<Element> elementStack = new Stack<Element>();
    private Stack<EmptyElement> emptyElementStack = new Stack<EmptyElement>();
    private Stack<Element> formatStack = new Stack<Element>();
    private Stack<Stack<?>> lastStack = new Stack<Stack<?>>();
    private List<WordInfo> currentEleWIList = new LinkedList<WordInfo>();
    private boolean previousIsChar = false;
    private int formatCounter = 0;
    private StringBuilder startTagsSB = null;
    private StringBuilder newHtmlTable = new StringBuilder();
    private StringBuilder leftSB = new StringBuilder();
    private StringBuilder rightSB = new StringBuilder();
    private Element html = null;
//    private Element oriHTML = new Element("html");
//    private Stack<Element> oriElementStack = new Stack<Element>();
    static Dictionary DICTIONARY = Translator.GEN_DICT;
//    private List<String> structList = null;
//    private List<String> dataList = null;
//    private List<String> formatList = null;
    private static final Logger LOG = Constants.LOGGER;

    public HtmlSAXHandler() {
//        structList = Arrays.asList(new String[]{"head", "table", "col", "colgroup", "tr",
//                    "thead", "tbody", "tfoot", "dl", "ol", "ul", "menu"});
//        Collections.sort(structList);
//
//        dataList = Arrays.asList(new String[]{"body", "address", "blockquote", "center",
//                    "del", "div", "h1", "h2", "h3", "h4", "h5", "h6", "li", "td", "th",
//                    "noscript", "p", "pre", "title", "style", "script", "dt", "dd"});
//        Collections.sort(dataList);
//
//        formatList = Arrays.asList(new String[]{"a", "font", "span", "sub",
//                    "sup", "strong", "b", "i", "em", "u", "s", "big", "small",
//                    "strike"});
//        Collections.sort(formatList);
    }


    public Element getHtml() {
        return html;
    }

//    public Element getOriHTML() {
//        return oriHTML;
//    }

    public StringBuilder getNewHtmlTable() {
		return newHtmlTable;
	}

	@Override
    public void startDocument() throws SAXException {
        LOG.info("start document   : ");
    }

    @Override
    public void endDocument() throws SAXException {
        LOG.info("end document     : ");
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        LOG.log(Level.INFO, "start element    : {0}", qName);
        if (TranslationApp.stopTranslate) {
			throw new RuntimeException("Stop Translating");
		}
        //
//        if ("html".equalsIgnoreCase(qName)) {
//            fillAttrs(attributes, oriHTML);
//            oriElementStack.push(oriHTML);
//        } else if ("html".equalsIgnoreCase(qName)
//                || "head".equalsIgnoreCase(qName)
//                || "meta".equalsIgnoreCase(qName)
//                || "title".equalsIgnoreCase(qName)
//                || "style".equalsIgnoreCase(qName)) {
//            Element oriElement = new Element(qName);
//            fillAttrs(attributes, oriElement);
//            Element oriParent = oriElementStack.peek();
//            oriParent.addData(oriElement);
//            oriElementStack.push(oriElement);
//        }

        if ("html".equalsIgnoreCase(qName)) {
            html = new Element("html");
            fillAttrs(attributes, html);
            elementStack.push(html);
            lastStack.push(elementStack);
            formatCounter = 0;
            previousIsChar = false;
            startTagsSB = new StringBuilder();

            newHtmlTable.append(html.getStartTag());
//            fillAttrs(attributes, oriHTML);
//            oriElementStack.push(oriHTML);

        } else if ("head".equalsIgnoreCase(qName) // structure
                || "table".equalsIgnoreCase(qName)
                || "col".equalsIgnoreCase(qName)
                || "colgroup".equalsIgnoreCase(qName)
                || "tr".equalsIgnoreCase(qName)
                || "thead".equalsIgnoreCase(qName)
                || "tbody".equalsIgnoreCase(qName)
                || "tfoot".equalsIgnoreCase(qName)
                || "dl".equalsIgnoreCase(qName)
                || "ol".equalsIgnoreCase(qName)
                || "ul".equalsIgnoreCase(qName)
                || "menu".equalsIgnoreCase(qName)) {
            Element element = new Element(qName);
            fillAttrs(attributes, element);
            Element parent = elementStack.peek();
            parent.addData(element);
            elementStack.push(element);
            lastStack.push(elementStack);
            formatCounter = 0;
            previousIsChar = false;
            startTagsSB = new StringBuilder();

            if (!"head".equalsIgnoreCase(qName)) {
            	leftSB.append(element.getStartTag());
            	rightSB.append(element.getStartTag());
            } else {
            	newHtmlTable.append(element.getStartTag());
            }
            
//			Element oriElement = new Element(qName);
//			fillAttrs(attributes, oriElement);
//			Element oriParent = oriElementStack.peek();
//			oriParent.addData(oriElement);
//			oriElementStack.push(oriElement);

        } else if ("body".equalsIgnoreCase(qName) // block
                || "address".equalsIgnoreCase(qName)
                || "blockquote".equalsIgnoreCase(qName)
                || "center".equalsIgnoreCase(qName)
                || "del".equalsIgnoreCase(qName)
                || "div".equalsIgnoreCase(qName)
                || "h1".equalsIgnoreCase(qName)
                || "h2".equalsIgnoreCase(qName)
                || "h3".equalsIgnoreCase(qName)
                || "h4".equalsIgnoreCase(qName)
                || "h5".equalsIgnoreCase(qName)
                || "h6".equalsIgnoreCase(qName)
                || "li".equalsIgnoreCase(qName)
                || "td".equalsIgnoreCase(qName)
                || "th".equalsIgnoreCase(qName)
                || "noscript".equalsIgnoreCase(qName)
                || "p".equalsIgnoreCase(qName)
                || "pre".equalsIgnoreCase(qName)
                || "title".equalsIgnoreCase(qName) // data
                || "style".equalsIgnoreCase(qName)
                || "script".equalsIgnoreCase(qName)
                || "dt".equalsIgnoreCase(qName)
                || "dd".equalsIgnoreCase(qName)) {
            Element element = new Element(qName);
            fillAttrs(attributes, element);
            Element parent = elementStack.peek();
            parent.addData(element);
            elementStack.push(element);
            lastStack.push(elementStack);
            formatCounter = 0;
            currentEleWIList = new LinkedList<WordInfo>();
            previousIsChar = false;
            startTagsSB = new StringBuilder();

            if (!"body".equalsIgnoreCase(qName)
            	&& !"div".equalsIgnoreCase(qName)
           		&& !"title".equalsIgnoreCase(qName)
           		&& !"title".equalsIgnoreCase(qName)
           		&& !"style".equalsIgnoreCase(qName)
          		&& !"script".equalsIgnoreCase(qName)) {
            	leftSB.append(element.getStartTag());
            	rightSB.append(element.getStartTag());
            } else if ("body".equalsIgnoreCase(qName)) {
            	newHtmlTable.append(element.getStartTag());
            	newHtmlTable.append("<table width='100%'>");
            } else {
            	newHtmlTable.append(element.getStartTag());
            }
//			Element oriElement = new Element(qName);
//			fillAttrs(attributes, oriElement);
//			Element oriParent = oriElementStack.peek();
//			oriParent.addData(oriElement);
//			oriElementStack.push(oriElement);

        } else if ("a".equalsIgnoreCase(qName) // format
                || "font".equalsIgnoreCase(qName)
                || "span".equalsIgnoreCase(qName)
                || "sub".equalsIgnoreCase(qName)
                || "sup".equalsIgnoreCase(qName)
                || "strong".equalsIgnoreCase(qName)
                || "b".equalsIgnoreCase(qName)
                || "i".equalsIgnoreCase(qName)
                || "em".equalsIgnoreCase(qName)
                || "u".equalsIgnoreCase(qName)
                || "s".equalsIgnoreCase(qName)
                || "big".equalsIgnoreCase(qName)
                || "small".equalsIgnoreCase(qName)
                || "strike".equalsIgnoreCase(qName)) {
            Element element = new Element(qName);
            fillAttrs(attributes, element);
            formatStack.push(element);
            lastStack.push(formatStack);
            if (!previousIsChar) {
                startTagsSB.append(element.getStartTag());
            }

            leftSB.append(element.getStartTag());
//            rightSB.append(element.getStartTag());
//            newHtmlTable.append(element.getStartTag());
//			Element oriElement = new Element(qName);
//			fillAttrs(attributes, oriElement);
//			Element oriParent = oriElementStack.peek();
//			oriParent.addData(oriElement);
//			oriElementStack.push(oriElement);

        } else {
            LOG.log(Level.INFO, "confirm this is EmptyElement? {0}", qName);	// emptyElement no need use in WordInfo
            EmptyElement ee = new EmptyElement(qName);
            fillAttrs(attributes, ee);
            elementStack.peek().addData(ee);
            emptyElementStack.push(ee);
            lastStack.push(emptyElementStack);
            formatCounter = 0;
            previousIsChar = false;
            startTagsSB = new StringBuilder();

//            newHtmlTable.append(ee.getStartTag());
            if (!"meta".equalsIgnoreCase(qName)) {
	            leftSB.append(ee.toString());
	            rightSB.append(ee.toString());
            } else {
            	newHtmlTable.append(ee.toString());
            }
//			Element oriElement = new Element(qName);
//			fillAttrs(attributes, oriElement);
//			Element oriParent = oriElementStack.peek();
//			oriParent.addData(oriElement);
//			oriElementStack.push(oriElement);

        }
//		LOG.info("lastStack.size(): " + lastStack.size() + " : " + lastStack.peek());
//		LOG.info(objStack.peek().toString());
    }

    boolean beforeIsData = false;
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        LOG.log(Level.INFO, "end element      : {0}", qName);
        previousIsChar = false;
        startTagsSB = new StringBuilder();
//        newHtmlTable.append(((EmptyElement)((Stack<?>)lastStack.peek()).peek()).getEndTag());
        if (lastStack.peek() == formatStack) {
            Element formatElement = formatStack.pop();
            if (formatCounter > 0) {
            	formatCounter--;
            }
            lastStack.pop();
            leftSB.append(formatElement.getEndTag());
            if (currentEleWIList.size() > 0) {
                WordInfo wordInfo = currentEleWIList.get(currentEleWIList.size() - 1);
                LOG.log(Level.INFO, "last WordInfo: {0}", wordInfo);
                LOG.log(Level.INFO, "formatElement: {0}", formatElement);
                wordInfo.setEndTag(wordInfo.getEndTag() + formatElement.getEndTag());
                LOG.log(Level.INFO, "lastStack.pop() == formatStack: {0}", currentEleWIList);
            }
            beforeIsData = false;
        } else if (lastStack.peek() == emptyElementStack) {
            LOG.info("lastStack.pop() == emptyElementStack");
            emptyElementStack.pop();
            lastStack.pop();
            beforeIsData = false;
            if (!"meta".equalsIgnoreCase(qName)) {
//            	leftSB = new StringBuilder();
//            	rightSB = new StringBuilder();
//	            newHtmlTable.append("<tr>").append("<td>").append(leftSB).append("</td>").append(
//						"<td>").append(rightSB).append("</td>").append("</tr>");
//	            leftSB = new StringBuilder();
//	            rightSB = new StringBuilder();
            }
        } else {
            Element element = elementStack.pop();
            lastStack.pop();
            LOG.log(Level.INFO, "currentEleWIList: {0}", currentEleWIList);
            
            if ("body".equalsIgnoreCase(qName)) {
            	newHtmlTable.append("</table>").append(element.getEndTag());
            	beforeIsData = true;
            } else if ("html".equalsIgnoreCase(qName)
               		|| "head".equalsIgnoreCase(qName)
               		|| "title".equalsIgnoreCase(qName)
               		|| "style".equalsIgnoreCase(qName)
              		|| "script".equalsIgnoreCase(qName)) {
            	newHtmlTable.append(element.getEndTag());
            	beforeIsData = true;
            } else {
            	String reorderPhrases = "";
            	leftSB.append(element.getEndTag());
//            	if (!beforeIsData) {
	            	newHtmlTable.append("<tr valign='top'>").append("<td>").append(leftSB).append("</td>").append("\r\n\r\n").append(
	            	"<td>").append(rightSB);
//            	} else {
//            		newHtmlTable.append(element.getEndTag());
//            	}
            	beforeIsData = true;
            	if (currentEleWIList.size() > 0) {
            		List<List<WordInfo>> wiListParts = partSentences(currentEleWIList);
            		LOG.log(Level.INFO, "wiListParts: {0}", wiListParts);
            		for (List<WordInfo> list : wiListParts) {
            			LOG.log(Level.INFO, "before reorderPhrases: {0}", list);
            			try {
            				reorderPhrases = Translator.reorderPhrases(list);
            			} catch (Throwable e) {
            				reorderPhrases = list.toString();
            				LOG.throwing(this.getClass().getName(), "", e);
            			}
            			LOG.log(Level.INFO, "after reorderPhrases: {0}", reorderPhrases);
            			element.addData(reorderPhrases);
//            			rightSB.append(reorderPhrases).append(element.getEndTag());
            			newHtmlTable.append(reorderPhrases);
            		}
            		currentEleWIList = new LinkedList<WordInfo>();
            	}
//            	if (!isData(elementStack.peek().getName())) {
            		newHtmlTable.append("</td>").append("</tr>").append("\r\n\r\n");
//            	}
            	leftSB = new StringBuilder();
            	rightSB = new StringBuilder();
            }
        }

        // for oriHTML
//        oriElementStack.pop();
    }

	@Override
    public void characters(char ch[], int start, int length)
            throws SAXException {
        String contents = new String(ch, start, length);
        LOG.log(Level.INFO, "start characters : {0}", contents);
//        newHtmlTable.append(contents);
        String lastElementName = ((Element) (((Stack<?>) lastStack.peek()).peek())).getName();
        if ("title".equalsIgnoreCase(lastElementName)
           		|| "style".equalsIgnoreCase(lastElementName)
          		|| "script".equalsIgnoreCase(lastElementName)) {
        	newHtmlTable.append(contents);
        } else {
        	leftSB.append(contents);
        }
//		if (contents.trim().length() > 0) {
        LOG.info("current Content: " + contents);
        List<WordInfo> wiList = readWordsInSentencePart(contents);
        LOG.log(Level.INFO, "character wiList: {0}", wiList);
        LOG.log(Level.INFO, "lastStack.peek() == elementStack: {0}", (lastStack.peek() == elementStack));
        LOG.log(Level.INFO, "lastStack.peek() == formatStack: {0}", (lastStack.peek() == formatStack));
        LOG.log(Level.INFO, "lastStack.peek() == emptyElementStack: {0}", (lastStack.peek() == emptyElementStack));
        if (lastStack.peek() == elementStack) {		// add vào data tag
//            if (Util.isNotEmpty(contents)) {
                currentEleWIList.addAll(wiList);
//            } else {
//                ((Element) (((Stack<?>) lastStack.peek()).peek())).addData("\r\n\r\n");
//            }
        } else if (lastStack.peek() == formatStack) {	// bỏ double start tag <span>a<i>b</i>c
            if ((formatCounter + 1) == formatStack.size()) {
                formatCounter++;
                Element formatPeekEle = formatStack.peek();
                WordInfo firstWI = wiList.get(0);
                firstWI.setStartTag(formatPeekEle.getStartTag());	// + firstWI.getStartTag());
            } else if (!previousIsChar) {	// lấy start tag liên tục <span><span><span>
                WordInfo firstWI = wiList.get(0);
                firstWI.setStartTag(startTagsSB.append(firstWI.getStartTag()).toString());
            }
            currentEleWIList.addAll(wiList);
            LOG.log(Level.INFO, "character currentEleWIList: {0}", currentEleWIList);
        }
//		} else {
//			((Element)(((Stack<?>)lastStack.peek()).peek())).addData(contents);
//		}
//        if (Util.isNotEmpty(contents)) {
        previousIsChar = true;
//        }

//        oriElementStack.peek().addData(contents);
    }
    private static Pattern WORD_SEPARATE_PATTERN = Pattern.compile("[\\.?!,\\:;\"“”()]");
    private static Pattern WORD_WITH_SPACE_SEPARATE_PATTERN = Pattern.compile("[\\p{Space}\\.?!,\\:;\"“”()]");

    private List<List<WordInfo>> partSentences(List<WordInfo> elementWIList) {
        List<List<WordInfo>> ret = new LinkedList<List<WordInfo>>();
        List<WordInfo> wiList = new LinkedList<WordInfo>();
        ret.add(wiList);
        int order = 0;
        LOG.log(Level.INFO, "elementWIList: {0}", elementWIList);
        WordInfo preWI = null;
        WordInfo signWI = null;
        for (WordInfo wi : elementWIList) {
            LOG.log(Level.INFO, "current preWI: {0}", preWI);
            LOG.log(Level.INFO, "current WI: {0}", wi);
            LOG.log(Level.INFO, "current signWI: {0}", signWI);
            Matcher wordSepStartMat = WORD_SEPARATE_PATTERN.matcher(wi.getStartSign().trim());
            Matcher wordSepEndMat = WORD_SEPARATE_PATTERN.matcher(wi.getEndSign().trim());
            boolean wordSepStartMatFind = wordSepStartMat.find();
			boolean wordSepEndMatFind = wordSepEndMat.find();
			if (wordSepStartMatFind && wordSepEndMatFind) {
                wiList = new LinkedList<WordInfo>();
                order = 0;
                wi.setOrder(++order);
                wiList.add(wi);
                ret.add(wiList);
                wiList = new LinkedList<WordInfo>();
                ret.add(wiList);
                order = 0;
                signWI = null;
                preWI = wi;
            } else if (wordSepStartMatFind) {
                wiList = new LinkedList<WordInfo>();
                order = 0;
                wi.setOrder(++order);
                wiList.add(wi);
                ret.add(wiList);
                signWI = null;
                preWI = wi;
            } else if (wordSepEndMatFind) {	// gặp phải ngăn cách ở endSign, làm new wiList
                wi.setOrder(++order);
                wiList.add(wi);
                LOG.log(Level.INFO, "1. updated ret: {0}", ret);
                wiList = new LinkedList<WordInfo>();
                LOG.info("1. just make new wiList");
                ret.add(wiList);
                order = 0;
                signWI = null;
                preWI = wi;
            } else {
                Matcher wordSepNameMat = WORD_WITH_SPACE_SEPARATE_PATTERN.matcher(wi.getName());
                boolean find = wordSepNameMat.find() || "".equals(wi.getName().trim());
                if (find && preWI != null) {	// gặp phải ngăn cách nằm riêng biệt và có wi trước đó
                    preWI.setEndTag(preWI.getEndTag() + wi.signTranslated());
                    LOG.log(Level.INFO, "changed preWI: {0}", preWI);
                    LOG.log(Level.INFO, "2. updated ret: {0}", ret);

                    wiList = new LinkedList<WordInfo>();
                    LOG.info("2. just make new wiList");
                    ret.add(wiList);
                    order = 0;
                    signWI = null;
//					preWI = wi;	// vì current wi là sign only nên không được lưu do vậy không update preWI = wi
                } else if (find && preWI == null) {	// gặp phải ngăn cách không có wi trước đó nên giành lại wi sau
                    signWI = wi;
                    LOG.log(Level.INFO, "signWI = wi: {0}", signWI);
                } else {
                    if (signWI != null) {	// check trước đó có sign ko, nếu có thì
                        wi.setStartTag(signWI.signTranslated() + wi.getStartTag());
                        LOG.log(Level.INFO, "changed wi: {0}", wi);
                        signWI = null;
                    }
                    wi.setOrder(++order);
                    preWI = wi;
                    wiList.add(wi);
                    LOG.log(Level.INFO, "3. updated ret: {0}", ret);
                }
            }
        }
        int retSize = ret.size();
        if (retSize > 0) {
            List<WordInfo> last = ret.get(retSize - 1);
            if (last.isEmpty()) {
                ret.remove(retSize - 1);
            }
        }
        return ret;
    }
    private static final Pattern WORDS_PATTERN = Pattern.compile(
            "([\\.?!,:;\"“”()\\p{Space}]*)([^ \\.?!,:;\"“”()\\r\\n\\t\\f]+)([\\.?!,:;\"“”()\\p{Space}]*)");

    private List<WordInfo> readWordsInSentencePart(String sentence) {
        Matcher mat = WORDS_PATTERN.matcher(sentence);
        List<WordInfo> wordInfoList = new ArrayList<WordInfo>();
        int order = 0;
        LOG.log(Level.INFO, "readWordsInSentencePart is processing: {0}, length: {1}", new Object[]{sentence, sentence.length()});
        boolean isMatched = mat.find();
        if (!isMatched) {
            wordInfoList.add(new WordInfo(++order, sentence, "", "", ""));
//			if (currentEleWIList.size() > 0) {
//				WordInfo wordInfo = currentEleWIList.get(currentEleWIList.size() - 1);
//				wordInfo.setEndSign(wordInfo.getEndSign() + sentence);
//			} else {
//				currentEleWIList.add(new WordInfo(++order, "", null, sentence, ""));
//			}
        }
        while (isMatched) {
            String group2 = mat.group(2);
//			LOG.info("group2: " + group2);
            if (group2.length() > 0) {
                ComplexWordDef wordDefinition = DICTIONARY.getDefinition(group2);
//				LOG.info("wordDefinition: " + wordDefinition);
                if (wordDefinition != null) {
                    wordInfoList.add(new WordInfo(++order, group2, wordDefinition.getDefinitions(), mat.group(1), mat.group(3)));
                } else {
//					newWords.add(group2);
                    wordInfoList.add(new WordInfo(++order, group2, "", mat.group(1), mat.group(3)));
                }
            } else {
//				newWords.add(group2);
                wordInfoList.add(new WordInfo(++order, group2, "", mat.group(1), mat.group(3)));
            }
            isMatched = mat.find();
        }
        LOG.log(Level.INFO, "wordInfoList: {0}", wordInfoList);
        return wordInfoList;
    }

    private void fillAttrs(Attributes attributes, EmptyElement bean) {
        for (int i = 0; i < attributes.getLength(); i++) {
            String localAttrName = attributes.getQName(i);
            String valueAttr = attributes.getValue(i);
//			LOG.info("localAttrName: " + localAttrName);
//			LOG.info("valueAttr: " + valueAttr);
            bean.addAttr(localAttrName, valueAttr);
        }
    }

    @Override
    public void startDTD(String name, String publicId, String systemId)
            throws SAXException {
    }

    @Override
    public void endDTD() throws SAXException {
    }

    @Override
    public void startEntity(String name) throws SAXException {
    }

    @Override
    public void endEntity(String name) throws SAXException {
    }

    @Override
    public void startCDATA() throws SAXException {
    }

    @Override
    public void endCDATA() throws SAXException {
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
        if (!elementStack.isEmpty()) {
            LOG.log(Level.INFO, "start comment : {0}", new String(ch, start, length));
            Element obj = elementStack.peek();
            obj.addData("<!--" + new String(ch, start, length) + "-->");
            newHtmlTable.append("<!--").append(new String(ch, start, length)).append("-->");
        }
    }
    
//    public boolean isStructure(String str) {
//        return Collections.binarySearch(structList, str) >= 0;
//    }
//
//    public boolean isData(String str) {
//        return Collections.binarySearch(dataList, str) >= 0;
//    }
//
//    public boolean isFormat(String str) {
//        return Collections.binarySearch(formatList, str) >= 0;
//    }

    public static void main(String[] argv) throws SAXException, IOException,
            ParserConfigurationException {
        File oldFile = new File("I:/The Nhap 22-2.htm"); //Constants.ORI_HTML_FILE_NAME);
        File newFile = new File(Constants.NEW_HTML_FILE_NAME);

		FileUtil.writeContentToFile("I:/test.html", translateFromHTML(oldFile, newFile));
//		StringBuilder sb = new StringBuilder();
//		sb.append("<html>").append("<body>");
//		
//		sb.append("<table>");
//		sb.append("	<tr>");
//		sb.append("		<td>");
//		sb.append(result);
//		sb.append("		</td>");
//		sb.append("		<td>");
//		sb.append(handler.getHtml());
//		sb.append("		</td>");
//		sb.append("</tr>");
//		sb.append("</table>");

//		sb.append("</body>").append("</html>");
//		FileUtil.writeContentToFile("I:/test-table.html", sb.toString());
    }
//	static List<String> newWords;
    public static String translateFromHTML(File oldFile, File newFile) throws FileNotFoundException,
            IOException, ParserConfigurationException, SAXException,
            SAXNotRecognizedException, SAXNotSupportedException {
		LOG.log(Level.INFO, "oldFile: {0}, newFile: {1}", new Object[] {
				oldFile.toString(), newFile.toString() });
//		newWords = new LinkedList<String>();
//        File newTmpFile = new File(newFile.getAbsoluteFile() + ".tmp");
		File newTmpFile = new File(oldFile.getAbsolutePath() + ".tmp.html");
        FileUtil.initStrictHtml(oldFile, newTmpFile);
        InputStream bakFileInputStream = new FileInputStream(newTmpFile);
        BufferedInputStream bis = new BufferedInputStream(bakFileInputStream);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();

        HtmlSAXHandler handler = new HtmlSAXHandler();
        xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        //xmlReader.setProperty("http://apache.org/xml/properties/input-buffer-size", new Integer(65536));

        saxParser.parse(bis, handler);

        bakFileInputStream.close();
        bis.close();
        FileUtil.writeContentToFile(newFile.getAbsolutePath(), handler.getNewHtmlTable().toString());
        FileUtil.delete(newTmpFile);
        return handler.getNewHtmlTable().toString();
    }
}
