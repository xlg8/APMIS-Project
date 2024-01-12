package de.symeda.sormas.api.campaign.form;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;
import javax.validation.Valid;

import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.CampaignCriteria;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.utils.SortProperty;

@Remote
public interface CampaignFormMetaFacade {

	CampaignFormMetaDto saveCampaignFormMeta(@Valid CampaignFormMetaDto campaignFormMetaDto);

	/**
	 * Validates the campaign form by checking whether mandatory elements are included, only supported types are used
	 * and elements used in associations are included in the schema. In addition, cleans any elements that are used
	 * in the UI from any HTML tags but those defined in {@link CampaignFormElement#ALLOWED_HTML_TAGS}.
	 */
	void validateAndClean(CampaignFormMetaDto campaignFormMetaDto);

	void validateAllFormMetas();

	CampaignFormMetaDto buildCampaignFormMetaFromJson(String formId, String languageCode, String schemaDefinitionJson, String translationsJson)
		throws IOException;

	List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferences();
	
	List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRound(String round); 
	
	List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRoundAndUserLanguage(String round, String userLanguage); 
	
	List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRoundandCampaign(String round, String campaignUUID);	
	List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignandRoundAndPashto(String round, String campaignUUID);	
	List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRoundandCampaignRoundAndDari(String round, String campaignUUID);

	List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRoundandCampaignandForm(String round, String campaignUUID, Set<FormAccess> userFormAccess);

	CampaignFormMetaDto getCampaignFormMetaByUuid(String campaignFormUuid);
	
	CampaignFormMetaReferenceDto getCampaignFormMetaReferenceByUuid(String campaignFormUuid);

	List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaign(String uuid);
	List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignAndUserLanguage(String uuid, String userLanguage);

	
	List<CampaignFormMetaReferenceDto> getCampaignFormMetaAsReferencesByCampaignIntraCamapaign(String uuid);

    List<CampaignFormMetaDto> getAllAfter(Date campaignFormMetaChangeDate);

	List<String> getAllUuids();

	List<CampaignFormMetaDto> getByUuids(List<String> uuids);

	List<CampaignFormMetaReferenceDto> getCampaignFormMetaAsReferencesByCampaignPostCamapaign(String uuid);

	Collection<CampaignFormMetaDto> getAllFormElement();
	
	List<CampaignFormMetaDto> getIndexList(CampaignFormCriteria campaignFormCriteria, Integer first, Integer max,
			List<SortProperty> sortProperties);

	long count(CampaignFormCriteria campaignFormCriteria);
//	List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRoundAndFormExpiry(String round);
	
	Date formExpiryDate(CampaignFormDataCriteria criteria);	
	
<<<<<<< HEAD
	void dearchiveForms(List<String> userUuids);
	
	void archiveForms(List<String> userUuids);
=======
	

>>>>>>> branch 'development' of https://github.com/omoluabidotcom/APMIS-Project.git
	
}
