package de.symeda.sormas.backend.campaign.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;

import de.symeda.sormas.api.Modality;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaExpiryDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaExpiryFacade;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;

@Stateless(name = "CampaignFormMetaWithExpFacade")
public class CampaignFormMetaWithExpFacadeEjb implements CampaignFormMetaExpiryFacade {

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	private boolean tray;

	@EJB
	private CampaignFormMetaWithExpiryService service;
	@EJB
	private UserService userService;

	public CampaignFormMetaExpDay fromDto(@NotNull CampaignFormMetaExpiryDto source, boolean checkChangeDate) {
		CampaignFormMetaExpDay target = DtoHelper.fillOrBuildEntity(source, service.getByUuid(source.getUuid()),
				CampaignFormMetaExpDay::new, checkChangeDate);

		System.out.println(
				"dssssssssssssssefaasdgasdgasdgasdfasdfasdfasfeasfdasdfs " + service.getByUuid(source.getUuid()));

		target.setFormId(source.getFormId());
		target.setCampaignId(source.getCampaignId());
		target.setExpiryDay(source.getExpiryDay().intValue());
		
		

		return target;
	}

	public static CampaignFormMetaExpiryDto toDto(CampaignFormMetaExpDay source) {
		if (source == null) {
			return null;
		}

		CampaignFormMetaExpiryDto target = new CampaignFormMetaExpiryDto();
		DtoHelper.fillDto(target, source);

		target.setFormId(source.getFormId());
		target.setCampaignId(source.getCampaignId());
		
		

		return target;
	}

//	@Override
//	public CampaignFormMetaDto saveCampaignFormMeta(@Valid CampaignFormMetaDto campaignFormMetaDto)
//			throws ValidationRuntimeException {
//		validateAndClean(campaignFormMetaDto);
//
//		CampaignFormMeta campaignFormMeta = fromDto(campaignFormMetaDto, true);
//		service.ensurePersisted(campaignFormMeta);
//		return toDto(campaignFormMeta);
//	}

//	@Override
//	public CampaignFormMetaDto buildCampaignFormMetaFromJson(String formId, String languageCode,
//			String schemaDefinitionJson, String translationsJson) throws IOException {
//		CampaignFormMetaDto campaignForm = new CampaignFormMetaDto();
//		campaignForm.setFormId(formId);
//		campaignForm.setLanguageCode(languageCode);
//		ObjectMapper mapper = new ObjectMapper();
//		if (StringUtils.isNotBlank(schemaDefinitionJson)) {
//			campaignForm.setCampaignFormElements(
//					Arrays.asList(mapper.readValue(schemaDefinitionJson, CampaignFormElement[].class)));
//		}
//		if (StringUtils.isNotBlank(translationsJson)) {
//			campaignForm.setCampaignFormTranslations(
//					Arrays.asList(mapper.readValue(translationsJson, CampaignFormTranslations[].class)));
//		}
//
//		return campaignForm;
//	}

	



	


//	@Override
//	public List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRoundAndFormExpiry(String round) {
//		String queryString = "SELECT \n" + "    campaign.id AS campaign_id,\n"
//				+ "    campaign.changeDate AS campaign_change_date,\n"
//				+ "    campaign.creationDate AS campaign_creation_date,\n" + "    c.campaignid AS campaign_uuid,\n"
//				+ "    campaign.campaignFormElements AS campaign_form_elements,\n"
//				+ "    campaign.campaignFormTranslations AS campaign_form_translations,\n"
//				+ "    c.expiryday AS days_expired,\n" + "    campaign.districtentry AS district,\n"
//				+ "    campaign.formCategory AS form_category,\n" + "    campaign.formId AS form_id,\n"
//				+ "    campaign.formName AS form_name,\n" + "    campaign.formType AS form_type,\n"
//				+ "    campaign.languageCode AS language_code,\n" + "    campaign.modality AS modality\n"
//
//				+ "FROM CampaignFormMeta campaign\n" + "JOIN campaignformmetawithexp c ON campaign.uuid  = c.formid\n"
//				+ "WHERE campaign.formType = " + "'" + round + "'" + "ORDER BY campaign.changeDate DESC;";
//
//		Query seriesDataQuery = em.createNativeQuery(queryString);
//
//		List<CampaignFormDataIndexDto> resultData = new ArrayList<>();
//		return service.getByRoundAndExpiryDate(round).stream().map(CampaignFormMetaFacadeEjb::toReferenceDto)
//				.sorted(Comparator.comparing(ReferenceDto::toString)).collect(Collectors.toList());
//	}



//
//	@Override
//	public Collection<CampaignFormMetaDto> getAllFormElement() {
//		List<CampaignFormMeta> allAfter = service.getAllFormElements(userService.getCurrentUser());
//		List<CampaignFormMeta> filtered = new ArrayList<>();
//		allAfter.removeIf(e -> e.getFormCategory() == null);
//
//		for (FormAccess n : userService.getCurrentUser().getFormAccess()) {
//			boolean yn = allAfter.stream().filter(e -> !e.getFormCategory().equals(null))
//					.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()).size() > 0;
//			if (yn) {
//				filtered.addAll(allAfter.stream().filter(e -> !e.getFormCategory().equals(null))
//						.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()));
//			}
//		}
//
//		return filtered.stream().map(campaignFormMeta -> toDto(campaignFormMeta)).collect(Collectors.toList());
//	}



	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllUuids() {
		String rawStr = userService.getCurrentUser().getFormAccess().toString();
		String nQuery = "select uuid from campaignformmeta where formcategory in ('"
				+ rawStr.replace("]", "").replace("[", "").replace(",", "','").replaceAll(" ", "") + "')";
		System.out.println(nQuery);
		Query campaignsStatisticsQuery = em.createNativeQuery(nQuery);
		for (Object str : campaignsStatisticsQuery.getResultList()) {
			System.out.println(str);

		}

		return campaignsStatisticsQuery.getResultList();

		// return service.getAllUuids();
	}

//	@Override
//	public List<CampaignFormMetaDto> getByUuids(List<String> uuids) {
//		return service.getByUuids(uuids).stream().map(campaignFormMeta -> toDto(campaignFormMeta))
//				.collect(Collectors.toList());
//	}


	
	@Override
	public List<CampaignFormMetaExpiryDto> getFormsWithExpiry(){
		
	
			String nQuery = "select * from campaignformmetawithexp ";
			System.out.println(nQuery);
			Query campaignsStatisticsQuery = em.createNativeQuery(nQuery);


		      List<Object[]> resultList = campaignsStatisticsQuery.getResultList();

		        // Create a list to hold the DTOs
		        List<CampaignFormMetaExpiryDto> resultData = new ArrayList<>();

		        // Iterate over the result list and create DTO objects
		        for (Object[] result : resultList) {
		            CampaignFormMetaExpiryDto dto = new CampaignFormMetaExpiryDto();
		            dto.setFormId((String) result[0]);
		            dto.setCampaignId((String) result[1]);
		            dto.setExpiryDay(((Number) result[2]).longValue());
		            dto.setEnddate((Date) result[3]);
		            dto.setUuid((String) result[0]);
		            // Add the DTO object to the list
		            resultData.add(dto);
		        }
		        
		        return resultData;


		}
		


//	@Override
//	public Date formExpiryDate(CampaignFormDataCriteria criteria) {
//		// TODO Auto-generated method stub
//		boolean filterIsNull = false;
//		if (criteria != null) {
//			filterIsNull = criteria.getCampaign() == null;
//		}
//
//		String joiner = "";
//
//		if (!filterIsNull && criteria != null) {
//			final CampaignReferenceDto campaign = criteria.getCampaign();
//			final CampaignFormMetaReferenceDto form = criteria.getCampaignFormMeta();
//
//			//@formatter:off
//			final String campaignFilter = campaign != null ? "campwitex.campaignid = '"+campaign.getUuid()+"'" : "";
//			final String formFilter = form != null ? " AND  campwitex.formid = '"+form.getUuid()+"'" : "";
//			
//			joiner = "where "+campaignFilter + formFilter;
//			
////			System.out.println(campaignFilter+" ===================== "+joiner);
//		}
//		
//		
//		String queryBuilder = "SELECT \n"
//				+ "campwitex.enddate as endDate \n"
//				+ "FROM campaignformmetawithexp campwitex\r\n"
//				+ "LEFT OUTER JOIN campaigns campaignG ON campwitex.campaignid = campaignG.uuid "
//				+ joiner;
//		
//		System.out.println(" ===================== "+queryBuilder);
//			
//		
//		try {
//			return (Date) em.createNativeQuery(queryBuilder).getSingleResult();
//		} catch (NoResultException e) {
//			return null;
//		    // Handle the case when no result is found
//		}
//		
//		
//		
//	}



