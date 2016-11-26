package com.github.jishida.gradle.shellscript.util

import org.gradle.internal.impldep.org.apache.http.client.HttpResponseException

import javax.net.ssl.*
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.regex.Pattern

import static com.github.jishida.gradle.shellscript.util.EnvironmentUtils.getJavaVersion

final class URLUtils {
    private final static class Regex {
        final static URL_FILE_PATH = Pattern.compile('^.*/([a-zA-Z0-9._\\-%]+)$')
        final static SOURCE_FORGE = Pattern.compile('^.*/([a-zA-Z0-9._\\-%]+)/download$')
    }

    private static class InsecureX509TrustManager implements X509TrustManager {
        @Override
        void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}

        @Override
        void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}

        @Override
        X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0] }
    }

    private static SSLContext _insecureSSLContext
    private static HostnameVerifier _insecureHostnameVerifier

    static {
        final javaVersion = getJavaVersion()
        if (javaVersion == null ||
                javaVersion[0] < 1 ||
                (javaVersion[0] == 1 && javaVersion[1] < 7) ||
                (javaVersion[0] == 1 && javaVersion[1] == 7 && javaVersion[2] == 0 && javaVersion[3] < 111) ||
                (javaVersion[0] == 1 && javaVersion[1] == 8 && javaVersion[2] == 0 && javaVersion[3] < 101)
        ) {
            try {
                final keyStore = KeyStore.getInstance(KeyStore.defaultType)
                new File(System.getProperty('java.home'), 'lib/security/cacerts').withInputStream {
                    keyStore.load(it, 'changeit'.toCharArray())
                }

                final certificateFactory = CertificateFactory.getInstance('X.509')
                URLUtils.getResourceAsStream('DSTRootCAX3.der').withStream {
                    final certificate = certificateFactory.generateCertificate(it)
                    keyStore.setCertificateEntry('DSTRootCAX3', certificate)
                }
                final trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.defaultAlgorithm)
                trustManagerFactory.init(keyStore)
                final sslContext = SSLContext.getInstance('TLS')
                sslContext.init(null, trustManagerFactory.trustManagers, null)
                SSLContext.default = sslContext
            } catch (Exception e) {
                throw new RuntimeException(e)
            }
        }
    }

    static String findFileName(URL self) {
        if (self.host == 'sourceforge.net') {
            final matcher = Regex.SOURCE_FORGE.matcher(self.path)
            if (matcher.matches()) {
                return URLDecoder.decode(matcher.group(1), 'UTF-8')
            }
        } else {
            final matcher = Regex.URL_FILE_PATH.matcher(self.path)
            if (matcher.matches()) {
                return URLDecoder.decode(matcher.group(1), 'UTF-8')
            }
        }
        null
    }

    static void downloadFile(
            final URL url, final File file, final boolean insecure = false) throws HttpResponseException {
        final connection = (HttpURLConnection) url.openConnection()
        if (connection instanceof HttpsURLConnection && insecure) {
            connection.hostnameVerifier = insecureHostnameVerifier
            connection.SSLSocketFactory = insecureSSLContext.socketFactory
        }
        connection.instanceFollowRedirects = false
        connection.connect()
        switch (connection.responseCode) {
            case 301:
            case 302:
            case 303:
                downloadFile(new URL(connection.getHeaderField('Location')), file, insecure)
                break
            case 200:
                connection.inputStream.withStream {
                    file << it
                }
                break
            default:
                throw new HttpResponseException(connection.responseCode, 'unexpected response code')
        }
    }

    private static SSLContext getInsecureSSLContext() {
        if (_insecureSSLContext == null) {
            final trustManagers = new TrustManager[0]
            trustManagers[0] = new InsecureX509TrustManager()
            final sslContext = SSLContext.getInstance('TLS')
            sslContext.init(null, trustManagers, null)
            _insecureSSLContext = sslContext
        }
        _insecureSSLContext
    }

    private static HostnameVerifier getInsecureHostnameVerifier() {
        _insecureHostnameVerifier ?: (_insecureHostnameVerifier = new HostnameVerifier() {
            @Override
            boolean verify(String s, SSLSession sslSession) { return true }
        })
    }
}