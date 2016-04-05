package com.free.translation.util;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import android.util.*;
import com.free.searcher.*;

public class HTMLToText {

	// private static final Logger logger = Logger.getLogger(HTMLToText.class.getName());

	// "script", "style"
	private static Pattern SCRIPT_STYLE_REMOVE = Pattern.compile(
		"<(script|style)[^>]*?>([\u0001-\uFFFF]*?)</\\1\\s*>",
		Pattern.CASE_INSENSITIVE);

	private static Pattern CR_TAGS = Pattern.compile(
		"</?(blockquote|br|cite|h1|h2|h3|h4|h5|h6|hr|li|p|pre|tr|title"
		+"|o:p|last-name|book-title|lang|subtitle|empty-line)[^>]*/?>",
		Pattern.CASE_INSENSITIVE);
//		"</?(blockquote|br|li|p|tr|title|h1|h2|h3|h4|h5|h6|hr
//		|o:p|last-name|book-title|lang|subtitle|empty-line)[^>]*/?>",
	
	// "html", "meta", "head", "table", "col", "colgroup", "tr", "thead",
	// "tbody", "tfoot", "dl", "ol", "ul", "menu"
	// "body", "address", "center", "del", "div",
	// "td", "th", "noscript", "pre",
	// "dt", "dd"
	// "a",
	// "span", "sub", "sup", "strong", "b", "i", "em", "u",
	// "s", "big", "small", "strike"
	// "img", "link" <hgroup class="longsutta"> </q>
	private static Pattern REMOVE_TAGS = Pattern.compile(
		"</?(!DOCTYPE|html|head|table|base|meta|link|noscript"
		+"|frameset|frame|iframe|noframes"
		+"|body|center|div|ul|ol|menu|dir|dl|dt|dd|address|ins|del"
		+"|a|span|bdo|em|strong|dfn|code|samp|kbd|var|abbr|acronym"
		+"|q|sub|sup|tt|i|b|big|small|u|s|strike|basefont|font"
		+"|object|param|applet|img|map|area|form|label|input|select"
		+"|option|textarea|optgroup|fieldset|legend|button|isindex"
		+"|caption|thead|tfoot|tbody|colgroup|col|th|td|hgroup"
		+"|\\?xml|container|rootfiles|rootfile"
		+"|FictionBook|description|title-info|author)[^>]*?/?>",
//		"</?(!DOCTYPE|html|meta|head|table|col|colgroup"
//		+ "|thead|tbody|tfoot|ol|ul"
//		+ "|body|div"
//		+ "|td|th"
//		+ "|font|hgroup|q"
//		+ "|a|span|sub|sup|strong|b|i|em|u"
//		+ "|s|big|small|strike" 
//		+ "|\\?xml|container|rootfiles|rootfile"
//		+ "|FictionBook|description|title-info|author|code"
//		+ "|form|select|option|input|iframe|param|basefont"
//		+ "|applet|map|label|optgroup|textarea|fieldset|legend"
//		+ "|button|isindex|base|menu|dir|dl|dd|dt|address|pre"
//		+ "|center|ins|del|bdo|dfn|code|samp|kbd"
//		+ "|var|cite|abbr|acronym|tt|object|caption"
//		+ "|frameset|frame|noframes|noscript"
//		+ "|img|link)[^>]*?/?>",
		Pattern.CASE_INSENSITIVE);
	// <![IF !supportFootnotes]>
	// <![ENDIF]>
	private static Pattern REMOVE_CR_PATTERN = Pattern
	.compile("[\\r\\n\\t\\f ]+");
	private static Pattern REMOVE_COMMENT_PATTERN = Pattern
	.compile("<!--[\u0000-\uFFFF]*?-->");
	private static Pattern REMOVE_STARTNOTE = Pattern.compile(
		"<![-]*\\[IF [^]]*]>", Pattern.CASE_INSENSITIVE);
	private static Pattern REMOVE_ENDNOTE = Pattern.compile("<!\\[ENDIF][-]*>",
															Pattern.CASE_INSENSITIVE);

	public static String htmlToText(File inFile) throws IOException {
		String wholeFile = FileUtil.readFileByMetaTag(inFile);
		wholeFile = htmlToText(wholeFile);
		return wholeFile;
	}

	public static String htmlToText(String wholeFile) {
		wholeFile = HTMLToText.removeTags(wholeFile);
		wholeFile = Util.fixCharCode(wholeFile);
		wholeFile = Util.replaceAll(wholeFile, Util.ENTITY_NAME,
									Util.ENTITY_CODE);
		wholeFile = Util.changeToVUTimes(wholeFile);

		Log.d("Html to text: char num: ", wholeFile.length() + "");
		return wholeFile;
	}

	public static String removeTags(String wholeFile) {

		long millis = System.currentTimeMillis();

		wholeFile = REMOVE_CR_PATTERN.matcher(wholeFile).replaceAll(" ");

		wholeFile = REMOVE_COMMENT_PATTERN.matcher(wholeFile).replaceAll("");

		wholeFile = SCRIPT_STYLE_REMOVE.matcher(wholeFile).replaceAll("");

		wholeFile = CR_TAGS.matcher(wholeFile).replaceAll("\r\n");

		wholeFile = REMOVE_TAGS.matcher(wholeFile).replaceAll("");

		wholeFile = REMOVE_STARTNOTE.matcher(wholeFile).replaceAll("");

		wholeFile = REMOVE_ENDNOTE.matcher(wholeFile).replaceAll("");

		Log.d("Time for converting: "
			  , "" + (System.currentTimeMillis() - millis));
		return wholeFile;
	}

	public static void main(String[] args) throws Exception {
		String temp = removeTags("<hr>a<br><font face=<strong>Phần I - Đức Phật</strong></font><p><br>");
		String temp2 = removeTags("hello <![IF !supportFootnotes]>world<![ENDIF]> wide web");
		String temp3 = removeTags("<![ENDIF]>");
		String temp4 = removeTags("<p xmlns=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" class=\"NoSpacing \" style=\"; margin-top:0cm; \">DUYÊN SANH (12 NHÂN DUYÊN)");
		System.out.println(temp);
		System.out.println(temp2);
		System.out.println(temp3);
		System.out.println(temp4);
	}
}
