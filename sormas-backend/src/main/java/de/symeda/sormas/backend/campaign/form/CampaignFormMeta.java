package de.symeda.sormas.backend.campaign.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.symeda.auditlog.api.Audited;
import de.symeda.auditlog.api.AuditedIgnore;
import de.symeda.sormas.api.Modality;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormTranslations;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.util.ModelConstants;

@Entity
@Audited
public class CampaignFormMeta extends AbstractDomainObject {

	private static final long serialVersionUID = -5200626281564146919L;

	public static final String TABLE_NAME = "campaignformmeta";

	public static final String FORM_ID = "formId";
	public static final String FORM_TYPE = "formType";
	public static final String DISTRICTENTRY = "districtentry";
	public static final String LANGUAGE_CODE = "languageCode";
	public static final String FORM_NAME = "formName"; 
	public static final String FORM_CATEGORY = "formCategory";
	public static final String MODALITY = "modality";
	public static final String CAMPAIGN_FORM_ELEMENTS = "campaignFormElements";
	public static final String CAMPAIGN_FORM_TRANSLATIONS = "campaignFormTranslations";
	public static final String DAYSTOEXPIRE = "daysExpired";
	public static final String FORM_NAME_PASHTO = "formname_ps_af"; 
	public static final String FORM_NAME_DARI = "formname_fa_af"; 
	public static final String ARCHIVED = "archived";

	private String formId;
	private String formType;

	private boolean districtentry = false;
	
	private FormAccess formCategory;
	private int daysExpired;
	
	private String modality;	

	private String formName;
	private String formname_ps_af;
	private String formname_fa_af;
	private String languageCode;
	private List<CampaignFormElement> campaignFormElements;
//	private List<CampaignFormElement> campaignFormElementsList;
//	private String campaignFormElements;
//	private List<CampaignFormElement> campaignFormElementsList;
	private List<CampaignFormTranslations> campaignFormTranslations;
//	private List<CampaignFormTranslations> campaignFormTranslationsList;
//	private String campaignFormTranslations;
//	private List<CampaignFormTranslations> campaignFormTranslationsList;
	private boolean archived = false;

	@Column
	public boolean getArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}
	
	@Column
	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}
	
	@Column
	public String getFormType() {
		return formType;
	}

	public void setFormType(String formType) {
		this.formType = formType;
	}

	@Enumerated(EnumType.STRING)
	@Column
	public FormAccess getFormCategory() {
		return formCategory;
	}

	public void setFormCategory(FormAccess formCategory) {
		this.formCategory = formCategory;
	}

	@Column
	public int getDaysExpired() {
		return daysExpired;
	}

	public void setDaysExpired(int daysExpired) {
		this.daysExpired = daysExpired;
	}
	
	@Column
	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	
	@Column
	public String getFormname_ps_af() {
		return formname_ps_af;
	}

	public void setFormname_ps_af(String formname_ps_af) {
		this.formname_ps_af = formname_ps_af;
	}

	@Column
	public String getFormname_fa_af() {
		return formname_fa_af;
	}

	public void setFormname_fa_af(String formname_fa_af) {
		this.formname_fa_af = formname_fa_af;
	}

	@Column
	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	@AuditedIgnore
	@Type(type = ModelConstants.HIBERNATE_TYPE_JSON)
	@Column(columnDefinition = ModelConstants.COLUMN_DEFINITION_JSON)
	public List<CampaignFormElement> getCampaignFormElements() {
		return campaignFormElements;
	}

	public void setCampaignFormElements(List<CampaignFormElement> campaignFormElements) {
		this.campaignFormElements = campaignFormElements;
//		campaignFormElementsList = null;
	}
	
//	@Lob
//	@Type(type = "org.hibernate.type.TextType")
//	public String getCampaignFormElements() {
//		return campaignFormElements;
//	}
//
//	public void setCampaignFormElements(String campaignFormElements) {
//		this.campaignFormElements = campaignFormElements;
//		campaignFormElementsList = null;
//	}

