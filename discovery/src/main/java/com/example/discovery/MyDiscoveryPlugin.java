package com.example.discovery;

import com.digicert.tlm.SdkContext;
import com.digicert.tlm.plugin.Response;
import com.digicert.tlm.plugin.WorkflowEntryPoint;
import com.digicert.tlm.plugin.model.PluginConfiguration;
import com.digicert.tlm.workflows.WorkflowExecutionException;
import com.digicert.tlm.workflows.discovery.AbstractDiscoveryWorkflow;
import com.digicert.tlm.workflows.discovery.dto.FetchUsersResponse;
import com.digicert.tlm.workflows.discovery.dto.ImportDataRequest;
import com.digicert.tlm.workflows.discovery.dto.ImportDataResponse;
import com.digicert.tlm.workflows.discovery.dto.TestConnectionResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@WorkflowEntryPoint(name = "MyDiscoveryPlugin")
public class MyDiscoveryPlugin extends AbstractDiscoveryWorkflow {

    private final MyPluginConfiguration configuration;

    public MyDiscoveryPlugin(SdkContext context, PluginConfiguration<MyPluginConfiguration> configuration) {
        this.configuration = configuration.getExtendedConfig();
    }

    @Override
    public Response<JsonNode> getDiscoveryData(JsonNode request) throws WorkflowExecutionException {
        try {
            log.info("GetDiscoveryData request: {}", objectMapper.writeValueAsString(request));
            ImportDataResponse<JsonNode> response = new ImportDataResponse<>();

            ImportDataRequest importDataRequest =
                    objectMapper.readValue(
                            request.toString(),
                            ImportDataRequest.class
                    );

            ImportStateMap oldImportState =
                    objectMapper.convertValue(
                            importDataRequest.getImportState(),
                            ImportStateMap.class
                    );

            ImportDataResponse.ImportData scan1 = new ImportDataResponse.ImportData();
            scan1.setCertString("-----BEGIN CERTIFICATE-----\n" +
                    "MIIEMjCCAxqgAwIBAgIBATANBgkqhkiG9w0BAQUFADB7MQswCQYDVQQGEwJHQjEb\n" +
                    "MBkGA1UECAwSR3JlYXRlciBNYW5jaGVzdGVyMRAwDgYDVQQHDAdTYWxmb3JkMRow\n" +
                    "GAYDVQQKDBFDb21vZG8gQ0EgTGltaXRlZDEhMB8GA1UEAwwYQUFBIENlcnRpZmlj\n" +
                    "YXRlIFNlcnZpY2VzMB4XDTA0MDEwMTAwMDAwMFoXDTI4MTIzMTIzNTk1OVowezEL\n" +
                    "MAkGA1UEBhMCR0IxGzAZBgNVBAgMEkdyZWF0ZXIgTWFuY2hlc3RlcjEQMA4GA1UE\n" +
                    "BwwHU2FsZm9yZDEaMBgGA1UECgwRQ29tb2RvIENBIExpbWl0ZWQxITAfBgNVBAMM\n" +
                    "GEFBQSBDZXJ0aWZpY2F0ZSBTZXJ2aWNlczCCASIwDQYJKoZIhvcNAQEBBQADggEP\n" +
                    "ADCCAQoCggEBAL5AnfRu4ep2hxxNRUSOvkbIgwadwSr+GB+O5AL686tdUIoWMQua\n" +
                    "BtDFcCLNSS1UY8y2bmhGC1Pqy0wkwLxyTurxFa70VJoSCsN6sjNg4tqJVfMiWPPe\n" +
                    "3M/vg4aijJRPn2jymJBGhCfHdr/jzDUsi14HZGWCwEiwqJH5YZ92IFCokcdmtet4\n" +
                    "YgNW8IoaE+oxox6gmf049vYnMlhvB/VruPsUK6+3qszWY19zjNoFmag4qMsXeDZR\n" +
                    "rOme9Hg6jc8P2ULimAyrL58OAd7vn5lJ8S3frHRNG5i1R8XlKdH5kBjHYpy+g8cm\n" +
                    "ez6KJcfA3Z3mNWgQIJ2P2N7Sw4ScDV7oL8kCAwEAAaOBwDCBvTAdBgNVHQ4EFgQU\n" +
                    "oBEKIz6W8Qfs4q8p74Klf9AwpLQwDgYDVR0PAQH/BAQDAgEGMA8GA1UdEwEB/wQF\n" +
                    "MAMBAf8wewYDVR0fBHQwcjA4oDagNIYyaHR0cDovL2NybC5jb21vZG9jYS5jb20v\n" +
                    "QUFBQ2VydGlmaWNhdGVTZXJ2aWNlcy5jcmwwNqA0oDKGMGh0dHA6Ly9jcmwuY29t\n" +
                    "b2RvLm5ldC9BQUFDZXJ0aWZpY2F0ZVNlcnZpY2VzLmNybDANBgkqhkiG9w0BAQUF\n" +
                    "AAOCAQEACFb8AvCb6P+k+tZ7xkSAzk/ExfYAWMymtrwUSWgEdujm7l3sAg9g1o1Q\n" +
                    "GE8mTgHj5rCl7r+8dFRBv/38ErjHT1r0iWAFf2C3BUrz9vHCv8S5dIa2LX1rzNLz\n" +
                    "Rt0vxuBqw8M0Ayx9lt1awg6nCpnBBYurDC/zXDrPbDdVCYfeU0BsWO/8tqtlbgT2\n" +
                    "G9w84FoVxp7Z8VlIMCFlA2zs6SFz7JsDoeA3raAVGI/6ugLOpyypEBMs1OUIJqsi\n" +
                    "l2D4kF501KKaU73yqWjgom7C12yxow+ev+to51byrvLjKzg6CYG1a4XXvi3tPxq3\n" +
                    "smPi9WIsgtRqAEFQ8TmDn5XpNpaYbg==\n" +
                    "-----END CERTIFICATE-----\n" +
                    "-----BEGIN CERTIFICATE-----\n" +
                    "MIIFgTCCBGmgAwIBAgIQOXJEOvkit1HX02wQ3TE1lTANBgkqhkiG9w0BAQwFADB7\n" +
                    "MQswCQYDVQQGEwJHQjEbMBkGA1UECAwSR3JlYXRlciBNYW5jaGVzdGVyMRAwDgYD\n" +
                    "VQQHDAdTYWxmb3JkMRowGAYDVQQKDBFDb21vZG8gQ0EgTGltaXRlZDEhMB8GA1UE\n" +
                    "AwwYQUFBIENlcnRpZmljYXRlIFNlcnZpY2VzMB4XDTE5MDMxMjAwMDAwMFoXDTI4\n" +
                    "MTIzMTIzNTk1OVowgYgxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpOZXcgSmVyc2V5\n" +
                    "MRQwEgYDVQQHEwtKZXJzZXkgQ2l0eTEeMBwGA1UEChMVVGhlIFVTRVJUUlVTVCBO\n" +
                    "ZXR3b3JrMS4wLAYDVQQDEyVVU0VSVHJ1c3QgUlNBIENlcnRpZmljYXRpb24gQXV0\n" +
                    "aG9yaXR5MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAgBJlFzYOw9sI\n" +
                    "s9CsVw127c0n00ytUINh4qogTQktZAnczomfzD2p7PbPwdzx07HWezcoEStH2jnG\n" +
                    "vDoZtF+mvX2do2NCtnbyqTsrkfjib9DsFiCQCT7i6HTJGLSR1GJk23+jBvGIGGqQ\n" +
                    "Ijy8/hPwhxR79uQfjtTkUcYRZ0YIUcuGFFQ/vDP+fmyc/xadGL1RjjWmp2bIcmfb\n" +
                    "IWax1Jt4A8BQOujM8Ny8nkz+rwWWNR9XWrf/zvk9tyy29lTdyOcSOk2uTIq3XJq0\n" +
                    "tyA9yn8iNK5+O2hmAUTnAU5GU5szYPeUvlM3kHND8zLDU+/bqv50TmnHa4xgk97E\n" +
                    "xwzf4TKuzJM7UXiVZ4vuPVb+DNBpDxsP8yUmazNt925H+nND5X4OpWaxKXwyhGNV\n" +
                    "icQNwZNUMBkTrNN9N6frXTpsNVzbQdcS2qlJC9/YgIoJk2KOtWbPJYjNhLixP6Q5\n" +
                    "D9kCnusSTJV882sFqV4Wg8y4Z+LoE53MW4LTTLPtW//e5XOsIzstAL81VXQJSdhJ\n" +
                    "WBp/kjbmUZIO8yZ9HE0XvMnsQybQv0FfQKlERPSZ51eHnlAfV1SoPv10Yy+xUGUJ\n" +
                    "5lhCLkMaTLTwJUdZ+gQek9QmRkpQgbLevni3/GcV4clXhB4PY9bpYrrWX1Uu6lzG\n" +
                    "KAgEJTm4Diup8kyXHAc/DVL17e8vgg8CAwEAAaOB8jCB7zAfBgNVHSMEGDAWgBSg\n" +
                    "EQojPpbxB+zirynvgqV/0DCktDAdBgNVHQ4EFgQUU3m/WqorSs9UgOHYm8Cd8rID\n" +
                    "ZsswDgYDVR0PAQH/BAQDAgGGMA8GA1UdEwEB/wQFMAMBAf8wEQYDVR0gBAowCDAG\n" +
                    "BgRVHSAAMEMGA1UdHwQ8MDowOKA2oDSGMmh0dHA6Ly9jcmwuY29tb2RvY2EuY29t\n" +
                    "L0FBQUNlcnRpZmljYXRlU2VydmljZXMuY3JsMDQGCCsGAQUFBwEBBCgwJjAkBggr\n" +
                    "BgEFBQcwAYYYaHR0cDovL29jc3AuY29tb2RvY2EuY29tMA0GCSqGSIb3DQEBDAUA\n" +
                    "A4IBAQAYh1HcdCE9nIrgJ7cz0C7M7PDmy14R3iJvm3WOnnL+5Nb+qh+cli3vA0p+\n" +
                    "rvSNb3I8QzvAP+u431yqqcau8vzY7qN7Q/aGNnwU4M309z/+3ri0ivCRlv79Q2R+\n" +
                    "/czSAaF9ffgZGclCKxO/WIu6pKJmBHaIkU4MiRTOok3JMrO66BQavHHxW/BBC5gA\n" +
                    "CiIDEOUMsfnNkjcZ7Tvx5Dq2+UUTJnWvu6rvP3t3O9LEApE9GQDTF1w52z97GA1F\n" +
                    "zZOFli9d31kWTz9RvdVFGD/tSo7oBmF0Ixa1DVBzJ0RHfxBdiSprhTEUxOipakyA\n" +
                    "vGp4z7h/jnZymQyd/teRCBaho1+V\n" +
                    "-----END CERTIFICATE-----\n" +
                    "-----BEGIN CERTIFICATE-----\n" +
                    "MIIGGTCCBAGgAwIBAgIQE31TnKp8MamkM3AZaIR6jTANBgkqhkiG9w0BAQwFADCB\n" +
                    "iDELMAkGA1UEBhMCVVMxEzARBgNVBAgTCk5ldyBKZXJzZXkxFDASBgNVBAcTC0pl\n" +
                    "cnNleSBDaXR5MR4wHAYDVQQKExVUaGUgVVNFUlRSVVNUIE5ldHdvcmsxLjAsBgNV\n" +
                    "BAMTJVVTRVJUcnVzdCBSU0EgQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkwHhcNMTgx\n" +
                    "MTAyMDAwMDAwWhcNMzAxMjMxMjM1OTU5WjCBlTELMAkGA1UEBhMCR0IxGzAZBgNV\n" +
                    "BAgTEkdyZWF0ZXIgTWFuY2hlc3RlcjEQMA4GA1UEBxMHU2FsZm9yZDEYMBYGA1UE\n" +
                    "ChMPU2VjdGlnbyBMaW1pdGVkMT0wOwYDVQQDEzRTZWN0aWdvIFJTQSBPcmdhbml6\n" +
                    "YXRpb24gVmFsaWRhdGlvbiBTZWN1cmUgU2VydmVyIENBMIIBIjANBgkqhkiG9w0B\n" +
                    "AQEFAAOCAQ8AMIIBCgKCAQEAnJMCRkVKUkiS/FeN+S3qU76zLNXYqKXsW2kDwB0Q\n" +
                    "9lkz3v4HSKjojHpnSvH1jcM3ZtAykffEnQRgxLVK4oOLp64m1F06XvjRFnG7ir1x\n" +
                    "on3IzqJgJLBSoDpFUd54k2xiYPHkVpy3O/c8Vdjf1XoxfDV/ElFw4Sy+BKzL+k/h\n" +
                    "fGVqwECn2XylY4QZ4ffK76q06Fha2ZnjJt+OErK43DOyNtoUHZZYQkBuCyKFHFEi\n" +
                    "rsTIBkVtkuZntxkj5Ng2a4XQf8dS48+wdQHgibSov4o2TqPgbOuEQc6lL0giE5dQ\n" +
                    "YkUeCaXMn2xXcEAG2yDoG9bzk4unMp63RBUJ16/9fAEc2wIDAQABo4IBbjCCAWow\n" +
                    "HwYDVR0jBBgwFoAUU3m/WqorSs9UgOHYm8Cd8rIDZsswHQYDVR0OBBYEFBfZ1iUn\n" +
                    "Z/kxwklD2TA2RIxsqU/rMA4GA1UdDwEB/wQEAwIBhjASBgNVHRMBAf8ECDAGAQH/\n" +
                    "AgEAMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjAbBgNVHSAEFDASMAYG\n" +
                    "BFUdIAAwCAYGZ4EMAQICMFAGA1UdHwRJMEcwRaBDoEGGP2h0dHA6Ly9jcmwudXNl\n" +
                    "cnRydXN0LmNvbS9VU0VSVHJ1c3RSU0FDZXJ0aWZpY2F0aW9uQXV0aG9yaXR5LmNy\n" +
                    "bDB2BggrBgEFBQcBAQRqMGgwPwYIKwYBBQUHMAKGM2h0dHA6Ly9jcnQudXNlcnRy\n" +
                    "dXN0LmNvbS9VU0VSVHJ1c3RSU0FBZGRUcnVzdENBLmNydDAlBggrBgEFBQcwAYYZ\n" +
                    "aHR0cDovL29jc3AudXNlcnRydXN0LmNvbTANBgkqhkiG9w0BAQwFAAOCAgEAThNA\n" +
                    "lsnD5m5bwOO69Bfhrgkfyb/LDCUW8nNTs3Yat6tIBtbNAHwgRUNFbBZaGxNh10m6\n" +
                    "pAKkrOjOzi3JKnSj3N6uq9BoNviRrzwB93fVC8+Xq+uH5xWo+jBaYXEgscBDxLmP\n" +
                    "bYox6xU2JPti1Qucj+lmveZhUZeTth2HvbC1bP6mESkGYTQxMD0gJ3NR0N6Fg9N3\n" +
                    "OSBGltqnxloWJ4Wyz04PToxcvr44APhL+XJ71PJ616IphdAEutNCLFGIUi7RPSRn\n" +
                    "R+xVzBv0yjTqJsHe3cQhifa6ezIejpZehEU4z4CqN2mLYBd0FUiRnG3wTqN3yhsc\n" +
                    "SPr5z0noX0+FCuKPkBurcEya67emP7SsXaRfz+bYipaQ908mgWB2XQ8kd5GzKjGf\n" +
                    "FlqyXYwcKapInI5v03hAcNt37N3j0VcFcC3mSZiIBYRiBXBWdoY5TtMibx3+bfEO\n" +
                    "s2LEPMvAhblhHrrhFYBZlAyuBbuMf1a+HNJav5fyakywxnB2sJCNwQs2uRHY1ihc\n" +
                    "6k/+JLcYCpsM0MF8XPtpvcyiTcaQvKZN8rG61ppnW5YCUtCC+cQKXA0o4D/I+pWV\n" +
                    "idWkvklsQLI+qGu41SWyxP7x09fn1txDAXYw+zuLXfdKiXyaNb78yvBXAfCNP6CH\n" +
                    "MntHWpdLgtJmwsQt6j8k9Kf5qLnjatkYYaA7jBU=\n" +
                    "-----END CERTIFICATE-----\n" +
                    "-----BEGIN CERTIFICATE-----\n" +
                    "MIIHBzCCBe+gAwIBAgIQDFRwdI2MuRg7F41QS6s1UjANBgkqhkiG9w0BAQsFADCB\n" +
                    "lTELMAkGA1UEBhMCR0IxGzAZBgNVBAgTEkdyZWF0ZXIgTWFuY2hlc3RlcjEQMA4G\n" +
                    "A1UEBxMHU2FsZm9yZDEYMBYGA1UEChMPU2VjdGlnbyBMaW1pdGVkMT0wOwYDVQQD\n" +
                    "EzRTZWN0aWdvIFJTQSBPcmdhbml6YXRpb24gVmFsaWRhdGlvbiBTZWN1cmUgU2Vy\n" +
                    "dmVyIENBMB4XDTIyMDEwNjAwMDAwMFoXDTIyMTIzMTIzNTk1OVowczELMAkGA1UE\n" +
                    "BhMCSU4xEDAOBgNVBAgTB0d1amFyYXQxEjAQBgNVBAcTCUFobWVkYWJhZDEgMB4G\n" +
                    "A1UEChMXVHVuY2VyIEluZm90ZWNoIFB2dCBMdGQxHDAaBgNVBAMTE2FwaXRlc3Qy\n" +
                    "LnBraW9wcy5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCwIlXX\n" +
                    "gZ/8vmxgKQGgKvhlHfbavJ/wxwnxqqHR9KAT9Q6FIHN6WAOCJ8HK86kXEFJcV5xv\n" +
                    "tKK6WdtUx4o3sSWmcWmGVsFhL0v6oEzhQt8atOpWyAo7cgcSm8h0A3vY9M+Kn37H\n" +
                    "ndrMUh//Lmn2kU6df7UePZXie2yA1sJw4Qr2Z5NysvZRqFVgkVJ9owwuCfxpkUVg\n" +
                    "l2aq1qX03jJzzeZbZf+dcekmcolodJUFh6nTIkPISOV6g9JF8OcNR1uzv1kicu6b\n" +
                    "bRoBiXWDi8Oyz/8sfMctK8Q+1EPUN80CNpX2a71LmFIeBYtooH9L+QmzCHRQyBu1\n" +
                    "PJ+wiXSD4WgPr62JAgMBAAGjggNyMIIDbjAfBgNVHSMEGDAWgBQX2dYlJ2f5McJJ\n" +
                    "Q9kwNkSMbKlP6zAdBgNVHQ4EFgQUzxO3uaOvZ3mIJwXeaPj8PXAPWlowDgYDVR0P\n" +
                    "AQH/BAQDAgWgMAwGA1UdEwEB/wQCMAAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsG\n" +
                    "AQUFBwMCMEoGA1UdIARDMEEwNQYMKwYBBAGyMQECAQMEMCUwIwYIKwYBBQUHAgEW\n" +
                    "F2h0dHBzOi8vc2VjdGlnby5jb20vQ1BTMAgGBmeBDAECAjBaBgNVHR8EUzBRME+g\n" +
                    "TaBLhklodHRwOi8vY3JsLnNlY3RpZ28uY29tL1NlY3RpZ29SU0FPcmdhbml6YXRp\n" +
                    "b25WYWxpZGF0aW9uU2VjdXJlU2VydmVyQ0EuY3JsMIGKBggrBgEFBQcBAQR+MHww\n" +
                    "VQYIKwYBBQUHMAKGSWh0dHA6Ly9jcnQuc2VjdGlnby5jb20vU2VjdGlnb1JTQU9y\n" +
                    "Z2FuaXphdGlvblZhbGlkYXRpb25TZWN1cmVTZXJ2ZXJDQS5jcnQwIwYIKwYBBQUH\n" +
                    "MAGGF2h0dHA6Ly9vY3NwLnNlY3RpZ28uY29tMDcGA1UdEQQwMC6CE2FwaXRlc3Qy\n" +
                    "LnBraW9wcy5jb22CF3d3dy5hcGl0ZXN0Mi5wa2lvcHMuY29tMIIBfwYKKwYBBAHW\n" +
                    "eQIEAgSCAW8EggFrAWkAdQBGpVXrdfqRIDC1oolp9PN9ESxBdL79SbiFq/L8cP5t\n" +
                    "RwAAAX4wpFqIAAAEAwBGMEQCIEqhMkSc52LVjQYqRIAZoNqB8hSdmCNPwwZBmcoa\n" +
                    "NdAWAiA9dAM4cllC0eHeBZooVGf6TUuc2h2tXyFUSiIKSet5+QB3AEHIyrHfIkZK\n" +
                    "EMahOglCh15OMYsbA+vrS8do8JBilgb2AAABfjCkWkcAAAQDAEgwRgIhANKXQOk8\n" +
                    "uzgq2RGGE9xC3HNkaoPSWVJAoEz+CMg6q3ykAiEAm1/s6UVL7OniK6sdHqszz8n8\n" +
                    "8TzZ1Jf047xSfLP+TsEAdwApeb7wnjk5IfBWc59jpXflvld9nGAK+PlNXSZcJV3H\n" +
                    "hAAAAX4wpFoiAAAEAwBIMEYCIQCW5bLnNQgO0O1rSCgPNfDj5HyUyFf7WRqpgOMD\n" +
                    "XvsuIQIhAKH4HIFe5H1uxl22x/1dKq1g0vCLc3K0Mh44BpBQz7TxMA0GCSqGSIb3\n" +
                    "DQEBCwUAA4IBAQBrCytrfj+v5gZFiqiBOum4BxXzuHgV1sqrmRzH9KxLSy30kRTT\n" +
                    "jQbVDlVql6eCz8vfwXAAYk5QFelRLSHCqB/tfr1aNWl9osTv1v02p4CwWKvEmmge\n" +
                    "Mh6pJvOcEzRgAieDFI8RwKhBNpu+TzdRevvY41KmT5tqABl8zk7LtV4iJaGDMS0S\n" +
                    "B3Juzn5CimRCTdATsQD7WPLSAp1pa8BIOauuzMC/4oYSB6DjV+Ryn2CX6SPTMAka\n" +
                    "cCRAyARwaCceNwx/TQrpPlV9yzzGEkX85M6yloWJEGO+cV7ci/1xmUG6Jbk5DN+n\n" +
                    "1O4WmS9ym1Wvo3xgrKPi0pa9TceyrvJGIA4j\n" +
                    "-----END CERTIFICATE-----");
            scan1.setIp("192.168.1.10");
            scan1.setPort(443);
            scan1.setHostname("server.example.com");
            scan1.setDomainName("example.com");
            scan1.setOperatingSystem("Ubuntu 22.04");
            scan1.setProtocol("HTTPS");
            scan1.setService("Web Server");
            scan1.setScannedDate(new Date());
            scan1.setCertThumbprint("A1B2C3D4E5F6");
            scan1.setCertId("cert-12345");
            scan1.setOperatingSystem("Linux");
            scan1.setProtocol("HTTP");
            scan1.setFirstFound(System.currentTimeMillis() - 86400000L);
            scan1.setLastFound(System.currentTimeMillis());
            scan1.setSecurityHeader("Strict-Transport-Security");
            scan1.setCipher("TLS_AES_128_GCM_SHA256");
            scan1.setRating("A+");
            scan1.setHandshakeProtocol("TLSv1.3");

            ImportDataResponse.ImportData scan2 = new ImportDataResponse.ImportData();
            scan2.setCertString("-----BEGIN CERTIFICATE-----\n" +
                    "MIIHPzCCBaegAwIBAgIQcK0l2Aa/QYT3BlswQpPUGTANBgkqhkiG9w0BAQsFADBg\n" +
                    "MQswCQYDVQQGEwJHQjEYMBYGA1UEChMPU2VjdGlnbyBMaW1pdGVkMTcwNQYDVQQD\n" +
                    "Ey5TZWN0aWdvIFB1YmxpYyBTZXJ2ZXIgQXV0aGVudGljYXRpb24gQ0EgT1YgUjM2\n" +
                    "MB4XDTI1MTExOTAwMDAwMFoXDTI2MTExOTIzNTk1OVowYzELMAkGA1UEBhMCVVMx\n" +
                    "EDAOBgNVBAgTB0Zsb3JpZGExGzAZBgNVBAoTElJhcGlkIFdlYiBTZXJ2aWNlczEl\n" +
                    "MCMGA1UEAxMcc2Vuc29yY240NTA4OTEubG9hZGd1aWRlLmNvbTCCASIwDQYJKoZI\n" +
                    "hvcNAQEBBQADggEPADCCAQoCggEBAM8p0uRMDm5yuPoR97DwogtvHVaECRrMsYXH\n" +
                    "rlFYMbC+Me5A5tnXvtN9siJqYcN9AI1whCFnTTOePbgNO/TszLpy2OmXB9cHDnAW\n" +
                    "M0mTC2fkK4cQvI5ZltONwaKWFWMeEfojx3RSIRNuFXhebjwcPuJcEKofXhaigl9T\n" +
                    "g/azruUiK6vMetCurMW3qSO1QWqVHFX85oaUC44W9npm/RfJP3viCT6V3MhBX0cg\n" +
                    "FgYYiD87Irr882pNPBUq1GTMBKlnxoPdh7UOkg9DTsAq1XFVj4nHBOVXrbV8MRBa\n" +
                    "4OcsgeVFdpSjAiaTTljrT8MJ4WRmOMavfjg3HlQe3qtTEaMojLcCAwEAAaOCA3Aw\n" +
                    "ggNsMB8GA1UdIwQYMBaAFONmdLtwaI0sXU4OpkqPmzcinIKSMB0GA1UdDgQWBBR7\n" +
                    "4UCJq6y+2qtUGrcuSAy104EmcTAOBgNVHQ8BAf8EBAMCBaAwDAYDVR0TAQH/BAIw\n" +
                    "ADATBgNVHSUEDDAKBggrBgEFBQcDATBKBgNVHSAEQzBBMDUGDCsGAQQBsjEBAgED\n" +
                    "BDAlMCMGCCsGAQUFBwIBFhdodHRwczovL3NlY3RpZ28uY29tL0NQUzAIBgZngQwB\n" +
                    "AgIwVAYDVR0fBE0wSzBJoEegRYZDaHR0cDovL2NybC5zZWN0aWdvLmNvbS9TZWN0\n" +
                    "aWdvUHVibGljU2VydmVyQXV0aGVudGljYXRpb25DQU9WUjM2LmNybDCBhAYIKwYB\n" +
                    "BQUHAQEEeDB2ME8GCCsGAQUFBzAChkNodHRwOi8vY3J0LnNlY3RpZ28uY29tL1Nl\n" +
                    "Y3RpZ29QdWJsaWNTZXJ2ZXJBdXRoZW50aWNhdGlvbkNBT1ZSMzYuY3J0MCMGCCsG\n" +
                    "AQUFBzABhhdodHRwOi8vb2NzcC5zZWN0aWdvLmNvbTBJBgNVHREEQjBAghxzZW5z\n" +
                    "b3JjbjQ1MDg5MS5sb2FkZ3VpZGUuY29tgiB3d3cuc2Vuc29yY240NTA4OTEubG9h\n" +
                    "ZGd1aWRlLmNvbTCCAYEGCisGAQQB1nkCBAIEggFxBIIBbQFrAHcA1219ENGn9XfC\n" +
                    "x+lf1wC/+YLJM1pl4dCzAXMXwMjFaXcAAAGam+apMwAABAMASDBGAiEA2cBYHmFl\n" +
                    "KxTXiJUXyH8T7uyuMf6Akdv70z7j+PkXzMUCIQDdvpdwY7AS5c2ijpBcyYQ3Z8bw\n" +
                    "5NMXNHZsm6DRPtnTSQB3AK9niDtXsE7dj6bZfvYuqOuBCsdxYPAkXlXWDC/nhYc6\n" +
                    "AAABmpvms+cAAAQDAEgwRgIhAJS6YCL8XUQHlwd1PTAoKOttFEOMzRA5UcyL+JxW\n" +
                    "kva3AiEA23zC4BLzAgulFarafIarEibZ4rEyUW/eGz1hfkcMi6cAdwCsqzBwbOvs\n" +
                    "hDH0E9L0kV8RHkIkQ7HypoxPPCs7px4CwwAAAZqb5qknAAAEAwBIMEYCIQC/Jr0d\n" +
                    "fyvYevO2qowCNuFRYYw11e4L+lilbqpa8VlADAIhAPvHDRr3+q1eQRS8vL9fH4CN\n" +
                    "uAzz2CwwYP4prTiW1ao3MA0GCSqGSIb3DQEBCwUAA4IBgQCRJo+VpbC4TiUBT2lU\n" +
                    "5P1+xwIK8VY9dbvXiM6mmu0jYKzEh7505A03MZg9xgwAQHs2+qcZShA7vdhCs3kV\n" +
                    "XIsYfT/RRLJbQvnkLneRgF9X4HqKS2HW8An6eVuExS36xkCdfiKH0qUpIsBtYi+h\n" +
                    "x7muxzWABIJX8bFNWAWOB+sTqB4zTGUsZLlvVAYtrUz4fvkyqK/gJBjc4ZPYxzLh\n" +
                    "z0Adh1W3oIOQrWgoc7JHN5ypobGyHxN5m0TwIgj2Nnpa8t8Z8tpU/WZxaAnExzjn\n" +
                    "rHoV98aOv69EPkCijcmc18ysA7Gyoe6bC64v+68MsRbLqgy/ZUby5yOjuhlMk/hs\n" +
                    "YyESLuHIGFkQlZQgs7UPK8XQV/+7NtpenWLXC+6A+dcMz9XLrtFWgE9Hb6/2wOzK\n" +
                    "a0Q3vNtpGWtdWAGLMnTdE9aMIG2+3Z7O2pC4UgsmNaHDblSTRGrJSm+9gfDHjcpa\n" +
                    "T4rNmw3DjctttKo9yzKVWctTCg3RvO9QBCK8BowO7OzwlmU=\n" +
                    "-----END CERTIFICATE----------BEGIN CERTIFICATE-----\n" +
                    "MIIGTDCCBDSgAwIBAgIQLBo8dulD3d3/GRsxiQrtcTANBgkqhkiG9w0BAQwFADBf\n" +
                    "MQswCQYDVQQGEwJHQjEYMBYGA1UEChMPU2VjdGlnbyBMaW1pdGVkMTYwNAYDVQQD\n" +
                    "Ey1TZWN0aWdvIFB1YmxpYyBTZXJ2ZXIgQXV0aGVudGljYXRpb24gUm9vdCBSNDYw\n" +
                    "HhcNMjEwMzIyMDAwMDAwWhcNMzYwMzIxMjM1OTU5WjBgMQswCQYDVQQGEwJHQjEY\n" +
                    "MBYGA1UEChMPU2VjdGlnbyBMaW1pdGVkMTcwNQYDVQQDEy5TZWN0aWdvIFB1Ymxp\n" +
                    "YyBTZXJ2ZXIgQXV0aGVudGljYXRpb24gQ0EgT1YgUjM2MIIBojANBgkqhkiG9w0B\n" +
                    "AQEFAAOCAY8AMIIBigKCAYEApkMtJ3R06jo0fceI0M52B7K+TyMeGcv2BQ5AVc3j\n" +
                    "lYt76TvHIu/nNe22W/RJXX9rWUD/2GE6GF5x0V4bsY7K3IeJ8E7+KzG/TGboySfD\n" +
                    "u+F52jqQBbY62ofhYjMeiAbLI02+FqwHeM8uIrUtcX8b2RCxF358TB0NHVccAXZc\n" +
                    "FYgZndZCeXxjuca7pJJ20LLUnXtgXcjAE1vY4WvbReW0W6mkeZyNGdmpTcFs5Y+s\n" +
                    "yy6LtE5Zocji9J9NlNnReox2RWVyEXpA1ChZ4gqN+ZpVSIQ0HBorVFbBKyhdZyEX\n" +
                    "gZgNSNtBRwxqwIzJePJhYd4ZUhO1vk+/uP3nwDk0p95q/j7naXNCSvESnrHPypaB\n" +
                    "WRK066nKfPRPi9m9kIOhMdYfS8giFRTcdgL24Ycilj7ecAK9Trh0VbjwouJ4WH+x\n" +
                    "bt47u68ZFCD/ac55I0DNHkCpaPruj6e9Rmr7K46wZDAYXuEAqB7tGG/jd6JAA+H2\n" +
                    "O44CV98NRsU213f1kScIZntNAgMBAAGjggGBMIIBfTAfBgNVHSMEGDAWgBRWc1hk\n" +
                    "lfmSGrASKgRieaFAFYghSTAdBgNVHQ4EFgQU42Z0u3BojSxdTg6mSo+bNyKcgpIw\n" +
                    "DgYDVR0PAQH/BAQDAgGGMBIGA1UdEwEB/wQIMAYBAf8CAQAwHQYDVR0lBBYwFAYI\n" +
                    "KwYBBQUHAwEGCCsGAQUFBwMCMBsGA1UdIAQUMBIwBgYEVR0gADAIBgZngQwBAgIw\n" +
                    "VAYDVR0fBE0wSzBJoEegRYZDaHR0cDovL2NybC5zZWN0aWdvLmNvbS9TZWN0aWdv\n" +
                    "UHVibGljU2VydmVyQXV0aGVudGljYXRpb25Sb290UjQ2LmNybDCBhAYIKwYBBQUH\n" +
                    "AQEEeDB2ME8GCCsGAQUFBzAChkNodHRwOi8vY3J0LnNlY3RpZ28uY29tL1NlY3Rp\n" +
                    "Z29QdWJsaWNTZXJ2ZXJBdXRoZW50aWNhdGlvblJvb3RSNDYucDdjMCMGCCsGAQUF\n" +
                    "BzABhhdodHRwOi8vb2NzcC5zZWN0aWdvLmNvbTANBgkqhkiG9w0BAQwFAAOCAgEA\n" +
                    "BZXWDHWC3cubb/e1I1kzi8lPFiK/ZUoH09ufmVOrc5ObYH/XKkWUexSPqRkwKFKr\n" +
                    "7r8OuG+p7VNB8rifX6uopqKAgsvZtZsq7iAFw04To6vNcxeBt1Eush3cQ4b8nbQR\n" +
                    "MQLChgEAqwhuXp9P48T4QEBSksYav7+aFjNySsLYlPzNqVM3RNwvBdvp6vgDtGwc\n" +
                    "xlKQZVuuNVIaoYyls8swhxDeSHKpRdxRauTLZ+pl+wGvy0pnrLEJGSz9mOEmfbod\n" +
                    "e/XopR2NGqaHJ6bIjyxPu6UtyQGI26En7UAEozACrHz06Nx2jTAY9E6NeB6XuobE\n" +
                    "wLK025ZRmvglcURG1BrV24tGHHTgxCe8M3oGlpUSMTKQ2dkgljZVYt+gKdFtWELZ\n" +
                    "MuRdi+X3XsrR8LFz+aLUiDRfQqhmw3RxjIyVKvvu9UPYY1nsvxYmFnUSeM+2q1z/\n" +
                    "iPUry+xDY9MC6+IhleKT094VKdFVp7LXH42+wvU+17lRolQ2mK2N/nBLVBwaIhib\n" +
                    "QXw4VYKwB86Bc6eS6iqsc94KEgD/U4VsjmgfhK+Xp4NM+VYzTTa3QeV3p8xOM0cw\n" +
                    "q1p8oZFA+OBcz3FYWpDIe5j0NWKlw9hXsTyPY/HeZUV59akskSOSRSmDfe8wJDPX\n" +
                    "58uB9/7lud0G3x0pxQAcffP0ayKavNwDTw4UfJ34cEw=\n" +
                    "-----END CERTIFICATE-----\n" +
                    "-----BEGIN CERTIFICATE-----\n" +
                    "MIIGlTCCBH2gAwIBAgIRANJ/u8HeNZ5SFq1hSVhgmcQwDQYJKoZIhvcNAQEMBQAw\n" +
                    "gYgxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpOZXcgSmVyc2V5MRQwEgYDVQQHEwtK\n" +
                    "ZXJzZXkgQ2l0eTEeMBwGA1UEChMVVGhlIFVTRVJUUlVTVCBOZXR3b3JrMS4wLAYD\n" +
                    "VQQDEyVVU0VSVHJ1c3QgUlNBIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MB4XDTIx\n" +
                    "MDMyMjAwMDAwMFoXDTM4MDExODIzNTk1OVowXzELMAkGA1UEBhMCR0IxGDAWBgNV\n" +
                    "BAoTD1NlY3RpZ28gTGltaXRlZDE2MDQGA1UEAxMtU2VjdGlnbyBQdWJsaWMgU2Vy\n" +
                    "dmVyIEF1dGhlbnRpY2F0aW9uIFJvb3QgUjQ2MIICIjANBgkqhkiG9w0BAQEFAAOC\n" +
                    "Ag8AMIICCgKCAgEAk77VNlJ12AEjoBxHQknuY7a3If3EldVIKyZ8FFMQ2nn9K7ct\n" +
                    "pNQs+uoy3UnCub0PSD17WphUr55dMXRPB/xQId2kz2hPGxJjbSWZTCqZ80gwYfqB\n" +
                    "fB6nCErcPiscHxhMcao1jK34bug7StnllALWiYQTqm3ITzPMUJY3kjPcX4jnn1TZ\n" +
                    "SPCYQ9Zm/Z8XOEPFAVEL1+MjDxRdWxTnS77d9MjaAzfR1jmhIVEwg7Bt1zBOlluR\n" +
                    "8HAkq79FgWRDDb0hOi886Z4NyyC1QifM2m+b7mQwkDnNk2WBITG1I1AzNyLjOO34\n" +
                    "MTDMRf5i+dFdMnlCh99qzFYZQE3Oqrv5tXZJlPEn+JGlg+UGs2MOgNzgElWApjtm\n" +
                    "tDmHLcjw0NEU6eQNTQ72XVdyxTscR1ad4tX7gWGMzE2AkDRbt9cUddzYBEifwMEo\n" +
                    "iLTpHMqnsfFWt3tJTFnlIBWohAIp+jiUaZpJBo/NH3kUFxIMg3reH7GX7vmXeCik\n" +
                    "yESS6X0mBaZYcpt5E9gRX67FOGI0aLKGMI74kGGeMmz1BzbNokxu7Io27fLmmRVE\n" +
                    "cMN8vJw5wLTha/eDJSNX2RKA5UnwdQ/vjescm1QotCE8/HwK/+97a3X/ix2gGQWr\n" +
                    "+vgrgULoOLq7+6r9PeDzyt9Ol5cp7fMYVumllqy9w5CYsuD5otSmR0N8bc8CAwEA\n" +
                    "AaOCASAwggEcMB8GA1UdIwQYMBaAFFN5v1qqK0rPVIDh2JvAnfKyA2bLMB0GA1Ud\n" +
                    "DgQWBBRWc1hklfmSGrASKgRieaFAFYghSTAOBgNVHQ8BAf8EBAMCAYYwDwYDVR0T\n" +
                    "AQH/BAUwAwEB/zAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwEQYDVR0g\n" +
                    "BAowCDAGBgRVHSAAMFAGA1UdHwRJMEcwRaBDoEGGP2h0dHA6Ly9jcmwudXNlcnRy\n" +
                    "dXN0LmNvbS9VU0VSVHJ1c3RSU0FDZXJ0aWZpY2F0aW9uQXV0aG9yaXR5LmNybDA1\n" +
                    "BggrBgEFBQcBAQQpMCcwJQYIKwYBBQUHMAGGGWh0dHA6Ly9vY3NwLnVzZXJ0cnVz\n" +
                    "dC5jb20wDQYJKoZIhvcNAQEMBQADggIBADpvBIlq7bMU0cFDT/9P9+BsgCkRgQs0\n" +
                    "S6Bf7vJSlWMHwby0VGvxCS0hrbi0K2BINZbEbsVsgpQq04431yyoVn3Hldorgq24\n" +
                    "RldRDOOipEZDTFB9wC9HYt1thHF00XeG2C8KC1plwoEzKAIhPvefI/C3cT0CfTXJ\n" +
                    "uFjUbKIgSwjNjw6YHtLgoy/hd5+JLUlLco/gzFX/qWbT7tEquOMYpsNKWZj8TLqP\n" +
                    "q6zMiG4Na6feEZte6YPXGrMWlTWN341vDedc+yxQqSug79HJUQcOZs7KyDWztmae\n" +
                    "QxsPE49UV/8XwrfZtZaYyrs4FpD94Z4Q8dzXGL8+qEJjxgcza7W6PROaClubavd1\n" +
                    "VKPm8+aCW77u7SxpR2TFGL6kPdxsKyFijpcunR5V79sUyROfNdzjrAcFWZXK8sbb\n" +
                    "9FlnwuVG677JLv+ZVTX5AxLvW5OB4zt5uS+zB62wJ/Wv+jXGAttSAcJec4iFgCWH\n" +
                    "Rvdi/jJoSzRLa3nEzx6pFIzclSCnh0u1xCeLcUBypSiPga8W+6PkuoyQq8U9qs9E\n" +
                    "oxG5NvrvlyshwUS9yvcZRGw7Ljlx4jJH/BhIPR8kIBCQj1vna9TziZOrw1Of8hDU\n" +
                    "bHKFG9Pm8Dp2vbjz/2JH39qvxshPKVllGfq+5klPm7yZRUYTiCMAbqwNdL/nsqF2\n" +
                    "Rnnyp58XRStJ\n" +
                    "-----END CERTIFICATE-----\n" +
                    "-----BEGIN CERTIFICATE-----\n" +
                    "MIIF3jCCA8agAwIBAgIQAf1tMPyjylGoG7xkDjUDLTANBgkqhkiG9w0BAQwFADCB\n" +
                    "iDELMAkGA1UEBhMCVVMxEzARBgNVBAgTCk5ldyBKZXJzZXkxFDASBgNVBAcTC0pl\n" +
                    "cnNleSBDaXR5MR4wHAYDVQQKExVUaGUgVVNFUlRSVVNUIE5ldHdvcmsxLjAsBgNV\n" +
                    "BAMTJVVTRVJUcnVzdCBSU0EgQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkwHhcNMTAw\n" +
                    "MjAxMDAwMDAwWhcNMzgwMTE4MjM1OTU5WjCBiDELMAkGA1UEBhMCVVMxEzARBgNV\n" +
                    "BAgTCk5ldyBKZXJzZXkxFDASBgNVBAcTC0plcnNleSBDaXR5MR4wHAYDVQQKExVU\n" +
                    "aGUgVVNFUlRSVVNUIE5ldHdvcmsxLjAsBgNVBAMTJVVTRVJUcnVzdCBSU0EgQ2Vy\n" +
                    "dGlmaWNhdGlvbiBBdXRob3JpdHkwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIK\n" +
                    "AoICAQCAEmUXNg7D2wiz0KxXDXbtzSfTTK1Qg2HiqiBNCS1kCdzOiZ/MPans9s/B\n" +
                    "3PHTsdZ7NygRK0faOca8Ohm0X6a9fZ2jY0K2dvKpOyuR+OJv0OwWIJAJPuLodMkY\n" +
                    "tJHUYmTbf6MG8YgYapAiPLz+E/CHFHv25B+O1ORRxhFnRghRy4YUVD+8M/5+bJz/\n" +
                    "Fp0YvVGONaanZshyZ9shZrHUm3gDwFA66Mzw3LyeTP6vBZY1H1dat//O+T23LLb2\n" +
                    "VN3I5xI6Ta5MirdcmrS3ID3KfyI0rn47aGYBROcBTkZTmzNg95S+UzeQc0PzMsNT\n" +
                    "79uq/nROacdrjGCT3sTHDN/hMq7MkztReJVni+49Vv4M0GkPGw/zJSZrM233bkf6\n" +
                    "c0Plfg6lZrEpfDKEY1WJxA3Bk1QwGROs0303p+tdOmw1XNtB1xLaqUkL39iAigmT\n" +
                    "Yo61Zs8liM2EuLE/pDkP2QKe6xJMlXzzawWpXhaDzLhn4ugTncxbgtNMs+1b/97l\n" +
                    "c6wjOy0AvzVVdAlJ2ElYGn+SNuZRkg7zJn0cTRe8yexDJtC/QV9AqURE9JnnV4ee\n" +
                    "UB9XVKg+/XRjL7FQZQnmWEIuQxpMtPAlR1n6BB6T1CZGSlCBst6+eLf8ZxXhyVeE\n" +
                    "Hg9j1uliutZfVS7qXMYoCAQlObgOK6nyTJccBz8NUvXt7y+CDwIDAQABo0IwQDAd\n" +
                    "BgNVHQ4EFgQUU3m/WqorSs9UgOHYm8Cd8rIDZsswDgYDVR0PAQH/BAQDAgEGMA8G\n" +
                    "A1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQEMBQADggIBAFzUfA3P9wF9QZllDHPF\n" +
                    "Up/L+M+ZBn8b2kMVn54CVVeWFPFSPCeHlCjtHzoBN6J2/FNQwISbxmtOuowhT6KO\n" +
                    "VWKR82kV2LyI48SqC/3vqOlLVSoGIG1VeCkZ7l8wXEskEVX/JJpuXior7gtNn3/3\n" +
                    "ATiUFJVDBwn7YKnuHKsSjKCaXqeYalltiz8I+8jRRa8YFWSQEg9zKC7F4iRO/Fjs\n" +
                    "8PRF/iKz6y+O0tlFYQXBl2+odnKPi4w2r78NBc5xjeambx9spnFixdjQg3IM8WcR\n" +
                    "iQycE0xyNN+81XHfqnHd4blsjDwSXWXavVcStkNr/+XeTWYRUc+ZruwXtuhxkYze\n" +
                    "Sf7dNXGiFSeUHM9h4ya7b6NnJSFd5t0dCy5oGzuCr+yDZ4XUmFF0sbmZgIn/f3gZ\n" +
                    "XHlKYC6SQK5MNyosycdiyA5d9zZbyuAlJQG03RoHnHcAP9Dc1ew91Pq7P8yF1m9/\n" +
                    "qS3fuQL39ZeatTXaw2ewh0qpKJ4jjv9cJ2vhsE/zB+4ALtRZh8tSQZXq9EfX7mRB\n" +
                    "VXyNWQKV3WKdwrnuWih0hKWbt5DHDAff9Yk2dDLWKMGwsAvgnEzDHNb842m1R0aB\n" +
                    "L6KCq9NjRHDEjf8tM7qtj3u1cIiuPhnPQCjY/MiQu12ZIvVS5ljFH4gxQ+6IHdfG\n" +
                    "jjxDah2nGN59PRbxYvnKkKj9\n" +
                    "-----END CERTIFICATE-----");
            scan2.setIp("10.0.0.170");
            scan2.setPort(8443);
            scan2.setHostname("api.example.net");
            scan2.setDomainName("extentest.net");
            scan2.setOperatingSystem("Windows Server 2022");
            scan2.setProtocol("HTTPS");
            scan2.setService("API Gateway");
            scan2.setScannedDate(new Date());
            scan2.setOperatingSystem("Windows Server 2022");
            scan2.setProtocol("HTTPS");
            scan2.setCertThumbprint("FFF222333AAA");
            scan2.setCertId("cert-67890");
            scan2.setFirstFound(System.currentTimeMillis() - 172800000L); // 2 days ago
            scan2.setLastFound(System.currentTimeMillis());
            scan2.setSecurityHeader("Content-Security-Policy");
            scan2.setCipher("TLS_CHACHA20_POLY1305_SHA256");
            scan2.setRating("A");
            scan2.setHandshakeProtocol("TLSv1.2");

            response.setImportDataList(List.of(scan1, scan2));
            ImportStateMap importStateMap = new ImportStateMap();
            long newOffset = 2 + Optional.ofNullable(oldImportState).map(ImportStateMap::getOffset).orElse(0L);
            importStateMap.setOffset(newOffset);
            importStateMap.setType("SSL");
            importStateMap.setLastUpdated(new Date());

            response.setImportState(objectMapper.convertValue(
                    importStateMap, new TypeReference<Map<String, Object>>() {})
            );

            return response;
        } catch (Exception e) {
            throw new WorkflowExecutionException("Error in getDiscoveryData", e);
        }
    }

