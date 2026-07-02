package com.example.automation;

import com.digicert.tlm.SdkContext;
import com.digicert.tlm.plugin.PluginUtils;
import com.digicert.tlm.plugin.Response;
import com.digicert.tlm.plugin.WorkflowEntryPoint;
import com.digicert.tlm.plugin.model.PluginConfiguration;
import com.digicert.tlm.workflows.WorkflowExecutionException;
import com.digicert.tlm.workflows.automation.AbstractAutomationWorkflow;
import com.digicert.tlm.workflows.automation.dto.GenerateCsrRequest;
import com.digicert.tlm.workflows.automation.dto.GenerateCsrResponse;
import com.digicert.tlm.workflows.automation.dto.InstallCertificateRequest;
import com.digicert.tlm.workflows.automation.dto.InstallCertificateResponse;
import com.digicert.tlm.workflows.automation.dto.RefreshConfigurationRequest;
import com.digicert.tlm.workflows.automation.dto.RefreshConfigurationResponse;
import com.digicert.tlm.workflows.automation.dto.TestConnectionResponse;
import com.digicert.tlm.workflows.automation.dto.ValidateCertificateRequest;
import com.digicert.tlm.workflows.automation.dto.ValidateCertificateResponse;
import com.example.automation.extended.configuration.MyPluginConfiguration;
import com.example.automation.extended.request.MyRefreshRequest;
import com.example.automation.extended.response.MyRefreshResponse;
import com.example.automation.helper.MyAutomationPluginHelper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@WorkflowEntryPoint(name = "MyAutomationPlugin")
@SuppressWarnings("unchecked")
public class MyAutomationPlugin extends AbstractAutomationWorkflow {

    public MyAutomationPlugin(SdkContext context, PluginConfiguration<MyPluginConfiguration> configuration) {
        Optional.ofNullable(configuration.getExtendedConfig()).ifPresent(cfg -> {
            log.info("Loaded extended configuration: {}", cfg);
        });
    }

