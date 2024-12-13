package de.symeda.sormas.backend.campaign.form;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.jsoup.safety.Whitelist;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.symeda.sormas.api.Modality;
import de.symeda.sormas.api.ReferenceDto;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.form.CampaignFormCriteria;
//import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElementType;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaExpiryDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaFacade;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaHistoryExtractDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaIndexDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormTranslations;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.community.CommunityHistoryExtractDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.utils.HtmlHelper;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.disease.DiseaseConfigurationFacadeEjb;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.QueryHelper;

@Stateless(name = "CampaignFormMetaFacade")
public class CampaignFormMetaFacadeEjb implements CampaignFormMetaFacade {

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	private boolean tray;

	@EJB
	private CampaignFormMetaService service;
	@EJB
	private UserService userService;

	public CampaignFormMeta fromDto(@NotNull CampaignFormMetaDto source, boolean checkChangeDate) {
		CampaignFormMeta target = DtoHelper.fillOrBuildEntity(source, service.getByUuid(source.getUuid()),
				CampaignFormMeta::new, checkChangeDate);

		System.out.println(
				"dssssssssssssssefaasdgasdgasdgasdfasdfasdfasfeasfdasdfs " + service.getByUuid(source.getUuid()));

		target.setFormId(source.getFormId());
		target.setFormType(source.getFormType().toString().toLowerCase());
		target.setFormName(source.getFormName());
		target.setModality(source.getModality().toString());
		target.setFormCategory(source.getFormCategory());
		target.setLanguageCode(source.getLanguageCode());
		target.setCampaignFormElements(source.getCampaignFormElements());
		target.setCampaignFormTranslations(source.getCampaignFormTranslations());
		target.setDaysExpired(source.getDaysExpired());
		target.setDistrictentry(source.isDistrictentry());

		return target;
	}

	public static CampaignFormMetaDto toDto(CampaignFormMeta source) {
		if (source == null) {
			return null;
		}

		CampaignFormMetaDto target = new CampaignFormMetaDto();
		DtoHelper.fillDto(target, source);

		target.setFormId(source.getFormId());
		target.setFormType((source.getFormType().toLowerCase().equals(CampaignPhase.PRE.toString().toLowerCase()))
				? CampaignPhase.PRE
				: (source.getFormType().toLowerCase().equals(CampaignPhase.INTRA.toString().toLowerCase()))
						? CampaignPhase.INTRA
						: CampaignPhase.POST);
		target.setFormName(source.getFormName());
		target.setFormname_ps_af(source.getFormname_ps_af());
		target.setFormname_fa_af(source.getFormname_fa_af());
		if (source.getModality() != null)
			target.setModality(source.getModality().equals(Modality.S2S.toString()) ? Modality.S2S
					: source.getModality().equals(Modality.HF2HF.toString()) ? Modality.HF2HF
							: source.getModality().equals(Modality.M2M.toString()) ? Modality.M2M : Modality.H2H);
		target.setFormCategory(source.getFormCategory());
		target.setLanguageCode(source.getLanguageCode());
		target.setCampaignFormElements(source.getCampaignFormElements());
		target.setCampaignFormTranslations(source.getCampaignFormTranslations());
		target.setDaysExpired(source.getDaysExpired());
		target.setDistrictentry(source.isDistrictentry());

		return target;
	}

	@Override
	public CampaignFormMetaDto saveCampaignFormMeta(@Valid CampaignFormMetaDto campaignFormMetaDto)
			throws ValidationRuntimeException {
		validateAndClean(campaignFormMetaDto);

		CampaignFormMeta campaignFormMeta = fromDto(campaignFormMetaDto, true);
		service.ensurePersisted(campaignFormMeta);
		return toDto(campaignFormMeta);
	}

	@Override
	public CampaignFormMetaDto buildCampaignFormMetaFromJson(String formId, String languageCode,
			String schemaDefinitionJson, String translationsJson) throws IOException {
		CampaignFormMetaDto campaignForm = new CampaignFormMetaDto();
		campaignForm.setFormId(formId);
		campaignForm.setLanguageCode(languageCode);
		ObjectMapper mapper = new ObjectMapper();
		if (StringUtils.isNotBlank(schemaDefinitionJson)) {
			campaignForm.setCampaignFormElements(
					Arrays.asList(mapper.readValue(schemaDefinitionJson, CampaignFormElement[].class)));
		}
		if (StringUtils.isNotBlank(translationsJson)) {
			campaignForm.setCampaignFormTranslations(
					Arrays.asList(mapper.readValue(translationsJson, CampaignFormTranslations[].class)));
		}

		return campaignForm;
	}

