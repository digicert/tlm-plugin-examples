package com.example.certdelivery;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.digicert.tlm.SdkContext;
import com.digicert.tlm.plugin.PluginUtils;
import com.digicert.tlm.plugin.Response;
import com.digicert.tlm.plugin.WorkflowEntryPoint;
import com.digicert.tlm.plugin.model.PluginConfiguration;
import com.digicert.tlm.workflows.WorkflowExecutionException;
import com.digicert.tlm.workflows.certdelivery.AbstractCertificateDeliveryWorkflow;
import com.digicert.tlm.workflows.certdelivery.dto.DispatchCertificateRequest;
import com.digicert.tlm.workflows.certdelivery.dto.DispatchCertificateResponse;
import com.digicert.tlm.workflows.certdelivery.dto.GenerateCsrRequest;
import com.digicert.tlm.workflows.certdelivery.dto.GenerateCsrResponse;
import com.example.certdelivery.extended.configuration.MyPluginConfiguration;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

/**
 * Certificate Delivery Plugin for TLM (Trust Lifecycle Manager).
 * <p>
 * This plugin implements the certificate delivery workflow, providing
 * functionality to:
 * <ul>
 * <li>Generate Certificate Signing Requests (CSRs) with configurable
 * parameters</li>
 * <li>Download and install certificates from DigiCert's certificate delivery
 * service</li>
 * </ul>
 * <p>
 * The plugin stores generated CSRs, private keys, and downloaded certificates
 * in a temporary
 * directory structure: {@code <tempDir>/MyCertDeliveryPlugin/<flowId>/}
 *
 * @see AbstractCertificateDeliveryWorkflow
 */
@Slf4j
@WorkflowEntryPoint(name = "MyCertDeliveryPlugin")
@SuppressWarnings("unchecked")
public class MyCertDeliveryPlugin extends AbstractCertificateDeliveryWorkflow {

    /**
     * Constructs a new MyCertDeliveryPlugin instance.
     * <p>
     * Initializes the plugin with the provided SDK context and configuration.
     * If extended configuration is present, it will be logged for debugging
     * purposes.
     *
     * @param context       The SDK context providing runtime environment and
     *                      utilities
     * @param configuration The plugin configuration containing extended settings
     */
    public MyCertDeliveryPlugin(SdkContext context, PluginConfiguration<MyPluginConfiguration> configuration) {
        Optional.ofNullable(configuration.getExtendedConfig()).ifPresent(cfg -> {
            log.info("Loaded extended configuration: {}", cfg);
        });
    }

