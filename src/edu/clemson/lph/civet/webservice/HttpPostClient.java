package edu.clemson.lph.civet.webservice;

/*
Copyright 2016 Michael K Martin

This file is part of Civet.

Civet is free software: you can redistribute it and/or modify
it under the terms of the Lesser GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Civet is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the Lesser GNU General Public License
along with Civet.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;

import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
 
/**
 * This example demonstrates the use of the {@link ResponseHandler} to simplify
 * the process of processing the HTTP response and releasing associated resources.
 */
public class HttpPostClient {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private int status;
	private String sBody;
	private String sError;
	private List <BasicNameValuePair> lHeaders = new ArrayList <BasicNameValuePair>();
	private List <BasicNameValuePair> lParameters = new ArrayList <BasicNameValuePair>();

	public HttpPostClient() {
		
	}
	
	public void authenticate() throws WebServiceException {
		String sType = UsaHerdsWebServiceAuthentication.getTokenType();
		String sToken = UsaHerdsWebServiceAuthentication.getToken();
		addHeader("authorization", sType + " " + sToken );
	}
	
	public void addHeader( String sName, String sValue ) {
		lHeaders.add(new BasicNameValuePair(sName, sValue));
	}

	public void addParameter( String sName, String sValue ) {
		lParameters.add(new BasicNameValuePair(sName, sValue));
	}

    public final boolean getURL(String sURL) {
    	boolean bRet = true;
        CloseableHttpClient httpclient = null;
        if( CivetConfig.trustAllCerts() ) {
        	SSLContextBuilder builder = new SSLContextBuilder();
            try {
				builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
						builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				httpclient = HttpClients.custom().setSSLSocketFactory(
						sslsf).build();
				logger.error("Successfully created Allow All Hostnames client");
			} catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
				// TODO Auto-generated catch block
				logger.error("Failed to build certificate handler", e);
				return false;
			}
        }
        else {
        	httpclient = HttpClients.createDefault();
        }
        try {
            HttpPost httppost = new HttpPost(sURL);
            httppost.setEntity(new UrlEncodedFormEntity(lParameters));
            for( BasicNameValuePair pair : lHeaders ) {
            	httppost.addHeader(pair.getName(), pair.getValue());
            }
            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                @Override
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    HttpPostClient.this.status = status;
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                }
            };
            
            sBody = httpclient.execute(httppost, responseHandler);
            if( status >= 200 && status < 300 ) {
            	bRet = true;
            	sError = null;
            	// Do further error checking for errors returned in body but at a general level
            }
            else {
            	bRet = false;
            	sError = "Unexpected response status: " + status + "\n" + sBody;
            }
        } catch( ClientProtocolException ex ) {
        	sError = ex.getMessage();
        	bRet = false;
        } catch (IOException e) {
			sError = e.getMessage();
			bRet = false;
		} finally {
            try {
				httpclient.close();
			} catch (IOException e) {
				sError = e.getMessage();
			}
        }
        return bRet;
    }
    
    public String getBody() {
    	return sBody;
    }
    
    public String getError() {
    	return sError + '\n' + sBody;
    }

}
