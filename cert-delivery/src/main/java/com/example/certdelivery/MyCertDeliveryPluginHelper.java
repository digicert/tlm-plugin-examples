package com.example.certdelivery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import com.digicert.tlm.utils.DownloadCertificateUtil;
import com.digicert.tlm.workflows.WorkflowExecutionException;

public class MyCertDeliveryPluginHelper {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Generates a CSR and private key pair.
     *
     * @param subjectDn          The subject DN (e.g.,
     *                           "CN=example.com,O=Example,C=US")
     * @param keyAlgorithm       The key algorithm (e.g., "RSA", "EC")
     * @param keySize            The key size (e.g., 2048 for RSA)
     * @param signatureAlgorithm The signature algorithm (e.g., "sha256", "sha384")
     * @return A Pair containing the CSR (left) and private key (right) in PEM
     *         format
     */
    public static Pair<String, String> generateCSR(String subjectDn, String keyAlgorithm, int keySize,
            String signatureAlgorithm) {
        try {
            // Generate key pair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm, "BC");
            keyPairGenerator.initialize(keySize);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Build X500Name using Bouncy Castle's X500NameBuilder for proper escaping
            X500Name x500Name = buildX500Name(subjectDn);

            // Build the signature algorithm string
            String sigAlgName = buildSignatureAlgorithmName(signatureAlgorithm, keyAlgorithm);

            // Create CSR builder
            PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(x500Name,
                    keyPair.getPublic());

            // Create content signer
            ContentSigner contentSigner = new JcaContentSignerBuilder(sigAlgName)
                    .setProvider("BC")
                    .build(keyPair.getPrivate());

            // Build CSR
            PKCS10CertificationRequest csr = csrBuilder.build(contentSigner);

            // Convert CSR to PEM format
            String csrPem = convertToPem(csr);

            // Convert private key to PEM format
            String privateKeyPem = convertToPem(keyPair.getPrivate());

            return Pair.of(csrPem, privateKeyPem);

        } catch (NoSuchAlgorithmException | OperatorCreationException | IOException e) {
            throw new WorkflowExecutionException("Failed to generate CSR", e);
        } catch (Exception e) {
            throw new WorkflowExecutionException("Failed to generate CSR", e);
        }
    }

