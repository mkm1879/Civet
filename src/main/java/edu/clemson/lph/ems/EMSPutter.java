package edu.clemson.lph.ems;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClientBuilder;
import edu.clemson.lph.logging.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;

public class EMSPutter {
      private static Logger logger = Logger.getLogger();
	
	public boolean putOne(String sLockToken, String sStatus) throws IOException {
		boolean bRet = false;
		try {
			HttpClient client = HttpClientBuilder.create().build();
			String sURIBase = CivetConfig.getEMSSubscriptionURL();
			String sURI = sURIBase + "/locks/" + sLockToken + "/?updateType=" + sStatus;
			HttpPut putter = new HttpPut(sURI);
			putter.addHeader("x-auth-token", CivetConfig.getEMSToken());
			HttpResponse response = null;
			response = client.execute(putter);
			int iStatus = response.getStatusLine().getStatusCode();
			if( CivetConfig.isEMSVerbose() ) {
				StringBuffer sb = new StringBuffer();
				Header headers[] = response.getAllHeaders();
				for( Header h : headers ) {
					String sName = h.getName();
					String sValue = h.getValue();
					sb.append(sName + ": = " + sValue + "\n");
					System.out.println(sName + ": " +sValue);
				}
				logger.info(sb.toString());
			}
			if( iStatus == 200 ) {
				bRet = true;
			}
			else {
				logger.error("Response Code from Putter: " + sURI + " : " + iStatus);
				System.out.println("Response Code from Putter: " + sURI + " : " + iStatus);
			}
		} catch( Exception e ) {
			logger.error(e);
			e.printStackTrace();
		}
		return bRet;
	}

}