    /**
     * Generates a Certificate Signing Request (CSR) based on the provided
     * parameters.
     * <p>
     * This method creates a new key pair and CSR, then stores both the CSR and
     * private key
     * to files in the temporary directory for later use during certificate
     * installation.
     *
     * <h3>Example JSON Input:</h3>
     * <pre>{@code
     * {
     * "subjectDn":
     * "CN=windowsagent.test\u0000L=CA\u0000ST=CA\u0000O=Digicert\u0000C=US\u0000emailAddress=testacc@mail.com",
     * "dnsNames": "windowsagent.test",
     * "signatureAlgorithm": "sha256",
     * "keyAlgorithm": "RSA",
     * "keySize": "2048",
     * "flowId": "df571b3f-153c-406a-ad47-ca729f2e7ed5",
     * "ipAddresses": "",
     * "emails": ""
     * }
     *
     * }</pre>
     *
     * <h3>Input (GenerateCsrRequest):</h3>
     * <table border="1">
     * <tr><th>Field</th><th>Type</th><th>Description</th><th>Example</th></tr>
     * <tr><td>subjectDn</td><td>String</td><td>Subject Distinguished Name with
     * fields separated by null character (\u0000). Ensure that you split by null
     * character
     * when parsing the subject DN to extract individual fields.
     * Supports: CN, O, OU, L, ST, C, emailAddress</td>
     * <td>"CN=example.com\u0000O=DigiCert Inc\u0000C=US"</td></tr>
     * <tr><td>keyAlgorithm</td><td>String</td><td>Key algorithm for key pair
     * generation</td><td>"RSA", "EC"</td></tr>
     * <tr><td>keySize</td><td>String</td><td>Key size in bits</td><td>"2048",
     * "4096"</td></tr>
     * <tr><td>signatureAlgorithm</td><td>String</td><td>Hash algorithm for CSR
     * signing</td><td>"sha256", "sha384", "sha512"</td></tr>
     * <tr><td>dnsNames</td><td>String</td><td>Comma-separated DNS names for SAN
     * extension</td><td>"example.com,www.example.com"</td></tr>
     * <tr><td>ipAddresses</td><td>String</td><td>Comma-separated IP addresses for
     * SAN extension</td><td>"192.168.1.1,10.0.0.1"</td></tr>
     * <tr><td>emails</td><td>String</td><td>Comma-separated email addresses for SAN
     * extension</td><td>"admin@example.com"</td></tr>
     * <tr><td>flowId</td><td>String</td><td>Unique workflow identifier (UUID) for
     * tracking</td><td>"df571b3f-153c-406a-ad47-ca729f2e7ed5"</td></tr>
     * </table>
     *
     * <h3>Output (GenerateCsrResponse):</h3>
     * <table border="1">
     * <tr><th>Field</th><th>Type</th><th>Description</th></tr>
     * <tr><td>csr</td><td>String</td><td>The generated CSR in PEM format
     * (-----BEGIN CERTIFICATE REQUEST-----...)</td></tr>
     * </table>
     *
     * <h3>Files Created:</h3>
     * <ul>
     * <li>{@code <tempDir>/MyCertDeliveryPlugin/<flowId>/request.csr} - The CSR in
     * PEM format</li>
     * <li>{@code <tempDir>/MyCertDeliveryPlugin/<flowId>/request.key} - The private
     * key in PEM format</li>
     * </ul>
     *
     * @param request JSON request containing GenerateCsrRequest fields
     * @return GenerateCsrResponse containing the generated CSR in PEM format
     * @throws WorkflowExecutionException if CSR generation fails due to invalid
     * parameters or I/O errors
     */
    @Override
    public Response<JsonNode> generateCsr(JsonNode request) throws WorkflowExecutionException {
        try {
            log.info("GenerateCsr request: {}", objectMapper.writeValueAsString(request));

            GenerateCsrRequest<JsonNode> csrRequest = PluginUtils.convertWrappedObject(request,
                    GenerateCsrRequest.class);
            //This is determined by flow_settings in configuration.json file. If not set, it will default to system temp directory.
            GenerateCsrResponse<JsonNode> response = new GenerateCsrResponse<>();

            Pair<String, String> csrAndKey = MyCertDeliveryPluginHelper.generateCSRWithSANs(csrRequest.getSubjectDn(),
                    csrRequest.getKeyAlgorithm(),
                    Integer.parseInt(csrRequest.getKeySize()), csrRequest.getSignatureAlgorithm(),
                    csrRequest.getDnsNames(), csrRequest.getIpAddresses(), csrRequest.getEmails());

            // Store CSR and private key to files under tempdirectory/pluginname/flowId/
            String flowId = csrRequest.getFlowId();
            String pluginName = "MyCertDeliveryPlugin";
            String destination = Optional.ofNullable(csrRequest.getExtendedRequest()).map(
                ext -> Optional.ofNullable(ext.get("destination")).map(JsonNode::asText)
                    .orElse(System.getProperty("java.io.tmpdir"))).orElse(System.getProperty("java.io.tmpdir"));
            Path pluginDir = Path.of(destination, pluginName, flowId);
            Files.createDirectories(pluginDir);
            Path csrPath = pluginDir.resolve("request.csr");
            Path keyPath = pluginDir.resolve("request.key");
            Files.writeString(csrPath, csrAndKey.getLeft());
            Files.writeString(keyPath, csrAndKey.getRight());

            log.info("CSR written to: {}", csrPath);
            log.info("Private key written to: {}", keyPath);

            response.setCsr(csrAndKey.getLeft());

            log.info("GenerateCsr response: {}", objectMapper.writeValueAsString(response));
            return response;

        } catch (Exception e) {
            log.error("Failed to generate CSR", e);
            throw new WorkflowExecutionException("Failed to generate CSR: " + e.getMessage(), e);
        }
    }

