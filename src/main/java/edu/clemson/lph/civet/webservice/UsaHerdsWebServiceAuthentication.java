package edu.clemson.lph.civet.webservice;

import edu.clemson.lph.logging.Logger;

import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.utils.JSONParser;

public class UsaHerdsWebServiceAuthentication {
      private static Logger logger = Logger.getLogger();
	static {
	     logger.setLevel(CivetConfig.getLogLevel());
	}
	private static final String AUTH_URL = "authenticate";
	private static long lAuthExpires;
	private static String sToken = null;
	private static String sTokenType = "bearer";


	private UsaHerdsWebServiceAuthentication() {
	}
	
	/**
	 * Get the current security token if any and not expired
	 * else authenticate using username and password provided by CivetConfig
	 * @return token as String or null on failure.
	 * @throws Exception 
	 */
	public static String getToken() throws WebServiceException {
		String sURL = CivetConfig.getHERDSWebServiceURL();
		String sUserName = CivetConfig.getHERDSUserName();
		String sPassword = CivetConfig.getHERDSPassword();
		return getToken( sURL, sUserName, sPassword );
	}
	
	/**
	 * Get the security token for the given username and password
	 * @param sURL Base URL of USAHERDS Civet Webservice
	 * @param sUserName
	 * @param sPassword
	 * @return token as String or null on failure.
	 * @throws WebServiceException 
	 */
	public static String getToken( String sURL, String sUserName, String sPassword ) throws WebServiceException {
		long lNow = System.currentTimeMillis();
		String sSep = (sURL.endsWith("/") || sURL.endsWith("\\") ) ? "" : "/";
		sURL = sURL + sSep + AUTH_URL;
		if( sToken == null || lAuthExpires <= lNow ) {
			// Reauthenticate
			HttpPostClient auth = new HttpPostClient();
			auth.addParameter("grant_type", "password");
			auth.addParameter("username", sUserName);
			auth.addParameter("password", sPassword);
			if( auth.getURL(sURL) ) {
				JSONParser parser = new JSONParser( auth.getBody() );
				sToken = parser.get("access_token");
				sTokenType = parser.get("token_type");
				lAuthExpires = lNow + (1000l + Integer.parseInt(parser.get("expires_in")));
			}
			else {
				sToken = null;
				throw new WebServiceException("Error in authorization at URL: " + sURL);
			}
		}
		return sToken;
	}
	
	public static String getTokenType() {
		return sTokenType;
	}

}