    /**
     * Tests the connection to the managed system.
     * <p>
     * <b>Sample Request JSON:</b>
     *
     * <pre>
     * null
     * </pre>
     *
     * <b>Sample Response JSON:</b>
     *
     * <pre>
     * {
     *   "errors": null,
     *   "active": true
     * }
     * </pre>
     * <p>
     * The request is null or empty as per actual plugin invocation.
     *
     * @param request JSON request (typically null/empty)
     * @return Response indicating if the connection is active
     * @throws WorkflowExecutionException if an error occurs during connection test
     */
    /**
     * Tests the connection to the managed system.
     * <p>
     * <b>Sample Error Response JSON:</b>
     *
     * <pre>
     * {
     *   "errors": [
     *     { "code": "PLUGIN_ERROR", "message": "Error in testConnection: .." }
     *   ],
     *   "active": false
     * }
     * </pre>
     */
    @Override
    public Response<JsonNode> testConnection(JsonNode request) throws WorkflowExecutionException {
        try {
            log.info("TestConnection request: {}", objectMapper.writeValueAsString(request));
            TestConnectionResponse<JsonNode> response = new TestConnectionResponse<>();
            response.setActive(true);
            log.info("TestConnection response: {}", objectMapper.writeValueAsString(response));
            return response;
        } catch (Exception e) {
            throw new WorkflowExecutionException("Error in testConnection: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a Certificate Signing Request (CSR).
     * <p>
     * <b>Sample Request JSON:</b>
     *
     * <pre>
     * {
     *   "flowId": "6855593c-ba92-49e4-8190-54d599979b52",
     *   "flowStartDate": "2026-01-29T23:11:20Z",
     *   "subjectDn": "CN=example.com,L=Lehi,ST=Utah,O=DigiCert\\, Inc.,C=US",
     *   "dnsNames": "",
     *   "signatureAlgorithm": "sha256",
     *   "keyAlgorithm": "RSA",
     *   "keySize": "2048",
     *   "virtualServerName": "partition1/alias1"
     * }
     * </pre>
     *
     * <b>Sample Response JSON:</b>
     *
     * <pre>
     * {
     *   "errors": null,
     *   "csr": "-----BEGIN CERTIFICATE REQUEST-----\nMIICpDCCAYwCAQAwXzEUMBIGA1UEAwwLZXhhbXBsZS5jb20xFTATBgNVBAoMDEV4..==\n-----END CERTIFICATE REQUEST-----\n"
     * }
     * </pre>
     *
     * @param request JSON request for CSR generation
     * @return Response containing the generated CSR
     * @throws WorkflowExecutionException if an error occurs during CSR generation
     */
    /**
     * Generates a Certificate Signing Request (CSR).
     * <p>
     * <b>Sample Error Response JSON:</b>
     *
     * <pre>
     * {
     *   "errors": [
     *     { "code": "PLUGIN_ERROR", "message": "Error in generateCsr: .." }
     *   ]
     * }
     * </pre>
     */
    @Override
    public Response<JsonNode> generateCsr(JsonNode request) throws WorkflowExecutionException {
        try {
            log.info("GenerateCsr request: {}", objectMapper.writeValueAsString(request));
            GenerateCsrRequest<JsonNode> csrRequest =
                PluginUtils.convertWrappedObject(request, GenerateCsrRequest.class);
            GenerateCsrResponse<JsonNode> response = new GenerateCsrResponse<>();
            Pair<String, String> csrAndKey =
                MyAutomationPluginHelper.generateCSR(csrRequest.getSubjectDn(), csrRequest.getKeyAlgorithm(),
                    Integer.parseInt(csrRequest.getKeySize()), csrRequest.getSignatureAlgorithm());

            // Store CSR and private key to files under tempdirectory/pluginname/flowId/
            String flowId = csrRequest.getFlowId();
            String pluginName = "MyAutomationPlugin";
            String tempDir = System.getProperty("java.io.tmpdir");
            Path pluginDir = Path.of(tempDir, pluginName, flowId);
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
            throw new WorkflowExecutionException("Error in GenerateCsr: " + e.getMessage(), e);
        }
    }

    /**
     * Installs a certificate on the managed system.
     * <p>
     * <b>Sample Request JSON:</b>
     *
     * <pre>
     * {
     *   "flowId": "8a265aeb-451c-44b7-a1fd-3ec00c98c4e0",
     *   "flowStartDate": "2026-01-28T22:47:09Z",
     *   "currentCertificateThumbprint": "36E718C8EBE677764423FCED6F891D475F3FD67A7253101AB3E36E5A9486744A",
     *   "certificateLink": "https://one.digicert.com/mpki/api/v1/ts/artifact/55dc890b-541b-462a-8e7b-c0c4e13fe3cd",
     *   "managementIp": null,
     *   "ipAddress": "192.168.1.101",
     *   "port": "443",
     *   "appName": null,
     *   "appVersion": null,
     *   "virtualServerName": "partition1/alias1"
     * }
     * </pre>
     *
     * <b>Sample Response JSON:</b>
     *
     * <pre>
     * {
     *   "errors": null
     * }
     * </pre>
     *
     * @param request JSON request for certificate installation
     * @return Response indicating the result of the installation
     * @throws WorkflowExecutionException if an error occurs during installation
     */
    /**
     * Installs a certificate on the managed system.
     * <p>
     * <b>Sample Error Response JSON:</b>
     *
     * <pre>
     * {
     *   "errors": [
     *     { "code": "PLUGIN_ERROR", "message": "Error in installCertificate: .." }
     *   ]
     * }
     * </pre>
     */
    @Override
    public Response<JsonNode> installCertificate(JsonNode request) throws WorkflowExecutionException {
        try {
            log.info("InstallCertificate request: {}", objectMapper.writeValueAsString(request));
            // Parse the request into MyInstallCertificateRequest
            InstallCertificateRequest<JsonNode> installRequest =
                PluginUtils.convertWrappedObject(request, InstallCertificateRequest.class);
            String certLink = installRequest.getCertificateLink();
            log.info("Downloading certificate from link: {}", certLink);
            String flowId = installRequest.getFlowId();
            String pluginName = "MyAutomationPlugin";
            String tempDir = System.getProperty("java.io.tmpdir");
            Path pluginDir = Path.of(tempDir, pluginName, flowId);
            Files.createDirectories(pluginDir);
            Path downloadedPath = null;
            if (certLink != null && !certLink.isEmpty()) {
                try {
                    downloadedPath = MyAutomationPluginHelper.downloadFileToDirectory(certLink, pluginDir.toString());
                    log.info("Certificate downloaded to: {}", downloadedPath);
                } catch (Exception ex) {
                    log.error("Failed to download certificate: {}", ex.getMessage());
                }
            }
            InstallCertificateResponse<JsonNode> response = new InstallCertificateResponse<>();
            // TODO: Implement actual logic using installRequest and downloadedPath
            log.info("InstallCertificate response: {}", objectMapper.writeValueAsString(response));
            return response;
        } catch (Exception e) {
            throw new WorkflowExecutionException("Error in installCertificate: " + e.getMessage(), e);
        }
    }

    /**
     * Validates a certificate on the managed system.
     * <p>
     * <b>Sample Request JSON:</b>
     *
     * <pre>
     * {
     *   "flowId": "8a265aeb-451c-44b7-a1fd-3ec00c98c4e0",
     *   "flowStartDate": "2026-01-28T22:47:09Z",
     *   "certificateLink": "https://one.digicert.com/mpki/api/v1/ts/artifact/55dc890b-541b-462a-8e7b-c0c4e13fe3cd",
     *   "certificateSerialNumber": "6a0df325fd660828a86e6c7a8a3e4ecc1a697fcd",
     *   "managementIp": null,
     *   "ipAddress": "192.168.1.101",
     *   "port": "443",
     *   "virtualServerName": "partition1/alias1",
     *   "subjectDn": "CN=example.com,L=Lehi,ST=Utah,O=DigiCert\\, Inc.,C=US",
     * }
     * </pre>
     *
     * <b>Sample Response JSON:</b>
     *
     * <pre>
     * {
     *   "errors": null,
     *   "certificate": "-----BEGIN CERTIFICATE-----\nMIIDPDCCAiSgAwIBAgIGAZwISxn+MA0GCSqGSIb3DQEBCwUAMF8xCzAJBgNVBAYT..\n-----END CERTIFICATE-----\n"
     * }
     * </pre>
     *
     * @param request JSON request for certificate validation
     * @return Response containing the validation result and certificate details
     * @throws WorkflowExecutionException if an error occurs during validation
     */
    /**
     * Validates a certificate on the managed system.
     * <p>
     * <b>Sample Error Response JSON:</b>
     *
     * <pre>
     * {
     *   "errors": [
     *     { "code": "PLUGIN_ERROR", "message": "Error in validateCertificate: .." }
     *   ]
     * }
     * </pre>
     */
    @Override
    public Response<JsonNode> validateCertificate(JsonNode request) throws WorkflowExecutionException {
        try {
            log.info("ValidateCertificate request: {}", objectMapper.writeValueAsString(request));
            // Map request to ValidateCertificateRequest<MyValidateRequest>
            ValidateCertificateRequest<JsonNode> validateRequest =
                PluginUtils.convertWrappedObject(request, ValidateCertificateRequest.class);
            ValidateCertificateResponse<JsonNode> response = new ValidateCertificateResponse<>();
            String flowId = validateRequest.getFlowId();
            String pluginName = "MyAutomationPlugin";
            String tempDir = System.getProperty("java.io.tmpdir");
            Path pluginFileZip = Path.of(tempDir, pluginName, flowId, "certificate.zip");
            log.info("Looking for certificate zip at: {}", pluginFileZip);
            if (Files.exists(pluginFileZip)) {
                log.info("Certificate zip found at: {}", pluginFileZip);
                String extractedCert = MyAutomationPluginHelper.extractCertificateFromZip(pluginFileZip.toString());
                response.setCertificate(extractedCert);
                log.info("Extracted certificate from zip for validation.");
                return response;
            }
            log.info("ValidateCertificate response: {}", objectMapper.writeValueAsString(response));
            return response;
        } catch (Exception e) {
            throw new WorkflowExecutionException("Error in validateCertificate: " + e.getMessage(), e);
        }
    }

    /**
     * Refreshes the configuration and retrieves system information.
     * <p>
     * <b>Sample Request JSON:</b>
     *
     * <pre>
     * null
     * </pre>
     *
     * <b>Sample Response JSON:</b>
     *
     * <pre>
     * {
     *   "errors": null,
     *   "automationInfo": { .. },
     *   ..
     * }
     * </pre>
     *
     * <b>Sample Error Response JSON:</b>
     *
     * <pre>
     * {
     *   "errors": [
     *     { "code": "PLUGIN_ERROR", "message": "Error in refreshConfiguration: .." }
     *   ]
     * }
     * </pre>
     * <p>
     * The request is null or empty as per actual plugin invocation.
     *
     * @param request JSON request (typically null/empty)
     * @return Response containing refreshed configuration and system info
     * @throws WorkflowExecutionException if an error occurs during refresh
     */
    @Override
    public Response<JsonNode> refreshConfiguration(JsonNode request) throws WorkflowExecutionException {
        try {
            //The following is an example demonstrating how you can map extended variables to your custom request
            // class in addition to default parameters, provided the cloud supports this.
            RefreshConfigurationRequest<MyRefreshRequest> refreshRequest =
                PluginUtils.convertWrappedObject(request, RefreshConfigurationRequest.class, MyRefreshRequest.class);

            log.info("RefreshConfiguration request: {}", objectMapper.writeValueAsString(refreshRequest));

            // Use helper to populate dummy response data
            RefreshConfigurationResponse<MyRefreshResponse> response =
                MyAutomationPluginHelper.populateDummyRefreshConfigurationResponse();

            RefreshConfigurationResponse<JsonNode> refreshResponse =
                PluginUtils.convertWrappedObject(response, RefreshConfigurationResponse.class, JsonNode.class);
            log.info("RefreshConfiguration response: {}", objectMapper.writeValueAsString(refreshResponse));
            return refreshResponse;
        } catch (Exception e) {
            throw new WorkflowExecutionException("Error in refreshConfiguration: " + e.getMessage(), e);
        }
    }

}
