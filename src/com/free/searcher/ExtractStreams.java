package com.free.searcher;

import com.itextpdf.text.exceptions.UnsupportedPdfException ;
import com.itextpdf.text.pdf. PRStream;
import com.itextpdf.text.pdf. PdfObject;
import com.itextpdf.text.pdf. PdfReader;
import java.io.File ;
import java.io.FileOutputStream ;
import java.io.IOException ;
import com.itextpdf.text.pdf.*;
import java.io.*;
/**
 * @author iText
 */
public class ExtractStreams {
	public static final String SRC = "/storage/emulated/0/.a/Pa-Auk-Daily-Chants_Khema.pdf" ;
	public static final String DEST = "/storage/emulated/0/.a/Pa-Auk-Daily-Chants_Khema.pdf%s" ;
	public static void main ( String [] args ) throws
	IOException {
		File file = new File ( DEST ) ;
		file. getParentFile (). mkdirs () ;
		new ExtractStreams () .parse ( SRC, DEST ) ;
	}
	public void parse (String src, String dest ) throws
	IOException {
		PdfReader reader = new PdfReader(src);
		for (int i = 0; i < reader.getXrefSize(); i++) {
			PdfObject pdfobj = reader.getPdfObject(i);
			if (pdfobj == null || !pdfobj.isStream()) {
				continue;
			}
			PdfStream stream = (PdfStream) pdfobj;
			PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);
			if (pdfsubtype != null && pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
				byte[] img = PdfReader.getStreamBytesRaw((PRStream) stream);
				OutputStream out = new  BufferedOutputStream( new FileOutputStream(new File(new File(src).getParentFile(), String.format("%1$05d", i) + ".jpg")));
				out.write(img);
				out.flush();
				out.close();
			}
		}
//		PdfReader reader = new PdfReader (src ) ;
//		PdfObject obj ;
//		for (int i = 1 ; i <= reader. getXrefSize () ; i ++
//		) {
//			obj = reader. getPdfObject (i );
//			if (obj != null && obj. isStream()) {
//				PRStream stream = ( PRStream)obj ;
//				byte [] b ;
//				try {
//					b = PdfReader .getStreamBytes(
//						stream );
//				}
//				catch (UnsupportedPdfException e ) {
//					b = PdfReader .getStreamBytesRaw (
//						stream );
//				}
//				FileOutputStream fos = new
//					FileOutputStream ( String . format (dest, i )) ;
//				fos. write ( b);
//				fos. flush () ;
//				fos. close () ;
//			}
//		}
	}
}