//	@Transient
//	public List<CampaignFormElement> getCampaignFormElementsList() {
//		if (campaignFormElementsList == null) {
//			if (StringUtils.isBlank(campaignFormElements)) {
//				campaignFormElementsList = new ArrayList<>();
//			} else {
//				try {
//					ObjectMapper mapper = new ObjectMapper();
//					campaignFormElementsList = Arrays.asList(mapper.readValue(campaignFormElements, CampaignFormElement[].class));
//				} catch (IOException e) {
//					System.out.println(e.getMessage());
//					throw new ValidationRuntimeException(
//						"Content of campaignFormElements could not be parsed to List<CampaignFormElement> - ID: " + getId());
//				}
//				
//			}
//		}
//		return campaignFormElementsList;
//	}
//
//	public void setCampaignFormElementsList(List<CampaignFormElement> campaignFormElementsList) {
//		this.campaignFormElementsList = campaignFormElementsList;
//
//		if (this.campaignFormElementsList == null) {
//			campaignFormElements = null;
//			return;
//		}
//
//		try {
//			ObjectMapper mapper = new ObjectMapper();
//			campaignFormElements = mapper.writeValueAsString(campaignFormElementsList);
//		} catch (JsonProcessingException e) {
//			throw new RuntimeException("Content of campaignFormElementsList could not be parsed to JSON String - ID: " + getId());
//		}
//	}

	@AuditedIgnore
	@Type(type = ModelConstants.HIBERNATE_TYPE_JSON)
	@Column(columnDefinition = ModelConstants.COLUMN_DEFINITION_JSON)
	public List<CampaignFormTranslations> getCampaignFormTranslations() {
		return campaignFormTranslations;
	}

	public void setCampaignFormTranslations(List<CampaignFormTranslations> campaignFormTranslations) {
		this.campaignFormTranslations = campaignFormTranslations;
//		campaignFormTranslationsList = null;
	}

	@Column
	public boolean isDistrictentry() {
		return districtentry;
	}

	public void setDistrictentry(boolean districtentry) {
		this.districtentry = districtentry;
	}
	
	public String getModality() {
		return modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}

//	@Transient
//	public List<CampaignFormTranslations> getCampaignFormTranslationsList() {
//		if (campaignFormTranslationsList == null) {
//			if (StringUtils.isBlank(campaignFormTranslations)) {
//				campaignFormTranslationsList = new ArrayList<>();
//			} else {
//				try {
//					ObjectMapper mapper = new ObjectMapper();
//					campaignFormTranslationsList = Arrays.asList(mapper.readValue(campaignFormTranslations, CampaignFormTranslations[].class));
//				} catch (IOException e) {
//					throw new ValidationRuntimeException(
//						"Content of campaignFormTranslations could not be parsed to List<CampaignFormTranslations> - ID: " + getId());
//				}
//			}
//		}
//		return campaignFormTranslationsList;
//	}
//
//	public void setCampaignFormTranslationsList(List<CampaignFormTranslations> campaignFormTranslationsList) {
//		this.campaignFormTranslationsList = campaignFormTranslationsList;
//
//		if (this.campaignFormTranslationsList == null) {
//			campaignFormTranslations = null;
//			return;
//		}
//
//		try {
//			ObjectMapper mapper = new ObjectMapper();
//			campaignFormTranslations = mapper.writeValueAsString(campaignFormTranslationsList);
//		} catch (JsonProcessingException e) {
//			throw new RuntimeException("Content of campaignFormTranslationsList could not be parsed to JSON String - ID: " + getId());
//		}
//	}

	public CampaignFormMetaReferenceDto toReference() {
		return new CampaignFormMetaReferenceDto(getUuid(), formName, formname_ps_af, formname_fa_af, formType, formCategory, daysExpired);
	}

	@Override
	public String toString() {
		return formName;
	}
	

}