    /**
     * Generates a CSR with Subject Alternative Names (SANs).
     *
     * @param subjectDn          The subject DN
     * @param keyAlgorithm       The key algorithm
     * @param keySize            The key size
     * @param signatureAlgorithm The signature algorithm
     * @param dnsNames           Comma-separated DNS names
     * @param ipAddresses        Comma-separated IP addresses
     * @param emails             Comma-separated email addresses
     * @return A Pair containing the CSR (left) and private key (right) in PEM
     *         format
     */
    public static Pair<String, String> generateCSRWithSANs(String subjectDn, String keyAlgorithm, int keySize,
            String signatureAlgorithm, String dnsNames,
            String ipAddresses, String emails) {
        try {
            // Generate key pair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm, "BC");
            keyPairGenerator.initialize(keySize);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Build X500Name using Bouncy Castle's X500NameBuilder for proper escaping
            X500Name x500Name = buildX500Name(subjectDn);

            // Build the signature algorithm string
            String sigAlgName = buildSignatureAlgorithmName(signatureAlgorithm, keyAlgorithm);

            // Create CSR builder
            PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(x500Name,
                    keyPair.getPublic());

            // Add SANs if provided
            ExtensionsGenerator extensionsGenerator = new ExtensionsGenerator();
            List<GeneralName> sanList = new ArrayList<>();

            if (dnsNames != null && !dnsNames.isEmpty()) {
                for (String dns : dnsNames.split(",")) {
                    String trimmed = dns.trim();
                    if (!trimmed.isEmpty()) {
                        sanList.add(new GeneralName(GeneralName.dNSName, trimmed));
                    }
                }
            }

            if (ipAddresses != null && !ipAddresses.isEmpty()) {
                for (String ip : ipAddresses.split(",")) {
                    String trimmed = ip.trim();
                    if (!trimmed.isEmpty()) {
                        sanList.add(new GeneralName(GeneralName.iPAddress, trimmed));
                    }
                }
            }

            if (emails != null && !emails.isEmpty()) {
                for (String email : emails.split(",")) {
                    String trimmed = email.trim();
                    if (!trimmed.isEmpty()) {
                        sanList.add(new GeneralName(GeneralName.rfc822Name, trimmed));
                    }
                }
            }

            if (!sanList.isEmpty()) {
                GeneralNames subjectAltNames = new GeneralNames(sanList.toArray(new GeneralName[0]));
                extensionsGenerator.addExtension(Extension.subjectAlternativeName, false, subjectAltNames);
                csrBuilder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
                        extensionsGenerator.generate());
            }

            // Create content signer
            ContentSigner contentSigner = new JcaContentSignerBuilder(sigAlgName)
                    .setProvider("BC")
                    .build(keyPair.getPrivate());

            // Build CSR
            PKCS10CertificationRequest csr = csrBuilder.build(contentSigner);

            // Convert to PEM format
            String csrPem = convertToPem(csr);
            String privateKeyPem = convertToPem(keyPair.getPrivate());

            return Pair.of(csrPem, privateKeyPem);

        } catch (Exception e) {
            throw new WorkflowExecutionException("Failed to generate CSR with SANs", e);
        }
    }

    /**
     * Builds an X500Name from a null-separated subject DN string.
     * Uses Bouncy Castle's X500NameBuilder to properly handle special characters
     * according to RFC 2253/RFC 4514 standards.
     * 
     * For example: "CN=test\u0000O=SomeCompany, Inc\u0000C=US"
     * 
     * @param subjectDn The null-separated subject DN string
     * @return X500Name object with properly escaped values
     */
    private static X500Name buildX500Name(String subjectDn) {
        if (subjectDn == null || subjectDn.isEmpty()) {
            return new X500Name("");
        }

        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);

        // Split by null character (the field separator)
        String[] parts = subjectDn.split("\u0000");

        for (String part : parts) {
            String trimmedPart = part.trim();
            if (trimmedPart.isEmpty()) {
                continue;
            }

            // Find the first '=' to separate attribute type from value
            int eqIndex = trimmedPart.indexOf('=');
            if (eqIndex > 0) {
                String attrType = trimmedPart.substring(0, eqIndex).trim().toUpperCase();
                String attrValue = trimmedPart.substring(eqIndex + 1);

                ASN1ObjectIdentifier oid = getOidForAttributeType(attrType);
                if (oid != null) {
                    // X500NameBuilder handles all special character escaping internally
                    builder.addRDN(oid, attrValue);
                }
            }
        }

        return builder.build();
    }

    /**
     * Maps common DN attribute type names to their ASN1 OIDs.
     */
    private static ASN1ObjectIdentifier getOidForAttributeType(String attrType) {
        return switch (attrType) {
            case "CN", "COMMONNAME" -> BCStyle.CN;
            case "C", "COUNTRYNAME" -> BCStyle.C;
            case "ST", "STATE", "STATEORPROVINCENAME" -> BCStyle.ST;
            case "L", "LOCALITY", "LOCALITYNAME" -> BCStyle.L;
            case "O", "ORGANIZATION", "ORGANIZATIONNAME" -> BCStyle.O;
            case "OU", "ORGANIZATIONALUNIT", "ORGANIZATIONALUNITNAME" -> BCStyle.OU;
            case "E", "EMAIL", "EMAILADDRESS" -> BCStyle.EmailAddress;
            default -> null;
        };
    }

    /**
     * Builds the full signature algorithm name from the hash algorithm and key
     * algorithm.
     */
    private static String buildSignatureAlgorithmName(String signatureAlgorithm, String keyAlgorithm) {
        String hashAlg = signatureAlgorithm.toUpperCase();
        if (!hashAlg.startsWith("SHA")) {
            hashAlg = hashAlg.replace("sha", "SHA");
        }

        if ("RSA".equalsIgnoreCase(keyAlgorithm)) {
            return hashAlg + "withRSA";
        } else if ("EC".equalsIgnoreCase(keyAlgorithm) || "ECDSA".equalsIgnoreCase(keyAlgorithm)) {
            return hashAlg + "withECDSA";
        } else if ("DSA".equalsIgnoreCase(keyAlgorithm)) {
            return hashAlg + "withDSA";
        }
        return hashAlg + "with" + keyAlgorithm;
    }

    /**
     * Converts an object to PEM format.
     */
    private static String convertToPem(Object obj) throws IOException {
        StringWriter stringWriter = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
            pemWriter.writeObject(obj);
        }
        return stringWriter.toString();
    }

    /**
     * Downloads a file from a URL to a specified directory.
     *
     * @param fileUrl   The URL to download from
     * @param directory The directory to save the file to
     * @return The path to the downloaded file
     * @throws IOException if an error occurs during download
     */
    public static Path downloadFileToDirectory(String fileUrl, String directory) throws IOException {
        URL url = new URL(fileUrl);
        String fileName = "certificate.zip";
        Path targetPath = Path.of(directory, fileName);
        Files.createDirectories(Path.of(directory));
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
    public static String extractEndEntityCertificateFromZip(String zipFilePath) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(Path.of(zipFilePath)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                // Match .cer files but skip _ica.cer and .p7b files
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

    /**
     * Extracts the ICA certificate (_ica.cer) from a zip file and returns its
     * contents as a string.
     *
     * @param zipFilePath Path to the zip file
     * @return The contents of the _ica.cer file as a String, or null if not found
     * @throws IOException if an error occurs during extraction
     */
    public static String extractIcaCertificateFromZip(String zipFilePath) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(Path.of(zipFilePath)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                // Match _ica.cer files
                if (name.endsWith("_ica.cer")) {
                    // Read the _ica.cer file contents
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

    /**
     * Extracts both end-entity and ICA certificates from a zip file.
     *
     * @param zipFilePath Path to the zip file
     * @return A Pair containing the end-entity certificate (left) and ICA
     *         certificate (right)
     * @throws IOException if an error occurs during extraction
     */
    public static Pair<String, String> extractCertificatesFromZip(String zipFilePath) throws IOException {
        String endEntityCert = null;
        String icaCert = null;

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(Path.of(zipFilePath)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                if (name.endsWith(".p7b")) {
                    // Skip .p7b files
                    continue;
                }

                if (name.endsWith("_ica.cer")) {
                    // ICA certificate
                    icaCert = readZipEntryContent(zis);
                } else if (name.endsWith(".cer")) {
                    // End-entity certificate
                    endEntityCert = readZipEntryContent(zis);
                }
            }
        }
        return Pair.of(endEntityCert, icaCert);
    }

    /**
     * Reads the content of a zip entry.
     */
    private static String readZipEntryContent(ZipInputStream zis) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(zis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Downloads a certificate package from a URL and extracts the certificate
     * chain.
     * Uses the Unirest-based DownloadCertificateUtil which supports proxy
     * configuration.
     * <p>
     * The returned map contains:
     * <ul>
     * <li>"end_entity.cer" - the end-entity certificate content</li>
     * <li>"ica.cer" - the intermediate CA certificate content</li>
     * </ul>
     *
     * @param zipFileUrl URL of the certificate zip package
     * @return Map with certificate type as key and certificate content as value
     * @throws IOException if an I/O error occurs during download or extraction
     */
    public static Map<String, String> downloadCertificateChain(String zipFileUrl) throws IOException {
        DownloadCertificateUtil.init();
        return DownloadCertificateUtil.downloadAndGetCertificatesChain(zipFileUrl);
    }
}
