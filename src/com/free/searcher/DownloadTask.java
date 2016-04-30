package com.free.searcher;

import android.os.*;
import android.util.*;
import java.net.*;
import java.io.*;

import java.util.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import android.widget.*;
import android.graphics.*;
import android.app.*;

public class DownloadTask extends AsyncTask<String, Void, String> {

	private Activity activity = null;
	private String savePath = Environment.getExternalStorageDirectory().getAbsolutePath();

	public DownloadTask(String savePath) {
		this.savePath = savePath;
	}

	public DownloadTask(Fragment s, String savePath) {
		this.savePath = savePath;
		this.activity = s.getActivity();
	}
	
	public DownloadTask(Activity s, String savePath) {
		this.savePath = savePath;
		this.activity = s;
	}
	
	protected String doInBackground(String... urls) {
		try {
			return DownloadText(urls[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	protected void onPostExecute(String result) {
		//Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
		Log.d("DownloadTask", result);
	}

	private String DownloadText(String URL) throws IOException {
		Log.d("url", URL);
		InputStream in = null;
		try {			
			in = Util.OpenHttpGETConnection(URL);
//			in = OpenHttpPOSTConnection(URL);
		} catch (Exception e) {
			Log.d("Networking", e.getLocalizedMessage());
		}
		
		String URLLower = URL.toLowerCase();
		if (URLLower.startsWith("http://")) {
			URL = URL.substring(7);
		} else if (URLLower.startsWith("https://")) {
			URL = URL.substring(8);
		}
		
		int lastIndexOf = URL.lastIndexOf("?");
		if (lastIndexOf > 0) {
			URL = URL.substring(0, lastIndexOf);
		}
		
		lastIndexOf = URL.lastIndexOf("#");
		if (lastIndexOf > 0) {
			URL = URL.substring(0, lastIndexOf);
		}
		
		Log.d("url", URL);
		String savedPath = savePath + "/" + URL;
		Log.d("savePath", savedPath);
		FileUtil.saveISToFile(in, savedPath);
		in.close();
		return savedPath;
	}
}

class DownloadImageTask extends AsyncTask
<String, Void, Bitmap> {
	
	private int imgId = 0;
	private Activity activity = null;

	public DownloadImageTask(Fragment s, int imgId) {
		this.activity = s.getActivity();
		this.imgId = imgId;
	}

	public DownloadImageTask(Activity s, int imgId) {
		this.activity = s;
		this.imgId = imgId;
	}
	
	protected Bitmap doInBackground(String... urls) {
		return DownloadImage(urls[0]);
	}

	protected void onPostExecute(Bitmap result) {
		ImageView img = (ImageView) activity.findViewById(imgId);
		img.setImageBitmap(result);
	}

	private Bitmap DownloadImage(String URL)
	{        
		Bitmap bitmap = null;
		InputStream in = null;        
		try {
			in = Util.OpenHttpGETConnection(URL);
			bitmap = BitmapFactory.decodeStream(in);
			in.close();
		} catch (Exception e) {
			Log.d("DownloadImage", e.getLocalizedMessage());            
		}
		return bitmap;                
	}
}

class AccessWebServiceTask extends AsyncTask
<String, Void, String> {

	private Activity activity = null;

	public AccessWebServiceTask(Fragment s, int imgId) {
		this.activity = s.getActivity();
		
	}

	public AccessWebServiceTask(Activity s, int imgId) {
		this.activity = s;
		
	}
	
	protected String doInBackground(String... urls) {
		return WordDefinition(urls[0]);
	}

	protected void onPostExecute(String result) {
		Toast.makeText(activity, result, 
					   Toast.LENGTH_LONG).show();
	}

	private String WordDefinition(String word) {
		InputStream in = null;
		String strDefinition = "";
		try {
			in = Util.OpenHttpGETConnection( 
				"http://services.aonaware.com/DictService/" + 
				"DictService.asmx/Define?word=" + word);
			Document doc = null;
			DocumentBuilderFactory dbf = 
				DocumentBuilderFactory.newInstance();
			DocumentBuilder db;            
			try {
				db = dbf.newDocumentBuilder();
				doc = db.parse(in);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}            
			doc.getDocumentElement().normalize(); 

			//---retrieve all the <Definition> elements---
			NodeList definitionElements = 
				doc.getElementsByTagName("Definition"); 

			//---iterate through each <Definition> elements---
			for (int i = 0; i < definitionElements.getLength(); i++) { 
				Node itemNode = definitionElements.item(i); 
				if (itemNode.getNodeType() == Node.ELEMENT_NODE) 
				{            
					//---convert the Definition node into an Element---
					Element definitionElement = (Element) itemNode;

					//---get all the <WordDefinition> elements under 
					// the <Definition> element---
					NodeList wordDefinitionElements = 
						definitionElement.
						getElementsByTagName("WordDefinition");

					strDefinition = "";
					//---iterate through each <WordDefinition> 
					// elements---
					for (int j = 0; j < 
						 wordDefinitionElements.getLength(); j++) {
						//---get all the child nodes under the 
						// <WordDefinition> element---
						NodeList textNodes = 
							(wordDefinitionElements.item(j)).
							getChildNodes();
						strDefinition += 
							((Node) 
							textNodes.item(0)).getNodeValue() + 
							". \n";                            
					}
				} 
			}
		} catch (Exception e) {
			Log.d("NetworkingActivity", e.getLocalizedMessage());
		}   
		//---return the definitions of the word---
		return strDefinition;
	}

}
    
    