    /**
     * Downloads and installs a certificate from the provided certificate link.
     * <p>
     * This method downloads the certificate package (ZIP file) from DigiCert's
     * certificate
     * delivery service, extracts the end-entity certificate and ICA (Intermediate
     * CA) certificate,
     * and saves them to the temporary directory.
     *
     * <h3>Example JSON Input:</h3>
     * <pre>{@code
     * {
     * "certificateLink":
     * "https://demo.one.digicert.com/mpki/api/v1/ts/artifact/060c12d8-4c2d-4c77-8d20-ee99b9ae70e6",
     * "flowId": "31c9bcef-3f6a-46fc-b1a1-c5ce51243ac8",
     * "filename": "example.com"
     * }
     * </pre>
     *
     * <h3>Input (DispatchCertificateRequest):</h3>
     * <table border="1">
     * <tr><th>Field</th><th>Type</th><th>Description</th><th>Example</th></tr>
     * <tr><td>certificateLink</td><td>String</td><td>URL to download the
     * certificate package (ZIP file)</td>
     * <td>"https://demo.one.digicert.com/mpki/api/v1/ts/artifact/060c12d8-..."</td></tr>
     * <tr><td>flowId</td><td>String</td><td>Unique workflow identifier (UUID)
     * matching the CSR generation step</td>
     * <td>"31c9bcef-3f6a-46fc-b1a1-c5ce51243ac8"</td></tr>
     * <tr><td>subjectDn</td><td>String</td><td>Subject DN of the certificate (for
     * reference/logging)</td>
     * <td>"CN=example.com\u0000O=DigiCert Inc\u0000C=US"</td></tr>
     * <tr><td>filename</td><td>String</td><td>Base filename for saving
     * certificates</td><td>"example.com"</td></tr>
     * <tr><td>createnew</td><td>boolean</td><td>Flag indicating whether to create
     * new certificate</td><td>true, false</td></tr>
     * <tr><td>See example above</td></tr>
     * </table>
     *
     * <h3>Output (DispatchCertificateResponse):</h3>
     * <table border="1">
     * <tr><th>Field</th><th>Type</th><th>Description</th></tr>
     * <tr><td>certificate</td><td>String</td><td>End-entity certificate content in
     * PEM format (if extracted)</td></tr>
     * <tr><td>icaCertificate</td><td>String</td><td>Intermediate CA certificate
     * content in PEM format (if extracted)</td></tr>
     * </table>
     *
     * <h3>Files Created:</h3>
     * <ul>
     * <li>{@code <tempDir>/MyCertDeliveryPlugin/<flowId>/certificate.zip} -
     * Downloaded certificate package</li>
     * <li>{@code <tempDir>/MyCertDeliveryPlugin/<flowId>/<filename>.crt} -
     * End-entity certificate</li>
     * <li>{@code <tempDir>/MyCertDeliveryPlugin/<flowId>/<filename>_ica.crt} -
     * Intermediate CA certificate</li>
     * </ul>
     *
     * <h3>ZIP File Structure Expected:</h3>
     * <ul>
     * <li>{@code *.cer} (not ending with _ica.cer) - End-entity certificate</li>
     * <li>{@code *_ica.cer} - Intermediate CA certificate</li>
     * <li>{@code *.p7b} - PKCS#7 bundle (skipped)</li>
     * </ul>
     *
     * @param request JSON request containing DispatchCertificateRequest fields
     * @return DispatchCertificateResponse (empty response on success)
     * @throws WorkflowExecutionException if download or extraction fails
     */
    @Override
    public Response<JsonNode> installCertificate(JsonNode request) throws WorkflowExecutionException {
        try {
            log.info("DispatchCertificate request: {}", objectMapper.writeValueAsString(request));

            DispatchCertificateRequest<JsonNode> dispatchRequest = PluginUtils.convertWrappedObject(request,
                    DispatchCertificateRequest.class);
            DispatchCertificateResponse<JsonNode> response = new DispatchCertificateResponse<>();

            String certificateLink = dispatchRequest.getCertificateLink();
            String flowId = dispatchRequest.getFlowId();
            String filename = Optional.ofNullable(dispatchRequest.getFilename()).orElse("certificate");

            log.info("Processing certificate dispatch - flowId: {}, filename: {}", flowId, filename);

            // Download and extract certificates using Unirest-based utility
            String pluginName = "MyCertDeliveryPlugin";
            //This is determined by flow_settings in configuration.json file. If not set, it will default to system temp directory.
            String destination = Optional.ofNullable(dispatchRequest.getExtendedRequest()).map(
                ext -> Optional.ofNullable(ext.get("destination")).map(JsonNode::asText)
                    .orElse(System.getProperty("java.io.tmpdir"))).orElse(System.getProperty("java.io.tmpdir"));
            Path pluginDir = Path.of(destination, pluginName, flowId);
            Files.createDirectories(pluginDir);

            log.info("Downloading certificate from: {}", certificateLink);
            Map<String, String> certMap = MyCertDeliveryPluginHelper.downloadCertificateChain(certificateLink);
            log.info("Certificate package downloaded and extracted successfully");

            String endEntityCert = certMap.get("end_entity.cer");
            String icaCert = certMap.get("ica.cer");

            if (endEntityCert != null) {
                log.info("End-entity certificate extracted successfully");
                // Save end-entity certificate to file
                Path certPath = pluginDir.resolve(filename + ".crt");
                Files.writeString(certPath, endEntityCert);
                log.info("End-entity certificate written to: {}", certPath);
            } else {
                log.warn("End-entity certificate not found in zip");
            }

            if (icaCert != null) {
                log.info("ICA certificate extracted successfully");
                // Save ICA certificate to file
                Path icaPath = pluginDir.resolve(filename + "_ica.crt");
                Files.writeString(icaPath, icaCert);
                log.info("ICA certificate written to: {}", icaPath);
            } else {
                log.warn("ICA certificate not found in zip");
            }

            return response;

        } catch (Exception e) {
            log.error("Failed to dispatch certificate", e);
            throw new WorkflowExecutionException("Failed to dispatch certificate: " + e.getMessage(), e);
        }
    }

}