    @Override
    public Response<JsonNode> getUserDetails(JsonNode request) throws WorkflowExecutionException {
        try {
            log.info("GetUserDetails request: {}", objectMapper.writeValueAsString(request));

            FetchUsersResponse <JsonNode> response = new FetchUsersResponse<>();
            response.setAccountId("account123");
            FetchUsersResponse.User user1 = new FetchUsersResponse.User();
            user1.setEmail("discoveryUser@test.com");
            user1.setId("user123");
            user1.setUsername("discoveryUser");
            user1.setRole("admin");
            response.setUserList(List.of(user1));

            log.info("GetUserDetails response: {}", objectMapper.writeValueAsString(response));

            return response;
        } catch (Exception e) {
            throw new WorkflowExecutionException("Error in getUserDetails", e);
        }
    }

    @Override
    public Response<JsonNode> testConnection(JsonNode request) throws WorkflowExecutionException {
        try {
            log.info("TestConnection request: {}", objectMapper.writeValueAsString(request));
            TestConnectionResponse<JsonNode> response = new TestConnectionResponse<JsonNode>();
            response.setActive(true);

            log.info("TestConnection response: {}", objectMapper.writeValueAsString(response));
            return response;
        } catch (Exception e) {
            throw new WorkflowExecutionException("Error in testConnection", e);
        }
    }
}
