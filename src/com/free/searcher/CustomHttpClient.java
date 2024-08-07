//package com.free.searcher;
//
//import org.apache.http.HttpVersion;
//import org.apache.http.client.HttpClient;
//import org.apache.http.conn.ClientConnectionManager;
//import org.apache.http.conn.params.ConnManagerParams;
//import org.apache.http.conn.scheme.PlainSocketFactory;
//import org.apache.http.conn.scheme.Scheme;
//import org.apache.http.conn.scheme.SchemeRegistry;
//import org.apache.http.conn.ssl.SSLSocketFactory;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
//import org.apache.http.params.BasicHttpParams;
//import org.apache.http.params.HttpConnectionParams;
//import org.apache.http.params.HttpParams;
//import org.apache.http.params.HttpProtocolParams;
//import org.apache.http.protocol.HTTP;
//
//import android.content.Context;
//import android.util.Log;
//import android.webkit.WebView;
//import org.apache.http.client.methods.*;
//import java.net.*;
//import org.apache.http.impl.client.*;
//import org.apache.http.client.*;
//import java.io.*;
//
//public class CustomHttpClient {
//	private static final String TAG = "CustomHttpClient";
//	private static HttpClient customHttpClient;
//
//	/** A private Constructor prevents any other class from instantiating. */
//	private CustomHttpClient() {
//	}
//
//	public static synchronized HttpClient getHttpClient() { //Context context) {
//		if (customHttpClient == null) {
//	        HttpParams params = new BasicHttpParams();
//	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//	        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
//	        HttpProtocolParams.setUseExpectContinue(params, true);
//
//	        // There are two ways to get an HTTP agent string in Android, shown here.
//	        // The first is device-oriented, the second is browser-oriented.
//	        String httpAgent = System.getProperty("http.agent");
//	        Log.v(TAG, "http agent string = " + httpAgent);
////	        httpAgent = new WebView(context).getSettings().getUserAgentString();
////	        Log.v(TAG, "http agent string = " + httpAgent);
//	        HttpProtocolParams.setUserAgent(params, httpAgent);
//
//	        ConnManagerParams.setTimeout(params, 8000);
//			ConnManagerParams.setMaxTotalConnections(params, 16);
//
//	        HttpConnectionParams.setConnectionTimeout(params, 5000);
//	        HttpConnectionParams.setSoTimeout(params, 10000);
//
//	        SchemeRegistry schReg = new SchemeRegistry();
//	        schReg.register(new Scheme("http", 
//									   PlainSocketFactory.getSocketFactory(), 80));
//	        schReg.register(new Scheme("https", 
//									   SSLSocketFactory.getSocketFactory(), 443));
//	        ClientConnectionManager conMgr = new 
//				ThreadSafeClientConnManager(params,schReg);
//
//	        customHttpClient = new DefaultHttpClient(conMgr, params);
//		}
//		return customHttpClient;
//	}
//
//	public String getHttpContent(String address) {
//		if (customHttpClient != null) {
//			try {
//				HttpGet request = new HttpGet(address);
//				HttpParams params = request.getParams();
//				HttpConnectionParams.setSoTimeout(params, 60000);   // 1 minute
//				request.setParams(params);
//				Log.v("connection timeout", String.valueOf(HttpConnectionParams.getConnectionTimeout(params)));
//				Log.v("socket timeout", String.valueOf(HttpConnectionParams.getSoTimeout(params)));
//
//				String page = customHttpClient.execute(request, new BasicResponseHandler());
//				return page;
//			} catch (SocketTimeoutException e) {
//				// covers:
//				//      ClientProtocolException
//				//      ConnectTimeoutException
//				//      ConnectionPoolTimeoutException
//				//      SocketTimeoutException
//				e.printStackTrace();
//			} catch (ClientProtocolException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return "";
//    }
//
//	public Object clone() throws CloneNotSupportedException {
//		throw new CloneNotSupportedException();
//	}
//}