	@Override
	public List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferences() {
		return service.getAll().stream().map(CampaignFormMetaFacadeEjb::toReferenceDto)
				.sorted(Comparator.comparing(ReferenceDto::toString)).collect(Collectors.toList());
	}

	@Override
	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaign(String uuid) {

		return service.getCampaignFormMetasAsReferencesByCampaign(uuid);
	}

	@Override
	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignAndUserLanguage(String uuid,
			String userLanguage) {

		return service.getCampaignFormMetasAsReferencesByCampaignAndUserLanguage(uuid, userLanguage);

	}

	@Override
	public List<CampaignFormMetaReferenceDto> getCampaignFormMetaAsReferencesByCampaignIntraCamapaign(String uuid) {
		return service.getCampaignFormMetasAsReferencesByCampaignIntraCampaign(uuid);
	}

	@Override
	public List<CampaignFormMetaReferenceDto> getCampaignFormMetaAsReferencesByCampaignPostCamapaign(String uuid) {
		return service.getCampaignFormMetasAsReferencesByCampaignPostCampaign(uuid);
	}

	@Override
	public List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRound(String round) {
		return service.getByRound(round).stream().map(CampaignFormMetaFacadeEjb::toReferenceDto)
				.sorted(Comparator.comparing(ReferenceDto::toString)).collect(Collectors.toList());
	}

