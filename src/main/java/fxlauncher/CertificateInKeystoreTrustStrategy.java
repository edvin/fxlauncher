package fxlauncher;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.conn.ssl.TrustStrategy;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

public class CertificateInKeystoreTrustStrategy implements TrustStrategy {

    private static final Logger log = Logger.getLogger("CertificateInKeystoreTrustStrategy");
    private String certDigest;
    private String alias;
    private KeyStore keyStore;

    public CertificateInKeystoreTrustStrategy(String certDigest, String alias, KeyStore keyStore) {
        this.certDigest = certDigest;
        this.alias = alias;
        this.keyStore = keyStore;
    }

    @Override
    public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        try {
            log.info("Comparing the expected and actual certificates");
            X509Certificate cert = this.keyStore != null ? (X509Certificate) this.keyStore.getCertificate(this.alias) : null;
            String actualCert;
            if (x509Certificates == null || x509Certificates.length == 0)
                throw new RuntimeException("err-server-presented-no-certificates");
            else if(!(actualCert = DigestUtils.sha1Hex(x509Certificates[0].getEncoded())).equals(this.certDigest)) {
                log.info("Unexpected certificate certdigest found: "+this.certDigest+" actual:: "+actualCert);
                log.info("Presented certificate is "+x509Certificates[0]);
                throw new RuntimeException("err-https-certificate-mismatch");
            }
            return cert == null || (cert == x509Certificates[0]);
        }
        catch(KeyStoreException kse) {
            throw new RuntimeException("err-ketStore-not-found");
        }
    }
}