	@LocalBean
	@Stateless
	public static class CampaignFormMetaWithExpFacadeEjbLocal extends CampaignFormMetaWithExpFacadeEjb {
		
		public CampaignFormMetaWithExpFacadeEjbLocal() {
		}
	}



	@Override
	public List<CampaignFormMetaExpiryDto> getAllAfter(Date campaignFormMetaChangeDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date formExpiryDate(CampaignFormDataCriteria criteria) {
		// TODO Auto-generated method stub
		return null;
	}



//	@Override
//	public CampaignFormMetaDto saveCampaignFormMeta(@Valid CampaignFormMetaDto campaignFormMetaDto) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void validateAndClean(CampaignFormMetaDto campaignFormMetaDto) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void validateAllFormMetas() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public CampaignFormMetaDto buildCampaignFormMetaFromJson(String formId, String languageCode,
//			String schemaDefinitionJson, String translationsJson) throws IOException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferences() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRound(String round) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRoundAndUserLanguage(String round,
//			String userLanguage) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public CampaignFormMetaReferenceDto getCampaignFormMetaReferenceByUuid(String campaignFormUuid) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public List<CampaignFormMetaDto> getIndexList(CampaignFormCriteria campaignFormCriteria, Integer first, Integer max,
//			List<SortProperty> sortProperties) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public long count(CampaignFormCriteria campaignFormCriteria) {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public void dearchiveForms(List<String> userUuids) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void archiveForms(List<String> userUuids) {
//		// TODO Auto-generated method stub
		
//	}

}