	@Override
	public List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRoundAndUserLanguage(String round,
			String userLanguage) {
		if (userLanguage.equalsIgnoreCase("pashto")) {
			return service.getByRound(round).stream().map(CampaignFormMetaFacadeEjb::toReferenceDtoPashto)
					.sorted(Comparator.comparing(ReferenceDto::toString)).collect(Collectors.toList());
		} else if (userLanguage.equalsIgnoreCase("dari")) {
			return service.getByRound(round).stream().map(CampaignFormMetaFacadeEjb::toReferenceDtoDari)
					.sorted(Comparator.comparing(ReferenceDto::toString)).collect(Collectors.toList());
		} else if (userLanguage.equalsIgnoreCase("english")) {
			return service.getByRound(round).stream().map(CampaignFormMetaFacadeEjb::toReferenceDto)
					.sorted(Comparator.comparing(ReferenceDto::toString)).collect(Collectors.toList());
		} else {
			return service.getByRound(round).stream().map(CampaignFormMetaFacadeEjb::toReferenceDto)
					.sorted(Comparator.comparing(ReferenceDto::toString)).collect(Collectors.toList());
		}
	}

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
	@Override
	public List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRoundUserLanguageCampaignandForm(
			String round, String campaignUUID, Set<FormAccess> userFormAccess, String userLanguage) {
		// TODO Auto-generated method stub

		System.out.println(userLanguage + "< -------^^^^^^^^ User Language ");

		List<CampaignFormMetaReferenceDto> filterdList = new ArrayList<>();

		List<CampaignFormMetaReferenceDto> allForms = new ArrayList<>();

		if (userLanguage != null) {
			switch (userLanguage) {
			case "Pashto":
				allForms = getCampaignFormMetasAsReferencesByCampaignandRoundAndPashto(round, campaignUUID);
			case "Dari":
				allForms = getAllCampaignFormMetasAsReferencesByRoundandCampaignRoundAndDari(round, campaignUUID);
			default:
				allForms = getAllCampaignFormMetasAsReferencesByRoundandCampaign(round, campaignUUID);
			}

			allForms.removeIf(e -> e.getFormCategory() == null);

			for (FormAccess n : userFormAccess) {
				boolean yn = allForms.stream().filter(e -> !e.getFormCategory().equals(null))
						.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()).size() > 0;
				if (yn) {
					filterdList.addAll(allForms.stream().filter(e -> !e.getFormCategory().equals(null))
							.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()));
				}
			}

		} else {
			allForms = getAllCampaignFormMetasAsReferencesByRoundandCampaign(round, campaignUUID);
			allForms.removeIf(e -> e.getFormCategory() == null);

			for (FormAccess n : userFormAccess) {
				boolean yn = allForms.stream().filter(e -> !e.getFormCategory().equals(null))
						.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()).size() > 0;
				if (yn) {
					filterdList.addAll(allForms.stream().filter(e -> !e.getFormCategory().equals(null))
							.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()));
				}
			}
		}

		return filterdList;
	}

	@Override
	public List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRoundandCampaign(String round,
			String campaignUUID) {
		return service.getCampaignFormMetasAsReferencesByCampaignandRound(round, campaignUUID);
	}

	@Override
	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignandRoundAndPashto(String round,
			String campaignUUID) {
		return service.getCampaignFormMetasAsReferencesByCampaignandRoundAndPashto(round, campaignUUID);
	}

	@Override
	public List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRoundandCampaignRoundAndDari(
			String round, String campaignUUID) {
		return service.getCampaignFormMetasAsReferencesByCampaignandRoundAndDari(round, campaignUUID);
	}

	@Override
	public List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRoundandCampaignandForm(String round,
			String campaignUUID, Set<FormAccess> userFormAccess) {
		List<CampaignFormMetaReferenceDto> filterdList = new ArrayList<>();
		List<CampaignFormMetaReferenceDto> allForms = service.getCampaignFormMetasAsReferencesByCampaignandRound(round,
				campaignUUID);
		allForms.removeIf(e -> e.getFormCategory() == null);

		for (FormAccess n : userFormAccess) {
			boolean yn = allForms.stream().filter(e -> !e.getFormCategory().equals(null))
					.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()).size() > 0;
			if (yn) {
				filterdList.addAll(allForms.stream().filter(e -> !e.getFormCategory().equals(null))
						.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()));
			}
		}
		return filterdList;
	}

	@Override
	public CampaignFormMetaDto getCampaignFormMetaByUuid(String campaignFormUuid) {
		return toDto(service.getByUuid(campaignFormUuid));
	}

	@Override
	public CampaignFormMetaReferenceDto getCampaignFormMetaReferenceByUuid(String campaignFormUuid) {
		return toReferenceDto(service.getByUuid(campaignFormUuid));
	}

	/*
	 * @Override public CampaignReferenceDto getReferenceByUuid(String uuid) {
	 * return toReferenceDto(campaignService.getByUuid(uuid)); }
	 */

	@Override
	public List<CampaignFormMetaDto> getAllAfter(Date date) {
		List<CampaignFormMeta> allAfter = service.getAllAfter(date, userService.getCurrentUser());
		List<CampaignFormMeta> filtered = new ArrayList<>();
		allAfter.removeIf(e -> e.getFormCategory() == null);

		for (FormAccess n : userService.getCurrentUser().getFormAccess()) {
			boolean yn = allAfter.stream().filter(e -> !e.getFormCategory().equals(null))
					.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()).size() > 0;
			if (yn) {
				filtered.addAll(allAfter.stream().filter(e -> !e.getFormCategory().equals(null))
						.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()));
			}
		}

		return filtered.stream().map(campaignFormMeta -> toDto(campaignFormMeta)).collect(Collectors.toList());
	}

	@Override
	public Collection<CampaignFormMetaDto> getAllFormElement() {
		List<CampaignFormMeta> allAfter = service.getAllFormElements(userService.getCurrentUser());
		List<CampaignFormMeta> filtered = new ArrayList<>();
		allAfter.removeIf(e -> e.getFormCategory() == null);

		for (FormAccess n : userService.getCurrentUser().getFormAccess()) {
			boolean yn = allAfter.stream().filter(e -> !e.getFormCategory().equals(null))
					.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()).size() > 0;
			if (yn) {
				filtered.addAll(allAfter.stream().filter(e -> !e.getFormCategory().equals(null))
						.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()));
			}
		}

		return filtered.stream().map(campaignFormMeta -> toDto(campaignFormMeta)).collect(Collectors.toList());
	}

	@Override
	public List<CampaignFormMetaDto> getIndexList(CampaignFormCriteria campaignFormCriteria, Integer first, Integer max,
			List<SortProperty> sortProperties) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMeta> cq = cb.createQuery(CampaignFormMeta.class);
		Root<CampaignFormMeta> campaignFormMeta = cq.from(CampaignFormMeta.class);

		Predicate filter = null;

		if (campaignFormCriteria != null) {
			filter = service.buildCriteriaFilter(campaignFormCriteria, cb, campaignFormMeta);
		}

		if (filter != null) {
			cq.where(filter);
		}

		if (sortProperties != null && !sortProperties.isEmpty()) {
			List<Order> order = new ArrayList<Order>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case CampaignFormMeta.UUID:
				case CampaignFormMeta.FORM_ID:
				case CampaignFormMeta.FORM_NAME:
				case CampaignFormMeta.FORM_CATEGORY:
					expression = campaignFormMeta.get(sortProperty.propertyName);
					break;
				case CampaignFormMeta.DAYSTOEXPIRE:
					expression = campaignFormMeta.get(CampaignFormMeta.DAYSTOEXPIRE);
					break;
				case CampaignFormMeta.DISTRICTENTRY:
					expression = campaignFormMeta.get(CampaignFormMeta.DISTRICTENTRY);
					break;
				case CampaignFormMeta.FORM_TYPE:
					expression = campaignFormMeta.get(CampaignFormMeta.FORM_TYPE);
					break;
				case CampaignFormMeta.CHANGE_DATE:
					expression = campaignFormMeta.get(CampaignFormMeta.CHANGE_DATE);
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.desc(campaignFormMeta.get(CampaignFormMeta.CHANGE_DATE)));
		}

		cq.select(campaignFormMeta);

		return QueryHelper.getResultList(em, cq, first, max, CampaignFormMetaFacadeEjb::toDto);
	}

	@Override
	public long count(CampaignFormCriteria campaignFormCriteria) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<CampaignFormMeta> root = cq.from(CampaignFormMeta.class);

		Predicate filter = null;

		if (campaignFormCriteria != null) {
			filter = service.buildCriteriaFilter(campaignFormCriteria, cb, root);
		}

		if (filter != null) {
			cq.where(filter);
		}

		cq.select(cb.count(root));
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public void dearchiveForms(List<String> userUuids) {
		updateActiveState(userUuids, false);
	}

	@Override
	public void archiveForms(List<String> userUuids) {
		updateActiveState(userUuids, true);
	}

	private void updateActiveState(List<String> userUuids, boolean active) {

		List<CampaignFormMeta> metaDatas = service.getByUuids(userUuids);
		for (CampaignFormMeta metaData : metaDatas) {
			CampaignFormMeta oldmetaData;
			try {
				oldmetaData = (CampaignFormMeta) BeanUtils.cloneBean(metaData);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid bean access", e);
			}

			metaData.setArchived(active);
			service.ensurePersisted(metaData);
		}
	}

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

	@Override
	public List<CampaignFormMetaDto> getByUuids(List<String> uuids) {
		return service.getByUuids(uuids).stream().map(campaignFormMeta -> toDto(campaignFormMeta))
				.collect(Collectors.toList());
	}

	@Override
	public void validateAllFormMetas() {
		List<CampaignFormMeta> forms = service.getAll();

		for (CampaignFormMeta form : forms) {
			try {
				CampaignFormMetaDto formDto = toDto(form);
				validateAndClean(formDto);
			} catch (ValidationRuntimeException e) {

				throw new ValidationRuntimeException(form.getFormId() + ": " + e.getMessage());

			} catch (Exception e) {
				throw new ValidationRuntimeException(form.getFormId() + ": " + I18nProperties
						.getValidationError(Validations.campaignFormMetaValidationUnexpectedError, e.getMessage()));
			}
		}
	}

	@Override
	public void validateAndClean(CampaignFormMetaDto campaignFormMetaDto) throws ValidationRuntimeException {
		if (CollectionUtils.isEmpty(campaignFormMetaDto.getCampaignFormElements())) {
			return;
		}

		// Throw an exception when the schema definition contains an element without an
		// ID or type
		campaignFormMetaDto.getCampaignFormElements().stream()
				.filter(e -> StringUtils.isBlank(e.getId()) || StringUtils.isBlank(e.getType())).findFirst()
				.ifPresent(e -> {
					if (StringUtils.isBlank(e.getId())) {
						throw new ValidationRuntimeException(
								I18nProperties.getValidationError(Validations.campaignFormElementIdRequired));
					} else {
						throw new ValidationRuntimeException(I18nProperties
								.getValidationError(Validations.campaignFormElementTypeRequired, e.getId()));
					}
				});

		// Throw an exception when the schema definition contains the same ID more than
		// once
		campaignFormMetaDto.getCampaignFormElements().forEach(e -> {
			if (Collections.frequency(campaignFormMetaDto.getCampaignFormElements(), e) > 1) {
				throw new ValidationRuntimeException(
						I18nProperties.getValidationError(Validations.campaignFormElementDuplicateId, e.getId()));
			}
		});

		// Throw an error if any translation does not have a language code or contains
		// an element without an ID or caption
		if (CollectionUtils.isNotEmpty(campaignFormMetaDto.getCampaignFormTranslations())) {
			campaignFormMetaDto.getCampaignFormTranslations().forEach(cft -> {
				if (StringUtils.isBlank(cft.getLanguageCode())) {
					throw new ValidationRuntimeException(
							I18nProperties.getValidationError(Validations.campaignFormTranslationLanguageCodeRequired));
				}

				cft.getTranslations().stream()
						.filter(t -> StringUtils.isBlank(t.getElementId()) || StringUtils.isBlank(t.getCaption()))
						.findFirst().ifPresent(e -> {
							if (StringUtils.isBlank(e.getElementId())) {
								throw new ValidationRuntimeException(I18nProperties
										.getValidationError(Validations.campaignFormTranslationIdRequired));
							} else {
								throw new ValidationRuntimeException(I18nProperties.getValidationError(
										Validations.campaignFormTranslationCaptionRequired, e.getElementId(),
										cft.getLanguageCode()));
							}
						});
			});
		}

		Map<String, String> idsAndTypes = campaignFormMetaDto.getCampaignFormElements().stream()
				.collect(Collectors.toMap(CampaignFormElement::getId, CampaignFormElement::getType));

		for (CampaignFormElement element : campaignFormMetaDto.getCampaignFormElements()) {
			// Clean the element caption from all HTML tags that are not explicitly allowed
			if (StringUtils.isNotBlank(element.getCaption())) {
				Whitelist whitelist = Whitelist.none();
				whitelist.addTags(CampaignFormElement.ALLOWED_HTML_TAGS);
				element.setCaption(HtmlHelper.cleanHtml(element.getCaption(), whitelist));
			}

			// Validate form elements
			validateCampaignFormElementType(element.getId(), element.getType());
			validateCampaignFormElementStyles(element.getId(), element.getStyles());
			if (StringUtils.isNotBlank(element.getDependingOn())
					&& ArrayUtils.isEmpty(element.getDependingOnValues())) {
				throw new ValidationRuntimeException(I18nProperties
						.getValidationError(Validations.campaignFormDependingOnValuesMissing, element.getId()));
			}
			validateCampaignFormMetaDependency(element.getId(), element.getDependingOn(),
					element.getDependingOnValues(), idsAndTypes);
		}

		// Validate element IDs used in translations and clean HTML used in translation
		// captions
//		if (CollectionUtils.isNotEmpty(campaignFormMetaDto.getCampaignFormTranslations())) {
//			for (CampaignFormTranslations translations : campaignFormMetaDto.getCampaignFormTranslations()) {
//				translations.getTranslations().forEach(e -> {
//					if (idsAndTypes.get(e.getElementId()) == null) {
//						throw new ValidationRuntimeException(
//								I18nProperties.getValidationError(Validations.campaignFormTranslationIdInvalid,
//										e.getElementId(), translations.getLanguageCode()));
//					}
//
//					if (StringUtils.isNotBlank(e.getCaption())) {
//						Whitelist whitelist = Whitelist.none();
//						whitelist.addTags(CampaignFormElement.ALLOWED_HTML_TAGS);
//						e.setCaption(HtmlHelper.cleanHtml(e.getCaption(), whitelist));
//					}
//				});
//			}
//		}
	}

	private void validateCampaignFormElementType(String id, String type) throws ValidationRuntimeException {
		if (!StringUtils.equalsAny(type, CampaignFormElement.VALID_TYPES)) {
			throw new ValidationRuntimeException(
					I18nProperties.getValidationError(Validations.campaignFormUnsupportedType, type, id));
		}
	}

	private void validateCampaignFormElementStyles(String id, String[] styles) throws ValidationRuntimeException {
		if (ArrayUtils.isEmpty(styles)) {
			return;
		}

		for (String style : styles) {
			if (!StringUtils.equalsAny(style, CampaignFormElement.VALID_STYLES)) {
				throw new ValidationRuntimeException(
						I18nProperties.getValidationError(Validations.campaignFormUnsupportedStyle, style, id));
			}
		}
	}

	private void validateCampaignFormMetaDependency(String id, String dependingOn, String[] dependingOnValues,
			Map<String, String> otherElements) throws ValidationRuntimeException {
		if (StringUtils.isBlank(dependingOn)) {
			return;
		}

		// Schema must contain an element with an ID matching the dependingOn attribute
		if (!otherElements.containsKey(dependingOn)) {
			throw new ValidationRuntimeException(
					I18nProperties.getValidationError(Validations.campaignFormDependingOnNotFound, dependingOn, id));
		}

		// The element referenced by the dependingOn attribute must be of a type that is
		// compatible with the dependingOnValues.
		for (String dependingOnValue : dependingOnValues) {
			if (!isValueValidForType(otherElements.get(dependingOn), dependingOnValue)) {
				throw new ValidationRuntimeException(
						I18nProperties.getValidationError(Validations.campaignFormUnsupportedDependingOnValue,
								dependingOnValue, id, otherElements.get(dependingOn), dependingOn));
			}
		}
	}

	private boolean isValueValidForType(String type, String value) {
		if (type.equals(CampaignFormElementType.NUMBER.toString())) {
			try {
				Integer.parseInt(value.replaceAll("!", ""));
			} catch (NumberFormatException e) {
				return false;
			}
		}

		if (type.equals(CampaignFormElementType.DATE.toString())) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:ms");
			dateFormat.setLenient(false);

			try {
				dateFormat.parse(value.trim());
				System.out.println(">>>>>>>>>>>>>>>>>>> daTE VALEUE been checked >>" + value);

			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				return false;
			}
		}

		if (type.equals(CampaignFormElementType.DECIMAL.toString())) {
			try {

				if (value.contains(".")) {
					if (value.length() - value.indexOf(".") - 1 == 2) {

					} else {
						return false;
					}
				} else {
					return false;
				}

			} catch (NumberFormatException e) {
				return false;
			}
		}

		if (type.equals(CampaignFormElementType.YES_NO.toString())) {
			return StringUtils.equalsAnyIgnoreCase(value.replaceAll("!", ""),
					CampaignFormElementType.YES_NO.getAllowedValues());
		}

		return true;
	}

	public static CampaignFormMetaReferenceDto toReferenceDto(CampaignFormMeta entity) {
		if (entity == null) {
			return null;
		}

		return new CampaignFormMetaReferenceDto(entity.getUuid(), entity.toString(), entity.getFormType(),
				entity.getFormCategory(), entity.getDaysExpired());
	}

	public static CampaignFormMetaReferenceDto toReferenceDtoDari(CampaignFormMeta entity) {
		if (entity == null) {
			return null;
		}

		return new CampaignFormMetaReferenceDto(entity.getUuid(), entity.getFormname_fa_af().toString(),
				entity.getFormType(), entity.getFormCategory(), entity.getDaysExpired());
	}

	public static CampaignFormMetaReferenceDto toReferenceDtoPashto(CampaignFormMeta entity) {
		if (entity == null) {
//			System.out.println("entity is null yyyyyyyyyyyyyyyyyyyyy" + entity);
			return null;
		}
//		System.out.println(entity.getUuid() + "entity is NOT   null yyyyyy" + entity.getFormname_ps_af() + "yyyyyyyyyyyyyyy" + entity);

		return new CampaignFormMetaReferenceDto(entity.getUuid(), entity.getFormname_ps_af().toString(),
				entity.getFormType(), entity.getFormCategory(), entity.getDaysExpired());
	}

	@Override
	public List<CampaignFormMetaExpiryDto> getFormsWithExpiry() {
//
		String nQuery = "select formid, campaignid, expiryday, enddate, changedate , upper(CAST(uuid AS TEXT)) as uuid from campaignformmetawithexp ";
//		System.out.println(nQuery);
		Query getFormsWithExpiryQuery = em.createNativeQuery(nQuery);
//
		List<CampaignFormMetaExpiryDto> resultData = new ArrayList<>();

//		@SuppressWarnings("unchecked")
//		List<Object[]> resultList = getFormsWithExpiryQuery.getResultList();
//
//		resultData.addAll(resultList.stream()
//				.map((result) -> new CampaignFormMetaExpiryDto((String) result[0].toString(),
//						(String) result[1].toString(), ((BigInteger) result[2]).longValue(), (Date) result[3], (Date) result[4]))
//				.collect(Collectors.toList()));
//
//		return resultData;//getFormsWithExpiryQuery.getResultList();

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = getFormsWithExpiryQuery.getResultList();

		// Create a list to hold the DTOs
//	        List<CampaignFormMetaExpiryDto> resultData = new ArrayList<>();

		// Iterate over the result list and create DTO objects
		for (Object[] result : resultList) {
			CampaignFormMetaExpiryDto dto = new CampaignFormMetaExpiryDto();
			dto.setFormId((String) result[0]);
			dto.setCampaignId((String) result[1]);
			dto.setExpiryDay(((Number) result[2]).longValue());
			dto.setEnddate((Date) result[3]);
			dto.setChangeDate((Date) result[4]);
			dto.setUuid((String) result[5]);

			resultData.add(dto);
		}

		return resultData;

//
	}

	@Override
	public List<CampaignFormMetaIndexDto> getFormExpressions(String formUuid) {

		String getFormExpressionQuery = "SELECT \n" +
			    "    elements->>'id' AS variableName, \n" +
			    "    elements->>'type' AS format, \n" +
			    "    elements->>'caption' AS variableCaption, \n" +
			    "    elements->>'expression' AS description \n" +
			    "FROM campaignformmeta, \n" +
			    "     LATERAL json_array_elements(campaignformelements) AS elements \n" +
			    "WHERE \n" +
			    "    elements->>'expression' IS NOT NULL AND \n" +
			    "    elements->>'caption' IS NOT NULL AND \n" +
			    "    campaignformmeta.\"uuid\" = '" + formUuid + "';";
		
		Query getFormExpressionsQuery = em.createNativeQuery(getFormExpressionQuery);
		//
		List<CampaignFormMetaIndexDto> resultData = new ArrayList<>();
		
		@SuppressWarnings("unchecked")
		
		List<Object[]> resultList = getFormExpressionsQuery.getResultList();
		// Iterate over the result list and create DTO objects
		

		resultData.addAll(resultList.stream()
				.map((result) -> new CampaignFormMetaIndexDto(
				(String) result[0].toString() == null ? "" : (String) result[0].toString(), 
				(String) result[1].toString() == null ? "" : (String) result[1].toString(),
				(String) result[2].toString() == null ? "" : (String) result[2].toString(),
				(String) result[3].toString() == null ? "" : (String) result[3].toString()
				)).collect(Collectors.toList()));
		return resultData;
	}

	@Override
	public Date formExpiryDate(CampaignFormDataCriteria criteria) {
		// TODO Auto-generated method stub
		boolean filterIsNull = false;
		if (criteria != null) {
			filterIsNull = criteria.getCampaign() == null;
		}

		String joiner = "";

		if (!filterIsNull && criteria != null) {
			final CampaignReferenceDto campaign = criteria.getCampaign();
			final CampaignFormMetaReferenceDto form = criteria.getCampaignFormMeta();

			//@formatter:off
			final String campaignFilter = campaign != null ? "campwitex.campaignid = '"+campaign.getUuid()+"'" : "";
			final String formFilter = form != null ? " AND  campwitex.formid = '"+form.getUuid()+"'" : "";
			
			joiner = "where "+campaignFilter + formFilter;
			
//			System.out.println(campaignFilter+" ===================== "+joiner);
		}
		
		
		String queryBuilder = "SELECT \n"
				+ "campwitex.enddate as endDate \n"
				+ "FROM campaignformmetawithexp campwitex\r\n"
				+ "LEFT OUTER JOIN campaigns campaignG ON campwitex.campaignid = campaignG.uuid "
				+ joiner;
		
		System.out.println(" ===================== "+queryBuilder);
			
		
		try {
			return (Date) em.createNativeQuery(queryBuilder).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public List<CampaignFormMetaHistoryExtractDto> getFormsMetaHistory (String formUuid){

		ObjectMapper objectMapper = new ObjectMapper();
		List<CampaignFormMetaHistoryExtractDto> resultData = new ArrayList<>();
		
		
		StringBuilder queryStringBuilder = new StringBuilder();
		queryStringBuilder.append("WITH current_data AS (")
		                  .append("SELECT id, uuid, CAST(campaignformelements AS TEXT) AS campaignformelements, formid, formname, changedate AS start_date, ")
		                  .append("LEAD(changedate) OVER (PARTITION BY uuid ORDER BY changedate) AS end_date ")
		                  .append("FROM campaignformmeta_history ");

		if (formUuid != null) {
		    queryStringBuilder.append("WHERE uuid = '").append(formUuid).append("' ");
		}

		queryStringBuilder.append("), ")
		                  .append("updated_end_date AS (")
		                  .append("SELECT cd.id, cd.uuid, cd.campaignformelements, cd.formid, cd.formname, cd.start_date, ")
		                  .append("COALESCE(cd.end_date, (SELECT changedate FROM campaignformmeta WHERE campaignformmeta.uuid = cd.uuid)) AS end_date ")
		                  .append("FROM current_data cd) ")
		                  .append("SELECT uuid, formname, campaignformelements, formid,  start_date, end_date ")
		                  .append("FROM updated_end_date ")
		                  .append("ORDER BY start_date ASC;");
		
		String queryString = queryStringBuilder.toString();
		
		Query seriesDataQuery = em.createNativeQuery(queryString);
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();
		
//		resultData.addAll(resultList.stream()
//			    .map(result -> {
//			    	
////			    	String campaignFormElementsJson = (String) result[2];
////			    	Map<String, Object> campaignFormElements = Collections.emptyMap();
////			    	if (campaignFormElementsJson != null && !campaignFormElementsJson.trim().isEmpty()) {
////			    	    try {
////			    	        campaignFormElements = objectMapper.readValue(outputStream, Map.class);
////			    	    } catch (Exception e) {
////			    	        e.printStackTrace();
////			    	    }
////			    	}else {
////			    		System.out.println("Null Discovered ");
////			    	}
//
////			        String campaignFormElementsJson = (String) result[2];
////			        Map<String, Object> campaignFormElements = null;
////			        try {
////			            campaignFormElements = objectMapper.readValue(campaignFormElementsJson, Map.class);
////			        } catch (Exception e) {
////			            e.printStackTrace();
////			        }
//			        return new CampaignFormMetaHistoryExtractDto(
//			            (String) result[0], // UUID
//			            (String) result[1], // Name
//			            ((String) result[1]).toString(), // Campaign Form Elements
//			            (String) result[3], // External ID
//			            ((Timestamp) result[4]).toLocalDateTime(), // Start Date
//			            result[5] != null ? ((Timestamp) result[5]).toLocalDateTime() : LocalDateTime.now() // End Date
//			        );
//			    })
//			    .collect(Collectors.toList()));
//		
//		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

//		resultData.addAll(resultList.stream()
//		    .map(result -> {
//		        String campaignFormElementsJson = (String) result[2];
//		        Map<String, Object> campaignFormElements = new HashMap<>();
//		        if (campaignFormElementsJson != null && !campaignFormElementsJson.trim().isEmpty()) {
//		            try {
//		                campaignFormElements = objectMapper.readValue(campaignFormElementsJson, Map.class);
//		            } catch (Exception e) {
//		                // Log error or handle appropriately
//		                e.printStackTrace();
//		            }
//		        }
//		        return new CampaignFormMetaHistoryExtractDto(
//		            (String) result[0],
//		            (String) result[1],
//		            campaignFormElements,
//		            (String) result[3],
//		            ((Timestamp) result[4]).toLocalDateTime(),
//		            result[5] != null ? ((Timestamp) result[5]).toLocalDateTime() : LocalDateTime.now()
//		        );
//		    })
//		    .collect(Collectors.toList()));
		
		 resultData.addAll(resultList.stream()
		            .map(result -> {
		                String campaignFormElementsJson = (String) result[2];
		                List<CampaignFormElement> campaignFormElements = new ArrayList<>();
		                
		                if (campaignFormElementsJson != null && !campaignFormElementsJson.trim().isEmpty()) {
		                    try {
		                        campaignFormElements = objectMapper.readValue(
		                            campaignFormElementsJson, 
		                            new TypeReference<List<CampaignFormElement>>() {}
		                        );
		                    } catch (Exception e) {
//		                        logger.error("Error parsing JSON for campaign form elements", e);
		                    }
		                }
		                
		                return new CampaignFormMetaHistoryExtractDto(
		                    (String) result[0],
		                    (String) result[1],
		                    (String) result[2],
		                    (String) result[3],
		                    ((Timestamp) result[4]).toLocalDateTime(),
		                    result[5] != null 
		                        ? ((Timestamp) result[5]).toLocalDateTime() 
		                        : LocalDateTime.now()
		                );
		            })
		            .collect(Collectors.toList()));

		return resultData;
	}
	
	
	@Override
	public List<CampaignFormMetaReferenceDto> getCampaignFormByCampaignAndFormType(String campaignUuid,
			String formType) {
		
		
		String getFormExpressionQuery = "SELECT cfm.uuid, cfm.formname, cfm.formcategory FROM campaignformmeta cfm WHERE cfm.formcategory = 'ADMIN' "
				+ "AND cfm.id IN ("
				+ "SELECT xx.campaignformmeta_id FROM campaign_campaignformmeta xx "
				+ "LEFT JOIN campaigns c ON c.id = xx.campaign_id "
				+ "where c.uuid = '" + campaignUuid + "');";
		
		Query getFormExpressionsQuery = em.createNativeQuery(getFormExpressionQuery);
		//
		List<CampaignFormMetaReferenceDto> resultData = new ArrayList<>();
		
		@SuppressWarnings("unchecked")
		
		List<Object[]> resultList = getFormExpressionsQuery.getResultList();
		// Iterate over the result list and create DTO objects
		

		resultData.addAll(resultList.stream()
				.map((result) -> new CampaignFormMetaReferenceDto(
				(String) result[0].toString() == null ? "" : (String) result[0].toString(), 
				(String) result[1].toString() == null ? "" : (String) result[1].toString(),
				(String) result[2].toString() == null ? "" : (String) result[2].toString()
				)).collect(Collectors.toList()));
		return resultData;
					}

	
	@LocalBean
	@Stateless
	public static class CampaignFormMetaFacadeEjbLocal extends CampaignFormMetaFacadeEjb {

		public CampaignFormMetaFacadeEjbLocal() {
		}
	}


	

}
