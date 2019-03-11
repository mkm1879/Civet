package edu.clemson.lph.civet.webservice;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class TrustCerts {

	public TrustCerts() {
	}
	
	public static HttpsURLConnection Trust( String url, String keyStorePath) {
		SSLContext sslContext = null;

		String trustStorePath = keyStorePath;
		String keyStorePw = "password";//put your key store password
		String trustStorePw = "password";//put your truststore password
		String keyPass = "password";//put your certificate key password


		sslContext = initializeSSLContext(keyStorePath, keyStorePw, trustStorePath, trustStorePw, keyPass, false);

		URL obj;
		HttpsURLConnection conn = null;

		try {
		    obj = new URL(url);
		    conn=(HttpsURLConnection) obj.openConnection();
		    conn.setSSLSocketFactory(sslContext.getSocketFactory());
		} catch (MalformedURLException e) {
		    System.out.println("MalformedURLException occurred " + e.getMessage());
		} catch (IOException e) {
		    System.out.println("IOException occurred in Trust() " + e.getMessage());
		}
		return conn;
	}

	private static SSLContext initializeSSLContext(final String keyStorePath, final String pwKeyStore, final String trustStorePath, final String pwTrustStore, final String keyPass, final boolean trustall) {

		char[] keyStorePw = pwKeyStore.toCharArray();
		char[] trustStorePw = pwTrustStore.toCharArray();
		char[] keyPw = keyPass.toCharArray();

		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextInt();

		KeyStore ks = null;
		try {
			ks = KeyStore.getInstance("JKS");
		} catch (KeyStoreException exp) {
			System.out.println("SSLContextUtil::KeyStoreException exception occurred while reading the config file : " +exp.getMessage());
		}
		FileInputStream fis = null;
		try {
			try {
				fis = new FileInputStream(keyStorePath);
			} catch (FileNotFoundException exp) {
				System.out.println("SSLContextUtil::FileNotFoundException exception occurred " +exp.getMessage());
			}
			try {
				ks.load(fis, keyStorePw);
			} catch (NoSuchAlgorithmException exp) {
				System.out.println("SSLContextUtil::NoSuchAlgorithmException exception occurred " +exp.getMessage());
			} catch (CertificateException exp) {
				System.out.println("SSLContextUtil::CertificateException exception occurred " +exp.getMessage());
			} catch (IOException exp) {
				System.out.println("SSLContextUtil::CertificateException exception occurred " +exp.getMessage());
			}           
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException exp) {
					System.out.println("SSLContextUtil::IOException exception occurred " +exp.getMessage());
				}
		}

		System.out.println("SSLContextUtil::[initializeSSLContext] KMF keystorepw loaded.");

		KeyManagerFactory kmf = null;
		try {
			kmf = KeyManagerFactory.getInstance("SunX509");
		} catch (NoSuchAlgorithmException exp) {
			System.out.println("SSLContextUtil::IOException exception occurred " +exp.getMessage());
		}
		try {
			kmf.init(ks, keyPw);
		} catch (UnrecoverableKeyException exp) {
			System.out.println("SSLContextUtil::UnrecoverableKeyException exception occurred " +exp.getMessage());
		} catch (KeyStoreException exp) {
			System.out.println("SSLContextUtil::KeyStoreException exception occurred " +exp.getMessage());
		} catch (NoSuchAlgorithmException exp) {
			System.out.println("SSLContextUtil::NoSuchAlgorithmException exception occurred " +exp.getMessage());
		}

		System.out.println("SSLContextUtil::[initializeSSLContext] KMF init done.");

		KeyStore ts = null;
		try {
			ts = KeyStore.getInstance("JKS");
		} catch (KeyStoreException exp) {
			System.out.println("SSLContextUtil::NoSuchAlgorithmException exception occurred " +exp.getMessage());
		}
		FileInputStream tfis = null;
		SSLContext sslContext = null;
		try {
			tfis = new FileInputStream(trustStorePath);
			ts.load(tfis, trustStorePw);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ts);
			System.out.println("SSLContextUtil::[initializeSSLContext] Truststore initialized");
			sslContext = SSLContext.getInstance("TLS");         

			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers() ,secureRandom);

		} catch (NoSuchAlgorithmException exp) {
			System.out.println("SSLContextUtil::NoSuchAlgorithmException exception occurred " +exp.getMessage());
		} catch (CertificateException exp) {
			System.out.println("SSLContextUtil::NoSuchAlgorithmException exception occurred " +exp.getMessage());
		} catch (IOException exp) {
			System.out.println("SSLContextUtil::NoSuchAlgorithmException exception occurred " +exp.getMessage());
		} catch (KeyStoreException exp) {
			System.out.println("SSLContextUtil::NoSuchAlgorithmException exception occurred " +exp.getMessage());
		} catch (KeyManagementException exp) {
			System.out.println("SSLContextUtil::NoSuchAlgorithmException exception occurred " +exp.getMessage());
		} finally {
			if (tfis != null)
				try {
					tfis.close();
				} catch (IOException exp) {
					System.out.println("SSLContextUtil::NoSuchAlgorithmException exception occurred " +exp.getMessage());
				}
		}

		if((sslContext == null)){
			System.out.println("SSLContextUtil::[initializeSSLContext] sslContext is null");
		}
		return sslContext;
	}
	
	public static void TrustAll() {
		TrustManager[] trustAllCerts = new TrustManager[]{
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					public void checkClientTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
					}
					public void checkServerTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
					}
				}};

		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	

}
