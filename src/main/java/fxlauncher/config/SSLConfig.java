package fxlauncher.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLConfig {

	private static final Logger log = Logger.getLogger(SSLConfig.class.getName());

	public static void init(Boolean ignoreSSL) {
		if (!ignoreSSL)
			return;

		log.info("Attempting to ignore SSL exceptions");
		TrustManager[] trustManager = new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		} };
		SSLContext sslContext;

		try {
			sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustManager, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			log.warning("failed to ignore SSL exceptions");
			e.printStackTrace();
		}

		log.info("SSL exceptions are ignored");
		HostnameVerifier hostnameVerifier = (s, sslSession) -> true;
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
	}
}
