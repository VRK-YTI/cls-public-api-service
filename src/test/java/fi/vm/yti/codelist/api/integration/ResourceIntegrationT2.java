package fi.vm.yti.codelist.api.integration;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fi.vm.yti.codelist.api.AbstractTestBase;
import fi.vm.yti.codelist.api.PublicApiServiceApplication;
import fi.vm.yti.codelist.common.dto.CodeDTO;
import fi.vm.yti.codelist.common.dto.CodeRegistryDTO;
import fi.vm.yti.codelist.common.dto.CodeSchemeDTO;
import static fi.vm.yti.codelist.common.constants.ApiConstants.API_PATH_CODES;
import static fi.vm.yti.codelist.common.constants.ApiConstants.API_PATH_CODESCHEMES;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PublicApiServiceApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "automatedtest" })
@TestPropertySource(locations = "classpath:test-port.properties")
public class ResourceIntegrationT2 extends AbstractTestBase {

    private final TestRestTemplate restTemplate = new TestRestTemplate();
    @LocalServerPort
    private int randomServerPort;

    @Test
    public void getRegistriesTest() {
        final String apiUrl = createApiUrl(randomServerPort) + "/";
        final LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        final HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(parameters, null);
        final ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class, "");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final ObjectNode jsonObject = (ObjectNode) mapper.readTree(response.getBody());
            final ArrayNode registriesArray = (ArrayNode) jsonObject.get("results");
            assertEquals(8, registriesArray.size());
        } catch (final IOException e) {
            Assert.fail("Exception " + e);
        }
    }

    @Test
    public void getRegistryTest() {
        final String apiUrl = createApiUrl(randomServerPort) + "/" + TEST_CODEREGISTRY_CODEVALUE + "/";
        final LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        final HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(parameters, null);
        final ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class, "");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final CodeRegistryDTO codeRegistry = mapper.readValue(response.getBody(), CodeRegistryDTO.class);
            assertEquals(TEST_CODEREGISTRY_CODEVALUE, codeRegistry.getCodeValue());
        } catch (final IOException e) {
            Assert.fail("Exception " + e);
        }
    }

    @Test
    public void getCodeSchemesTest() {
        final String apiUrl = createApiUrl(randomServerPort) + "/" + TEST_CODEREGISTRY_CODEVALUE + API_PATH_CODESCHEMES + "/";
        final LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        final HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(parameters, null);
        final ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class, "");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final ObjectNode jsonObject = (ObjectNode) mapper.readTree(response.getBody());
            final ArrayNode codeSchemesArray = (ArrayNode) jsonObject.get("results");
            assertEquals(8, codeSchemesArray.size());
        } catch (final IOException e) {
            Assert.fail("Exception " + e);
        }
    }

    @Test
    public void getCodeSchemeTest() {
        final String apiUrl = createApiUrl(randomServerPort) + "/" + TEST_CODEREGISTRY_CODEVALUE + API_PATH_CODESCHEMES + "/" + TEST_CODESCHEME_CODEVALUE + "/";
        final LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        final HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(parameters, null);
        final ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class, "");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final CodeSchemeDTO codeScheme = mapper.readValue(response.getBody(), CodeSchemeDTO.class);
            assertEquals(TEST_CODESCHEME_CODEVALUE, codeScheme.getCodeValue());
        } catch (final IOException e) {
            Assert.fail("Exception " + e);
        }
    }

    @Test
    public void getCodesTest() {
        final String apiUrl = createApiUrl(randomServerPort) + "/" + TEST_CODEREGISTRY_CODEVALUE + API_PATH_CODESCHEMES + "/" + TEST_CODESCHEME_CODEVALUE + API_PATH_CODES + "/";
        final LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        final HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(parameters, null);
        final ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class, "");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final ObjectNode jsonObject = (ObjectNode) mapper.readTree(response.getBody());
            final ArrayNode codesArray = (ArrayNode) jsonObject.get("results");
            assertEquals(8, codesArray.size());
        } catch (final IOException e) {
            Assert.fail("Exception " + e);
        }
    }

    @Test
    public void getCodeTest() {
        final String apiUrl = createApiUrl(randomServerPort) + "/" + TEST_CODEREGISTRY_CODEVALUE + API_PATH_CODESCHEMES + "/" + TEST_CODESCHEME_CODEVALUE + API_PATH_CODES + "/" + TEST_CODE_CODEVALUE + "/";
        final LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        final HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(parameters, null);
        final ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class, "");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final CodeDTO code = mapper.readValue(response.getBody(), CodeDTO.class);
            assertEquals(TEST_CODE_CODEVALUE, code.getCodeValue());
        } catch (final IOException e) {
            Assert.fail("Exception " + e);
        }
    }
}
