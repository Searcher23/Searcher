package com.free.searcher;

import static java.util.Locale.ENGLISH;

import android.util.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.Map.*;
import java.util.regex.*;

import java.util.Map.Entry;

import java.nio.charset.*;
import java.lang.reflect.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.*;

public class Util {
	public static final String LEEDS_BIT_PALI_TRANSLIT = "LeedsBit PaliTranslit";
	public static final String NORMAN = "Norman";
	public static final String TIMES_CSX_1 = "Times_CSX+1";
	public static final String TIMES_CSX = "Times_CSX+";
	public static final String VZ_TIME = "VZTime";
	public static final String VRI_ROMAN_PALI = "VriRomanPali";
	public static final String VU_TIMES = "VU Times";
	public static final String UNKNOWN1 = "Unknown1";
	public static final String UNKNOWN2 = "Unknown2";
	public static final String CONVERTED_TXT = ".converted.txt";
	// public static final Logger LOGGER = Logger.getLogger(Util.class.getName());
	public static final String UTF8 = "UTF-8";

	public static final Pattern META_PATTERN = Pattern.compile("<meta[^>]+?>",
			Pattern.CASE_INSENSITIVE);
	public static final Pattern BR_PATTERN = Pattern.compile("<br[^>]*?>",
			Pattern.CASE_INSENSITIVE);
	public static final Pattern LINK_PATTERN = Pattern.compile("<link[^>]*?>",
			Pattern.CASE_INSENSITIVE);
	public static final Pattern HR_PATTERN = Pattern.compile(
			"<hr[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern COL_PATTERN = Pattern.compile(
			"<col[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern IMG_PATTERN = Pattern.compile(
			"<IMG[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);

	public static final Pattern H1_PATTERN = Pattern.compile(
			"<H1[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern H2_PATTERN = Pattern.compile(
			"<H2[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern H3_PATTERN = Pattern.compile(
			"<H3[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern H4_PATTERN = Pattern.compile(
			"<H4[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern H5_PATTERN = Pattern.compile(
			"<H5[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern H6_PATTERN = Pattern.compile(
			"<H6[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);

	public static final Pattern P_PATTERN = Pattern.compile(
			"<p[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern DIV_PATTERN = Pattern.compile(
			"<div[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern FONT_PATTERN = Pattern.compile(
			"<font[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern SPAN_PATTERN = Pattern.compile(
			"<span[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern A_PATTERN = Pattern.compile(
			"<a[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern TABLE_PATTERN = Pattern.compile(
			"<table[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern TR_PATTERN = Pattern.compile(
			"<tr[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern TD_PATTERN = Pattern.compile(
			"<td[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern STYLE_PATTERN = Pattern.compile(
			"<style[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);
	public static final Pattern BODY_PATTERN = Pattern.compile(
			"<body[\\p{Space}]+([^>]*?)>", Pattern.CASE_INSENSITIVE);

	public static final Pattern[] ELEMENTS_PATTERNS = { META_PATTERN,
			BR_PATTERN, LINK_PATTERN, P_PATTERN, DIV_PATTERN, FONT_PATTERN,
			SPAN_PATTERN, A_PATTERN, HR_PATTERN, TABLE_PATTERN, COL_PATTERN,
			TR_PATTERN, TD_PATTERN, STYLE_PATTERN, BODY_PATTERN, H1_PATTERN,
			H2_PATTERN, H3_PATTERN, H4_PATTERN, H5_PATTERN, H6_PATTERN,
			IMG_PATTERN };

	private static final Pattern[] ILLEGAL_PATTERNS = { META_PATTERN,
			BR_PATTERN, LINK_PATTERN, HR_PATTERN, COL_PATTERN, IMG_PATTERN };

	public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

	public static boolean isNotEmpty(Object str) {
		return str != null && !"null".equals(str)
				&& str.toString().trim().length() > 0;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

	public static String trim(String st) {
		return (st == null) ? "" : st.trim();
	}
	
	public static String getUrlStatus(String ss) {
		String st = "";
		try {
			st = URLDecoder.decode(ss.replaceAll("\\+", "-000000000000000-"), "utf-8").replaceAll("-000000000000000-", "+");
		} catch (UnsupportedEncodingException e) {
			Log.d("decode", ss, e);
		}
		return st.substring("file:".length(), st.length()).replaceAll("///", "/").replaceAll("//", "/");
	}

	public static String collectionToSlashString(Collection<?> collection) {
		if (collection != null && collection.size() > 0) {
			StringBuilder sb = new StringBuilder();
			Object obj = null;
			for (Iterator<?> iterator = collection.iterator(); iterator
					.hasNext();) {
				obj = iterator.next();
				sb.append(obj.toString());
				if (iterator.hasNext()) {
					sb.append("/");
				}
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	public static String collectionToStr(Collection<?> collection) {
		if (collection != null) {
			StringBuilder sb = new StringBuilder();
			for (Object obj : collection) {
				sb.append(obj.toString());
			}
			return sb.toString();
		} else {
			return "";
		}
	}

//	public static void closeLog(Logger logger) {
//		if (logger != null) {
//			for (int i = 0; i < logger.getHandlers().length; i++) {
//				logger.getHandlers()[i].close();
//			}
//		}
//	}

	public static String readValue(String whole, String key) {
		Pattern pattern = Pattern.compile(key + "=([\"'])(.+?)\\1",
				Pattern.CASE_INSENSITIVE);
		Matcher mat = pattern.matcher(whole);
		if (mat.find()) {
			return mat.group(2);
		} else {
			pattern = Pattern.compile(key
					+ "=([^ \t\r\n\f\b\"'/>]+?)[ \t\r\n\f\b\"'/>]",
					Pattern.CASE_INSENSITIVE);
			mat = pattern.matcher(whole);
			if (mat.find()) {
				return mat.group(1);
			}
		}
		return "";
	}

	public static boolean isUTF8(String st) {
		int ch;
		for (int i = 0; i < Util.VUTimesUniqueChars.length; i++) {
			if ((ch = st.indexOf(Util.VUTimesUniqueChars[i])) >= 0) {
				Log.d("UTF8 char", Util.VUTimesUniqueChars[i] 
						+ " at " + ch);
				return true;
			}
		}
		return false;
	}

	public static final List<String> getListFromBundle(ResourceBundle rb,
			String prefix) {
		String name = null;
		List<String> ret = new LinkedList<String>();
		Enumeration<String> names = rb.getKeys();
		while (names.hasMoreElements()) {
			name = names.nextElement();
			if (name != null && name.startsWith(prefix)
					&& isInteger(name.substring(name.length() - 1))) {
				ret.add(rb.getString(name));
			}
		}
		Collections.sort(ret);
		return ret;
	}

	private static boolean isInteger(String substring) {
		try {
			Integer.parseInt(substring);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	public static String fixEndTags(String str) {
		StringBuffer sb = new StringBuffer();
		// str = encodeForRegex(str);
		for (int i = 0; i < ILLEGAL_PATTERNS.length; i++) {
			Matcher m = ILLEGAL_PATTERNS[i].matcher(str);
			// Loop through and create a new String
			// with the replacements
			while (m.find()) {
				if (!m.group(0).endsWith("/>")) {
					m.appendReplacement(sb, m.group(0).replaceAll(">", "/>"));
				}
			}
			// Add the last segment of input to
			// the new String
			m.appendTail(sb);
			str = sb.toString();
			sb = new StringBuffer();
			// System.gc();
		}
		// str = decodeForRegex(str);
		return str;
	}

	private static final Pattern CHAR_CODE_PATTERN = Pattern
			.compile("&#(\\d{2,5});");

	public static String fixCharCode(String wholeFile) {
		long millis = System.currentTimeMillis();
		Matcher mat = CHAR_CODE_PATTERN.matcher(wholeFile);
		StringBuffer sb = new StringBuffer();
		while (mat.find()) {
			// System.gc();
			mat.appendReplacement(sb,
					((char) Integer.parseInt(mat.group(1)) + ""));
		}
		mat.appendTail(sb);
		Log.d("fix char code time: "
				, "" + (System.currentTimeMillis() - millis));
		return sb.toString();
	}

	public static final Pattern KEY_VALUE_WITH_APOS_PATTERN = Pattern.compile(
			"((\\w+?)=['\"][^'\"]+?)['\"]", Pattern.CASE_INSENSITIVE);

	static Pattern CHARSET_PATTERN = Pattern
			.compile("(charset)=([^'\"/> \\t\\r\\n\\f]+)");

	public static String fixMetaTagsUTF8(String str) {
		// str = Util.encodeForRegex(str);
		StringBuffer sb = new StringBuffer();
		Matcher m = Util.META_PATTERN.matcher(str);
		// Loop through and create a new String
		// with the replacements
		boolean stop = false;
		while (m.find()) {
			String tag = m.group(0);
			Log.d("tag1: ", tag);
			// System.out.println("tag: " + tag);
			// Matcher mat2 = KEY_VALUE_WITH_APOS_PATTERN.matcher(tag);
			Matcher mat2 = CHARSET_PATTERN.matcher(tag);
			StringBuffer sb2 = new StringBuffer();
			// System.gc();
			while (mat2.find()) {
				Log.d("mat2.group()1: ", mat2.group());
				// System.out.println("mat2.group(1): " + mat2.group(1));
				// System.out.println("mat2.group(2): " + mat2.group(2));
				// System.out.println("mat2.groupCount(): " +
				// mat2.groupCount());
				if ("charset".equalsIgnoreCase(mat2.group(1))) {
					// System.out.println("fixMetaTagsUTF8: " + mat2.group());
					mat2.appendReplacement(sb2, "charset=UTF-8");
					stop = true;
					break;
				}
			}
			mat2.appendTail(sb2);
			m.appendReplacement(sb, sb2.toString());
			if (stop) {
				break;
			}
			sb2 = new StringBuffer();
		}
		m.appendTail(sb);
		str = sb.toString();
		// str = Util.decodeForRegex(str);
		return str;
	}

	// private static final String DOCTYPE_PATTERN = "<!DOCTYPE .+?>";

	// cần uri để download dtd
	public static final String DTD = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"file:dtds/xhtml1-transitional.dtd\">\r\n";

	// public static final String DTD =
	// "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"dtds/xhtml1-transitional.dtd\">\r\n";

	public static String initOriHtml(File inFile, File newFile)
			throws FileNotFoundException, IOException {
		String wholeFile = FileUtil.readFileByMetaTag(inFile);

		// wholeFile = fixAttrValueApos(wholeFile);
		// wholeFile = fixMetaTagsUTF8(wholeFile);
		// wholeFile = fixEndTags(wholeFile);
		wholeFile = fixCharCode(wholeFile);
		// wholeFile = removeComment(wholeFile);
		wholeFile = replaceAll(wholeFile, ENTITY_NAME, ENTITY_CODE);
		// wholeFile = HTMLToText.stringToUTF8(wholeFile);

		// if (wholeFile.indexOf("<!DOCTYPE") < 0) {
		// wholeFile = new StringBuilder(DTD).append(wholeFile).toString();
		// } else {
		// wholeFile = wholeFile.replaceFirst(DOCTYPE_PATTERN, DTD);
		// }
		FileUtil.writeFileAsCharset(newFile, wholeFile, UTF8);

		Log.d("Wrote to new temp file: ", newFile.getAbsolutePath());
		return wholeFile;
	}

	public static final String[] ENTITY_CODE = new String[] { "" + (char) 161,
			"" + (char) 162, "" + (char) 163, "" + (char) 164, "" + (char) 165,
			"" + (char) 166, "" + (char) 167, "" + (char) 168, "" + (char) 169,
			"" + (char) 170, "" + (char) 171, "" + (char) 172, "" + (char) 173,
			"" + (char) 174, "" + (char) 175, "" + (char) 176, "" + (char) 177,
			"" + (char) 178, "" + (char) 179, "" + (char) 180, "" + (char) 181,
			"" + (char) 182, "" + (char) 183, "" + (char) 184, "" + (char) 185,
			"" + (char) 186, "" + (char) 187, "" + (char) 188, "" + (char) 189,
			"" + (char) 190, "" + (char) 191, "" + (char) 192, "" + (char) 193,
			"" + (char) 194, "" + (char) 195, "" + (char) 196, "" + (char) 197,
			"" + (char) 198, "" + (char) 199, "" + (char) 200, "" + (char) 201,
			"" + (char) 202, "" + (char) 203, "" + (char) 204, "" + (char) 205,
			"" + (char) 206, "" + (char) 207, "" + (char) 208, "" + (char) 209,
			"" + (char) 210, "" + (char) 211, "" + (char) 212, "" + (char) 213,
			"" + (char) 214, "" + (char) 215, "" + (char) 216, "" + (char) 217,
			"" + (char) 218, "" + (char) 219, "" + (char) 220, "" + (char) 221,
			"" + (char) 222, "" + (char) 223, "" + (char) 224, "" + (char) 225,
			"" + (char) 226, "" + (char) 227, "" + (char) 228, "" + (char) 229,
			"" + (char) 230, "" + (char) 231, "" + (char) 232, "" + (char) 233,
			"" + (char) 234, "" + (char) 235, "" + (char) 236, "" + (char) 237,
			"" + (char) 238, "" + (char) 239, "" + (char) 240, "" + (char) 241,
			"" + (char) 242, "" + (char) 243, "" + (char) 244, "" + (char) 245,
			"" + (char) 246, "" + (char) 247, "" + (char) 248, "" + (char) 249,
			"" + (char) 250, "" + (char) 251, "" + (char) 252, "" + (char) 253,
			"" + (char) 254, "" + (char) 255,

			// "" + (char) 338, "" + (char) 339,
			//
			// "" + (char) 352, "" + (char) 353, "" + (char) 376,
			//
			// "" + (char) 710, "" + (char) 732, "" + (char) 8194,
			// "" + (char) 8195, "" + (char) 8201, "" + (char) 8204,
			// "" + (char) 8205, "" + (char) 8206, "" + (char) 8207,
			//
			// "" + (char) 8212,
			//
			// "" + (char) 8218, "" + (char) 8220, "" + (char) 8221,
			// "" + (char) 8222, "" + (char) 8224, "" + (char) 8225,
			// "" + (char) 8240, "" + (char) 8249, "" + (char) 8250,
			// "" + (char) 8364,
			//
			// "" + (char) 402, "" + (char) 8226, "" + (char) 8230,
			// "" + (char) 8242, "" + (char) 8243, "" + (char) 8254,
			// "" + (char) 8260, "" + (char) 8465, "" + (char) 8472,
			// "" + (char) 8476, "" + (char) 8482, "" + (char) 8501,
			// "" + (char) 8592, "" + (char) 8593, "" + (char) 8594,
			// "" + (char) 8595, "" + (char) 8596, "" + (char) 8629,
			// "" + (char) 8656, "" + (char) 8657, "" + (char) 8658,
			// "" + (char) 8659, "" + (char) 8660, "" + (char) 8704,
			// "" + (char) 8706, "" + (char) 8707, "" + (char) 8709,
			// "" + (char) 8711, "" + (char) 8712, "" + (char) 8713,
			// "" + (char) 8715, "" + (char) 8719, "" + (char) 8721,
			// "" + (char) 8722, "" + (char) 8727, "" + (char) 8730,
			// "" + (char) 8733, "" + (char) 8734, "" + (char) 8736,
			// "" + (char) 8743, "" + (char) 8744, "" + (char) 8745,
			// "" + (char) 8746, "" + (char) 8747, "" + (char) 8756,
			// "" + (char) 8764, "" + (char) 8773, "" + (char) 8776,
			// "" + (char) 8800, "" + (char) 8801, "" + (char) 8804,
			// "" + (char) 8805, "" + (char) 8834, "" + (char) 8835,
			// "" + (char) 8836, "" + (char) 8838, "" + (char) 8839,
			// "" + (char) 8853, "" + (char) 8855, "" + (char) 8869,
			// "" + (char) 8901, "" + (char) 8968, "" + (char) 8969,
			// "" + (char) 8970, "" + (char) 8971, "" + (char) 9001,
			// "" + (char) 9002, "" + (char) 913, "" + (char) 914,
			// "" + (char) 915, "" + (char) 916, "" + (char) 917, "" + (char)
			// 918,
			// "" + (char) 919, "" + (char) 920, "" + (char) 921, "" + (char)
			// 922,
			// "" + (char) 923, "" + (char) 924, "" + (char) 925, "" + (char)
			// 926,
			// "" + (char) 927, "" + (char) 928, "" + (char) 929, "" + (char)
			// 931,
			// "" + (char) 932, "" + (char) 933, "" + (char) 934, "" + (char)
			// 935,
			// "" + (char) 936, "" + (char) 937, "" + (char) 945, "" + (char)
			// 946,
			// "" + (char) 947, "" + (char) 948, "" + (char) 949, "" + (char)
			// 950,
			// "" + (char) 951, "" + (char) 952, "" + (char) 953, "" + (char)
			// 954,
			// "" + (char) 955, "" + (char) 956, "" + (char) 957, "" + (char)
			// 958,
			// "" + (char) 959, "" + (char) 960, "" + (char) 961, "" + (char)
			// 962,
			// "" + (char) 963, "" + (char) 964, "" + (char) 965, "" + (char)
			// 966,
			// "" + (char) 967, "" + (char) 9674, "" + (char) 968,
			// "" + (char) 969, "" + (char) 977, "" + (char) 978, "" + (char)
			// 982,
			// "" + (char) 9824, "" + (char) 9827, "" + (char) 9829,
			// "" + (char) 9830,

			"’", "‘", "–", " ", "\"", "\'", "<", ">", "&", "“", "”", " ", "…", };

	public static final String[] ENTITY_NAME = new String[] { "&iexcl;",
			"&cent;", "&pound;", "&curren;", "&yen;", "&brvbar;", "&sect;",
			"&uml;", "&copy;", "&ordf;", "&laquo;", "&not;", "&shy;", "&reg;",
			"&macr;", "&deg;", "&plusmn;", "&sup2;", "&sup3;", "&acute;",
			"&micro;", "&para;", "&middot;", "&cedil;", "&sup1;", "&ordm;",
			"&raquo;", "&frac14;", "&frac12;", "&frac34;", "&iquest;",
			"&Agrave;", "&Aacute;", "&Acirc;", "&Atilde;", "&Auml;", "&Aring;",
			"&AElig;", "&Ccedil;", "&Egrave;", "&Eacute;", "&Ecirc;", "&Euml;",
			"&Igrave;", "&Iacute;", "&Icirc;", "&Iuml;", "&ETH;", "&Ntilde;",
			"&Ograve;", "&Oacute;", "&Ocirc;",
			"&Otilde;",
			"&Ouml;",
			"&times;",
			"&Oslash;",
			"&Ugrave;",
			"&Uacute;",
			"&Ucirc;",
			"&Uuml;",
			"&Yacute;",
			"&THORN;",
			"&szlig;",
			"&agrave;",
			"&aacute;",
			"&acirc;",
			"&atilde;",
			"&auml;",
			"&aring;",
			"&aelig;",
			"&ccedil;",
			"&egrave;",
			"&eacute;",
			"&ecirc;",
			"&euml;",
			"&igrave;",
			"&iacute;",
			"&icirc;",
			"&iuml;",
			"&eth;",
			"&ntilde;",
			"&ograve;",
			"&oacute;",
			"&ocirc;",
			"&otilde;",
			"&ouml;",
			"&divide;",
			"&oslash;",
			"&ugrave;",
			"&uacute;",
			"&ucirc;",
			"&uuml;",
			"&yacute;",
			"&thorn;",
			"&yuml;",

			// "&OElig;", "&oelig;",
			//
			// "&Scaron;", "&scaron;", "&Yuml;",
			//
			// "&circ;", "&tilde;", "&ensp;", "&emsp;", "&thinsp;", "&zwnj;",
			// "&zwj;", "&lrm;", "&rlm;",
			//
			// "&mdash;",
			//
			// "&sbquo;", "&ldquo;", "&rdquo;", "&bdquo;", "&dagger;",
			// "&Dagger;",
			// "&permil;", "&lsaquo;", "&rsaquo ;", "&euro;",
			//
			// "&fnof;", "&bull;", "&hellip;", "&prime;", "&Prime;", "&oline;",
			// "&frasl;", "&image;", "&weierp;", "&real;", "&trade;",
			// "&alefsym;",
			// "&larr;", "&uarr;", "&rarr;", "&darr;", "&harr;", "&crarr;",
			// "&lArr;", "&uArr;", "&rArr;", "&dArr;", "&hArr;", "&forall;",
			// "&part;", "&exist;", "&empty;", "&nabla;", "&isin;", "&notin;",
			// "&ni;", "&prod;", "&sum;", "&minus;", "&lowast;", "&radic;",
			// "&prop;", "&infin;", "&ang;", "&and;", "&or;", "&cap;", "&cup;",
			// "&int;", "&there4;", "&sim;", "&cong;", "&asymp;", "&ne;",
			// "&equiv;", "&le;", "&ge;", "&sub;", "&sup;", "&nsub;", "&sube;",
			// "&supe;", "&oplus;", "&otimes;", "&perp;", "&sdot;", "&lceil;",
			// "&rceil;", "&lfloor;", "&rfloor;", "&lang;", "&rang;", "&Alpha;",
			// "&Beta;", "&Gamma;", "&Delta;", "&Epsilon;", "&Zeta;", "&Eta;",
			// "&Theta;", "&Iota;", "&Kappa;", "&Lambda;", "&Mu;", "&Nu;",
			// "&Xi;",
			// "&Omicron;", "&Pi;", "&Rho;", "&Sigma;", "&Tau;", "&Upsilon;",
			// "&Phi;", "&Chi;", "&Psi;", "&Omega;", "&alpha;", "&beta;",
			// "&gamma;", "&delta;", "&epsilon;", "&zeta;", "&eta;", "&theta;",
			// "&iota;", "&kappa;", "&lambda;", "&mu;", "&nu;", "&xi;",
			// "&omicron;", "&pi;", "&rho;", "&sigmaf;", "&sigma;", "&tau;",
			// "&upsilon;", "&phi;", "&chi;", "&loz;", "&psi;", "&omega;",
			// "&thetasym;", "&upsih;", "&piv;", "&spades;", "&clubs;",
			// "&hearts;", "&diams;",

			"&rsquo;", "&lsquo;", "&ndash;", "&nbsp;", "&quot;", "&apos;",
			"&lt;", "&gt;", "&amp;", "&ldquo;", "&rdquo;", "", "&hellip;", };

	public static final Pattern KEY_VALUE_PATTERN = Pattern.compile(
			"(\\w+?=[^\\p{Space}\"']+?)([\\p{Space}/>]+?)",
			Pattern.CASE_INSENSITIVE);

	public static String fixAttrValueApos(String str) {
		// str = encodeForRegex(str);
		// LOGGER.info("str: " + str);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < ELEMENTS_PATTERNS.length; i++) {
			Matcher m = ELEMENTS_PATTERNS[i].matcher(str);
			// Loop through and create a new String
			// with the replacements
			while (m.find()) {
				String tag = m.group(0);
				Log.d("tag2: ", tag);
				Matcher mat2 = KEY_VALUE_PATTERN.matcher(tag);
				StringBuffer sb2 = new StringBuffer();
				while (mat2.find()) {
					Log.d("mat2.group()2: ", mat2.group());
					mat2.appendReplacement(
							sb2,
							mat2.group(1).replaceFirst("=", "='") + "'"
									+ mat2.group(2));
				}
				mat2.appendTail(sb2);
				m.appendReplacement(sb, sb2.toString());
				sb2 = new StringBuffer();
			}
			// Add the last segment of input to
			// the new String
			m.appendTail(sb);
			str = sb.toString();
			sb = new StringBuffer();
		}
		// str = decodeForRegex(str);
		return str;
	}

	// public static final String decodeForRegex(String str) {
	// // str = str.replace('\u000F', '\\').replace('\u0010', '{')
	// // .replace('\u0011', '(').replace('\u0012', ')')
	// // .replace('\u0013', '}');
	// System.gc();
	// return str;
	// }
	//
	// public static final String encodeForRegex(String str) {
	// // str = str.replace('\\', '\u000F').replace('{', '\u0010')
	// // .replace('(', '\u0011').replace(')', '\u0012')
	// // .replace('}', '\u0013');
	// System.gc();
	// return str;
	// }

	public static String replaceAll(String s, String as[], String as1[]) {
		// long millis = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		for (int k = 0; k < as.length; k++) {
			if (as[k].length() > 0) {
				int i = 0;
				sb.setLength(0);
				int j;
				while ((j = s.indexOf(as[k], i)) >= 0) {
					sb.append(s, i, j);
					sb.append(as1[k]);
					// LOGGER.info("replaced " + as[k] + " = " + as1[k]);
					i = j + as[k].length();
				}
				sb.append(s, i, s.length());
				s = sb.toString();
			}
		}
		// LOGGER.info("replaced result: " + s);
		return s;
	}

	public static StringBuilder replaceAll(String s, String s1, String s2) {
		StringBuilder sb = new StringBuilder(s.length());
		if (s1.length() > 0) {
			int i = 0;
			int j;
			while ((j = s.indexOf(s1, i)) >= 0) {
				sb.append(s, i, j);
				sb.append(s2);
				i = j + s1.length();
			}
			sb.append(s, i, s.length());
			return sb;
		} else {
			return new StringBuilder(s);
		}
	}

	public static StringBuilder replaceAll(String s, StringBuilder sb,
			String s1, String s2) {
		int s1Length = s1.length();
		if (s1Length > 0) {
			int i = 0;
			int j;
			while ((j = s.indexOf(s1, i)) >= 0) {
				sb.append(s, i, j);
				sb.append(s2);
				i = j + s1Length;
			}
			sb.append(s, i, s.length());
			return sb;
		} else {
			return sb.append(s);
		}
	}
	
	public static void replaceAll(CharSequence s, int start, int end,
										   StringBuilder sb2, String[] s10, String[] s20) {
		replaceAll(s.toString(), start, end, sb2, s10, s20);
	}
	
	public static void replaceAll(String s, int start, int end,
										   StringBuilder sb2, String[] s10, String[] s20) {
		s = s.substring(start, end);
		for (int k = 0; k < s10.length; k++) {
			String s1 = s10[k];
			String s2 = s20[k];
			int s1Length = s1.length();
			if (s1Length > 0) {
				int i = 0;
				int j;
				StringBuilder sb = new StringBuilder();
				end = s.length();
				while (((j = s.indexOf(s1, i)) >= 0) && j < end) {
					sb.append(s, i, j);
					sb.append(s2);
					i = j + s1Length;
				}
				sb.append(s, i, end);
				s = sb.toString();
			}
		}
		sb2.append(s);
	}

	public static StringBuilder replaceAll(String s, int start, int end,
			StringBuilder sb, String s1, String s2) {
		int s1Length = s1.length();
		if (s1Length > 0) {
			int i = start;
			int j;
			while (((j = s.indexOf(s1, i)) >= 0) && j < end) {
				sb.append(s, i, j);
				sb.append(s2);
				i = j + s1Length;
			}
			sb.append(s, i, end);
			return sb;
		} else {
			return sb.append(s);
		}
	}

	public static StringBuilder replaceAll(StringBuilder s, int start, int end,
			StringBuilder sb, String s1, String s2) {
		int s1Length = s1.length();
		if (s1Length > 0) {
			int i = start;
			int j;
			while (((j = s.indexOf(s1, i)) >= 0) && j < end) {
				sb.append(s, i, j);
				sb.append(s2);
				i = j + s1Length;
			}
			sb.append(s, i, end);
			return sb;
		} else {
			return sb.append(s);
		}
	}
	
	public static StringBuilder replaceAll(String sourceCase, String sourceLower, int start, int end,
										   StringBuilder sb, String pattern, String tagStart, String tagEnd) {
		Log.d("sourceCase, sourceLower, start, end", sourceCase.length() + ", " + sourceLower.length()
			  +"," +start+"," + end+pattern+"," +tagStart+"," + tagEnd);
		int patternLength = pattern.length();
		if (patternLength > 0) {
			int i = start;
			int j;
			while (((j = sourceLower.indexOf(pattern, i)) >= 0) && ((j + patternLength) <= end)) {
				sb.append(sourceCase, i, j);
				sb.append(tagStart);
				i = j + patternLength;
				sb.append(sourceCase, j, i);
				sb.append(tagEnd);
			}
			Log.d("sourceCase, i, end", sourceCase.length() + ", " + i + ", " + end);
			sb.append(sourceCase, i, end);
			return sb;
		} else {
			return sb.append(sourceCase);
		}
	}
	
	public static StringBuilder replaceAll(StringBuilder sourceCase, StringBuilder sourceLower, int start, int end,
			StringBuilder sb, String pattern, String tagStart, String tagEnd) {
			Log.d("sourceCase, sourceLower, start, end", sourceCase.length() + ", " + sourceLower.length()
			+"," +start+"," + end+pattern+"," +tagStart+"," + tagEnd);
		int patternLength = pattern.length();
		if (patternLength > 0) {
			int i = start;
			int j;
			while (((j = sourceLower.indexOf(pattern, i)) >= 0) && ((j + patternLength) <= end)) {
				sb.append(sourceCase, i, j);
				sb.append(tagStart);
				i = j + patternLength;
				sb.append(sourceCase, j, i);
				sb.append(tagEnd);
			}
			Log.d("sourceCase, i, end", sourceCase.length() + ", " + i + ", " + end);
			sb.append(sourceCase, i, end);
			return sb;
		} else {
			return sb.append(sourceCase);
		}
	}

	public static String replace1Char(String origin, String[] oldChar,
			String[] newChar) {
		for (int i = 0; i < oldChar.length; i++) {
			// Util.LOGGER.info("origin: " + origin + ", oldChar[i]: " +
			// oldChar[i] + ", newChar[i]: " + newChar[i]);
			if (oldChar[i].equals(origin)) {
				// Util.LOGGER.info("Equal: " + origin + ", oldChar[i]: " +
				// oldChar[i] + ", newChar[i]: " + newChar[i]);
				return newChar[i];
			}
		}
		return origin;
	}

	public static String replace1Chars(String content, String[] oldChar,
			String[] newChar) {
		int len = content.length();
		int i = 0;
		StringBuilder sb = new StringBuilder();
		while (i < len) {
			// System.gc();
			final int endIndex = i + 1;
			final String substring = content.substring(i, endIndex);
			boolean appended = false;
			for (int j = 0; j < oldChar.length; j++) {
				if (substring.equals(oldChar[j])) {
					sb.append(newChar[j]);
					appended = true;
					break;
				}
			}
			if (!appended) {
				sb.append(substring);
			}
			i++;
		}
		return sb.toString();
	}

		public static String changeFont(String textNotYetChangeFont, String fontName) {
		if (!VU_TIMES.equals(fontName)) {
			String[] entry = null;
			if (fontName.startsWith("Vri")) {
				entry = FONT_NAMES_PROP.get(VRI_ROMAN_PALI);
			} else if (fontName.startsWith("VZ")) {
				entry = FONT_NAMES_PROP.get(VZ_TIME);
			} else {
				entry = FONT_NAMES_PROP.get(fontName);
			}
			if (entry == null) {
				return textNotYetChangeFont;
			}
			String changedFont = replaceAll(textNotYetChangeFont, entry,
					VU_Times);
			return changedFont;
		} else {
			Log.d("change", "Already UTF-8, no need change font");
		}
		return textNotYetChangeFont;
	}

	public static void printMap(Map<?, ?> m) {
		StringBuilder sb = new StringBuilder();
		for (Entry<?, ?> entry : m.entrySet()) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue())
					.append("\r\n");
		}
		Log.d("print", sb.toString());
	}
	
//	public static String zipToUrlStr(File zF, List<ZipEntry> list, boolean number, String sep) throws ZipException, IOException {
//		return zipToUrlStr(new ZipFile(zF), list, number, sep);
//	}
	
	public static String collectionToString(Collection<?> list, boolean number, String sep) {
		StringBuilder sb = new StringBuilder();
		if (!number) {
			for (Object obj : list) {
				sb.append(obj).append(sep);
			}
		} else {
			int counter = 0;
			for (Object obj : list) {
				sb.append(++counter + ": ").append(obj).append(sep);
			}
		}
		return sb.toString();
	}
	
	
	public static String arrayToString(Object[] list, boolean number, String sep) {
		if (list == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		if (!number) {
			for (Object obj : list) {
				sb.append(obj).append(sep);
			}
		} else {
			int counter = 0;
			for (Object obj : list) {
				sb.append(++counter + ": ").append(obj).append(sep);
			}
		}
		return sb.toString();
	}
	
	public static String filterCRPDF(File textFile)
	throws FileNotFoundException, UnsupportedEncodingException,
	IOException {
		String text = FileUtil.readFileWithCheckEncode(textFile
													   .getAbsolutePath());
		// System.err.println("ori text: " + text);
		text = text.replaceAll("\r?\n([0-9A-ZĀĪŪṬṂṀṆṄÑḌḶ \t])", "\u0007\u0007\u0007\u0007 $1");
		text = text.replaceAll("([^0-9\\.?!;\"':])\r?\n", "$1 ");
		text = text.replaceAll("\u0007\u0007\u0007\u0007", "\r\n");
		text = text.replaceAll(" *([\\({\\[<]+) +", " $1");
		text = text.replaceAll(" +([.,?!;:)}\\]>]+?) *", "$1 ");
		// System.out.println("filter text: " + text);
		return text;
	}
	
//	public static void parsePdfToText(String pdfFile, String txtFile)
//	throws IOException, PDFException, PDFSecurityException, InterruptedException {
//		Log.i("Source PDF:", pdfFile);
//		Log.i("Destination txtFile: ", txtFile);
//
//        // open the url
//        Document document = new Document();
//		File fileTmp = new File(txtFile + ".tmp");
//
//		document.setFile(pdfFile);
//		// create a file to write the extracted text to
//		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileTmp));
//
//		// Get text from the first page of the document, assuming that there
//		// is text to extract.
//		for (int pageNumber = 0, max = document.getNumberOfPages();
//			 pageNumber < max; pageNumber++) {
//			PageText pageText = document.getPageText(pageNumber);
//			Log.d("Extracting page text: ", pageNumber + ".");
//			if (pageText != null && pageText.getPageLines() != null) {
//				fileWriter.write(pageText.toString());
//			}
//		}
//		// close the writer
//		fileWriter.flush();
//		fileWriter.close();
//        // clean up resources
//        document.dispose();
//		File file = new File(txtFile);
//		file.delete();
//		fileTmp.renameTo(file);
//	}
	
	public static void parsePdfToText(String pdfFile, String txtFile)
	throws IOException {
		Log.i("Source PDF:", pdfFile);
		Log.i("Destination txtFile: ", txtFile);
		PdfReader reader = new PdfReader(pdfFile);
		String tmpTxt = txtFile + ".tmp";
		PrintWriter out = new PrintWriter(new FileOutputStream(tmpTxt));
		// Util.LOGGER.log(Level.INFO,
		// "parsePdfToText: {0}, txtFile: {1}, total pages: {2}", new
		// Object[]{pdfFile, txtFile, reader.getNumberOfPages()});
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			out.println(PdfTextExtractor.getTextFromPage(reader, i,
														 new LocationTextExtractionStrategy()));
			// out.println(PdfTextExtractor.getTextFromPage(reader, i, new
			// SimpleTextExtractionStrategy()));
		}
		out.flush();
		out.close();
		File file = new File(txtFile);
		file.delete();
		new File(tmpTxt).renameTo(file);
	}

	public static File fromPDF(String pdfFile) throws IOException  {
		return fromPDF(new File(pdfFile));
	}

	public static File fromPDF(File pdfFile) throws IOException  {
		String pdfPath = pdfFile.getAbsolutePath();
		String txtPath = SearchFragment.PRIVATE_PATH + pdfPath + ".txt";
		
		File txtFile = new File(txtPath);
		if (!txtFile.getParentFile().exists()) {
			txtFile.getParentFile().mkdirs();
		}
		
		Log.d("Source PDF", pdfPath);
		Log.d("Destination txtFile", txtPath);
		
		if (!txtFile.exists()
				|| (txtFile.lastModified() < pdfFile.lastModified())) {
//			try {
//				PDFBoxToHtml.convertToText(pdfPath, txtPath, null);
//				LOGGER.info("Used PDFBoxToHtml successfully");
//			} catch (Throwable t) {
//				JOptionPane.showMessageDialog(null, t.getMessage());
				try {
					parsePdfToText(pdfPath, txtPath);
					Log.d("convert pdf", "Used ItextPdfToHtml successfully");
				} catch (IOException th) {
					Log.e("fromPDF", th.getLocalizedMessage());
					throw th;
//					String command = "./lib/pdftohtml.exe \"" + pdfPath
//							+ "\" \"" + txtPath + "\"";
//					Util.LOGGER.info(command);
//					Runtime.getRuntime().exec(command);
//					File f = new File(txtPath);
//					LOGGER.info("file: " + f);
//					File file2 = new File(txtPath.substring(0, txtPath.length()
//							- ".html".length())
//							+ "s.html");
//					LOGGER.info("file2: " + file2);
//					if (file2.renameTo(f)) {
//						LOGGER.info("Rename successfully: "
//								+ f.getAbsolutePath());
//						htmlFile = f;
//					} else {
//						htmlFile = file2;
//						LOGGER.info("Renaming to " + f.getAbsolutePath()
//								+ " not OK");
//					}
//					LOGGER.info("Used pdftohtml.exe successfully");
//					// FileUtil.printInputStream(p.getInputStream(),
//					// p.getOutputStream(), null, null);
				}
//			}
		} else {
			Log.d(pdfPath + " has already converted before to file : "
					, txtPath);
		}
		return txtFile;
	}
	
	public static final String[] VU_Times = new String[] { 
		"ā", "ī", "ū", "ṅ", "ṭ", "ñ", "ḍ", "ṇ", "ḷ", "ṃ", "ṁ", 
		"Ā", "Ī", "Ū", "Ṅ", "Ṭ", "Ñ", "Ḍ", "Ṇ", "Ḷ", "Ṃ", "Ṁ", 
		"ō", "ṣ", "ē",
		" ", "\"", "&", "'", "(", ")", "+", ",", "-", ".", "/", "0", "1", "2", 
		"3", "4", "5", "6", "7", "8", "9", ":", ";", "=", "?", "A", "B", "C", 
		"D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "R", 
		"S", "T", "U", "V", "W", "X", "Y", "[", "]", "_", "a", "b", "c", "d", 
		"e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", 
		"s", "t", "u", "v", "w", "x", "y", "z", "{", "}", "“", "”", "‘", "’", "ṭ" 
	};

	public static final String[] Unknown1 = new String[] { 
		"á", "ì", "ú", "ò", "þ", "ñ", "ð", "ó", "¿", "í", "", 
		"Á", "Ì", "Ú", "Ò", "Þ", "Ñ", "Ð", "Ó", "¿", "Í", "", "õ", "å", "÷"};

//	public static final String[] Unknown2 = new String[] { 
//			"È", "Ê", "|", "~", "Ô", "Ò", "", "Ó", "Ä", "Ñ", "", 
//			"Œ", "", "", "", "", "", "", "", "", "", "", "", "", ""};

	public static final String[] Times_CSX_plus = new String[] { 
		"à", "ã", "å", "ï", "ñ", "¤", "ó", "õ", "ë", "ü", "§", 
		"â", "ä", "æ", "ð", "ò", "¥", "ô", "ö", "ì", "ý", };

	public static final String[] Times_CSX_plus_1 = new String[] { 
		"à", "ã", "å", "ï", "ñ", "¤", "ó", "õ", "ë", "ü", "§", 
		"â", "ä", "æ", "ð", "ò", "¥", "ô", "ö", "ì", "ý", "ą", };

	public static final String[] VriRomanPali = new String[] { 
		"±", "²", "³", "ª", "µ", "ñ", "¹", "º", "¼", "½", "", 
		"¾", "¿", "Ð", "©", "Ý", "Ñ", "Þ", "ð", "ý", "þ", "", };

	public static final String[] VZTime = new String[] { 
		"È", "Ê", "|", "~", "Ô", "Ò", "É", "Ó", "Ä", "", "Ñ", 
		"", "¢", "", "^", "®", "©", "", "ª", "£", "", "¥", };

	public static final String[] LeedsBitPaliTranslit = new String[] { 
		"Œ", "´", "è", "º", "Ê", "–", "¶", "ö", "Â", "µ", "×", 
		"î", "ç", "ò", "Ü", "é", "„", "ß", "ï", "â", "È", "Ṁ", };

	public static final String[] Foreign1 = new String[] { 
		"a", "i", "u", "v", "t", "b", "d", "n", "l", "j", "m", 
		"A", "I", "U", "V", "T", "B", "D", "N", "L", "J", "M", };

	public static final String[] Foreigs1 = new String[] { 
		"a", "i", "u", "v", "t", "b", "d", "n", "l", "j", "m", 
		"A", "I", "U", "V", "T", "B", "D", "N", "L", "J", "M", };

	public static final String[] Norman = new String[] { 
		"", "", "", "", "", "", "", "", "", "", "", 
		"", "", "", "", "", "", "", "", "", "", "", 
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", 
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", 
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", 
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", 
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", 
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "" 
	};

	public static final String[] VUTimesUniqueChars = new String[] { "ā", "ī",
			"ū", "ṅ", "ṭ", "ḍ", "ṇ", "ḷ", "ṃ", "Ā", "Ī", "Ū", "Ṅ", "Ṭ", "Ḍ",
			"Ṇ", "Ḷ", "Ṃ", "đ", "ă", "ắ", "ằ", "ẳ", "ẵ", "ặ", "Ă", "Ắ",
			"Ằ", "Ẳ", "Ẵ", "Ặ", "ấ", "ầ", "ẩ", "ẫ", "ậ", "Ấ", "Ầ", "Ẩ", "Ẫ",
			"Ậ", "ế", "ề", "ể", "ễ", "ệ", "ơ", "ớ", "ờ", "ở", "ỡ", "ợ", "Ơ",
			"Ớ", "Ờ", "Ở", "Ỡ", "Ợ", "ư", "ứ", "ừ", "ử", "ữ", "ự", "Ư", "Ứ",
			"Ừ", "Ử", "Ữ", "Ự", "ổ", "ỗ", "ồ", "ố", "ộ", "Ổ", "Ỗ", "Ồ", "Ố",
			"Ộ" };

	public static final String[] Times_CSXPlu1UniqueChars = new String[] { "ą",
	// "å", "ï", "¤", "ë", "ü", "§", "ä", "æ",
	// "ð", "¥", "ö",
	};

	public static final String[] Times_CSXPlusUniqueChars = new String[] { // "å",
			// "ï", 
			"¤", 
			// "ë", 
			// "ü", 
			// "§", 
			// "ä", 
			"æ", 
			// "ð", 
			// "¥", 
			// "ö", 
			};

	public static final String[] VriRomanPaliUniqueChars = new String[] { "±",
			// "²", 
			"³", "¹", "¼", "½", "¾", 
			// "¿", 
			};

	public static final String[] VZTimeUniqueChars = new String[] { "|", 
			"Ä", "¢", "®", "£", };

	public static final String[] LeedsBitPaliTranslitUniqueChars = new String[] {
			// "Œ", 
			"´", "¶", "×", "î", "ç", "„", "ß", };

	public static final String[] NormanUniqueChars = new String[] { "", "",
			"", "", "", "", "", "", "", "", "", "", "", "", "",
			"", "", "", "", "", "", };

	public static final String[] Unknown1UniqueChars = new String[] {
			"÷", //"Þ","¿"
	};
	
	//public static final String[] Unknown2UniqueChars = new String[] { "|", 
	//	"Ä", };
	
	public static final Map<String, String[]> FONT_NAMES_PROP = new HashMap<String, String[]>();

	public static final List<String> NOT_UTF8_CHARS = new LinkedList<String>();

	public static final List<KeyArrayEntry> UNIQUE_CHARS_PROP = new LinkedList<KeyArrayEntry>();

	static {
		FONT_NAMES_PROP.put(VU_TIMES, VU_Times);
		FONT_NAMES_PROP.put(TIMES_CSX_1, Times_CSX_plus_1);
		FONT_NAMES_PROP.put(TIMES_CSX, Times_CSX_plus);
		FONT_NAMES_PROP.put(VRI_ROMAN_PALI, VriRomanPali);
		FONT_NAMES_PROP.put(VZ_TIME, VZTime);
		FONT_NAMES_PROP.put(LEEDS_BIT_PALI_TRANSLIT, LeedsBitPaliTranslit);
		FONT_NAMES_PROP.put("Foreign1", Foreign1);
		FONT_NAMES_PROP.put("Foreigs1", Foreigs1);
		FONT_NAMES_PROP.put(NORMAN, Norman);
		FONT_NAMES_PROP.put(UNKNOWN1, Unknown1);
		//FONT_NAMES_PROP.put(UNKNOWN2, Unknown2);

		UNIQUE_CHARS_PROP.add(new KeyArrayEntry(VU_TIMES, VUTimesUniqueChars));
		UNIQUE_CHARS_PROP.add(new KeyArrayEntry(TIMES_CSX_1,
												Times_CSXPlu1UniqueChars));
		UNIQUE_CHARS_PROP.add(new KeyArrayEntry(TIMES_CSX,
												Times_CSXPlusUniqueChars));
		UNIQUE_CHARS_PROP.add(new KeyArrayEntry(VRI_ROMAN_PALI,
				VriRomanPaliUniqueChars));
		UNIQUE_CHARS_PROP.add(new KeyArrayEntry(VZ_TIME, VZTimeUniqueChars));
		UNIQUE_CHARS_PROP.add(new KeyArrayEntry(LEEDS_BIT_PALI_TRANSLIT,
				LeedsBitPaliTranslitUniqueChars));
		UNIQUE_CHARS_PROP.add(new KeyArrayEntry(NORMAN, NormanUniqueChars));
		UNIQUE_CHARS_PROP.add(new KeyArrayEntry(UNKNOWN1,
												Unknown1UniqueChars));
		//UNIQUE_CHARS_PROP.add(new KeyArrayEntry(UNKNOWN2,
		//										Unknown2UniqueChars));
		
		NOT_UTF8_CHARS.addAll(Arrays.asList(VriRomanPaliUniqueChars));
		NOT_UTF8_CHARS.addAll(Arrays.asList(VZTimeUniqueChars));
		NOT_UTF8_CHARS.addAll(Arrays.asList(LeedsBitPaliTranslitUniqueChars));
		NOT_UTF8_CHARS.addAll(Arrays.asList(NormanUniqueChars));
		NOT_UTF8_CHARS.addAll(Arrays.asList(Times_CSXPlu1UniqueChars));
		NOT_UTF8_CHARS.addAll(Arrays.asList(Times_CSXPlusUniqueChars));
		NOT_UTF8_CHARS.addAll(Arrays.asList(Unknown1UniqueChars));
		//NOT_UTF8_CHARS.addAll(Arrays.asList(Unknown2UniqueChars));
	}

	public static final String guessFontName(String content) {
		// LOGGER.info("Before Guess Content: " + content);
		for (KeyArrayEntry entry : UNIQUE_CHARS_PROP) {
			String key = entry.getKey();
			String[] value = entry.getValue();
			for (int i = 0; i < value.length; i++) {
				// Util.LOGGER.info("Font: " + key + ", value[i]: " + value[i]);
				// if (key.equals("Times_CSX+1")) {
				// if (content.indexOf(value[0]) < 0) {
				// break;
				// }
				// }
				if (content.indexOf(value[i]) >= 0) {
					Log.d("Font", key + " has unique char: " + value[i]);
					return key;
				}
			}
		}
		return "";
	}

	public static final String changeToVUTimes(String textNotYetChangeFont) {
		
//		Log.d("Origin textNotYetChangeFont", textNotYetChangeFont);
		
		if (textNotYetChangeFont == null) {
			return null;
		}
		textNotYetChangeFont = textNotYetChangeFont.replaceAll("[ \\x0b\f]{2,}", " ");
		String fontName = Util.guessFontName(textNotYetChangeFont);
		Log.d("Origin font", fontName);
		if (!"".equals(fontName)) {
			if (!Util.VU_TIMES.equalsIgnoreCase(fontName)) {
				String changedFont = Util.changeFont(textNotYetChangeFont,
						fontName);
				if (!isViet(changedFont)) {
					changedFont = Util.replaceAll(changedFont, new String[] {
							"È", "Ê", "Ò", "Ô", "Ó", "|", "Œ", "~", "û", "â",
							"Ţ", "Ã", "î", "ţ", }, new String[] { 
							"ā", "ī", "ñ", "ṭ", "ṇ", "ū", "Ā", "ṅ", "ū", "ā", 
							"Ṭ", "Ā", "ī", "ṭ", });
				} else {
					changedFont = Util.replaceAll(changedFont, new String[] {
						"Ð", "ð", "Á", "À", "Ả", "Ã", "Ạ", "Ằ", "Ắ", "Ẳ", "Ẵ", "Ặ", "Ầ", "Ấ", "Ẩ", "Ẫ", "Ậ", "Ù", "Ú", "Ủ", "Ũ", "Ụ", "Ừ", "Ứ", "Ử", "Ữ", "Ự", "Ý", "Ỳ", "Ỷ", "Ỹ", "Ỵ", "È", "É", "Ẻ", "Ẽ", "Ẹ", "Ề", "Ế", "Ể", "Ễ", "Ệ", "Í", "Ì", "Ỉ", "Ĩ", "Ị", "Ò", "Ó", "Ỏ", "Õ", "Ọ", "Ồ", "Ố", "Ổ", "Ỗ", "Ộ", "Ờ", "Ớ", "Ở", "Ỡ", "Ợ", "á", "à", "ả", "ã", "ạ", "ằ", "ắ", "ẳ", "ẵ", "ặ", "ầ", "ấ", "ẩ", "ẫ", "ậ", "ù", "ú", "ủ", "ũ", "ụ", "ừ", "ứ", "ử", "ữ", "ự", "ý", "ỳ", "ỷ", "ỹ", "ỵ", "è", "é", "ẻ", "ẽ", "ẹ", "ề", "ế", "ể", "ễ", "ệ", "í", "ì", "ỉ", "ĩ", "ị", "ò", "ó", "ỏ", "õ", "ọ", "ồ", "ố", "ổ", "ỗ", "ộ", "ờ", "ớ", "ở", "ỡ", "ợ" }, new String[] { 
						"Đ", "đ", "Á", "À", "Ả", "Ã", "Ạ", "Ằ", "Ắ", "Ẳ", "Ẵ", "Ặ", "Ầ", "Ấ", "Ẩ", "Ẫ", "Ậ", "Ù", "Ú", "Ủ", "Ũ", "Ụ", "Ừ", "Ứ", "Ử", "Ữ", "Ự", "Ý", "Ỳ", "Ỷ", "Ỹ", "Ỵ", "È", "É", "Ẻ", "Ẽ", "Ẹ", "Ề", "Ế", "Ể", "Ễ", "Ệ", "Í", "Ì", "Ỉ", "Ĩ", "Ị", "Ò", "Ó", "Ỏ", "Õ", "Ọ", "Ồ", "Ố", "Ổ", "Ỗ", "Ộ", "Ờ", "Ớ", "Ở", "Ỡ", "Ợ", "á", "à", "ả", "ã", "ạ", "ằ", "ắ", "ẳ", "ẵ", "ặ", "ầ", "ấ", "ẩ", "ẫ", "ậ", "ù", "ú", "ủ", "ũ", "ụ", "ừ", "ứ", "ử", "ữ", "ự", "ý", "ỳ", "ỷ", "ỹ", "ỵ", "è", "é", "ẻ", "ẽ", "ẹ", "ề", "ế", "ể", "ễ", "ệ", "í", "ì", "ỉ", "ĩ", "ị", "ò", "ó", "ỏ", "õ", "ọ", "ồ", "ố", "ổ", "ỗ", "ộ", "ờ", "ớ", "ở", "ỡ", "ợ" });
				}
				return changedFont;
			} else {
				if (!isViet(textNotYetChangeFont)) {
					textNotYetChangeFont = Util.replaceAll(
							textNotYetChangeFont, new String[] { 
									"È", "Ê", "Ò", "Ô", "Ó", "|", "Œ", "~", "û", "â", "Ţ",
									"Ã", "î", "ţ", }, new String[] { 
									"ā", "ī", "ñ", "ṭ", "ṇ", "ū", "Ā", "ṅ", "ū", "ā", "Ṭ",
									"Ā", "ī", "ṭ", });
				} else {
					textNotYetChangeFont = Util.replaceAll(textNotYetChangeFont, new String[] {
						"Ð", "ð", "Á", "À", "Ả", "Ã", "Ạ", "Ằ", "Ắ", "Ẳ", "Ẵ", "Ặ", "Ầ", "Ấ", "Ẩ", "Ẫ", "Ậ", "Ù", "Ú", "Ủ", "Ũ", "Ụ", "Ừ", "Ứ", "Ử", "Ữ", "Ự", "Ý", "Ỳ", "Ỷ", "Ỹ", "Ỵ", "È", "É", "Ẻ", "Ẽ", "Ẹ", "Ề", "Ế", "Ể", "Ễ", "Ệ", "Í", "Ì", "Ỉ", "Ĩ", "Ị", "Ò", "Ó", "Ỏ", "Õ", "Ọ", "Ồ", "Ố", "Ổ", "Ỗ", "Ộ", "Ờ", "Ớ", "Ở", "Ỡ", "Ợ", "á", "à", "ả", "ã", "ạ", "ằ", "ắ", "ẳ", "ẵ", "ặ", "ầ", "ấ", "ẩ", "ẫ", "ậ", "ù", "ú", "ủ", "ũ", "ụ", "ừ", "ứ", "ử", "ữ", "ự", "ý", "ỳ", "ỷ", "ỹ", "ỵ", "è", "é", "ẻ", "ẽ", "ẹ", "ề", "ế", "ể", "ễ", "ệ", "í", "ì", "ỉ", "ĩ", "ị", "ò", "ó", "ỏ", "õ", "ọ", "ồ", "ố", "ổ", "ỗ", "ộ", "ờ", "ớ", "ở", "ỡ", "ợ" }, new String[] { 
						"Đ", "đ", "Á", "À", "Ả", "Ã", "Ạ", "Ằ", "Ắ", "Ẳ", "Ẵ", "Ặ", "Ầ", "Ấ", "Ẩ", "Ẫ", "Ậ", "Ù", "Ú", "Ủ", "Ũ", "Ụ", "Ừ", "Ứ", "Ử", "Ữ", "Ự", "Ý", "Ỳ", "Ỷ", "Ỹ", "Ỵ", "È", "É", "Ẻ", "Ẽ", "Ẹ", "Ề", "Ế", "Ể", "Ễ", "Ệ", "Í", "Ì", "Ỉ", "Ĩ", "Ị", "Ò", "Ó", "Ỏ", "Õ", "Ọ", "Ồ", "Ố", "Ổ", "Ỗ", "Ộ", "Ờ", "Ớ", "Ở", "Ỡ", "Ợ", "á", "à", "ả", "ã", "ạ", "ằ", "ắ", "ẳ", "ẵ", "ặ", "ầ", "ấ", "ẩ", "ẫ", "ậ", "ù", "ú", "ủ", "ũ", "ụ", "ừ", "ứ", "ử", "ữ", "ự", "ý", "ỳ", "ỷ", "ỹ", "ỵ", "è", "é", "ẻ", "ẽ", "ẹ", "ề", "ế", "ể", "ễ", "ệ", "í", "ì", "ỉ", "ĩ", "ị", "ò", "ó", "ỏ", "õ", "ọ", "ồ", "ố", "ổ", "ỗ", "ộ", "ờ", "ớ", "ở", "ỡ", "ợ" });
				}
			}
		}
		return textNotYetChangeFont;
	}

	private static boolean isViet(String guessedFont) {
		String[] VIET_UNIQUE = { "đ", "Đ", "ă", "ắ", "ằ", "ẳ", "ẵ", "ặ", "Ă",
				"Ắ", "Ằ", "Ẳ", "Ẵ", "Ặ", "ấ", "ầ", "ẩ", "ẫ", "ậ", "Ấ", "Ầ",
				"Ẩ", "Ẫ", "Ậ", "ế", "ề", "ể", "ễ", "ệ", "ơ", "ớ", "ờ", "ở",
				"ỡ", "ợ", "Ơ", "Ớ", "Ờ", "Ở", "Ỡ", "Ợ", "ư", "ứ", "ừ", "ử",
				"ữ", "ự", "Ư", "Ứ", "Ừ", "Ử", "Ữ", "Ự", "ổ", "ỗ", "ồ", "ố",
				"ộ", "Ổ", "Ỗ", "Ồ", "Ố", "Ộ" };
		for (int i = 0; i < VIET_UNIQUE.length; i++) {
			if (guessedFont.contains(VIET_UNIQUE[i])) {
				return true;
			}
		}
		return false;
	}

	private static final String FILE_ENCODING = "file.encoding";

	public static void restartAppAsUTF8IfNeeded() throws IOException {
		if (!System.getProperty(FILE_ENCODING).equalsIgnoreCase(Util.UTF8)) {

			System.out.println("Starting without encoding UTF-8");

			String java = "\"" + System.getProperty("sun.boot.library.path")
					+ "\\java.exe\"";

			List<String> cmdList = new LinkedList<String>();

			cmdList.add(java);
			cmdList.add("-jar");
			cmdList.add("-Dfile.encoding=UTF-8");
			cmdList.add(System.getProperty("sun.java.command"));

			ProcessBuilder newProcess = new ProcessBuilder();
			newProcess.directory(null);
			List<String> command = newProcess.command();
			command.addAll(cmdList);
			newProcess.start();

			System.out.println(Util.collectionToString(command, false, " "));
			System.out.println("Restarted as encoding UTF-8");

			System.exit(0);
		} else {
			System.out.println("Already started with encoding UTF-8");
		}
	}

	public static String iteratorToString(Iterator<? extends Object> iter) {
		if (iter != null) {
			StringBuilder sb = new StringBuilder();
			for (; iter.hasNext();) {
				sb.append(iter.next().toString());
			}
			return sb.toString();
		} else {
			return "";
		}
	}
	
	private static int parseHex(byte b) {
        if (b >= '0' && b <= '9') return (b - '0');
        if (b >= 'A' && b <= 'F') return (b - 'A' + 10);
        if (b >= 'a' && b <= 'f') return (b - 'a' + 10);

        throw new IllegalArgumentException("Invalid hex char '" + b + "'");
    }
	
	public static byte[] decode(byte[] url) throws IllegalArgumentException {
        if (url.length == 0) {
            return new byte[0];
        }

        // Create a new byte array with the same length to ensure capacity
        byte[] tempData = new byte[url.length];

        int tempCount = 0;
        for (int i = 0; i < url.length; i++) {
            byte b = url[i];
            if (b == '%') {
                if (url.length - i > 2) {
                    b = (byte) (parseHex(url[i + 1]) * 16
						+ parseHex(url[i + 2]));
                    i += 2;
                } else {
                    throw new IllegalArgumentException("Invalid format");
                }
            }
            tempData[tempCount++] = b;
        }
        byte[] retData = new byte[tempCount];
        System.arraycopy(tempData, 0, retData, 0, tempCount);
        return retData;
    }
	
	public static String decodeUrlToFS(String filename) {
		byte[] bArr = filename.getBytes();
		byte[] bDest = decode(bArr);
		try {
			return new String(bDest, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return new String(bDest);
		}
	}

	public static String splitWords(String src) {
		return src.replaceAll("[0-9 \r\n\t\f~!@#$%^&*()_+{}|:\"<>?\\-=\\[\\]\\\\;',./`’”“‘]+", "\n");
	}

	// HTMLToText
	// "script", "style"
	private static Pattern SCRIPT_STYLE_REMOVE = Pattern.compile(
		"<(script|style)[^>]*?>([\u0001-\uFFFF]*?)</\\1\\s*>",
		Pattern.CASE_INSENSITIVE);

	private static Pattern CR_TAGS = Pattern.compile(
		"</?(blockquote|br|cite|h1|h2|h3|h4|h5|h6|hr|li|p|pre|tr|title|nobr|c1|c2|c3|c4|c5|c6"
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
		wholeFile = Util.removeTags(wholeFile);
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
}

class KeyArrayEntry {
	private String key;
	private String[] value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String[] getValue() {
		return value;
	}

	public void setValue(String[] value) {
		this.value = value;
	}

	public KeyArrayEntry(String key, String[] value) {
		this.key = key;
		this.value = value;
	}
}
