package fi.vm.yti.codelist.api.resource;

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

import fi.vm.yti.codelist.api.AbstractTestBase;
import fi.vm.yti.codelist.api.PublicApiServiceApplication;
import static fi.vm.yti.codelist.common.constants.ApiConstants.API_PATH_VERSION;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PublicApiServiceApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("automatedtest")
@TestPropertySource(locations = { "classpath:unit-test-port.properties" })
public class VersionResourceTest extends AbstractTestBase {

    private final TestRestTemplate restTemplate = new TestRestTemplate();
    @LocalServerPort
    private int randomServerPort;

    @Test
    public void testVersionRequest() {
        final String apiUrl = createApiUrlWithoutVersion(randomServerPort, API_PATH_VERSION) + "/";
        final HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(null, null);
        final ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }
}
