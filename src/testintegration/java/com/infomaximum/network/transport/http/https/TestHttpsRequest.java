package com.infomaximum.network.transport.http.https;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class TestHttpsRequest {

    private static final String DEFAULT_CERT_ALGORITHM = "RSA";
    private static final String DEFAULT_CERT_SIG_ALGORITHM = "SHA256WithRSA";
    private static final String ALIAS = "alias";
    protected static final String PASSWORD = "password";

    protected KeyStore keystore;
    protected Path keyStorePath;

    @BeforeAll
    public void setUp() throws Exception {
        keyStorePath = Files.createTempDirectory("testStore").toAbsolutePath().resolve("keystore");
        keyStorePath.toFile().deleteOnExit();

        keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null, null);
    }

    public void initKeyStore(String certAlgorithm, String certSigAlgorithm) throws Exception {
        //createCertificateStructure(keystore, certAlgorithm, certSigAlgorithm);
        try (FileOutputStream fos = new FileOutputStream(keyStorePath.toFile())) {
            keystore.store(fos, PASSWORD.toCharArray());
        }
    }

    public void initKeyStore() throws Exception {
        //createCertificateStructure(keystore, DEFAULT_CERT_ALGORITHM, DEFAULT_CERT_SIG_ALGORITHM);
        try (FileOutputStream fos = new FileOutputStream(keyStorePath.toFile())) {
            keystore.store(fos, PASSWORD.toCharArray());
        }
    }

    @AfterAll
    public void destroy() throws Exception {
        deleteDirectory(keyStorePath.toAbsolutePath().toFile());
    }

    private static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }
        if (!directory.delete()) {
            String message =
                    "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /*
    private void createCertificateStructure(KeyStore keyStore, String algorithm, String sigAlgorithm) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, IOException, CertificateException, SignatureException, KeyStoreException {
        // generate the certificate
        // first parameter  = Algorithm
        // second parameter = signrature algorithm
        // third parameter  = the provider to use to generate the keys (may be null or
        //                    use the constructor without provider)
        CertAndKeyGen certGen = new CertAndKeyGen(algorithm, sigAlgorithm, null);
        // generate it with 2048 bits
        certGen.generate(2048);

        // prepare the validity of the certificate
        long validSecs = (long) 365 * 24 * 60 * 60; // valid for one year
        // add the certificate information, currently only valid for one year.
        X509Certificate cert = certGen.getSelfCertificate(
                // enter your details according to your application
                new X500Name("CN=My Application,O=My Organisation,L=My City,C=DE"), validSecs);

        // set the certificate and the key in the keystore
        keyStore.setKeyEntry(ALIAS, certGen.getPrivateKey(), PASSWORD.toCharArray(),
                new X509Certificate[] { cert });
    }
     */
}