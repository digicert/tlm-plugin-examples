package com.example.automation.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import com.digicert.tlm.workflows.automation.dto.RefreshConfigurationResponse;
import com.example.automation.extended.response.MyRefreshResponse;

import lombok.experimental.UtilityClass;

/**
 * Helper class for MyAutomationPlugin containing cryptographic utilities.
 */
@UtilityClass
public class MyAutomationPluginHelper {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Generates a Certificate Signing Request (CSR) in PEM format.
     *
     * @return PEM-encoded CSR string
     * @throws Exception if an error occurs during CSR generation
     */
    public static Pair<String, String> generateCSR(String subjectDn, String keyType, Integer keySize,
                                                   String signatureAlgo) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyType);
        keyPairGenerator.initialize(keySize);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        X500Name subject = new X500Name(subjectDn);

        PKCS10CertificationRequestBuilder requestBuilder =
            new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());
        if ("sha256".equalsIgnoreCase(signatureAlgo)) {
            signatureAlgo = "SHA256withRSA";
        }
        ContentSigner signer = new JcaContentSignerBuilder(signatureAlgo).setProvider("BC").build(keyPair.getPrivate());

        PKCS10CertificationRequest csr = requestBuilder.build(signer);

        // Write CSR to PEM
        StringWriter csrWriter = new StringWriter();
        try (PemWriter pemWriter = new PemWriter(csrWriter)) {
            pemWriter.writeObject(new PemObject("CERTIFICATE REQUEST", csr.getEncoded()));
        }

        // Write Private Key to PEM
        StringWriter keyWriter = new StringWriter();
        try (PemWriter pemWriter = new PemWriter(keyWriter)) {
            pemWriter.writeObject(new PemObject("PRIVATE KEY", keyPair.getPrivate().getEncoded()));
        }

        return Pair.of(csrWriter.toString(), keyWriter.toString());
    }

    /**
     * Generates a self-signed X.509 certificate in PEM format.
     *
     * @return PEM-encoded certificate string
     * @throws Exception if an error occurs during certificate generation
     */
    public static String generateSelfSignedCertificate() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        X500Name issuer = new X500Name("CN=example.com,O=Example Corp,L=San Francisco,ST=CA,C=US");
        X500Name subject = issuer; // Self-signed
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date();
        Date notAfter = Date.from(LocalDateTime.now().plusYears(1).atZone(ZoneId.systemDefault()).toInstant());

        X509V3CertificateGenerator certGenerator = new X509V3CertificateGenerator();
        certGenerator.setSerialNumber(serial);
        certGenerator.setIssuerDN(new X500Principal(issuer.toString()));
        certGenerator.setSubjectDN(new X500Principal(subject.toString()));
        certGenerator.setNotBefore(notBefore);
        certGenerator.setNotAfter(notAfter);
        certGenerator.setPublicKey(keyPair.getPublic());
        certGenerator.setSignatureAlgorithm("SHA256withRSA");

        X509Certificate certificate = certGenerator.generateX509Certificate(keyPair.getPrivate(), "BC");

        StringWriter stringWriter = new StringWriter();
        try (PemWriter pemWriter = new PemWriter(stringWriter)) {
            pemWriter.writeObject(new PemObject("CERTIFICATE", certificate.getEncoded()));
        }

        return stringWriter.toString();
    }

    public static RefreshConfigurationResponse<MyRefreshResponse> populateDummyRefreshConfigurationResponse()
        throws Exception {
        RefreshConfigurationResponse<MyRefreshResponse> response = new RefreshConfigurationResponse<>();

        // Automation info
        RefreshConfigurationResponse.AutomationInfo automationInfo = new RefreshConfigurationResponse.AutomationInfo();
        automationInfo.setManagementIp("192.168.1.100");
        automationInfo.setHostName("digicert-demo.com");
        automationInfo.setOsName("Solaris");
        automationInfo.setOsFlavor("Ubuntu");
        automationInfo.setOsVersion("20.04");
        automationInfo.setOsArch("x86_64");
        automationInfo.setFips("disabled");
        automationInfo.setDataIps(new String[] {"192.168.1.101", "192.168.1.102"});
        automationInfo.setPeerInfo("192.168.1.100:PRIMARY,192.168.1.101:SECONDARY");
        automationInfo.setPartitions(new String[] {"partition1", "partition2"});
        response.setAutomationInfo(automationInfo);

        // Data IP info
        RefreshConfigurationResponse.DataIpInfo dataIpInfo1 = new RefreshConfigurationResponse.DataIpInfo();
        dataIpInfo1.setManagementIp("192.168.1.100");
        dataIpInfo1.setDataIp("192.168.1.101");
        dataIpInfo1.setPort(443);
        dataIpInfo1.setSslState(1);
        dataIpInfo1.setPartition("partition1");
        dataIpInfo1.setAlias("alias1");
        dataIpInfo1.setPortWithCert("443:cert1");

        RefreshConfigurationResponse.DataIpInfo dataIpInfo2 = new RefreshConfigurationResponse.DataIpInfo();
        dataIpInfo2.setManagementIp("192.168.1.100");
        dataIpInfo2.setDataIp("192.168.1.102");
        dataIpInfo2.setPort(443);
        dataIpInfo2.setSslState(1);
        dataIpInfo2.setPartition("partition1");
        dataIpInfo2.setAlias("alias1");
        dataIpInfo2.setPortWithCert("443:cert1");

        response.setDataIpInfo(List.of(dataIpInfo1, dataIpInfo2));

        // Certificates
        RefreshConfigurationResponse.Certificate cert1 = new RefreshConfigurationResponse.Certificate();
        cert1.setIpAddress("192.168.1.101");
        cert1.setPort(443);
        cert1.setDomainName("example.com");
        cert1.setIpAndPort("192.168.1.101:443");
        cert1.setServerParam("server-param");
        cert1.setCertificate(generateSelfSignedCertificate());
        cert1.setSni(true);
        cert1.setCipherDiscovery("TLS_AES_256_GCM_SHA384");

        RefreshConfigurationResponse.Certificate cert2 = new RefreshConfigurationResponse.Certificate();
        cert2.setIpAddress("192.168.1.102");
        cert2.setPort(443);
        cert2.setDomainName("example.com");
        cert2.setIpAndPort("192.168.1.102:443");
        cert2.setServerParam("server-param");
        cert2.setCertificate(generateSelfSignedCertificate());
        cert2.setSni(true);
        cert2.setCipherDiscovery("TLS_AES_256_GCM_SHA384");

        response.setCertificates(List.of(cert1, cert2));

        return response;
    }

    public static Path downloadFileToDirectory(String fileUrl, String directory) throws IOException {
        URL url = new URL(fileUrl);
        String fileName = "certificate.zip";
        Path targetPath = Path.of(directory, fileName);
        try (InputStream in = url.openStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return targetPath;
    }

    /**
     * Extracts the end-entity certificate (.cer) from a zip file and returns its
     * contents as a string.
     * Skips entries ending with .p7b and _ica.cer.
     *
     * @param zipFilePath Path to the zip file
     * @return The contents of the .cer file as a String, or null if not found
     * @throws IOException if an error occurs during extraction
     */
    public static String extractCertificateFromZip(String zipFilePath) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(Path.of(zipFilePath)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.endsWith(".cer") && !name.endsWith("_ica.cer")) {
                    // Read the .cer file contents
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(zis))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                    }
                    return sb.toString();
                }
            }
        }
        return null;
    }

}
