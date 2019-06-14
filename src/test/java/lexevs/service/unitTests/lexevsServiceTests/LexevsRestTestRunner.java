package lexevs.service.unitTests.lexevsServiceTests;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test harness for LexEVS Service (CTS2/REST) API
 */
public class LexevsRestTestRunner extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public LexevsRestTestRunner( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( LexevsRestTestRunner.class );
    }
    
    protected void setUp() throws Exception {
    	
    	// Default the URL to prod, stage, ...
		RestAssured.baseURI = "https://lexevs65cts2.nci.nih.gov";
		
		// Default the base path that is appended to the base URL
    	RestAssured.basePath = "/lexevscts2";
	}
	
    //*********************************************************************
 	// service
 	//*********************************************************************
	public final void test_service_call() {
		
		RestAssured.
			when().
				get("/service?format=json").
			then().
				statusCode(200).
				body("BaseService.serviceName", hasToString("CTS2 Development Framework RESTWebApp"),
 					 "BaseService.supportedProfile.structuralProfile[0]", hasToString("SP_ENTITY_DESCRIPTION"),
					 "BaseService.serviceVersion", hasToString("1.3.6.FINAL"),					 
					 "BaseService.supportedProfile.find { it.structuralProfile == 'SP_ENTITY_DESCRIPTION' }.functionalProfile.content", hasItems("FP_QUERY"),
					 "BaseService.supportedProfile.find { it.structuralProfile == 'SP_CODE_SYSTEM_VERSION' }.functionalProfile.content", hasItems("FP_QUERY"),
					 "BaseService.supportedProfile.find { it.structuralProfile == 'SP_ASSOCIATION' }.functionalProfile.content", hasItems("FP_QUERY"));
	}
	
	//*********************************************************************
	// codeSystemVersion
	//*********************************************************************
	public final void test_codeSystemVersion_call() {
		
		RestAssured.
			when().
				get("/codesystemversions?format=json").
			then().
				assertThat().
					statusCode(200).
					body("CodeSystemVersionCatalogEntryDirectory.complete", hasToString("COMPLETE"),
  						 "CodeSystemVersionCatalogEntryDirectory.numEntries", equalTo(50),
						 "CodeSystemVersionCatalogEntryDirectory.entry.codeSystemVersionName[0]", equalTo("MDR-13.0"),
						 "CodeSystemVersionCatalogEntryDirectory.entry.codeSystemVersionName[25]", equalTo("RadLex-3.14"),
					     "CodeSystemVersionCatalogEntryDirectory.entry.codeSystemVersionName[49]", equalTo("NCI_Thesaurus-19.05d"));
	}
	
	//*********************************************************************
	// codeSystemVersion - search "thesaurus"
	//*********************************************************************
	public final void test_codeSystemVersion_search_call() {
		
		RestAssured.
			when().
				get("/codesystemversions?matchvalue=thesaurus&filtercomponent=resourceSynopsis&format=json").
			then().
				assertThat().
					statusCode(200).
					body("CodeSystemVersionCatalogEntryDirectory.complete", hasToString("COMPLETE"),
  						 "CodeSystemVersionCatalogEntryDirectory.numEntries", equalTo(7),
  						 "CodeSystemVersionCatalogEntryDirectory.entry.find { it.codeSystemVersionName == 'UMLS_SemNet-3.2' }.versionOf.content", equalTo("UMLS_SemNet"),
  						 "CodeSystemVersionCatalogEntryDirectory.entry.find { it.codeSystemVersionName == 'NCI_Thesaurus-19.02d' }.versionOf.content", equalTo("NCI_Thesaurus"),
  						 "CodeSystemVersionCatalogEntryDirectory.entry.find { it.codeSystemVersionName == 'NCI Metathesaurus-201808' }.versionOf.content", equalTo("NCI Metathesaurus"));
	}
	
	//*********************************************************************
	// codeSystemVersion - search exact match on "NCI_Thesaurus-19.05d"
	//*********************************************************************
	public final void test_codeSystemVersion_search_exact_match_call() {
		
		RestAssured.
			when().
				get("/codesystemversions?matchvalue=NCI_Thesaurus-19.05d&filtercomponent=resourceName&matchalgorithm=exactMatch&format=json").
			then().
				assertThat().
					statusCode(200).
					body("CodeSystemVersionCatalogEntryDirectory.complete", hasToString("COMPLETE"),
  						 "CodeSystemVersionCatalogEntryDirectory.numEntries", equalTo(1),
  						 "CodeSystemVersionCatalogEntryDirectory.entry.find { it.codeSystemVersionName == 'NCI_Thesaurus-19.05d' }.versionOf.content", equalTo("NCI_Thesaurus"),
  					   	 "CodeSystemVersionCatalogEntryDirectory.entry.find { it.codeSystemVersionName == 'NCI_Thesaurus-19.05d' }.resourceSynopsis.value", equalTo("NCI Thesaurus"),
  						 "CodeSystemVersionCatalogEntryDirectory.entry.find { it.codeSystemVersionName == 'NCI_Thesaurus-19.05d' }.documentURI", equalTo("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#19.05d"));
	}
	
	
	//*********************************************************************
	// codeSystem - retrieve exact match on "NCI_Thesaurus-19.05d"
	//*********************************************************************
	public final void test_codeSystem_read_call() {
		
		RestAssured.
			when().
				get("/codesystem/NCI_Thesaurus/version/19.05d?format=json").
			then().
				assertThat().
					statusCode(200).
					rootPath("CodeSystemVersionCatalogEntryMsg.codeSystemVersionCatalogEntry").
					body("codeSystemVersionName", is("NCI_Thesaurus-19.05d"),
						 "officialResourceVersionId", is("19.05d"),
						 "about", is("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#19.05d"),
						 "formalName", is("NCI Thesaurus"),
						 "state", is("FINAL"),
						 "entryState", is("ACTIVE"),
						 "sourceAndNotation.sourceAndNotationDescription", is("LexEVS"),
						 "resourceSynopsis.value", is("NCI Thesaurus"));
	}
	
	//*********************************************************************
	// codeSystem - read entities - only 50 from NCI_Thesaurus-19.05d
	//*********************************************************************
	public final void test_codeSystem_search_call() {
		
		RestAssured.
			when().
				get("codesystem/NCI_Thesaurus/version/19.05d/entities?maxtoreturn=50&format=json").
			then().
				assertThat().
					statusCode(200).					
					body("EntityDirectory.complete", is("PARTIAL"),
						 "EntityDirectory.numEntries", is(50),
						 "EntityDirectory.entry.find { it.about == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C17998' }.name.name", equalTo("C17998"),
						 "EntityDirectory.entry.find { it.about == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C855' }.name.name", equalTo("C855"),
						 "EntityDirectory.entry.find { it.about == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C5964' }.name.name", equalTo("C5964"));
	}
		
	//*********************************************************************
	// codesystem entities - read
	//*********************************************************************
	public final void test_entities_read_call() {
		
		RestAssured.
			when().
				get("codesystem/NCI_Thesaurus/version/19.03d/entity/ncit:C938?format=json").
			then().
				assertThat().
					statusCode(200).
					body("EntityDescriptionMsg.entityDescription.namedEntity.entryState", hasToString("ACTIVE"),
						 "EntityDescriptionMsg.entityDescription.namedEntity.about", hasToString("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C938"),	
						 "EntityDescriptionMsg.entityDescription.namedEntity.entityID.namespace", hasToString("ncit"),
						 "EntityDescriptionMsg.entityDescription.namedEntity.entityID.name", hasToString("C938"),
						 "EntityDescriptionMsg.entityDescription.namedEntity.entityID.name", hasToString("C938"),
						 "EntityDescriptionMsg.entityDescription.namedEntity.describingCodeSystemVersion.version.content", hasToString("NCI_Thesaurus-19.03d"),
						 "EntityDescriptionMsg.entityDescription.namedEntity.describingCodeSystemVersion.codeSystem.content", hasToString("NCI_Thesaurus"),
						 "EntityDescriptionMsg.entityDescription.namedEntity.describingCodeSystemVersion.codeSystem.uri", hasToString("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#"));
	}
	
	
	//*********************************************************************
	// entities - search (all)
	//*********************************************************************
	public final void test_entities_search_all_call() {
		
		RestAssured.
			when().
				get("/entities?maxtoreturn=50&format=json").
			then().
				assertThat().
					statusCode(200).
					body("EntityDirectory.complete", hasToString("PARTIAL"),
  						 "EntityDirectory.numEntries", equalTo(50));
	}
	
	//*********************************************************************
	// entities - search for 
	//*********************************************************************
	public final void test_entities_search_specific_call() {
		
		RestAssured.
			when().
				get("/entities?matchvalue=heart&filtercomponent=resourceSynopsis&matchalgorithm=luceneQuery&maxtoreturn=50&format=json").
			then().
				assertThat().
					statusCode(200).
					body("EntityDirectory.complete", hasToString("PARTIAL"),
  						 "EntityDirectory.numEntries", equalTo(50),
  						 "EntityDirectory.entry.name.namespace[0]", equalTo("ns1363824265"),
  						 "EntityDirectory.entry.name.name[0]", equalTo("C0018798"),
  						 "EntityDirectory.entry.name.namespace[49]", equalTo("ns1363824265"),
 						 "EntityDirectory.entry.name.name[49]", equalTo("C0018827"));
	}
		
	//*********************************************************************
	// associations - children 
	//*********************************************************************
	public final void test_assoacitions_children_call() {
		
		RestAssured.
			when().
				get("/codesystem/NCI_Thesaurus/version/19.05d/entity/ncit:C1648/children?format=json").
			then().
				assertThat().
					statusCode(200).
					body("EntityDirectory.complete", hasToString("COMPLETE"),
  						 "EntityDirectory.numEntries", equalTo(4),
  						 "EntityDirectory.entry.find { it.about == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C29982' }.name.name", equalTo("C29982"),
  						 "EntityDirectory.entry.find { it.about == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C29982' }.name.namespace", equalTo("ncit"),
  						 "EntityDirectory.entry.find { it.about == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C1977' }.name.name", equalTo("C1977"),
  						 "EntityDirectory.entry.find { it.about == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C1977' }.name.namespace", equalTo("ncit"));
	}
	
	//*********************************************************************
	// associations - subjectof 
	//*********************************************************************
	public final void test_assoacitions_subjectof_call() {
		
		RestAssured.
			when().
				get("/codesystem/NCI_Thesaurus/version/19.05d/entity/ncit:C875/subjectof?format=json").
			then().
				assertThat().
					statusCode(200).
					body("AssociationDirectory.complete", hasToString("COMPLETE"),
  						 "AssociationDirectory.numEntries", equalTo(7),
  						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C875' }.subject.name", equalTo("C875"),
  						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C875' }.subject.namespace", equalTo("ncit"),
  						   						 
  						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C157711' }.subject.name", equalTo("C157711"),
 						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C157711' }.predicate.name", equalTo("subClassOf"),
 						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C157711' }.target.entity.namespace", equalTo("ncit"),
						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C157711' }.target.entity.name", equalTo("C292"),
						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C157711' }.assertedBy.codeSystem.content", equalTo("NCI_Thesaurus"),
						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C157711' }.assertedBy.codeSystem.uri", equalTo("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#"));
	}
	
	
	//*********************************************************************
	// associations - targetof 
	//*********************************************************************
	public final void test_assoacitions_targetof_call() {
		
		RestAssured.
			when().
				get("/codesystem/NCI_Thesaurus/version/19.05d/entity/ncit:C128784/targetof?maxtoreturn=50&format=json").
			then().
				assertThat().
					statusCode(200).
					body("AssociationDirectory.complete", hasToString("PARTIAL"),
  						 "AssociationDirectory.numEntries", equalTo(50),
  						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C128784' }.subject.name", equalTo("C128784"),
  						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C128784' }.subject.namespace", equalTo("ncit"),
  						   						 
  						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C105150' }.subject.name", equalTo("C105150"),
 						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C105150' }.predicate.name", equalTo("Concept_In_Subset"),
 						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C105150' }.target.entity.namespace", equalTo("ncit"),
						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C105150' }.target.entity.name", equalTo("C119740"),
						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C105150' }.assertedBy.codeSystem.content", equalTo("NCI_Thesaurus"),
						 "AssociationDirectory.entry.find { it.subject.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C105150' }.assertedBy.codeSystem.uri", equalTo("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#"));
	}
	
	
	//*********************************************************************
	// valuesets - search (all)
	//*********************************************************************
	public final void test_valuesets_all_call() {
			
		RestAssured.
			when().
				get("/valuesets?maxtoreturn=50&format=json").
			then().
				assertThat().
					statusCode(200).
					body("ValueSetCatalogEntryDirectory.complete", hasToString("PARTIAL"),
  						 "ValueSetCatalogEntryDirectory.numEntries", equalTo(50),
  						 
  						 "ValueSetCatalogEntryDirectory.entry.find { it.valueSetName == 'NCIt Neoplasm Core Terminology' }.about", equalTo("http://evs.nci.nih.gov/valueset/C126659"),
  						 "ValueSetCatalogEntryDirectory.entry.find { it.valueSetName == 'NCIt Neoplasm Core Terminology' }.currentDefinition.valueSetDefinition.content", equalTo("ed0a1ca6"),
  					  	 "ValueSetCatalogEntryDirectory.entry.find { it.valueSetName == 'NCIt Neoplasm Core Terminology' }.currentDefinition.valueSetDefinition.uri", equalTo("http://evs.nci.nih.gov/valueset/C126659"),
  						 
		                 "ValueSetCatalogEntryDirectory.entry.find { it.valueSetName == 'CDISC Questionnaire AIMS Test Code Terminology' }.about", equalTo("http://evs.nci.nih.gov/valueset/CDISC/C101805"),
			             "ValueSetCatalogEntryDirectory.entry.find { it.valueSetName == 'CDISC Questionnaire AIMS Test Code Terminology' }.currentDefinition.valueSetDefinition.content", equalTo("d97ef78d"),
		  	             "ValueSetCatalogEntryDirectory.entry.find { it.valueSetName == 'CDISC Questionnaire AIMS Test Code Terminology' }.currentDefinition.valueSetDefinition.uri", equalTo("http://evs.nci.nih.gov/valueset/CDISC/C101805"));
	 }
	
	//*********************************************************************
	// valuesets - read
	//*********************************************************************
	public final void test_valuesets_read_call() {
			
		RestAssured.
			when().
				get("/valueset/FDA Terminology?format=json").
			then().
				assertThat().
					statusCode(200).
					body("ValueSetCatalogEntryMsg.valueSetCatalogEntry.valueSetName", equalTo("FDA Terminology"),
  						 "ValueSetCatalogEntryMsg.valueSetCatalogEntry.currentDefinition.valueSetDefinition.uri", equalTo("http://evs.nci.nih.gov/valueset/FDA/C131123"),
  						 "ValueSetCatalogEntryMsg.valueSetCatalogEntry.currentDefinition.valueSet.content", equalTo("FDA Terminology"),
  						 "ValueSetCatalogEntryMsg.valueSetCatalogEntry.entryState", equalTo("ACTIVE"));
  	}
	
	//*********************************************************************
	// valuesets - search resourceName, exact match
	//*********************************************************************
	public final void test_valuesets_search_resourceName_exactMatch_call() {
			
		RestAssured.
			when().
				get("/valuesets?matchvalue=GAIA Terminology&filtercomponent=resourceName&matchalgorithm=exactMatch&format=json").
			then().
				assertThat().
					statusCode(200).
					body("ValueSetCatalogEntryDirectory.complete", hasToString("COMPLETE"),
  						 "ValueSetCatalogEntryDirectory.numEntries", equalTo(1),
  						 
  						 "ValueSetCatalogEntryDirectory.entry.find { it.valueSetName == 'GAIA Terminology' }.about", equalTo("http://evs.nci.nih.gov/valueset/GAIA/C125481"),
  						 "ValueSetCatalogEntryDirectory.entry.find { it.valueSetName == 'GAIA Terminology' }.currentDefinition.valueSetDefinition.content", equalTo("dbaa9b00"),
  					  	 "ValueSetCatalogEntryDirectory.entry.find { it.valueSetName == 'GAIA Terminology' }.currentDefinition.valueSetDefinition.uri", equalTo("http://evs.nci.nih.gov/valueset/GAIA/C125481"));
	 }
		
				
	//*********************************************************************
	// valuesets - search resourceSynopsis, contains
	//*********************************************************************
	public final void test_valuesets_search_resourceSynopsis_contains_call() {
			
		RestAssured.
			when().
				get("/valuesets?matchvalue=diabetes&filtercomponent=resourceSynopsis&maxtoreturn=50&matchalgorithm=contains&format=json").
			then().
				assertThat().
					statusCode(200).
					body("ValueSetCatalogEntryDirectory.complete", hasToString("COMPLETE"),
  						 "ValueSetCatalogEntryDirectory.numEntries", equalTo(2),
  						 
  						 "ValueSetCatalogEntryDirectory.entry.find { it.valueSetName == 'CDISC SDTM Diabetes Therapy Terminology' }.about", equalTo("http://evs.nci.nih.gov/valueset/CDISC/C101857"),
  						 "ValueSetCatalogEntryDirectory.entry.find { it.valueSetName == 'CDISC SDTM Diabetes Therapy Terminology' }.currentDefinition.valueSetDefinition.content", equalTo("4a0762e4"),
  					  	 "ValueSetCatalogEntryDirectory.entry.find { it.valueSetName == 'CDISC SDTM Diabetes Therapy Terminology' }.currentDefinition.valueSetDefinition.uri", equalTo("http://evs.nci.nih.gov/valueset/CDISC/C101857"),
  						 
		                 "ValueSetCatalogEntryDirectory.entry.find { it.valueSetName == 'GAIA Gestational Diabetes Mellitus Level of Diagnostic Certainty Terminology' }.about", equalTo("http://evs.nci.nih.gov/valueset/GAIA/C128709"),
			             "ValueSetCatalogEntryDirectory.entry.find { it.valueSetName == 'GAIA Gestational Diabetes Mellitus Level of Diagnostic Certainty Terminology' }.currentDefinition.valueSetDefinition.content", equalTo("ed84ffbe"),
		  	             "ValueSetCatalogEntryDirectory.entry.find { it.valueSetName == 'GAIA Gestational Diabetes Mellitus Level of Diagnostic Certainty Terminology' }.currentDefinition.valueSetDefinition.uri", equalTo("http://evs.nci.nih.gov/valueset/GAIA/C128709"));
	 }
	
	//*********************************************************************
	// valuesets - search resourceSynopsis, exact match
	//*********************************************************************
	public final void test_valuesets_search_resourceSynopsis_exactMatch_call() {
			
//		https://lexevs65cts2.nci.nih.gov/lexevscts2/valuesets?matchvalue=Microsoft&filtercomponent=resourceSynopsis&matchalgorithm=contains&format=json
//		should return this value set definition (has Micorsoft in the resourceSynopsis)
//				https://lexevs65cts2.nci.nih.gov/lexevscts2/valueset/FDA%20Individual%20Case%20Safety%20Report%20Terminology/definition/21e211c5
		
//      Appears the the value set search only searches the resourceName and NOT the resourceSynopsis (even though it is specified)
			
//		RestAssured.
//			when().
//				get("/valuesets?matchvalue=Microsoft&filtercomponent=resourceSynopsis&matchalgorithm=contains&format=json").
//			then().
//				assertThat().
//					statusCode(200).
//					body("ValueSetCatalogEntryDirectory.complete", hasToString("COMPLETE"),
//  						 "ValueSetCatalogEntryDirectory.numEntries", equalTo(1));
	  }
	
	//*********************************************************************
	// valuesets - resolve 
	//*********************************************************************
	public final void test_valuesets_resolve_call() {
			
		RestAssured.
			when().
				get("/valueset/FDA Terminology/definition/e32e022f?format=json").
			then().
				assertThat().
					statusCode(200).
					body("ValueSetDefinitionMsg.valueSetDefinition.definedValueSet.content", equalTo("FDA Terminology"),
					  	 "ValueSetDefinitionMsg.valueSetDefinition.documentURI", equalTo("http://evs.nci.nih.gov/valueset/FDA/C131123"),
						 "ValueSetDefinitionMsg.valueSetDefinition.state", equalTo("FINAL"),
						 "ValueSetDefinitionMsg.valueSetDefinition.entryState", equalTo("ACTIVE"),
						 "ValueSetDefinitionMsg.valueSetDefinition.entry[0].operator", equalTo("UNION"),
						 "ValueSetDefinitionMsg.valueSetDefinition.entry[0].associatedEntities.referencedEntity.uri", equalTo("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C131123"));
	 }
		
	
	//*********************************************************************
	// valuesets - resolved valuesets call 
	//*********************************************************************
	public final void test_valuesets_resolved_call() {
			
		RestAssured.
			when().
				get("/resolvedvaluesets?maxtoreturn=50&format=json").
			then().
				assertThat().
					statusCode(200).
					body("ResolvedValueSetDirectory.complete", equalTo("PARTIAL"),
						 "ResolvedValueSetDirectory.numEntries", equalTo(50),
						
						 "ResolvedValueSetDirectory.entry.find { it.resolvedValueSetURI == 'http://evs.nci.nih.gov/valueset/CDISC/C100110' }.resolvedHeader.resolutionOf.valueSetDefinition.content", equalTo("7626ea9"),
						 "ResolvedValueSetDirectory.entry.find { it.resolvedValueSetURI == 'http://evs.nci.nih.gov/valueset/CDISC/C100110' }.resolvedHeader.resolvedUsingCodeSystem[0].codeSystem.content", equalTo("NCI_Thesaurus"),
						 
						 "ResolvedValueSetDirectory.entry.find { it.resolvedValueSetURI == 'http://evs.nci.nih.gov/valueset/CDISC/C100176' }.resolvedHeader.resolutionOf.valueSetDefinition.content", equalTo("b85b6c1a"),
						 "ResolvedValueSetDirectory.entry.find { it.resolvedValueSetURI == 'http://evs.nci.nih.gov/valueset/CDISC/C100176' }.resolvedHeader.resolvedUsingCodeSystem[0].codeSystem.content", equalTo("NCI_Thesaurus"));
	 }
	
	//*********************************************************************
	// valuesets - resolution of a value set definition
	//*********************************************************************
	public final void test_valuesets_resolution_call() {
			
		RestAssured.
			when().
				get("/valueset/CDISC Questionnaire NPI Test Name Terminology/definition/e31ccb56/resolution/1?format=json").
			then().
				assertThat().
					statusCode(200);
	 }
	
	//*********************************************************************
	// mapversions - read all
	//*********************************************************************
	public final void test_mapversions_all_call() {
			
		RestAssured.
			when().
				get("/mapversions?format=json").
			then().
				assertThat().
					statusCode(200).
					body("MapVersionDirectory.complete", equalTo("COMPLETE"),
						 "MapVersionDirectory.numEntries", equalTo(7),
						 "MapVersionDirectory.entry.find { it.mapVersionName == 'MA_to_NCIt_Mapping-1.0' }.versionOf.content", equalTo("MA_to_NCIt_Mapping"),
						 "MapVersionDirectory.entry.find { it.mapVersionName == 'MA_to_NCIt_Mapping-1.0' }.formalName", equalTo("MA_to_NCIt_Mapping"),
						 
						 "MapVersionDirectory.entry.find { it.mapVersionName == 'NCIt_to_HGNC_Mapping-1.0' }.versionOf.content", equalTo("NCIt_to_HGNC_Mapping"),
						 "MapVersionDirectory.entry.find { it.mapVersionName == 'NCIt_to_HGNC_Mapping-1.0' }.formalName", equalTo("NCIt_to_HGNC_Mapping"));
	 }
				
	//*********************************************************************
	// mapversions - search resourceName
	//*********************************************************************
	public final void test_mapversions_search_resource_name_call() {
			
		RestAssured.
			when().
				get("/mapversions?matchvalue=GO&filtercomponent=resourceName&format=json").
			then().
				assertThat().
					statusCode(200).
					body("MapVersionDirectory.complete", equalTo("COMPLETE"),
						 "MapVersionDirectory.numEntries", equalTo(1),
						 "MapVersionDirectory.entry.find { it.mapVersionName == 'GO_to_NCIt_Mapping-1.1' }.versionOf.content", equalTo("GO_to_NCIt_Mapping"),
						 "MapVersionDirectory.entry.find { it.mapVersionName == 'GO_to_NCIt_Mapping-1.1' }.formalName", equalTo("GO_to_NCIt_Mapping"));
	 }
	
	//*********************************************************************
	// mapversions - search resourceSynopsis
	//*********************************************************************
	public final void test_mapversions_search_resource_synopsis_call() {
			
		RestAssured.
			when().
				get("/mapversions?matchvalue=NCIT&filtercomponent=resourceSynopsis&format=json").
			then().
				assertThat().
					statusCode(200).
					body("MapVersionDirectory.complete", equalTo("COMPLETE"),
						 "MapVersionDirectory.numEntries", equalTo(4),
						 "MapVersionDirectory.entry.find { it.mapVersionName == 'GO_to_NCIt_Mapping-1.1' }.versionOf.content", equalTo("GO_to_NCIt_Mapping"),
						 "MapVersionDirectory.entry.find { it.mapVersionName == 'GO_to_NCIt_Mapping-1.1' }.formalName", equalTo("GO_to_NCIt_Mapping"),
						 "MapVersionDirectory.entry.find { it.mapVersionName == 'NCIt_to_HGNC_Mapping-1.0' }.versionOf.content", equalTo("NCIt_to_HGNC_Mapping"),
						 "MapVersionDirectory.entry.find { it.mapVersionName == 'NCIt_to_HGNC_Mapping-1.0' }.formalName", equalTo("NCIt_to_HGNC_Mapping"));
	 }
	
	//*********************************************************************
	// map - read specific
	//*********************************************************************
	public final void test_map_specific_call() {
			
		RestAssured.
			when().
				get("/map/GO_to_NCIt_Mapping?format=json").
			then().
				assertThat().
					statusCode(200).
					body("MapCatalogEntryMsg.map.mapName", equalTo("GO_to_NCIt_Mapping"),
						 "MapCatalogEntryMsg.map.fromCodeSystem.content", equalTo("GO"),
						 "MapCatalogEntryMsg.map.toCodeSystem.content", equalTo("NCI_Thesaurus"),
						 "MapCatalogEntryMsg.map.about", equalTo("GO_to_NCIt_Mapping"));
	 }
	
	//*********************************************************************
	// map - specific map - list versions
	//*********************************************************************
	public final void test_map_versions_of_map_call() {
			
		RestAssured.
			when().
				get("/map/NCIt_to_ChEBI_Mapping/versions?format=json").
			then().
				assertThat().
					statusCode(200).
					body("MapVersionDirectory.complete", equalTo("COMPLETE"),
						 "MapVersionDirectory.numEntries", equalTo(1),
						 "MapVersionDirectory.entry.find { it.mapVersionName == 'NCIt_to_ChEBI_Mapping-1.0' }.versionOf.content", equalTo("NCIt_to_ChEBI_Mapping"),
						 "MapVersionDirectory.entry.find { it.mapVersionName == 'NCIt_to_ChEBI_Mapping-1.0' }.formalName", equalTo("NCIt_to_ChEBI_Mapping"));
	 }
	
	//*********************************************************************
	// map - specific map and version
	//*********************************************************************
	public final void test_map_version_map_call() {
			
		RestAssured.
			when().
				get("/map/NCIt_to_ChEBI_Mapping/version/NCIt_to_ChEBI_Mapping-1.0?format=json").
			then().
				assertThat().
					statusCode(200).
					body("MapVersionMsg.mapVersion.mapVersionName", equalTo("NCIt_to_ChEBI_Mapping-1.0"),
						 "MapVersionMsg.mapVersion.documentURI", equalTo("urn:oid:NCIt_to_ChEBI_Mapping"),
						 "MapVersionMsg.mapVersion.state", equalTo("FINAL"),
						 "MapVersionMsg.mapVersion.formalName", equalTo("NCIt_to_ChEBI_Mapping"),
						 						 
						 "MapVersionMsg.mapVersion.versionOf.content", equalTo("NCIt_to_ChEBI_Mapping"),
						 "MapVersionMsg.mapVersion.fromCodeSystemVersion.codeSystem.content", equalTo("NCI_Thesaurus"),
						 "MapVersionMsg.mapVersion.toCodeSystemVersion.codeSystem.content", equalTo("chebi"),
						 
						 "MapVersionMsg.mapVersion.sourceAndNotation.sourceAndNotationDescription", equalTo("LexEVS"));
	 }
	
	//*********************************************************************
	// map - specific map and version - entities
	//*********************************************************************
	public final void test_map_version_map_entities_call() {
			
		RestAssured.
			when().
				get("/map/NCIt_to_ChEBI_Mapping/version/NCIt_to_ChEBI_Mapping-1.0/entries?maxtoreturn=50&format=json").
			then().
				assertThat().
					statusCode(200).
					body("MapEntryDirectory.complete", equalTo("PARTIAL"),
						 "MapEntryDirectory.numEntries", equalTo(50),
						 "MapEntryDirectory.entry.find { it.assertedBy.mapVersion.content == 'NCIt_to_ChEBI_Mapping-1.0' }.assertedBy.map.content", hasToString("NCIt_to_ChEBI_Mapping"),
						 "MapEntryDirectory.entry.find { it.assertedBy.mapVersion.content == 'NCIt_to_ChEBI_Mapping-1.0' }.mapFrom.name", hasToString("C1003"),
						 
						 "MapEntryDirectory.entry.find { it.mapFrom.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C101790' }.mapFrom.name", hasToString("C101790"),
						 "MapEntryDirectory.entry.find { it.mapFrom.uri == 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C1028' }.mapFrom.name", hasToString("C1028"));

	 }
	
	//*********************************************************************
	// map - specific map and version - select one entity
	//*********************************************************************
	public final void test_map_version_map_entity_call() {
			
		RestAssured.
			when().
				get("/map/NCIt_to_ChEBI_Mapping/version/NCIt_to_ChEBI_Mapping-1.0/entry/C1028?format=json").
			then().
				assertThat().
					statusCode(200).
					body("MapEntryMsg.entry.entryState", equalTo("ACTIVE"),
						 "MapEntryMsg.entry.assertedBy.mapVersion.content", equalTo("NCIt_to_ChEBI_Mapping-1.0"),
						 "MapEntryMsg.entry.assertedBy.map.content", equalTo("NCIt_to_ChEBI_Mapping"),
						 "MapEntryMsg.entry.mapFrom.uri", equalTo("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C1028"));
	 }
}
