package fi.vm.yti.codelist.api.resource;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;

import fi.vm.yti.codelist.api.api.ApiUtils;
import fi.vm.yti.codelist.api.api.ResponseWrapper;
import fi.vm.yti.codelist.api.domain.Domain;
import fi.vm.yti.codelist.common.model.Code;
import fi.vm.yti.codelist.common.model.CodeRegistry;
import fi.vm.yti.codelist.common.model.CodeScheme;
import fi.vm.yti.codelist.common.model.Meta;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import static fi.vm.yti.codelist.common.constants.ApiConstants.*;

/**
 * REST resources for registries, schemes and codes.
 */
@Component
@Path("/v1/coderegistries")
@Api(value = "coderegistries", description = "Operations about coderegistries, codeschemes and codes.")
@Produces("text/plain")
public class CodeRegistryResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(CodeRegistryResource.class);
    private final ApiUtils apiUtils;
    private final Domain domain;

    @Inject
    public CodeRegistryResource(final ApiUtils apiUtils,
                                final Domain domain) {
        this.apiUtils = apiUtils;
        this.domain = domain;
    }

    @GET
    @ApiOperation(value = "Return list of available CodeRegistries.", response = CodeRegistry.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns all Registers in JSON format.")
    @Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8", MediaType.TEXT_PLAIN})
    public Response getCodeRegistries(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                      @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                      @ApiParam(value = "CodeRegistry CodeValue as string value.") @QueryParam("codeValue") final String codeRegistryCodeValue,
                                      @ApiParam(value = "CodeRegistry name as string value.") @QueryParam("name") final String name,
                                      @ApiParam(value = "Format for content.") @QueryParam("format") @DefaultValue(FORMAT_JSON) final String format,
                                      @ApiParam(value = "After date filtering parameter, results will be codes with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                      @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_CODEREGISTRIES);
        if (FORMAT_CSV.equalsIgnoreCase(format)) {
            final Set<CodeRegistry> codeRegistries = domain.getCodeRegistries(pageSize, from, codeRegistryCodeValue, name, Meta.parseAfterFromString(after), null);
            return Response.ok(constructRegistersCsv(codeRegistries)).build();
        } else {
            final Meta meta = new Meta(200, null, null, after);
            ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_CODEREGISTRY, expand)));
            final Set<CodeRegistry> codeRegistries = domain.getCodeRegistries(pageSize, from, codeRegistryCodeValue, name, meta.getAfter(), meta);
            meta.setResultCount(codeRegistries.size());
            final ResponseWrapper<CodeRegistry> wrapper = new ResponseWrapper<>();
            wrapper.setResults(codeRegistries);
            wrapper.setMeta(meta);
            return Response.ok(wrapper).build();
        }
    }

    @GET
    @Path("{codeRegistryCodeValue}")
    @ApiOperation(value = "Return one specific CodeRegistry.", response = CodeRegistry.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns one specific CodeRegistry in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCodeRegistry(@ApiParam(value = "CodeRegistry CodeValue.", required = true) @PathParam("codeRegistryCodeValue") final String codeRegistryCodeValue,
                                    @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_CODEREGISTRIES + "/" + codeRegistryCodeValue + "/");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_CODEREGISTRY, expand)));
        final CodeRegistry codeRegistry = domain.getCodeRegistry(codeRegistryCodeValue);
        if (codeRegistry != null) {
            return Response.ok(codeRegistry).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("{codeRegistryCodeValue}/codeschemes")
    @ApiOperation(value = "Return list of available CodeRegistries.", response = CodeRegistry.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns all Registers in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCodeRegistryCodeSchemes(@ApiParam(value = "CodeRegistry CodeValue.", required = true) @PathParam("codeRegistryCodeValue") final String codeRegistryCodeValue,
                                               @ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                               @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                               @ApiParam(value = "CodeRegistry PrefLabel as string value for searching.") @QueryParam("codeRegistryPrefLabel") final String codeRegistryPrefLabel,
                                               @ApiParam(value = "CodeScheme codeValue as string value for searching.") @QueryParam("codeValue") final String codeSchemeCodeValue,
                                               @ApiParam(value = "CodeScheme PrefLabel as string value for searching.") @QueryParam("prefLabel") final String codeSchemePrefLabel,
                                               @ApiParam(value = "Status enumerations in CSL format.") @QueryParam("status") @DefaultValue("VALID") final String status,
                                               @ApiParam(value = "Format for content.") @QueryParam("format") @DefaultValue(FORMAT_JSON) final String format,
                                               @ApiParam(value = "After date filtering parameter, results will be codes with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                               @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_CODEREGISTRIES + "/" + codeRegistryCodeValue + "/codeschemes/");
        final List<String> statusList = parseStatus(status);
        if (FORMAT_CSV.equalsIgnoreCase(format)) {
            final Set<CodeScheme> codeSchemes = domain.getCodeSchemes(pageSize, from, codeRegistryCodeValue, codeRegistryPrefLabel, codeSchemeCodeValue, codeSchemePrefLabel, statusList, Meta.parseAfterFromString(after), null);
            return Response.ok(constructCodeSchemesCsv(codeSchemes)).build();
        } else {
            final Meta meta = new Meta(200, null, null, after);
            ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_CODESCHEME, expand)));
            final CodeRegistry codeRegistry = domain.getCodeRegistry(codeRegistryCodeValue);
            if (codeRegistry != null) {
                final Set<CodeScheme> codeSchemes = domain.getCodeSchemes(pageSize, from, codeRegistryCodeValue, codeRegistryPrefLabel, codeSchemeCodeValue, codeSchemePrefLabel, statusList, meta.getAfter(), meta);
                meta.setResultCount(codeSchemes.size());
                final ResponseWrapper<CodeScheme> wrapper = new ResponseWrapper<>();
                wrapper.setResults(codeSchemes);
                wrapper.setMeta(meta);
                return Response.ok(wrapper).build();
            } else {
                meta.setCode(404);
                meta.setMessage("No such resource.");
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
    }

    @GET
    @Path("{codeRegistryCodeValue}/codeschemes/{codeSchemeCodeValue}")
    @ApiOperation(value = "Return one specific CodeScheme.", response = CodeScheme.class)
    @ApiResponse(code = 200, message = "Returns one specific CodeScheme in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCodeRegistryCodeScheme(@ApiParam(value = "CodeRegistry CodeValue.", required = true) @PathParam("codeRegistryCodeValue") final String codeRegistryCodeValue,
                                              @ApiParam(value = "CodeScheme CodeValue.", required = true) @PathParam("codeSchemeCodeValue") final String codeSchemeCodeValue,
                                              @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_CODEREGISTRIES + "/" + codeRegistryCodeValue + "/codeschemes/" + codeSchemeCodeValue + "/");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_CODESCHEME, expand)));
        final CodeRegistry codeRegistry = domain.getCodeRegistry(codeRegistryCodeValue);
        if (codeRegistry != null) {
            final CodeScheme codeScheme = domain.getCodeScheme(codeRegistryCodeValue, codeSchemeCodeValue);
            return Response.ok(codeScheme).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("{codeRegistryCodeValue}/codeschemes/{codeSchemeCodeValue}/codes")
    @ApiOperation(value = "Return content listing of one register.", response = Code.class)
    @ApiResponse(code = 200, message = "Returns a register matching code in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCodeRegistryCodeSchemeCodes(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                                   @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                                   @ApiParam(value = "CodeRegistry CodeValue.", required = true) @PathParam("codeRegistryCodeValue") final String codeRegistryCodeValue,
                                                   @ApiParam(value = "CodeScheme CodeValue.", required = true) @PathParam("codeSchemeCodeValue") final String codeSchemeCodeValue,
                                                   @ApiParam(value = "Code code.") @QueryParam("codeValue") final String codeCodeValue,
                                                   @ApiParam(value = "Code PrefLabel.") @QueryParam("prefLabel") final String prefLabel,
                                                   @ApiParam(value = "Status enumerations in CSL format.") @QueryParam("status") @DefaultValue("VALID") final String status,
                                                   @ApiParam(value = "Format for content.") @QueryParam("format") @DefaultValue(FORMAT_JSON) final String format,
                                                   @ApiParam(value = "After date filtering parameter, results will be codes with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                                   @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_CODEREGISTRIES + "/" + codeRegistryCodeValue + "/codeschemes/" + codeSchemeCodeValue + "/codes/");
        final List<String> statusList = parseStatus(status);
        if (FORMAT_CSV.equalsIgnoreCase(format)) {
            final Set<Code> codes = domain.getCodes(pageSize, from, codeRegistryCodeValue, codeSchemeCodeValue, codeCodeValue, prefLabel, statusList, Meta.parseAfterFromString(after), null);
            return Response.ok(constructCodesCsv(codes)).build();
        } else {
            final Meta meta = new Meta(Response.Status.OK.getStatusCode(), pageSize, from, after);
            ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_CODE, expand)));
            final CodeScheme codeScheme = domain.getCodeScheme(codeRegistryCodeValue, codeSchemeCodeValue);
            if (codeScheme != null) {
                final Set<Code> codes = domain.getCodes(pageSize, from, codeRegistryCodeValue, codeSchemeCodeValue, codeCodeValue, prefLabel, statusList, meta.getAfter(), meta);
                if (pageSize != null && from + pageSize < meta.getTotalResults()) {
                    meta.setNextPage(apiUtils.createNextPageUrl(API_VERSION, API_PATH_CODEREGISTRIES, after, pageSize, from + pageSize));
                }
                final ResponseWrapper<Code> wrapper = new ResponseWrapper<>();
                wrapper.setMeta(meta);
                if (codes == null) {
                    meta.setCode(404);
                    meta.setMessage("No such resource.");
                    return Response.status(Response.Status.NOT_FOUND).entity(wrapper).build();
                }
                wrapper.setResults(codes);
                return Response.ok(wrapper).build();
            } else {
                meta.setCode(404);
                meta.setMessage("No such resource.");
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
    }

    @GET
    @Path("{codeRegistryCodeValue}/codeschemes/{codeSchemeCodeValue}/codes/{codeCodeValue}")
    @ApiOperation(value = "Return one code from specific codescheme under specific coderegistry.", response = Code.class)
    @ApiResponse(code = 200, message = "Returns one registeritem from specific register in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCodeRegistryCodeSchemeCode(@ApiParam(value = "CodeRegistry CodeValue.", required = true) @PathParam("codeRegistryCodeValue") final String codeRegistryCodeValue,
                                                  @ApiParam(value = "CodeScheme CodeValue.", required = true) @PathParam("codeSchemeCodeValue") final String codeSchemeCodeValue,
                                                  @ApiParam(value = "Code code.", required = true) @PathParam("codeCodeValue") final String codeCodeValue,
                                                  @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_CODEREGISTRIES + "/" + codeRegistryCodeValue + "/codeschemes/" + codeSchemeCodeValue + "/codes/" + codeCodeValue);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_CODE, expand)));
        final Code code = domain.getCode(codeRegistryCodeValue, codeSchemeCodeValue, codeCodeValue);
        if (code == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(code).build();
    }
}
