package edu.clemson.lph.ems;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.utils.GUnzip;
import edu.clemson.lph.utils.JSONParser;

/**
 * 
 */
public class EMSGetter {
	private String sLastMsgFile = null;
	private String sLastLock = null;
	private String sSummaryJson = null;
	private JSONParser pSummary = null;
	
	public EMSGetter() {
	}
	
	public boolean getOne() throws IOException {
		boolean bRet = false;
		try {
			HttpClient client = HttpClientBuilder.create().build();
			String sURI = CivetConfig.getEMSSubscriptionURL();
			HttpGet getter= new HttpGet(sURI);
			getter.addHeader("x-auth-token", CivetConfig.getEMSToken() );
			HttpResponse response = null;
			response = client.execute(getter);
			int iStatus = response.getStatusLine().getStatusCode();
			if( iStatus == 200 ) {
				String sMsgID = null;
				String sLockToken = null;
				Header headers[] = response.getAllHeaders();
				for( Header h : headers ) {
					String sName = h.getName();
					String sValue = h.getValue();
					if( EmsSubscriber.bTesting )
						System.out.println(sName + ": " +sValue);
					if( "x-message-id".equalsIgnoreCase(sName) ) {
						sMsgID = sValue;
					}
					if( "x-lock-token".equalsIgnoreCase(sName) ) {
						sLockToken = sValue;
					}
				}
				InputStream in = response.getEntity().getContent();
				String sGZFileName = "Response.gz";
				try {
					OutputStream output = new FileOutputStream(sGZFileName);
					try {
						byte[] b = new byte[1024];
						int noOfBytes = 0;
						//read bytes from source file and write to destination file
						while( (noOfBytes = in.read(b)) != -1 )
						{
							output.write(b, 0, noOfBytes);
						}			
					} finally {
						if (output != null) {
							output.close();
						}
					}
				} finally {
					if (in != null) {
						in.close();
					}
				}
				String sOutFileName = sMsgID + ".xml";
				GUnzip.decompressGzip(new File(sGZFileName), new File(sOutFileName));
				bRet = true;
				sLastMsgFile = sOutFileName;
				sLastLock = sLockToken;
			} 
			else {
				System.out.println("Response Code from Getter: " + sURI + " : " + iStatus);
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return bRet;
	}
	
	public String getLastMsgFile() {
		return sLastMsgFile;
	}
	
	public String getLastLock() {
		return sLastLock;
	}
	
	public boolean getSummary() {
		boolean bRet = false;
		try {
			HttpClient client = HttpClientBuilder.create().build();
			String sBaseURI = CivetConfig.getEMSSubscriptionURL();
			String sURI = sBaseURI + "/summary";
			HttpGet getter= new HttpGet(sURI);
			getter.addHeader("x-auth-token", CivetConfig.getEMSToken());
			HttpResponse response = null;
			response = client.execute(getter);
			if( EmsSubscriber.bTesting ) {
				Header headers[] = response.getAllHeaders();
				for( Header h : headers ) {
					String sName = h.getName();
					String sValue = h.getValue();
					System.out.println(sName + ": " +sValue);
				}
			}
			int iStatus = response.getStatusLine().getStatusCode();
			if( iStatus == 200 ) {
				sSummaryJson = EntityUtils.toString(response.getEntity());
				pSummary = new JSONParser(sSummaryJson);
				bRet = true;
				if( EmsSubscriber.bTesting ) {
					System.out.println(sSummaryJson);
				}
			} 
			else {
				System.out.println("Response Code from Getter" + sURI + " : " + iStatus);
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return bRet;
	}
	
	public String getSummaryValue(String sKey) {
		if( pSummary == null )
			return null;
		return pSummary.get(sKey);
	}
	
	public int getSummaryInt(String sKey) {
		if( pSummary == null )
			return -1;
		return pSummary.getInt(sKey);
	}

	public String getLastCertificate() {
		String sRet = null;
		String sLastMsgFile = getLastMsgFile();
		if( sLastMsgFile != null && sLastMsgFile.trim().length() > 0 ) {
			try {
				byte bytes[] = FileUtils.readUTF8File(new File(sLastMsgFile));
				String sXml = new String(bytes, "UTF-8");
				String sPattern = "CviNumber=\"[^\"]*\"";
				Pattern pattern = Pattern.compile(sPattern);
			      Matcher m = pattern.matcher(sXml);
			      if (m.find( )) {
			    	  sRet = m.group(0);
			    	  sRet = sRet.substring(11);
			    	  int iEnd = sRet.indexOf('\"');
			    	  sRet = sRet.substring(0, iEnd);
			      }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sRet;
	}
}
