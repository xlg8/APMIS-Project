package de.symeda.sormas.backend.campaign.form;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.model.Parameter.Source;
import org.jsoup.safety.Whitelist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.TextFormat.ParseException;

import de.symeda.sormas.api.Modality;
import de.symeda.sormas.api.ReferenceDto;
import de.symeda.sormas.api.campaign.CampaignCriteria;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormCriteria;
//import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElementType;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaFacade;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormTranslations;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.report.UserReportModelDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.HtmlHelper;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.campaign.Campaign;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.infrastructure.area.Area;
import de.symeda.sormas.backend.infrastructure.district.District;
import de.symeda.sormas.backend.infrastructure.facility.Facility;
import de.symeda.sormas.backend.infrastructure.region.Region;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserFacadeEjb;
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
		target.setFormType(
				(source.getFormType().toLowerCase().equals(CampaignPhase.PRE.toString().toLowerCase())) ? CampaignPhase.PRE
						: (source.getFormType().toLowerCase().equals(CampaignPhase.INTRA.toString().toLowerCase()))
								? CampaignPhase.INTRA
								: CampaignPhase.POST);
		target.setFormName(source.getFormName());
		if(source.getModality() != null)
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
	public List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRoundandCampaign(String round,
			String campaignUUID) {
		return service.getCampaignFormMetasAsReferencesByCampaignandRound(round, campaignUUID);
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

		cq.multiselect(campaignFormMeta.get(CampaignFormMeta.FORM_ID), campaignFormMeta.get(CampaignFormMeta.FORM_NAME),
				campaignFormMeta.get(CampaignFormMeta.FORM_TYPE), campaignFormMeta.get(CampaignFormMeta.UUID),
				campaignFormMeta.get(CampaignFormMeta.FORM_CATEGORY),
				campaignFormMeta.get(CampaignFormMeta.DISTRICTENTRY),
				campaignFormMeta.get(CampaignFormMeta.DAYSTOEXPIRE),
				campaignFormMeta.get(CampaignFormMeta.CREATION_DATE),
				campaignFormMeta.get(CampaignFormMeta.CHANGE_DATE));
		// TODO: We'll need a user filter for users at some point, to make sure that
		// users can edit their own details,
		// but not those of others

		Predicate filter = null;

		if (campaignFormCriteria != null) {
			 System.out.println("DEBUGGER: 45fffffffiiilibraryii = "+ campaignFormCriteria);
			filter = service.buildCriteriaFilter(campaignFormCriteria, cb, campaignFormMeta);
		}

		if (filter != null) {
			/*
			 * No preemptive distinct because this does collide with ORDER BY
			 * User.location.address (which is not part of the SELECT clause). UserType
			 */
			cq.where(filter);
		}

		if (sortProperties != null && sortProperties.size() > 0) {
			List<Order> order = new ArrayList<Order>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case CampaignFormMeta.UUID:
//				case CampaignFormMeta.FORM_ID:
				case CampaignFormMeta.MODALITY:
//				case CampaignFormMeta.FORM_NAME:
//				case CampaignFormMeta.DAYSTOEXPIRE:
//				case CampaignFormMeta.DISTRICTENTRY:
//				case CampaignFormMeta.FORM_TYPE:
					System.out.println("DEBUGGER: gfgdgjhgfgddgfghhjhg");
					expression = campaignFormMeta.get(sortProperty.propertyName);
					break;
				case CampaignFormMeta.FORM_NAME:				
					System.out.println("DEBUGGER: firsthvshgshvshgd");
					expression = campaignFormMeta.get(CampaignFormMeta.FORM_NAME);
//					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					break;
				case CampaignFormMeta.FORM_TYPE:
					System.out.println("DEBUGGER: 456ddddddt67ujhgtyuikjhu");
					expression = campaignFormMeta.get(CampaignFormMeta.FORM_TYPE);
					break;
				case CampaignFormMeta.FORM_CATEGORY:
					System.out.println("DEBUGGER: jfhgsghsghsjgsfhgsghshgs");
					expression = campaignFormMeta.get(CampaignFormMeta.FORM_CATEGORY);
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
		if (CollectionUtils.isNotEmpty(campaignFormMetaDto.getCampaignFormTranslations())) {
			for (CampaignFormTranslations translations : campaignFormMetaDto.getCampaignFormTranslations()) {
				translations.getTranslations().forEach(e -> {
					if (idsAndTypes.get(e.getElementId()) == null) {
						throw new ValidationRuntimeException(
								I18nProperties.getValidationError(Validations.campaignFormTranslationIdInvalid,
										e.getElementId(), translations.getLanguageCode()));
					}

					if (StringUtils.isNotBlank(e.getCaption())) {
						Whitelist whitelist = Whitelist.none();
						whitelist.addTags(CampaignFormElement.ALLOWED_HTML_TAGS);
						e.setCaption(HtmlHelper.cleanHtml(e.getCaption(), whitelist));
					}
				});
			}
		}
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

	@LocalBean
	@Stateless
	public static class CampaignFormMetaFacadeEjbLocal extends CampaignFormMetaFacadeEjb {
	}

}
