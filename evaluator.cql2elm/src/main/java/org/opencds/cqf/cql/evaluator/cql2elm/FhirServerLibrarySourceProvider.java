package org.opencds.cqf.cql.evaluator.cql2elm;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.evaluator.fhir.adapter.AdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.util.BundleUtil;

/**
 * This class implements the cql-translator LibrarySourceProvider API, using a FHIR
 * server as a source for Library resources containing CQL content.
 */
public class FhirServerLibrarySourceProvider extends
    VersionComparingLibrarySourceProvider {

    Logger logger = LoggerFactory.getLogger(FhirServerLibrarySourceProvider.class);

    private IGenericClient client;
    private AdapterFactory adapterFactory;
    /**
     * @param client pre-configured and authorized FHIR server client
     * @param adapterFactory factory for HL7 Structure adapters
     */
    public FhirServerLibrarySourceProvider(IGenericClient client, AdapterFactory adapterFactory) {
        super(adapterFactory);
        this.client = client;
        this.adapterFactory = adapterFactory;
    }

    protected IBaseResource getLibrary(String url) {
        try {
            return (IBaseResource)this.client.read().resource("Library").withUrl(url).elementsSubset("name", "version", "content", "type").encodedJson().execute();
        }
        catch (Exception e) {
            logger.error(String.format("error while getting library with url %s", url), e);
        }

        return null;
    }

    @Override
    public IBaseResource getLibrary(VersionedIdentifier libraryIdentifier) {
        IBaseBundle result = this.client.search().forResource("Library").elementsSubset("name", "version")
            .where(new TokenClientParam("name").exactly().code(libraryIdentifier.getId())).encodedJson().execute();

        List<? extends IBaseResource> resources = BundleUtil.toListOfResourcesOfType(this.client.getFhirContext(),
                result, this.client.getFhirContext().getResourceDefinition("Library").getImplementingClass());
                
        if (resources == null || resources.isEmpty()) {
            return null;
        }

        Collection<IBaseResource> libraries = resources.stream().map(x -> (IBaseResource)x).collect(Collectors.toList());

        IBaseResource library = this.select(libraryIdentifier, libraries);

        // This is a subsetted resource, so we get the full version here.
        if (library != null) {
            return getLibrary(this.adapterFactory.createLibrary(library).getUrl());
        }

        return null;
    }
}