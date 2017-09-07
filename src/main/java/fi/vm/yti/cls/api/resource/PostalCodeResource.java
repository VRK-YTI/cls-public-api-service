package fi.vm.yti.cls.api.resource;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import fi.vm.yti.cls.api.api.ApiConstants;
import fi.vm.yti.cls.api.api.ApiUtils;
import fi.vm.yti.cls.api.api.ErrorWrapper;
import fi.vm.yti.cls.api.api.ListResponseWrapper;
import fi.vm.yti.cls.api.domain.Domain;
import fi.vm.yti.cls.common.model.Meta;
import fi.vm.yti.cls.common.model.Municipality;
import fi.vm.yti.cls.common.model.PostManagementDistrict;
import fi.vm.yti.cls.common.model.PostalCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * REST resources for postalcodes.
 */
@Component
@Path("/v1/postalcodes")
@Api(value = "postalcodes", description = "Operations about postal codes.")
@Produces("text/plain")
public class PostalCodeResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(PostalCodeResource.class);
    private final Domain domain;
    private final ApiUtils apiUtils;

    @Inject
    public PostalCodeResource(final ApiUtils apiUtils,
                              final Domain domain) {
        this.apiUtils = apiUtils;
        this.domain = domain;
    }

    @GET
    @ApiOperation(value = "Return postalcodes with query parameter filters.", response = PostalCode.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns postalcodes and metadata in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getPostalCodes(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                   @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                   @ApiParam(value = "Search parameter for code, prefix style wildcard support.") @QueryParam("codeValue") final String codeValue,
                                   @ApiParam(value = "Search parameter for codeName, prefix style wildcard support.") @QueryParam("codePrefLabel") final String codePrefLabel,
                                   @ApiParam(value = "Search parameter for codeType, prefix style wildcard support.") @QueryParam("codeType") final Integer codeType,
                                   @ApiParam(value = "Search parameter for areaCode, prefix style wildcard support.") @QueryParam("areaCode") final String areaCode,
                                   @ApiParam(value = "Search parameter for areaName, prefix style wildcard support.") @QueryParam("areaName") final String areaName,
                                   @ApiParam(value = "Search parameter for municipality code, prefix style wildcard support.") @QueryParam("municipalityCode") final String municipalityCode,
                                   @ApiParam(value = "Search parameter for municipality name, prefix style wildcard support.") @QueryParam("municipalityName") final String municipalityName,
                                   @ApiParam(value = "After date filtering parameter, results will be regions with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                   @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/postalcodes/ requested with codeValue: " + codeValue + ", codePrefLabel: " + codePrefLabel + ", codeType: " + codeType + ", municipalityCode: " + municipalityCode + ", municipalityName: " + municipalityName);
        final Meta meta = new Meta(Response.Status.OK.getStatusCode(), pageSize, from, after);
        final List<PostalCode> postalCodes = domain.getPostalCodes(pageSize, from, codeValue, codePrefLabel, codeType, areaCode, areaName, municipalityCode, municipalityName, meta.getAfter(), meta);
        if (pageSize != null && from + pageSize < meta.getTotalResults()) {
            meta.setNextPage(apiUtils.createNextPageUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_POSTALCODES, after, pageSize, from + pageSize));
        }
        final ListResponseWrapper<PostalCode> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(postalCodes);
        meta.setAfterResourceUrl(apiUtils.createAfterResourceUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_POSTALCODES, new Date(System.currentTimeMillis())));
        wrapper.setMeta(meta);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_POSTALCODE, expand)));
        return Response.ok(wrapper).build();
    }

    @GET
    @ApiOperation(value = "Return one postal code.", response = PostalCode.class)
    @ApiResponse(code = 200, message = "Returns a postal code matching code in JSON format.")
    @Path("{codeValue}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getPostalCode(@ApiParam(value = "PostalCode code.") @PathParam("codeValue") final String codeValue,
                                  @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/postalcodes/" + codeValue + "/ requested!");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_POSTALCODE, expand)));
        final PostalCode postalCode = domain.getPostalCode(codeValue);
        if (postalCode == null) {
            return createErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), ErrorWrapper.POSTALCODE_NOT_FOUND);
        }
        return Response.ok(postalCode).build();
    }

    @GET
    @ApiOperation(value = "Return one postal code.", response = PostalCode.class)
    @ApiResponse(code = 200, message = "Returns a postal code matching ID in JSON format.")
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getPostalCodeWithId(@ApiParam(value = "PostalCode id.") @PathParam("id") final String id,
                                        @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/postalcodes/id/" + id + "/ requested!");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_POSTALCODE, expand)));
        final PostalCode postalCode = domain.getPostalCodeWithId(id);
        if (postalCode == null) {
            return createErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), ErrorWrapper.POSTALCODE_NOT_FOUND);
        }
        return Response.ok(postalCode).build();
    }

    @GET
    @ApiOperation(value = "Return municipality for the postal code.", response = Municipality.class)
    @ApiResponse(code = 200, message = "Returns a municipality forching code in JSON format.")
    @Path("{codeValue}/municipality")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getPostalCodeMunicipality(@ApiParam(value = "PostalCode code.") @PathParam("codeValue") final String codeValue,
                                              @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/postalcodes/" + codeValue + "/municipality/ requested!");
        final PostalCode postalCode = domain.getPostalCode(codeValue);
        if (postalCode == null) {
            return createErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), ErrorWrapper.POSTALCODE_NOT_FOUND);
        }
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_POSTALCODE, expand)));
        final Municipality municipality = postalCode.getMunicipality();
        if (municipality == null) {
            return createErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), ErrorWrapper.MUNICIPALITY_NOT_FOUND);
        }
        return Response.ok(municipality).build();
    }

    @GET
    @ApiOperation(value = "Return postmanagementdistrict for the postal code.", response = PostManagementDistrict.class)
    @ApiResponse(code = 200, message = "Returns a postmanagementdistrict forching code in JSON format.")
    @Path("{codeValue}/postmanagementdistrict")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getPostalCodePostManagementDistrict(@ApiParam(value = "PostalCode code.") @PathParam("codeValue") final String codeValue,
                                                        @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/postalcodes/" + codeValue + "/postmanagementdistrict/ requested!");
        final PostalCode postalCode = domain.getPostalCode(codeValue);
        if (postalCode == null) {
            return createErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), ErrorWrapper.POSTALCODE_NOT_FOUND);
        }
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_POSTALCODE, expand)));
        final PostManagementDistrict postManagementDistrict = postalCode.getPostManagementDistrict();
        if (postManagementDistrict == null) {
            return createErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), ErrorWrapper.POSTMANAGEMENTDISTRICT_NOT_FOUND);
        }
        return Response.ok(postManagementDistrict).build();
    }

}
