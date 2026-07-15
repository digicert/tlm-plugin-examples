package com.example.certdelivery;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MyCertDeliveryPluginHelperPqcTest {

    private static final List<String> DEFAULT_ALGORITHMS = List.of(
            "ML-DSA-44",
            "ML-DSA-65",
            "ML-DSA-87",
            "SLH-DSA-SHA2-128f",
            "SLH-DSA-SHA2-128s",
            "SLH-DSA-SHA2-192f",
            "SLH-DSA-SHA2-192s",
            "SLH-DSA-SHA2-256f",
            "SLH-DSA-SHA2-256s");

    private static Stream<Arguments> pqcAlgorithms() {
        String configuredAlgorithms = System.getProperty("pqc.algorithms", "").trim();
        if (!configuredAlgorithms.isEmpty()) {
            return Arrays.stream(configuredAlgorithms.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .flatMap(MyCertDeliveryPluginHelperPqcTest::buildCasesForAlgorithm);
        }

        return DEFAULT_ALGORITHMS.stream().flatMap(MyCertDeliveryPluginHelperPqcTest::buildCasesForAlgorithm);
    }

    private static Stream<Arguments> buildCasesForAlgorithm(String algorithm) {
        return Stream.of(
                Arguments.of(algorithm, algorithm, "dns-only", "test.example", "", ""),
                Arguments.of(algorithm, algorithm, "dns-ip-email", "test.example", "192.168.10.10", "admin@test.example"));
    }

    private static Stream<Arguments> noHyphenFamilyAliases() {
        return Stream.of(
                Arguments.of("MLDSA-44", "MLDSA-44"),
                Arguments.of("MLDSA-65", "MLDSA-65"),
                Arguments.of("SLHDSA-SHA2-128f", "SLHDSA-SHA2-128f"),
                Arguments.of("SLHDSA-SHA2-256s", "SLHDSA-SHA2-256s"));
    }

    private static Stream<Arguments> crossFamilyCombinations() {
        return Stream.of(
                Arguments.of("ML-DSA-44", "SLHDSA-SHA2-128f"),
                Arguments.of("MLDSA-65", "SLH-DSA-SHA2-192s"),
                Arguments.of("SLH-DSA-SHA2-128f", "MLDSA-44"),
                Arguments.of("SLHDSA-SHA2-256s", "ML-DSA-87"));
    }

    @ParameterizedTest(name = "[{index}] keyAlgo={0}, sigAlgo={1}, sanProfile={2}")
    @MethodSource("pqcAlgorithms")
    void generateCsrWithSansSupportsPqcAlgorithms(String keyAlgorithm, String signatureAlgorithm, String sanProfile,
            String dnsNames, String ipAddresses, String emails) {
        System.out.printf("[PQC-TEST] keyAlgorithm=%s signatureAlgorithm=%s sanProfile=%s dns=%s ip=%s email=%s%n",
                keyAlgorithm, signatureAlgorithm, sanProfile, dnsNames, ipAddresses, emails);

        Pair<String, String> csrAndKey = MyCertDeliveryPluginHelper.generateCSRWithSANs(
                "CN=test.example\u0000O=TestCorp\u0000C=US",
                keyAlgorithm,
                0,
                signatureAlgorithm,
                dnsNames,
                ipAddresses,
                emails);

        assertNotNull(csrAndKey);
        assertNotNull(csrAndKey.getLeft());
        assertNotNull(csrAndKey.getRight());

        assertTrue(csrAndKey.getLeft().contains("BEGIN CERTIFICATE REQUEST"),
                "CSR PEM should contain certificate request header");
        assertTrue(csrAndKey.getRight().contains("BEGIN PRIVATE KEY"),
                "Private key PEM should contain private key header");
    }

    @ParameterizedTest(name = "[{index}] cross-family keyAlgo={0} with sigAlgo={1}")
    @MethodSource("crossFamilyCombinations")
    void allowsCrossFamilyPqcKeyAndSignatureAlgorithms(String keyAlgorithm, String signatureAlgorithm) {
        Pair<String, String> csrAndKey = MyCertDeliveryPluginHelper.generateCSRWithSANs(
                "CN=test.example\u0000O=TestCorp\u0000C=US",
                keyAlgorithm,
                0,
                signatureAlgorithm,
                "test.example",
                "",
                "");


        assertNotNull(csrAndKey.getLeft());
        assertTrue(csrAndKey.getLeft().contains("BEGIN CERTIFICATE REQUEST"));
    }

    @ParameterizedTest(name = "[{index}] alias keyAlgo={0}, sigAlgo={1}")
    @MethodSource("noHyphenFamilyAliases")
    void acceptsNoHyphenFamilyAliases(String keyAlgorithm, String signatureAlgorithm) {
        Pair<String, String> csrAndKey = MyCertDeliveryPluginHelper.generateCSRWithSANs(
                "CN=test.example\u0000O=TestCorp\u0000C=US",
                keyAlgorithm,
                0,
                signatureAlgorithm,
                "test.example",
                "",
                "");

        assertNotNull(csrAndKey.getLeft());
        assertTrue(csrAndKey.getLeft().contains("BEGIN CERTIFICATE REQUEST"));
    }
}
