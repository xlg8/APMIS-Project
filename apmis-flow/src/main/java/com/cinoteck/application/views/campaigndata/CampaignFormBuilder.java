package com.cinoteck.application.views.campaigndata;

import static de.symeda.sormas.api.campaign.ExpressionProcessorUtils.refreshEvaluationContext;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Objects;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.jsoup.select.Evaluator.IsEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.vaadin.addons.taefi.component.ToggleButtonGroup;

import com.cinoteck.application.UserProvider;
import com.google.common.collect.Sets;

//import org.hibernate.internal.build.AllowSysOut;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.VaadinService;

import de.symeda.sormas.api.AgeGroup;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.MapperUtil;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.data.translation.TranslationElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElementStyle;
import de.symeda.sormas.api.campaign.form.CampaignFormElementOptions;
import de.symeda.sormas.api.campaign.form.CampaignFormElementType;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormTranslations;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.Descriptions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;

import de.symeda.sormas.api.infrastructure.PopulationDataCriteria;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;

import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserActivitySummaryDto;
import de.symeda.sormas.api.user.UserRole;

public class CampaignFormBuilder extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7153373681106891254L;
	private final List<CampaignFormElement> formElements;
	private final Map<String, Object> formValuesMap;
	// private final FormLayout campaignFormLayout;
	private final Locale userLocale;
	private Map<String, String> userTranslations = new HashMap<String, String>();
	private Map<String, String> userOptTranslations = new HashMap<String, String>();
	Map<String, Component> fields;

	private Map<String, String> optionsValues = new HashMap<String, String>();
	private Map<String, String> optionsOrder = new HashMap<String, String>();

	private List<String> constraints;
	private List<CampaignFormTranslations> translationsOpt;
	private CampaignReferenceDto campaignReferenceDto;

	private List<PopulationDataDto> popDto;

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private boolean isDistrictEntry;
	private CampaignFormMetaReferenceDto campaignFormMeta;

	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	List<CommunityReferenceDto> communities;
	Binder<CampaignFormDataDto> binder = new BeanValidationBinder<>(CampaignFormDataDto.class);
	private UserProvider currentUser = new UserProvider();

	private ExpressionProcessor expressionProcessor;

	private final ExpressionParser expressionParser = new SpelExpressionParser();

	private boolean invalidForm = false;

	private boolean openedOnce = false;

	ComboBox<Object> cbCampaign = new ComboBox<>(I18nProperties.getCaption(Captions.Campaign));

	ComboBox<AreaReferenceDto> cbArea = new ComboBox<>(I18nProperties.getCaption(Captions.area));
	ComboBox<RegionReferenceDto> cbRegion = new ComboBox<>(I18nProperties.getCaption(Captions.region));
	ComboBox<DistrictReferenceDto> cbDistrict = new ComboBox<>(I18nProperties.getCaption(Captions.district));
	ComboBox<CommunityReferenceDto> cbCommunity = new ComboBox<>(I18nProperties.getCaption(Captions.community));
	Button reassignDataConfigUnit = new Button(I18nProperties.getCaption("Reassign Data"));

	FormLayout vertical = new FormLayout();
	HorizontalLayout reassigmentLayout = new HorizontalLayout();
	Button updateFormDataUnitAssignment = new Button("Update Form Data Unit");
	Button cancelFormDataUnitAssignment = new Button("Cancel");


	DatePicker formDate = new DatePicker();
	private boolean openData = false;
	private String uuidForm;
	private boolean checkDistrictEntry = false;
	private String formName;

	public CampaignFormBuilder(List<CampaignFormElement> formElements, 
			List<CampaignFormDataEntry> formValues,
			CampaignReferenceDto campaignReferenceDto,
			List<CampaignFormTranslations> translations, 
			String formName,
			CampaignFormMetaReferenceDto campaignFormMetaUUID, 
			boolean openData, 
			String uuidForm,
			boolean isDistrictEntry) {

		logger.debug("+++++++++++CampaignFormBuilder+++++: " + openData);

		this.openData = openData;
		this.uuidForm = uuidForm;
		this.formElements = formElements;
		this.campaignReferenceDto = campaignReferenceDto;
		this.campaignFormMeta = campaignFormMetaUUID;
		this.isDistrictEntry = isDistrictEntry;
		this.formName = formName;
		if (formValues != null) {
			this.formValuesMap = new HashMap<>();
			formValues.forEach(formValue -> formValuesMap.put(formValue.getId(), formValue.getValue()));
		} else {
			this.formValuesMap = new HashMap<>();
		}
		// this.campaignFormLayout = new FormLayout();
		this.fields = new HashMap<>();
		this.translationsOpt = translations;

		UserProvider userProvider = new UserProvider();
		I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
		this.userLocale = I18nProperties.getUserLanguage().getLocale();

//		logger.debug(userProvider.getUser().getLanguage() +" : I18nProperties.getUserLanguage().getLocale(): "+I18nProperties.getUserLanguage().getLocale());

		if (userLocale != null) {
			if (translations != null) {
				translations.stream().filter(t -> t.getLanguageCode().equals(userLocale.toString())).findFirst()
						.ifPresent(filteredTranslations -> userTranslations = filteredTranslations.getTranslations()
								.stream().collect(Collectors.toMap(TranslationElement::getElementId,
										TranslationElement::getCaption)));
			}
		}

		FormLayout vertical_ = new FormLayout();
		VerticalLayout formNameLabelLayout = new VerticalLayout();
		Label formNam = new Label();
		formNam.getElement().setProperty("innerHTML", "<h3>" + formName + "</h3>");
		formNameLabelLayout.add(formNam);
		vertical_.setColspan(formNameLabelLayout, 3);
		vertical_.add(formNameLabelLayout);

		cbCampaign.setItems(FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference());
		cbCampaign.setValue(campaignReferenceDto);
		cbCampaign.setRequired(true);
		cbCampaign.setReadOnly(true);
		cbCampaign.setId("my-disabled-textfield");
		cbCampaign.getStyle().set("-webkit-text-fill-color", "green !important");

		formDate.setLabel(I18nProperties.getCaption(Captions.CampaignFormData_formDate));
		LocalDate today = LocalDate.now();
		formDate.setValue(today);
		formDate.setRequired(true);
		formDate.setId("my-disabled-textfield");
		formDate.getStyle().set("-webkit-text-fill-color", "green !important");

		//

		popDto = FacadeProvider.getPopulationDataFacade().getPopulationDataWithCriteria(campaignReferenceDto.getUuid());

		logger.debug("++++++++++++++++++++++++++++++++" + popDto.size());

		cbArea = new ComboBox<>(I18nProperties.getCaption(Captions.area));
		cbArea.setRequired(true);

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			cbArea.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferencePashto());
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
			cbArea.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferenceDari());
		} else {

			cbArea.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());

		}
		cbArea.setId("my-disabled-textfield");
		cbArea.getStyle().set("-webkit-text-fill-color", "green !important");

		cbRegion = new ComboBox<>(I18nProperties.getCaption(Captions.region));
		cbRegion.setReadOnly(true);
		;
		cbRegion.setRequired(true);
		cbRegion.setId("my-disabled-textfield");
		cbRegion.getStyle().set("-webkit-text-fill-color", "green !important");

		cbDistrict = new ComboBox<>(I18nProperties.getCaption(Captions.district));
		cbDistrict.setReadOnly(true);
		cbDistrict.setRequired(true);
		cbDistrict.setId("my-disabled-textfield");
		cbDistrict.getStyle().set("-webkit-text-fill-color", "green !important");

		cbCommunity = new ComboBox<>(I18nProperties.getCaption(Captions.community));
		cbCommunity.setReadOnly(true);
		cbCommunity.setRequired(true);
		cbCommunity.setId("my-disabled-textfield");
		cbCommunity.getStyle().set("-webkit-text-fill-color", "green !important");
		Label cbLabel = new Label(cbCommunity.getLabel());
		cbLabel.addClassName("my-custom-label-style");

		// listeners logic
		cbArea.addValueChangeListener(e -> {
			if (e.getValue() != null) {

				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					provinces = FacadeProvider.getRegionFacade().getAllActiveByAreaPashto(e.getValue().getUuid());
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					provinces = FacadeProvider.getRegionFacade().getAllActiveByAreaDari(e.getValue().getUuid());
				} else {

					List<RegionReferenceDto> regionsList = FacadeProvider.getRegionFacade()
							.getAllActiveByArea(e.getValue().getUuid());
					List<RegionReferenceDto> allRegionList = new ArrayList<>();

					popDto.forEach(popDtoc -> allRegionList.add(popDtoc.getRegion()));

					List<RegionReferenceDto> filteredRegionListwithDup = regionsList.stream()
							.filter(allRegionList::contains).collect(Collectors.toList());

					// Remove duplicates using Set
					Set<RegionReferenceDto> uniqueSet = new HashSet<>(filteredRegionListwithDup);

					// Convert the set back to a list (if needed)
					List<RegionReferenceDto> filteredRegionList = new ArrayList<>(uniqueSet);

					provinces = filteredRegionList;// FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());

				}

				cbRegion.clear();
				cbRegion.setReadOnly(false);
				cbRegion.setItems(provinces);
				cbDistrict.clear();
				cbDistrict.setReadOnly(true);
				cbCommunity.clear();
				cbCommunity.setReadOnly(true);
			} else {
				cbRegion.clear();
				cbRegion.setReadOnly(true);
				cbDistrict.clear();
				cbDistrict.setReadOnly(true);
				cbCommunity.clear();
				cbCommunity.setReadOnly(true);
			}

		});

		cbRegion.addValueChangeListener(e -> {
			if (e.getValue() != null) {

				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					districts = FacadeProvider.getDistrictFacade().getAllActiveByRegionPashto(e.getValue().getUuid());
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					districts = FacadeProvider.getDistrictFacade().getAllActiveByRegionDari(e.getValue().getUuid());
				} else {
					List<DistrictReferenceDto> districtsList = FacadeProvider.getDistrictFacade()
							.getAllActiveByRegion(e.getValue().getUuid());
					List<DistrictReferenceDto> allDistrictList = new ArrayList<>();

					popDto.forEach(popDtoc -> allDistrictList.add(popDtoc.getDistrict()));

					List<DistrictReferenceDto> filteredDistrictListwithDup = districtsList.stream()
							.filter(allDistrictList::contains).collect(Collectors.toList());

					// Remove duplicates using Set
					Set<DistrictReferenceDto> uniqueSet = new HashSet<>(filteredDistrictListwithDup);

					// Convert the set back to a list (if needed)
					List<DistrictReferenceDto> filteredDistrictList = new ArrayList<>(uniqueSet);

					districts = filteredDistrictList;
//					districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
				}
				cbDistrict.setReadOnly(false);

				cbDistrict.setItems(districts);
				cbCommunity.clear();
				cbCommunity.setReadOnly(true);
			} else {
				cbDistrict.clear();
				cbDistrict.setReadOnly(true);
				cbCommunity.clear();
				cbCommunity.setReadOnly(true);
			}

		});

//		logger.debug(checkDistrictEntry + "checkingggggggggggggggggggggggggggggg" + campaignFormMetaDto);
		if (isDistrictEntry) {
			cbDistrict.addValueChangeListener(e -> {
				if (e.getValue() != null) {
					communities = FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid());
					cbCommunity.clear();
					cbCommunity.setReadOnly(false);
					;
					communities.sort(Comparator.comparingInt(CommunityReferenceDto::getNumber));

					cbCommunity.setItems(communities);
					cbCommunity.setValue(communities.get(0));
					cbCommunity.setItemLabelGenerator(itm -> {
						CommunityReferenceDto dcfv = (CommunityReferenceDto) itm;
						return dcfv.getNumber() + " | " + dcfv.getCaption();
					});

//					CampaignReferenceDto campaignReferenceDto = (CampaignReferenceDto) cbCampaign.getValue();

					logger.debug(e.getValue().getUuid() + "11111111-------- " + campaignReferenceDto.getUuid()
							+ " ----!!!!!!!!!!!!!!!!!!!!!!: " + AgeGroup.AGE_0_4);

					Integer comdto = FacadeProvider.getPopulationDataFacade().getDistrictPopulationByType(
							e.getValue().getUuid(), campaignReferenceDto.getUuid(), AgeGroup.AGE_0_4);

					logger.debug(" ========================== " + campaignReferenceDto.getUuid());

					VaadinService.getCurrentRequest().getWrappedSession().setAttribute("populationdata", comdto);
				} else {
					cbCommunity.clear();
					cbCommunity.setReadOnly(true);
					;
				}
			});
		} else {
			cbDistrict.addValueChangeListener(e -> {
				if (e.getValue() != null) {
					communities = FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid());
					cbCommunity.clear();
					cbCommunity.setReadOnly(false);
					;
					communities.sort(Comparator.comparingInt(CommunityReferenceDto::getNumber));

					cbCommunity.setItems(communities);
					cbCommunity.setItemLabelGenerator(itm -> {
						CommunityReferenceDto dcfv = (CommunityReferenceDto) itm;
						return dcfv.getNumber() + " | " + dcfv.getCaption();
					});

					logger.debug(
							e.getValue().getUuid() + "11111111xxxxxxxxxxxx-------- " + campaignReferenceDto.getUuid()
									+ " ----!!!!!!xxxxxxxxxxxxxxxxx!!!!!!!!!!!!!!!!: " + AgeGroup.AGE_0_4);

					Integer comdto = FacadeProvider.getPopulationDataFacade().getDistrictPopulationByType(
							e.getValue().getUuid(), campaignReferenceDto.getUuid(), AgeGroup.AGE_0_4);

					logger.debug(" ============xxxxxxxxxxxxx============== " + campaignReferenceDto.getUuid());

					VaadinService.getCurrentRequest().getWrappedSession().setAttribute("populationdata", comdto);
				} else {
					cbCommunity.clear();
					cbCommunity.setReadOnly(true);
				}
			});
		}
		cbCommunity.addValueChangeListener(e -> {

			if (!openData) {
				if (cbCommunity.getValue() != null && cbDistrict.getValue() != null && !openedOnce) {
					openedOnce = true;
					CampaignFormMetaDto campaignForm = FacadeProvider.getCampaignFormMetaFacade()
							.getCampaignFormMetaByUuid(campaignFormMeta.getUuid());

					CampaignDto campaign = FacadeProvider.getCampaignFacade().getByUuid(campaignReferenceDto.getUuid());

					CommunityReferenceDto community = (CommunityReferenceDto) cbCommunity.getValue();

					CommunityDto comdto = FacadeProvider.getCommunityFacade().getByUuid(community.getUuid());

					String formuuid = FacadeProvider.getCampaignFormDataFacade().getByClusterDropDown(community,
							campaignForm, campaign);

					VaadinService.getCurrentRequest().getWrappedSession().setAttribute("Clusternumber",
							comdto.getExternalId());
					VaadinService.getCurrentRequest().getWrappedSession().setAttribute("Clusternumber",
							comdto.getExternalId());
//				
					logger.debug(comdto.getExternalId() + "?comdto.getExternalId() going to session |" + formuuid
							+ "| >>>>>>" + comdto.getClusterNumber());
//				
					if (campaignForm.getFormCategory() == FormAccess.ADMIN
							|| campaignForm.getFormCategory() == FormAccess.MODALITY_PRE
							|| campaignForm.getFormCategory() == FormAccess.MODALITY_POST) {
						if (!formuuid.equals("nul")) {

							CampaignFormDataDto formData = FacadeProvider.getCampaignFormDataFacade()
									.getCampaignFormDataByUuid(formuuid);

							if (formData.getFormValues() != null) {

								formData.getFormValues().forEach(
										formValue -> formValuesMap.put(formValue.getId(), formValue.getValue()));
							}

							// setFormValues(formData.getFormValues());
							remove(vertical);
							buildForm(false);
							vertical.setVisible(true);

						} else {
							buildForm(true);
							vertical.setVisible(true);
						}
					} else {
						buildForm(true);
						vertical.setVisible(true);
					}
				}

			}

		});
		
		
		
		Icon cancelIcon = VaadinIcon.EXCLAMATION_CIRCLE_O.create();
		cancelIcon.getStyle().set("color", "red !important");
		
		cancelFormDataUnitAssignment.setIcon(cancelIcon);
		cancelFormDataUnitAssignment.addThemeVariants(ButtonVariant.LUMO_ERROR);
		
		updateFormDataUnitAssignment.setIcon(VaadinIcon.CHECK_CIRCLE_O.create());
		
		updateFormDataUnitAssignment.setVisible(false);
		cancelFormDataUnitAssignment.setVisible(false);

		reassignDataConfigUnit.addClickListener(e -> {
			cbCommunity.setReadOnly(false);
			updateFormDataUnitAssignment.setVisible(true);
			cancelFormDataUnitAssignment.setVisible(true);

		});

		this.openData = openData;
		this.uuidForm = uuidForm;
//		this.formElements = formElements;
		this.campaignReferenceDto = campaignReferenceDto;
		this.campaignFormMeta = campaignFormMetaUUID;
		this.isDistrictEntry = isDistrictEntry;
		this.formName = formName;
		
		cancelFormDataUnitAssignment.addClickListener(e -> {
			if (updateFormDataUnitAssignment.isVisible() || cbCommunity.isEnabled()) {
				updateFormDataUnitAssignment.setVisible(false);
				cbCommunity.setReadOnly(true);
				cancelFormDataUnitAssignment.setVisible(false);
			}
			
			
			
		});

		updateFormDataUnitAssignment.addClickListener(e -> {
			
			/*
			 * Add a validation for admin forms to avoid overriding the cluster on admin data if the cliuster already exists with data
			 * 
			 * Also confirm if it is fine to ovveride data of clusters that already have data 
			 * 
			 * 
			 * 
			 */
			
//			String campaignuuid = ""; 
//			
//			String formUuid = ""; 
//			
//			String clusterUUid  = ""; 
//			
//			String formCategory  = ""; 
//
//


			try {

				System.out.println("openData=-" + openData);
				System.out.println("uuidForm=-" + uuidForm);
				System.out.println("campaignFormMeta=-" + campaignFormMeta.getUuid());
				System.out.println("campaignReferenceDto=-" + campaignReferenceDto.getUuid());
				System.out.println("cbCommunity=-" + cbCommunity.getValue().toString() + "ttt"
						+ cbCommunity.getValue().getUuid().toString());
				FacadeProvider.getCampaignFormDataFacade().updateFormDataUnitAssignment(uuidForm,
						cbCommunity.getValue().getUuid().toString());
			} catch (Exception ex) {

			} finally {

				Notification.show("Form Configuration Unit Updated Succesfully");
				cbCommunity.setReadOnly(true);
				updateFormDataUnitAssignment.setVisible(false);
				cancelFormDataUnitAssignment.setVisible(false);


				UserActivitySummaryDto userActivitySummaryDto = new UserActivitySummaryDto();
				userActivitySummaryDto.setActionModule("Population Data Import");
				userActivitySummaryDto
						.setAction("User Updated Form Data Cluster Assignment " + cbCampaign.getValue().toString());
				UserProvider usr = new UserProvider();

				userActivitySummaryDto.setCreatingUser_string(usr.getUser().getUserName());
				FacadeProvider.getUserFacade().saveUserActivitySummary(userActivitySummaryDto);

			}
		});

		reassigmentLayout.add(reassignDataConfigUnit, updateFormDataUnitAssignment , cancelFormDataUnitAssignment);

		if (uuidForm != null) {
			if (currentUser.getUserRoles().contains(UserRole.ADMIN)
					|| currentUser.getUserRoles().contains(UserRole.COMMUNITY_INFORMANT)) {
				vertical_.add(cbCampaign, formDate, cbArea, cbRegion, cbDistrict, cbCommunity, reassigmentLayout);
			} else {
				vertical_.add(cbCampaign, formDate, cbArea, cbRegion, cbDistrict, cbCommunity);

			}

		} else {
			vertical_.add(cbCampaign, formDate, cbArea, cbRegion, cbDistrict, cbCommunity);

		}

		vertical_.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("520px", 2),
				new ResponsiveStep("1000px", 3));
		add(vertical_);

		if (userProvider.getUser().getArea() != null) {
			AreaReferenceDto singleArea = userProvider.getUser().getArea();
			AreaDto singleAreaDto = FacadeProvider.getAreaFacade().getByUuid(singleArea.getUuid());

			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				AreaReferenceDto singleAreatw0 = new AreaReferenceDto(singleAreaDto.getUuid(),
						singleAreaDto.getPs_af());
				cbArea.setValue(singleAreatw0);
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				AreaReferenceDto singleAreatw0 = new AreaReferenceDto(singleAreaDto.getUuid(),
						singleAreaDto.getFa_af());
				cbArea.setValue(singleAreatw0);
			} else {
				cbArea.setValue(userProvider.getUser().getArea());
			}

			// rda56kGbCAja
			cbArea.setReadOnly(true);
			;

			List<RegionReferenceDto> provinces = FacadeProvider.getRegionFacade()
					.getAllActiveByArea(userProvider.getUser().getArea().getUuid());
			cbRegion.clear();
			cbRegion.setReadOnly(false);
			;
			cbRegion.setItems(provinces);
		}

		if (userProvider.getUser().getRegion() != null) {
			RegionReferenceDto singleRegion = userProvider.getUser().getRegion();
			RegionDto singleRegionDto = FacadeProvider.getRegionFacade().getByUuid(singleRegion.getUuid());

			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				RegionReferenceDto singleRegiontw0 = new RegionReferenceDto(singleRegionDto.getUuid(),
						singleRegionDto.getPs_af());
				cbRegion.setValue(singleRegiontw0);
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				RegionReferenceDto singleRegiontw0 = new RegionReferenceDto(singleRegionDto.getUuid(),
						singleRegionDto.getFa_af());
				cbRegion.setValue(singleRegiontw0);
			} else {
				cbRegion.setValue(userProvider.getUser().getRegion());
			}

			cbRegion.setReadOnly(true);
			;

			List<DistrictReferenceDto> districts = FacadeProvider.getDistrictFacade()
					.getAllActiveByRegion(userProvider.getUser().getRegion().getUuid());
			cbDistrict.clear();
			cbDistrict.setReadOnly(false);
			;
			cbDistrict.setItems(districts);
		}

		if (userProvider.getUser().getDistrict() != null) {
			DistrictReferenceDto singleDistrict = userProvider.getUser().getDistrict();
			DistrictDto singleDistrictDto = FacadeProvider.getDistrictFacade().getByUuid(singleDistrict.getUuid());

			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				DistrictReferenceDto singleDistricttw0 = new DistrictReferenceDto(singleDistrictDto.getUuid(),
						singleDistrictDto.getPs_af());
				cbDistrict.setValue(singleDistricttw0);
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				DistrictReferenceDto singleDistricttw0 = new DistrictReferenceDto(singleDistrictDto.getUuid(),
						singleDistrictDto.getFa_af());
				cbDistrict.setValue(singleDistricttw0);
			} else {
				cbDistrict.setValue(userProvider.getUser().getDistrict());
			}

			cbDistrict.setReadOnly(true);
			;

			List<CommunityReferenceDto> communities = FacadeProvider.getCommunityFacade()
					.getAllActiveByDistrict(userProvider.getUser().getDistrict().getUuid());
			
			communities.sort(Comparator.comparingInt(CommunityReferenceDto::getNumber));

			
			cbCommunity.clear();
			cbCommunity.setReadOnly(false);
			;
			
		
			cbCommunity.setItems(communities);
			
			cbCommunity.setItemLabelGenerator(itm -> {
				CommunityReferenceDto dcfv = (CommunityReferenceDto) itm;
				return dcfv.getNumber() + " | " + dcfv.getCaption();
			});
		}

		if (userProvider.getUser().getCommunity() != null) {
			
			cbCommunity.clear();
			
			List<CommunityReferenceDto> items = userProvider.getUser().getCommunity().stream()
					.collect(Collectors.toList());
			
			
			for (CommunityReferenceDto item : items) {
				item.setCaption(item.getNumber() != null ? item.getNumber().toString() : item.getCaption());
			}
			
//			System.out.println(item  +  " Item caption ");
			
			System.out.println(items +  " items from form builder ");
			System.out.println(CommunityReferenceDto.clusternumber +  " items from form builder ");

//			Collections.sort(items, CommunityReferenceDto.clusternumber);
			items.sort(Comparator.comparingInt(CommunityReferenceDto::getNumber));

			cbCommunity.setItems(items);
		}

		if (openData) {
			CampaignFormDataDto formData = FacadeProvider.getCampaignFormDataFacade()
					.getCampaignFormDataByUuid(uuidForm);
			
			System.out.println( formData +  "checking if formm data is null fom form builder");

			LocalDate localDate = formData.getFormDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			
			System.out.println( localDate +  "checking if localDate is null fom form builder" + formData.getFormDate());


			formDate.setValue(localDate);
			cbArea.clear();
			cbArea.setValue(formData.getArea());
			cbRegion.clear();
			cbRegion.setValue(formData.getRegion());
			cbDistrict.clear();
			cbDistrict.setValue(formData.getDistrict());
			cbCommunity.clear();
			cbCommunity.setValue(formData.getCommunity());

			if (formData.getFormValues() != null) {

				formData.getFormValues()
						.forEach(formValue -> formValuesMap.put(formValue.getId(), formValue.getValue()));
			}

			buildForm(false);
			vertical.setVisible(true);

			cbArea.setReadOnly(true);
			;
			cbRegion.setReadOnly(true);
			;
			cbDistrict.setReadOnly(true);
			;
			cbCommunity.setReadOnly(true);
			;
			formDate.setReadOnly(true);
			;

		}

	}

	public void buildForm(boolean isNewForm) {
		int currentCol = -1;
		int sectionCount = 0;

		int ii = 0;

		TabSheet accrd = new TabSheet();
		accrd.setHeight(750, Unit.PIXELS);

		int accrd_count = 0;

		for (CampaignFormElement formElement : formElements) {
			CampaignFormElementOptions campaignFormElementOptions = new CampaignFormElementOptions();
			CampaignFormElementType type = CampaignFormElementType.fromString(formElement.getType());
			String fieldId = formElement.getId();
			List<CampaignFormElementStyle> styles;
			if (formElement.getStyles() != null) {
				styles = Arrays.stream(formElement.getStyles()).map(CampaignFormElementStyle::fromString)
						.collect(Collectors.toList());
			} else {
				styles = new ArrayList<>();
			}

			if (formElement.getOptions() != null) {
				campaignFormElementOptions = new CampaignFormElementOptions();

				optionsValues = formElement.getOptions().stream()
						.collect(Collectors.toMap(MapperUtil::getKey, MapperUtil::getCaption));

				if (userLocale != null) {
					if (translationsOpt != null) {
						translationsOpt.stream().filter(t -> t.getLanguageCode().equals(userLocale.toString()))
								.findFirst()
								.ifPresent(filteredTranslations -> filteredTranslations.getTranslations().stream()
										.filter(cd -> cd.getElementId().equals(formElement.getId())).findFirst()
										.ifPresent(optionsList -> {
											if (optionsList.getOptions() != null) {
												userOptTranslations = optionsList.getOptions().stream()
														.filter(c -> c != null && c.getCaption() != null)
														.collect(Collectors.toMap(MapperUtil::getKey,
																MapperUtil::getCaption));
											}
										}));
					}
				}

				if (userOptTranslations.size() == 0) {
					campaignFormElementOptions.setOptionsListValues(optionsValues);
					// get18nOptCaption(formElement.getId(), optionsValues));
				} else {
					campaignFormElementOptions.setOptionsListValues(userOptTranslations);
				}

			} else {
				optionsValues = new LinkedHashMap<String, String>();
			}

			if (formElement.getConstraints() != null) {
				campaignFormElementOptions = new CampaignFormElementOptions();
				constraints = (List<String>) Arrays.stream(formElement.getConstraints()).collect(Collectors.toList());
				ListIterator<String> lstItemsx = constraints.listIterator();
				int i = 1;
				while (lstItemsx.hasNext()) {
					String lss = lstItemsx.next().toString();
					if (lss.toLowerCase().contains("max")) {
						campaignFormElementOptions.setMax(Integer.parseInt(lss.substring(lss.lastIndexOf("=") + 1)));
					} else if (lss.toLowerCase().contains("min")) {
						campaignFormElementOptions.setMin(Integer.parseInt(lss.substring(lss.lastIndexOf("=") + 1)));
					} else if (lss.toLowerCase().contains("expression")) {
						campaignFormElementOptions.setExpression(true);
					}
				}

			}
			// input:checked

			String dependingOnId = formElement.getDependingOn();
			Object[] dependingOnValues = formElement.getDependingOnValues();

			Object value = formValuesMap.get(formElement.getId());

			int occupiedColumns = getOccupiedColumns(type, styles);

			final HashMap<String, String> data = (HashMap<String, String>) campaignFormElementOptions
					.getOptionsListValues();

			if (type == CampaignFormElementType.DAYWISE) {
				accrd_count++;
				if (accrd_count > 1) {

					final FormLayout layout = new FormLayout(vertical);
					// layout.addComponent(label);
					int temp = accrd_count;
					temp = temp - 1;
					layout.setClassName("daywise_background_" + temp); // .addStyleName(dependingOnId); sormas
																		// background: green
					accrd.add(get18nCaption(formElement.getId(), formElement.getCaption()), layout);

					vertical = new FormLayout();
					vertical.setSizeFull();
					vertical.setWidthFull();
					vertical.setHeightFull();

				}
			} else if (type == CampaignFormElementType.SECTION) {
				sectionCount++;

				vertical.setId("formSectionId-" + sectionCount);

			} else if (type == CampaignFormElementType.LABEL) {

				Label labx = new Label();
				labx.getElement().setProperty("innerHTML", get18nCaption(formElement.getId(),
						get18nCaption(formElement.getId(), formElement.getCaption())));
				labx.setId(formElement.getId());

				VerticalLayout labelLayout = new VerticalLayout();

				labelLayout.add(labx);
				vertical.setColspan(labelLayout, 3);
				vertical.add(labelLayout);
				if (dependingOnId != null && dependingOnValues != null) {
					// needed
					setVisibilityDependency(labx, dependingOnId, dependingOnValues, type, false);
				}
			} else {
				CampaignFormElementOptions constrainsVal = new CampaignFormElementOptions();
				boolean fieldIsRequired = formElement.isImportant();

				if (type == CampaignFormElementType.YES_NO) {

//					HashMap<Boolean, String> map = new HashMap<>();
//					map.put(true, I18nProperties.getCaption(Captions.actionYes));
//					map.put(false, I18nProperties.getCaption(Captions.actionNo));

					ToggleButtonGroup<Boolean> toggle = new ToggleButtonGroup<>(
							get18nCaption(formElement.getId(), formElement.getCaption()), List.of(true, false));
					toggle.setId(formElement.getId());

					toggle.setClassName("customTextWrap");

					HashMap<Boolean, String> map = new HashMap<>();
					map.put(true, "Yes");
					map.put(false, "No");

					HashMap<Boolean, String> mapPashto = new HashMap<>();
					mapPashto.put(true, "هو");
					mapPashto.put(false, "نه");

					HashMap<Boolean, String> mapDari = new HashMap<>();
					mapDari.put(true, "آره");
					mapDari.put(false, "خیر");

					toggle.setItemLabelGenerator(item -> {
						switch (currentUser.getUser().getLanguage().toString()) {
						case "Pashto":
							return mapPashto.get(item);
						case "Dari":
							return mapDari.get(item);
						default:
							return map.get(item);
						}
					});

//					toggle.setItemLabelGenerator(item -> map.get(item));
					toggle.getStyle().set("color", "Green");
					toggle.getStyle().set("background", "white");

					setFieldValue(toggle, type, value, optionsValues, formElement.getDefaultvalue(), false, null);

					vertical.add(toggle);
					fields.put(formElement.getId(), toggle);
//					System.out.println(dependingOnId + "dependingOnId11111111111111111111111 " + dependingOnValues);

					if (dependingOnId != null && dependingOnValues != null) {

//						System.out.println(dependingOnId + "dependingOnId 2222222222222222" + dependingOnValues
//								+ "tttttt" + formElement.isImportant());
						// needed
						setVisibilityDependency(toggle, dependingOnId, dependingOnValues, type,
								formElement.isImportant());
					} else {
						toggle.setRequiredIndicatorVisible(formElement.isImportant());
					}

				} else if (type == CampaignFormElementType.TEXT) {
					TextField textField = new TextField();
					textField.setLabel(get18nCaption(formElement.getId(), formElement.getCaption()));
					textField.setClassName("customTextWrap");

					// textField.setValue("Ruukinkatu 2");
					textField.setClearButtonVisible(true);
					textField.setPrefixComponent(VaadinIcon.PENCIL.create());
					textField.setId(formElement.getId());
					textField.setSizeFull();
					//
					setFieldValue(textField, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					vertical.add(textField);
					fields.put(formElement.getId(), textField);

					if (dependingOnId != null && dependingOnValues != null) {
						// needed
						setVisibilityDependency(textField, dependingOnId, dependingOnValues, type,
								formElement.isImportant());
					} else {
						textField.setRequiredIndicatorVisible(formElement.isImportant());
					}

				} else if (type == CampaignFormElementType.NUMBER) {
					NumberField numberField = new NumberField();
					numberField.setLabel(get18nCaption(formElement.getId(), formElement.getCaption()));
					numberField.setClassName("customTextWrap");

					numberField.setId(formElement.getId());
					numberField.setSizeFull();

					setFieldValue(numberField, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					vertical.add(numberField);
					fields.put(formElement.getId(), numberField);

					// Binder<String> binder = new Binder<>(String.class);

					if (fieldId.equalsIgnoreCase("Villagecode")) {
						numberField.setAllowedCharPattern("(?!.*000$).*");
//								 new RegexpValidator("(?!.*000$).*", I18nProperties.getValidationError(
//											errormsg == null ? caption + ": " + Validations.onlyDecimalNumbersAllowed : errormsg, caption) ));

						numberField.addValueChangeListener(e -> {

							String inputValue = e.getValue() != null ? e.getValue().toString() : "";

							if (e.getValue() != null && e.getValue().toString().length() == 3) {
								String result = inputValue.substring(0, 1);
								logger.debug(result + " resultrrrr lento " + e.getValue().toString().length()
										+ "ttt11111111111" + inputValue + " tttttttttttttttttt" + result.length());

								// Checking the finl length of the trimmd val
								int length = result.length();
								if (length == 1 && VaadinService.getCurrentRequest().getWrappedSession()
										.getAttribute("Clusternumber") != null) {

									String cCodeLengthCheck = VaadinService.getCurrentRequest().getWrappedSession()
											.getAttribute("Clusternumber").toString();

									int ccodeCheckLength = cCodeLengthCheck.length();
									if (ccodeCheckLength == 6) {
										result = VaadinService.getCurrentRequest().getWrappedSession()
												.getAttribute("Clusternumber") + "000" + result;
									} else if (ccodeCheckLength == 7) {
										result = VaadinService.getCurrentRequest().getWrappedSession()
												.getAttribute("Clusternumber") + "00" + result;
									}

									// Prefix "00" for single-digit numbers

									logger.debug(result + " resultrrrr lento ttt222222222222tttttttttttttttttt"
											+ result.length());

								}

								numberField.setValue(Double.parseDouble(result));

							}
							if (e.getValue() != null && e.getValue().toString().length() == 4) {
								String result = inputValue.substring(0, e.getValue().toString().length() - 2);

//								logger.debug(result + " result lento ttttttttttttttttttttt" + result.length() );

								int length = result.length();
								if (length == 2 && VaadinService.getCurrentRequest().getWrappedSession()
										.getAttribute("Clusternumber") != null) {
									// Prefix "0" for single-digit numbers
									String cCodeLengthCheck = VaadinService.getCurrentRequest().getWrappedSession()
											.getAttribute("Clusternumber").toString();

									int ccodeCheckLength = cCodeLengthCheck.length();
									if (ccodeCheckLength == 6) {
										result = VaadinService.getCurrentRequest().getWrappedSession()
												.getAttribute("Clusternumber") + "00" + result;
									} else if (ccodeCheckLength == 7) {
										result = VaadinService.getCurrentRequest().getWrappedSession()
												.getAttribute("Clusternumber") + "0" + result;
									}
//					            	result  =VaadinService.getCurrentRequest().getWrappedSession()
//											.getAttribute("Clusternumber") + "0" + result;
//									logger.debug(result + " resultrrrr lento ttttttttttttttttttttt" + result.length() );

								}

								numberField.setValue(Double.parseDouble(result));

							}
							if (e.getValue() != null && e.getValue().toString().length() == 5) {
//								logger.debug(e.getValue() + "lento ttttttttttttttttttttt" + e.getValue().toString().length() );
								String result = inputValue.substring(0, e.getValue().toString().length() - 2);

//								logger.debug(result + " result lento ttttttttttttttttttttt" + result.length() );

//					            String trimmedValue = inputValue.replaceFirst("^0+(?!$)", "");
								// Check the length of the trimmed value
								int length = result.length();
								if (length == 3 && VaadinService.getCurrentRequest().getWrappedSession()
										.getAttribute("Clusternumber") != null) {
									// Prefix "00" for single-digit numbers

									String cCodeLengthCheck = VaadinService.getCurrentRequest().getWrappedSession()
											.getAttribute("Clusternumber").toString();

									int ccodeCheckLength = cCodeLengthCheck.length();
									if (ccodeCheckLength == 6) {
										result = VaadinService.getCurrentRequest().getWrappedSession()
												.getAttribute("Clusternumber") + "0" + result;
									} else if (ccodeCheckLength == 7) {
										result = VaadinService.getCurrentRequest().getWrappedSession()
												.getAttribute("Clusternumber") + result;
									}

									numberField.setValue(Double.parseDouble(result));

								}
							}
						});

					}

					if (fieldId.equalsIgnoreCase("PopulationGroup_0_4")) {

						numberField.addValueChangeListener(e -> {
							if (VaadinService.getCurrentRequest().getWrappedSession()
									.getAttribute("populationdata") != null) {

								final String des = VaadinService.getCurrentRequest().getWrappedSession()
										.getAttribute("populationdata").toString();
								numberField.setValue(Double.parseDouble(des));
								numberField.setReadOnly(true);
							}

						});

					}

					if (fieldId.equalsIgnoreCase("PopulationGroup_5_10")) {

						numberField.addValueChangeListener(e -> {
							if (VaadinService.getCurrentRequest().getWrappedSession()
									.getAttribute("populationdata") != null) {

								final String des = VaadinService.getCurrentRequest().getWrappedSession()
										.getAttribute("populationdata").toString();
								numberField.setValue(Double.parseDouble(des));
								numberField.setReadOnly(true);
							}

						});

					}

					if (dependingOnId != null && dependingOnValues != null) {
						// needed
						setVisibilityDependency(numberField, dependingOnId, dependingOnValues, type,
								formElement.isImportant());
					} else {
						numberField.setRequiredIndicatorVisible(formElement.isImportant());
					}

				} else if (type == CampaignFormElementType.RANGE) {
					IntegerField integerField = new IntegerField();
					integerField.setLabel(get18nCaption(formElement.getId(), formElement.getCaption()));
					integerField.setClassName("customTextWrap");

//					integerField.setHelperText("Max 10 items");
					integerField.setId(formElement.getId());
					integerField.setStepButtonsVisible(true);
					integerField.setSizeFull();

					setFieldValue(integerField, type, value, optionsValues, formElement.getDefaultvalue(), false, null);

					vertical.add(integerField);
					fields.put(formElement.getId(), integerField);

					String validationMessageTag = "";
					Map<String, Object> validationMessageArgs = new HashMap<>();

					if (constrainsVal.isExpression()) {

						if (!fieldIsRequired) {
							// ApmisNotification notification = new ApmisNotification("Application
							// submitted!");
						}

						constrainsVal.setExpression(false);

					} else {

						if (constrainsVal.getMin() != null || constrainsVal.getMax() != null) {

							integerField.setMin(constrainsVal.getMin());
							integerField.setMax(constrainsVal.getMax());

							if (constrainsVal.getMin() == null) {
								validationMessageTag = Validations.numberTooBig;
								validationMessageArgs.put("value", constrainsVal.getMax());
							} else if (constrainsVal.getMax() == null) {
								validationMessageTag = Validations.numberTooSmall;
								validationMessageArgs.put("value", constrainsVal.getMin());
							} else {
								validationMessageTag = Validations.numberNotInRange;
								validationMessageArgs.put("min", constrainsVal.getMin());
								validationMessageArgs.put("max", constrainsVal.getMax());
							}

							// needed
							// field.addValidator(
							// new NumberValidator(I18nProperties.getValidationError(validationMessageTag,
							// validationMessageArgs), minValue, maxValue));

//							((TextField) field).addValidator(new NumberNumericValueValidator(
//									caption.toUpperCase() + ": "
//											+ 
//											
//											I18nProperties.getValidationError(validationMessageTag,
//													validationMessageArgs),
//									constrainsVal.getMin(), constrainsVal.getMax(), true, isOnError));

						} else {

							// needed
							// This should throw error as range suppose to have min and max if not taken
							// care by expression
						}
					}

					if (dependingOnId != null && dependingOnValues != null) {
						// needed
						setVisibilityDependency(integerField, dependingOnId, dependingOnValues, type,
								formElement.isImportant());
					} else {
						integerField.setRequiredIndicatorVisible(formElement.isImportant());
					}

				} else if (type == CampaignFormElementType.DECIMAL) {
					BigDecimalField bigDecimalField = new BigDecimalField();
					bigDecimalField.setLabel(get18nCaption(formElement.getId(), formElement.getCaption()));
					bigDecimalField.setClassName("customTextWrap");

					bigDecimalField.setWidth("240px");
					bigDecimalField.setValue(new BigDecimal("948205817.472950487"));
					bigDecimalField.setId(formElement.getId());
					bigDecimalField.setSizeFull();
					setFieldValue(bigDecimalField, type, value, optionsValues, formElement.getDefaultvalue(), false,
							null);
					vertical.add(bigDecimalField);
					fields.put(formElement.getId(), bigDecimalField);

					if (dependingOnId != null && dependingOnValues != null) {
						// needed
						setVisibilityDependency(bigDecimalField, dependingOnId, dependingOnValues, type,
								formElement.isImportant());
					} else {
						bigDecimalField.setRequiredIndicatorVisible(formElement.isImportant());
					}

				} else if (type == CampaignFormElementType.TEXTBOX) {
					TextArea textArea = new TextArea();
					textArea.setWidthFull();
					textArea.setLabel(get18nCaption(formElement.getId(), formElement.getCaption()));
					textArea.setClassName("customTextWrap");
					textArea.setId(formElement.getId());
					textArea.setSizeFull();
					setFieldValue(textArea, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					vertical.add(textArea);
					fields.put(formElement.getId(), textArea);

					if (dependingOnId != null && dependingOnValues != null) {
						// needed
						setVisibilityDependency(textArea, dependingOnId, dependingOnValues, type,
								formElement.isImportant());
					} else {
						textArea.setRequiredIndicatorVisible(formElement.isImportant());
					}

				} else if (type == CampaignFormElementType.RADIO) {
					RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
					radioGroup.setLabel(get18nCaption(formElement.getId(), formElement.getCaption()));
					radioGroup.setClassName("customTextWrap");

//					data = (HashMap<String, String>) campaignFormElementOptions
//							.getOptionsListValues();
					radioGroup.setItems(data.keySet().stream().collect(Collectors.toList()));

					radioGroup.setItemLabelGenerator(itm -> data.get(itm.toString().trim()));
					radioGroup.setId(formElement.getId());
					radioGroup.setSizeFull();
					setFieldValue(radioGroup, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					vertical.add(radioGroup);
					fields.put(formElement.getId(), radioGroup);

					if (dependingOnId != null && dependingOnValues != null) {
						// needed
						setVisibilityDependency(radioGroup, dependingOnId, dependingOnValues, type,
								formElement.isImportant());
					} else {
						radioGroup.setRequiredIndicatorVisible(formElement.isImportant());
					}

				} else if (type == CampaignFormElementType.RADIOBASIC) {
					RadioButtonGroup<String> radioGroupVert = new RadioButtonGroup<>();
					radioGroupVert.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
					radioGroupVert.setLabel(get18nCaption(formElement.getId(), formElement.getCaption()));
					radioGroupVert.setClassName("customTextWrap");

//					data = (HashMap<String, String>) campaignFormElementOptions
//							.getOptionsListValues();
					radioGroupVert.setItems(data.keySet().stream().collect(Collectors.toList()));
					radioGroupVert.setItemLabelGenerator(itm -> data.get(itm.toString().trim()));

					radioGroupVert.setId(formElement.getId());
					radioGroupVert.setSizeFull();
					setFieldValue(radioGroupVert, type, value, optionsValues, formElement.getDefaultvalue(), false,
							null);
					vertical.add(radioGroupVert);
					fields.put(formElement.getId(), radioGroupVert);

					if (dependingOnId != null && dependingOnValues != null) {
						// needed
						setVisibilityDependency(radioGroupVert, dependingOnId, dependingOnValues, type,
								formElement.isImportant());
					} else {
						radioGroupVert.setRequiredIndicatorVisible(formElement.isImportant());
					}

				} else if (type == CampaignFormElementType.DROPDOWN) {
					// Note: carrying out the option sorting only i the dropdown
					// to avoid getting a null pointer fro other input types with the
					// option method because making all the checks global would require including
					// the order value in other
					// input types that are not dropdown

					// get the order valuie, do a null check incase order wouldnt be specified
					boolean isNotSorted = false;
					try {
						if (formElement.getOptions().stream()
								.collect(Collectors.toMap(MapperUtil::getKey, MapperUtil::getOrder)) != null) {
							optionsOrder.clear();
							// pop the map with the order based off the key
							optionsOrder = formElement.getOptions().stream()
									.collect(Collectors.toMap(MapperUtil::getKey, MapperUtil::getOrder));
						}
						;
					} catch (NullPointerException ex) {
						optionsOrder.clear();
						optionsOrder = formElement.getOptions().stream()
								.collect(Collectors.toMap(MapperUtil::getKey, MapperUtil::getCaption));
						isNotSorted = true;
					}

					if (userOptTranslations.size() == 0) {
						campaignFormElementOptions.setOptionsListValues(optionsValues);

//<<<<<<< HEAD
					} else {
						campaignFormElementOptions.setOptionsListValues(userOptTranslations);
					}
					// Trying toGetting order when using translation(not adequately tes)
					if (optionsOrder != null) {
						if (userOptTranslations.size() == 0) {
							campaignFormElementOptions.setOptionsListOrder(optionsOrder);
						} else {
							campaignFormElementOptions.setOptionsListOrder(optionsOrder);
						}

					}

					final HashMap<String, String> dataOrder = (HashMap<String, String>) campaignFormElementOptions
							.getOptionsListOrder();

					ComboBox<String> select = new ComboBox<>(
							get18nCaption(formElement.getId(), formElement.getCaption()));
					select.setClassName("customTextWrap");

					List<String> sortedKeys = new ArrayList<>(data.keySet()); // Create a list of keys
					if (!isNotSorted) {
						if (dataOrder != null) {
							Comparator<String> orderComparator = (key1, key2) -> {
								String order1 = getOrderValue(dataOrder, key1);
								String order2 = getOrderValue(dataOrder, key2);
								return Integer.compare(Integer.parseInt(order1), Integer.parseInt(order2));
							};

							sortedKeys.sort(orderComparator);
						}
					}

					select.setItems(sortedKeys);

					select.setItemLabelGenerator(itm -> data.get(itm.toString().trim()));
					select.setClearButtonVisible(true);

					select.addValueChangeListener(ee -> {
					});

					setFieldValue(select, type, value, optionsValues, formElement.getDefaultvalue(), false, null);

					vertical.add(select);
					fields.put(formElement.getId(), select);

					System.out.println(dependingOnId + " dependingOnId 3333333333333333333333333" + dependingOnValues);

					if (dependingOnId != null && dependingOnValues != null) {
						// needed

						System.out.println(dependingOnId + " dependingOnId 44444444444444444444444" + dependingOnValues
								+ "44444444444444444444444" + formElement.isImportant());

						setVisibilityDependency(select, dependingOnId, dependingOnValues, type,
								formElement.isImportant());

					} else {
						select.setRequiredIndicatorVisible(formElement.isImportant());
					}

				} else if (type == CampaignFormElementType.CHECKBOX) {
					CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
					checkboxGroup.setLabel(get18nCaption(formElement.getId(), formElement.getCaption()));
					checkboxGroup.setClassName("customTextWrap");

//					data = (HashMap<String, String>) campaignFormElementOptions
//							.getOptionsListValues();
					checkboxGroup.setItems(data.keySet().stream().collect(Collectors.toList()));
					checkboxGroup.setItemLabelGenerator(itm -> data.get(itm.toString().trim()));

					checkboxGroup.setId(formElement.getId());
					checkboxGroup.setSizeFull();
					setFieldValue(checkboxGroup, type, value, optionsValues, formElement.getDefaultvalue(), false,
							null);
					vertical.add(checkboxGroup);
					fields.put(formElement.getId(), checkboxGroup);

					if (dependingOnId != null && dependingOnValues != null) {
						// needed
						setVisibilityDependency(checkboxGroup, dependingOnId, dependingOnValues, type,
								formElement.isImportant());
					} else {
						checkboxGroup.setRequiredIndicatorVisible(formElement.isImportant());
					}

				} else if (type == CampaignFormElementType.CHECKBOXBASIC) {
					CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
					checkboxGroup.setLabel(get18nCaption(formElement.getId(), formElement.getCaption()));
					checkboxGroup.setClassName("customTextWrap");

//					data = (HashMap<String, String>) campaignFormElementOptions
//							.getOptionsListValues();
					checkboxGroup.setItems(data.keySet().stream().collect(Collectors.toList()));
					checkboxGroup.setItemLabelGenerator(itm -> data.get(itm.toString().trim()));

					checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
					checkboxGroup.setId(formElement.getId());
					checkboxGroup.setSizeFull();
					setFieldValue(checkboxGroup, type, value, optionsValues, formElement.getDefaultvalue(), false,
							null);
					vertical.add(checkboxGroup);
					fields.put(formElement.getId(), checkboxGroup);

					if (dependingOnId != null && dependingOnValues != null) {
						// needed
						setVisibilityDependency(checkboxGroup, dependingOnId, dependingOnValues, type,
								formElement.isImportant());
					} else {
						checkboxGroup.setRequiredIndicatorVisible(formElement.isImportant());
					}

				} else if (type == CampaignFormElementType.DATE) {
					DatePicker.DatePickerI18n singleFormatI18n = new DatePicker.DatePickerI18n();
					singleFormatI18n.setDateFormat("dd-MM-yyyy");

					DatePicker datePicker = new DatePicker(
							get18nCaption(formElement.getId(), formElement.getCaption()));
					datePicker.setClassName("customTextWrap");

					datePicker.setI18n(singleFormatI18n);
					datePicker.setSizeFull();
					datePicker.setPlaceholder("DD-MM-YYYY");
					datePicker.setId(formElement.getId());
					setFieldValue(datePicker, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					vertical.add(datePicker);
					fields.put(formElement.getId(), datePicker);

					if (dependingOnId != null && dependingOnValues != null) {
						// needed
						setVisibilityDependency(datePicker, dependingOnId, dependingOnValues, type,
								formElement.isImportant());
					} else {
						datePicker.setRequiredIndicatorVisible(formElement.isImportant());
					}

				}

//needed

			}

			if (accrd_count == 0) {
				vertical.setVisible(false);
				add(vertical);
			} else {

				add(accrd);
			}

			userOptTranslations = new HashMap<String, String>();
		}
		checkExpression();
		disableExpressionFieldsForEditing();
		vertical.setSizeFull();
		vertical.setId("vertical_nn");
		vertical.setResponsiveSteps(
				// Use one column by default
				new ResponsiveStep("0", 1), new ResponsiveStep("520px", 2), new ResponsiveStep("1000px", 3));

		setId("campaignFormLayout");
		setSizeFull();
	}

	private String getOrderValue(Map<String, String> data, String key) {
		String orderValue = data.get(key);
		if (orderValue != null) {
			return orderValue;
		}
		return orderValue;
	}

	public <T extends Component> void setFieldValue(T field, CampaignFormElementType type, Object value,
			Map<String, String> options, String defaultvalue, Boolean isErrored, Object defaultErrorMsgr) {
		Boolean isExpressionValue = false;
		switch (type) {
		case YES_NO:

			if (value != null) {
				if (value instanceof Boolean) {
					((ToggleButtonGroup) field).setValue(value);
				}

				if (value instanceof String) {
					Boolean dvalue = value.toString().equalsIgnoreCase("YES") ? true
							: value.toString().equalsIgnoreCase("NO") ? false
									: value.toString().equalsIgnoreCase("true") ? true
											: value.toString().equalsIgnoreCase("false") ? false : null;
					((ToggleButtonGroup) field).setValue(dvalue);

				}

			} else {

				((ToggleButtonGroup) field).updateStyles();

			}

			break;
		case RANGE:
//			logger.debug("|" + value + "|================|" + defaultErrorMsgr + "|");
			boolean isExxpression = false;
			if (defaultErrorMsgr != null) {
				if (defaultErrorMsgr.toString().endsWith("..")) {
					isExxpression = true;
					defaultErrorMsgr = defaultErrorMsgr.toString().equals("..") ? null
							: defaultErrorMsgr.toString().replace("..", "");
				}
			}

			if (isExxpression && isErrored && value == null) {

				Object tempz = defaultErrorMsgr != null ? defaultErrorMsgr
						: "Data entered not in range or calculated rangexxx!";
				String lb = field.getElement().getProperty("label");

				// clear the input
				// field.getElement().executeJs("this.inputElement.value = ''");

				field.getElement().setProperty("invalid", true);
				field.getElement().setProperty("label", lb == null ? "" : lb);
				field.getElement().setProperty("errorMessage", defaultErrorMsgr != null ? defaultErrorMsgr.toString()
						: "Data entered not in range or calculated range!");

				// Notification.show("Error found", tempz.toString(),
				// Notification.TYPE_TRAY_NOTIFICATION);
			}

			if (value != null) {
				if (value.toString().equals("")) {

//					logger.debug("))))))))))))))))))))))))))):setting empty value to nulll --- not sure");
					((IntegerField) field).setValue(null);
				} else {
					((IntegerField) field).setValue(Integer.parseInt(value.toString()));
				}

			} else if (defaultvalue != null) {
				((IntegerField) field).setValue(Integer.parseInt(defaultvalue));
			} else {
				((IntegerField) field).setValue(null);
			}

			// ((IntegerField) field).setValue(value != null ?
			// Integer.parseInt(value.toString()) : defaultvalue != null ?
			// Integer.parseInt(defaultvalue) : null);

			break;
		case TEXT:

			if (value != null) {
				((TextField) field).setValue(value.toString());

			} else if (defaultvalue != null) {
				((TextField) field).setValue(defaultvalue);
			}
			break;
		case NUMBER:

			if (value != null) {
				String cvalue = value.toString().replace("null", "").trim();
				if (cvalue.equals("") || cvalue.equals("null")) {

//					logger.debug(cvalue + "|))))))))))))))))))))))))))):setting empty value to in |NUMBER nulll --- not sure");
					((NumberField) field).setValue(null);
				} else {

					((NumberField) field).setValue(Double.parseDouble(cvalue));
				}

			} else if (defaultvalue != null) {
				((NumberField) field).setValue(Double.parseDouble(defaultvalue));
			}

			break;

		case DECIMAL:
			if (value != null) {

				((TextField) field).setValue(value != null ? value.toString() : null);
			}
			break;
		case TEXTBOX:

			if (value != null) {

				if (value.equals(true)) {
					((TextArea) field).setEnabled(true);
				} else if (value.equals(false)) {
					((TextArea) field).setEnabled(false);
					// Notification.show("Warning:", Title "Expression resulted in wrong value
					// please check your data 1", Notification.TYPE_WARNING_MESSAGE);
				}
			}
			;
			((TextArea) field).setValue(value != null ? value.toString() : null);
			break;
		case DATE:
			if (value != null) {

				try {

					String vc = value + "";

//					logger.debug(vc.isEmpty() + "@@@=" + vc.equals("") + "==" + vc != "" + "@@ date to parse |"
//							+ value + "|");

					if (vc != "" || !vc.isEmpty() || !vc.equals("")) {
						Date dst = vc.contains("00:00:00") ? dateFormatter(value) : dateFormatterLongAndMobile(value);

						LocalDate value_Date = dst.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						((DatePicker) field).setValue(value_Date);
					}

				} catch (ConversionException e) {
					// TODO Auto-generated catch block
					((DatePicker) field).setValue(null);
					// e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			;
			break;
		case RADIO:
			((RadioButtonGroup) field).setValue(Sets.newHashSet(value).toString().replace("[", "").replace("]", ""));
			break;
		case RADIOBASIC:
			((RadioButtonGroup) field).setValue(Sets.newHashSet(value).toString().replace("[", "").replace("]", ""));
			break;
		case CHECKBOX:
			if (value != null) {
				String dcs = value.toString().replace("[", "").replace("]", "").replaceAll(", ", ",");
				String strArray[] = dcs.split(",");
				for (int i = 0; i < strArray.length; i++) {
					((CheckboxGroup) field).select(strArray[i]);
				}
			}
			;
			break;
		case CHECKBOXBASIC:

			if (value != null) {
				String dcxs = value.toString().replace("[", "").replace("]", "").replaceAll(", ", ",");
				String strArraxy[] = dcxs.split(",");
				for (int i = 0; i < strArraxy.length; i++) {
					((CheckboxGroup) field).select(strArraxy[i]);
				}
			}
			;
			break;
		case DROPDOWN:

			final HashMap<String, String> data_ = (HashMap<String, String>) options;

			// logger.debug(data_+ " : @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ : "+value);

//			if (value != null) {
//
//				if (value.equals(true)) {
//					((ComboBox) field).setEnabled(true);
//				} else if (value.equals(false)) {
//					((ComboBox) field).setEnabled(false);
//				}
//			}
//			;

			if (defaultvalue != null) {
				// String dxz = options.get(defaultvalue);
				((ComboBox) field).setValue(defaultvalue);
			}
			;

			if (value != null && data_ != null) {
				if (data_.get(value) != null) {

					// String dxz = options.get(value);
					((ComboBox) field).setValue(value);
				}
			}
			;

			break;
		default:
			throw new IllegalArgumentException(type.toString());
		}
	}

	private int getOccupiedColumns(CampaignFormElementType type, List<CampaignFormElementStyle> styles) {
		List<CampaignFormElementStyle> colStyles = styles.stream().filter(s -> s.toString().startsWith("col"))
				.collect(Collectors.toList());

		if (type == CampaignFormElementType.YES_NO && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.RADIO && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.CHECKBOX && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.DROPDOWN && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.CHECKBOXBASIC && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.RADIOBASIC && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.TEXTBOX && !styles.contains(CampaignFormElementStyle.INLINE)
				|| (type == CampaignFormElementType.TEXT || type == CampaignFormElementType.DATE
						|| type == CampaignFormElementType.NUMBER || type == CampaignFormElementType.DECIMAL
						|| type == CampaignFormElementType.RANGE)) {// && styles.contains(CampaignFormElementStyle.ROW))
																	// {
			return 12;
		}

		if (colStyles.isEmpty()) {
			switch (type) {
			case LABEL:
			case SECTION:
				return 12;
			default:
				return 4;
			}
		}

		// Multiple col styles are not supported; use the first one
		String colStyle = colStyles.get(0).toString();
		return Integer.parseInt(colStyle.substring(colStyle.indexOf("-") + 1));
	}

	private float calculateComponentWidth(CampaignFormElementType type, List<CampaignFormElementStyle> styles) {
		List<CampaignFormElementStyle> colStyles = styles.stream().filter(s -> s.toString().startsWith("col"))
				.collect(Collectors.toList());

		if (type == CampaignFormElementType.YES_NO && styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.RADIO && styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.RADIOBASIC && styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.CHECKBOX && styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.CHECKBOXBASIC && styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.DROPDOWN && styles.contains(CampaignFormElementStyle.INLINE)
				|| (type == CampaignFormElementType.TEXT || type == CampaignFormElementType.NUMBER
						|| type == CampaignFormElementType.DECIMAL || type == CampaignFormElementType.RANGE
						|| type == CampaignFormElementType.DATE || type == CampaignFormElementType.TEXTBOX)
				// && !styles.contains(CampaignFormElementStyle.ROW)
				|| type == CampaignFormElementType.LABEL || type == CampaignFormElementType.SECTION) {
			return 100f;
		}
		if (1 == 1) {
			return 100f;
		}

		if (colStyles.isEmpty()) {
			// return 33.3f;
		}

		// Multiple col styles are not supported; use the first one
		String colStyle = colStyles.get(0).toString();
		return Integer.parseInt(colStyle.substring(colStyle.indexOf("-") + 1)) / 12f * 100;
	}

//

	private Date dateFormatterLongAndMobile(Object value) {

		String dateStr = value + "";
		DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss a");
		DateFormat formatter_ = new SimpleDateFormat("MMM d, yyyy HH:mm:ss");
		DateFormat formatter_x = new SimpleDateFormat("MMM d, yyyy HH:mm:ss a");
		DateFormat formattercx = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		DateFormat formatterx = new SimpleDateFormat("dd/MM/yyyy");
		DateFormat formatterxn = new SimpleDateFormat("yyyy-MM-dd");
		Date date;
		logger.debug("date in question " + value);
		// +++++++++++++++++++++++===========
//date = (Date) formatterxn.parse(dateStr);
		try {
			date = (Date) formatterxn.parse(dateStr);
		} catch (ParseException ne) {
			logger.debug("date wont parse on " + ne.getMessage());
			try {
				date = (Date) formatter.parse(dateStr);
			} catch (ParseException e) {
				logger.debug("date wont parse on " + e.getMessage());
				try {
					date = (Date) formatter_.parse(dateStr);
				} catch (ParseException ex) {
					logger.debug("date wont parse on " + ex.getMessage());
					try {
						date = (Date) formatter_x.parse(dateStr);
					} catch (ParseException edz) {
						logger.debug("date wont parse on " + edz.getMessage());
						try {
							date = (Date) formatterx.parse(dateStr);
						} catch (ParseException ed) {
							logger.debug("date wont parse on " + ed.getMessage());

							try {
								date = (Date) formattercx.parse(dateStr);
							} catch (ParseException edx) {
								logger.debug("date wont parse on " + edx.getMessage());

								date = new Date((Long) value);

							}
						}
					}
				}
			}
		}
		return date;
	}

	private Date dateFormatter(Object value) throws ParseException {
		// TODO Auto-generated method stub

		String dateStr = value + "";
		DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
		Date date;

		date = (Date) formatter.parse(dateStr);

		// logger.debug(date);

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String formatedDate = cal.get(Calendar.DATE) + "/" + (cal.get(Calendar.MONTH) + 1) + "/"
				+ cal.get(Calendar.YEAR);
		// logger.debug("formatedDate : " + formatedDate);

		Date res = new Date(formatedDate + "");

		return res;
	}

	private void setVisibilityDependency(Component component, String dependingOnId, Object[] dependingOnValues,
			CampaignFormElementType typex, boolean isRequiredField) {
		Component dependingOnField = fields.get(dependingOnId);
		List<Object> dependingOnValuesList = Arrays.asList(dependingOnValues);

		if (dependingOnField == null) {
			return;
		}

		// fieldValueMatchesDependingOnValuesNOTValuer
		if (dependingOnValuesList.stream().anyMatch(v -> v.toString().contains("!"))) {

			// hide on default
			boolean hideNt = dependingOnValuesList.stream().anyMatch(
					v -> fieldValueMatchesDependingOnValuesNOTValuer(dependingOnField, dependingOnValuesList, typex));

			System.out.println(dependingOnValuesList + "JJJJ" + dependingOnField
					+ "HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH" + hideNt);

			if (hideNt) {
				component.setVisible(hideNt);
				// getElement().setProperty("required", requiredIndicatorVisible);
				component.getElement().setProperty("required", isRequiredField);
			} else {
				component.setVisible(hideNt);
				component.getElement().setProperty("required", false);
			}
			// check value and determine if to hide or show
			((AbstractField) dependingOnField).addValueChangeListener(e -> {
				boolean visible = fieldValueMatchesDependingOnValuesNOTValuer(dependingOnField, dependingOnValuesList,
						typex);

				component.setVisible(visible);
				if (typex != CampaignFormElementType.LABEL) {
					if (!visible) {

						if (typex == CampaignFormElementType.TEXT) {
							((TextField) component).setValue(" ");
							((TextField) component).setValue("");
						} else {

							((AbstractField) component).setValue(null);
						}

						((AbstractField) component).setRequiredIndicatorVisible(false);

						component.setVisible(visible);
					} else {
						component.setVisible(visible);
						((AbstractField) component).setRequiredIndicatorVisible(isRequiredField);
						component.getElement().setProperty("required", isRequiredField);
					}
				}
			});
		} else {

			// hide on default
			boolean hide = dependingOnValuesList.stream()
					.anyMatch(v -> fieldValueMatchesDependingOnValues(dependingOnField, dependingOnValuesList, typex));
			component.setVisible(hide);

			if (hide) {
				// getElement().setProperty("required", requiredIndicatorVisible);
				component.getElement().setProperty("required", isRequiredField);
			} else {
				component.getElement().setProperty("required", false);
			}

			// check value and determine if to hide or show
			((AbstractField) dependingOnField).addValueChangeListener(e -> {
				boolean visible = fieldValueMatchesDependingOnValues(dependingOnField, dependingOnValuesList, typex);

				if (typex != CampaignFormElementType.LABEL) {
					if (!visible) {

						((AbstractField) component).setRequiredIndicatorVisible(false);

						if (typex == CampaignFormElementType.TEXT) {
							((TextField) component).setValue(" ");
							((TextField) component).setValue("");
						} else {

							((AbstractField) component).setValue(null);
						}

						component.setVisible(visible);

					} else {
						component.setVisible(visible);
						((AbstractField) component).setRequiredIndicatorVisible(isRequiredField);
						component.getElement().setProperty("required", isRequiredField);
					}
				}
			});
		}
	}

	private boolean fieldValueMatchesDependingOnValues(Component dependingOnField, List<Object> dependingOnValuesList,
			CampaignFormElementType typex) {
		if (((AbstractField) dependingOnField).getValue() == null) {
			return false;
		}
//		logger.debug(Boolean.TRUE.equals(((ToggleButton) dependingOnField).getValue()) + "======= == =========: "
//				+ Boolean.TRUE.equals(((ToggleButton) dependingOnField).getValue()));
		if (dependingOnField instanceof ToggleButton) {
			// logger.debug("========getOptio");

			String stringValue = Boolean.TRUE.equals(((ToggleButton) dependingOnField).getValue()) ? "Yes" : "No";

			return dependingOnValuesList.stream().anyMatch(v ->
			// v.toString().equalsIgnoreCase(booleanValue) ||
			v.toString().equalsIgnoreCase(stringValue));

		} else {

			return dependingOnValuesList.stream().anyMatch(
					v -> v.toString().equalsIgnoreCase(((AbstractField) dependingOnField).getValue().toString()));
		}
	}

	private boolean fieldValueMatchesDependingOnValuesNOTValuer(Component dependingOnField,
			List<Object> dependingOnValuesList, CampaignFormElementType typex) {
		if (((AbstractField) dependingOnField).getValue() == null) {
			return false;
		}

		if (dependingOnField instanceof ToggleButton) {
			// String booleanValue = Boolean.TRUE.equals(((ToggleButton)
			// dependingOnField).getValue()) ? "false" : "true";
			String stringValue = Boolean.TRUE.equals(((ToggleButton) dependingOnField).getValue()) ? "no" : "yes";

			return dependingOnValuesList.stream().anyMatch(v ->
//					v.toString().replaceAll("!", "").equalsIgnoreCase(booleanValue)
//							||
			v.toString().replaceAll("!", "").equalsIgnoreCase(stringValue));
		} else {

			return dependingOnValuesList.stream().anyMatch(v -> !v.toString().replaceAll("!", "")
					.equalsIgnoreCase(((AbstractField) dependingOnField).getValue().toString()));
		}
	}

	public String get18nCaption(String elementId, String defaultCaption) {
		if (userTranslations != null && userTranslations.containsKey(elementId)) {
			return userTranslations.get(elementId);
		}

		return defaultCaption;
	}

	public List<CampaignFormDataEntry> getFormValues() {
		return fields.keySet().stream().map(id -> {
			Component field = fields.get(id);

			if (field instanceof DatePicker) {
//				logger.debug(((DatePicker) field).getValue() + "______________________))");

				String valc = ((DatePicker) field).getValue() != null ? ((DatePicker) field).getValue().toString()
						: null;

				return new CampaignFormDataEntry(id, valc);
			} else if (field instanceof ToggleButton) {

				String valc = ((ToggleButton) field).getValue() != null ? ((ToggleButton) field).getValue().toString()
						.equalsIgnoreCase("true")
						|| ((ToggleButton) field).getValue().toString().equalsIgnoreCase("YES")
						|| ((ToggleButton) field).getValue().toString().equalsIgnoreCase("[YES]")
						|| ((ToggleButton) field).getValue().toString().equalsIgnoreCase("[true]")
								? "Yes"
								: ((ToggleButton) field).getValue().toString().equalsIgnoreCase("[NO]")
										|| ((ToggleButton) field).getValue().toString().equalsIgnoreCase("NO")
										|| ((ToggleButton) field).getValue().toString().equalsIgnoreCase("false")
										|| ((ToggleButton) field).getValue().toString().equalsIgnoreCase("[false]")
												? "No"
												: null
						: null;

				return new CampaignFormDataEntry(id, valc);

			} else {
				if (id.equals("villagecode")) {
					String doubletoParse = ((AbstractField) field).getValue() != null
							? ((AbstractField) field).getValue().toString()
							: "0";
					double number = Double.parseDouble(doubletoParse);
					DecimalFormat decimalFormat = new DecimalFormat("0");
					decimalFormat.setMaximumFractionDigits(0);
					String formattedNumber = decimalFormat.format(number);
					return new CampaignFormDataEntry(id, formattedNumber);
				} else {
					return new CampaignFormDataEntry(id, ((AbstractField) field).getValue());
				}
			}
		}).collect(Collectors.toList());
	}

	private boolean validateAndSave() {
		hasErrorFormValuesReset();
		fields.forEach((key, value) -> {
			Component formField = fields.get(key);

			if (cbArea.getValue() == null) {
				cbArea.getElement().setProperty("invalid", true);
				hasErrorFormValues(1);
			}
			if (cbRegion.getValue() == null) {
				cbRegion.getElement().setProperty("invalid", true);
				hasErrorFormValues(2);
			}
			if (cbDistrict.getValue() == null) {
				cbDistrict.getElement().setProperty("invalid", true);
				hasErrorFormValues(3);
			}
			if (cbCommunity.getValue() == null) {
				cbCommunity.getElement().setProperty("invalid", true);
				hasErrorFormValues(4);
			}

			if (formDate.getValue() == null) {
				formDate.getElement().setProperty("invalid", true);
				hasErrorFormValues(5);
			}

			if (((AbstractField) formField).isRequiredIndicatorVisible()) {
				logger.debug(
						((AbstractField) formField).getValue() + "++++++++++" + ((AbstractField) formField).getId());

				if (((AbstractField) formField).getValue() == null || ((AbstractField) formField).getValue() == "") {
					hasErrorFormValues(6);
					formField.getElement().setProperty("invalid", true);
				} else {
					formField.getElement().setProperty("invalid", false);
				}
			}

		});

		fields.forEach((key, value) -> {
			Component formField = fields.get(key);
			if (formField.getElement().getProperty("invalid", false)) {
				hasErrorFormValues(7);
//				Notification.show("Error on field: " + formField.getElement().getProperty("label"));
				return;
			}

		});
		return invalidForm;
	}

//let change this method to litrate through all the field, check the validity, and return ;ist of those that are not valid in a catch block
//	public void validateFields() {
//		//field.getElement().setProperty("errorMessage", defaultErrorMsgr != null ? defaultErrorMsgr.toString() : "Data entered not in range or calculated range!");
//		
//		try {
//			fields.forEach((key, value) -> {
//
//				AbstractField formField = fields.get(key);
//				formField
//				if (!fields.get(key).val .isValid()) {
//					fields.get(key).setRequiredError("Error found");
//				}
//			});
//		} finally {
//
//			fields.forEach((key, value) -> {
//
//				AbstractField formField = fields.get(key);
//				try {
//
//					formField.validate();
//
//				} catch (Validator.InvalidValueException e) {
//
//					throw (InvalidValueException) e;
//				}
//			});
//		}
//
//	}
	public void hasErrorFormValues(int numer) {
//		Notification.show("Error found in: " + numer);
		invalidForm = true;

	}

	public void hasErrorFormValuesReset() {
		invalidForm = false;
	}

	public boolean saveFormValues() {
		validateAndSave();
		if (!invalidForm) {
			if (openData) {
				boolean saveChecker = true;
				UserProvider userProvider = new UserProvider();
				List<CampaignFormDataEntry> entries = getFormValues();

				CampaignFormDataEntry lotNo = new CampaignFormDataEntry();
				CampaignFormDataEntry lotClusterNo = new CampaignFormDataEntry();

				for (CampaignFormDataEntry sdxc : getFormValues()) {
					logger.debug(sdxc.getId() + "____values____ " + sdxc.getValue());
					if (sdxc.getId().equalsIgnoreCase("LotNo")) {
						lotNo = sdxc;
					}
					if (sdxc.getId().equalsIgnoreCase("LotClusterNo")) {
						lotClusterNo = sdxc;
					}
				}

				List<CampaignFormDataIndexDto> lotchecker = FacadeProvider.getCampaignFormDataFacade()
						.getCampaignFormDataByCampaignandFormMeta(campaignReferenceDto.getUuid(),
								campaignFormMeta.getUuid(), cbDistrict.getValue().getCaption(),
								cbCommunity.getValue().getCaption());

				lotchecker.removeIf(e -> e.getUuid().equals(uuidForm));

				List<String> listLotNo = new ArrayList();
				List<String> listLotClusterNo = new ArrayList();

				if (lotchecker.size() > 0) {
					for (CampaignFormDataIndexDto campaignFormDataIndexDto : lotchecker) {
						List<CampaignFormDataEntry> lotOwnSec = campaignFormDataIndexDto.getFormValues();
						if (lotOwnSec.contains(lotNo)) {
							listLotNo.add(lotOwnSec.get(lotOwnSec.indexOf(lotNo)).getValue().toString());
						}

						if (lotOwnSec.contains(lotClusterNo) && lotOwnSec.contains(lotNo)) {
							listLotClusterNo.add(lotOwnSec.get(lotOwnSec.indexOf(lotClusterNo)).getValue().toString());
						}
					}
				}

				for (String string : listLotClusterNo) {
					if (listLotNo.size() > 0) {
						if ((Long.parseLong(string) - Long.parseLong(lotClusterNo.getValue().toString()) == 0)
								&& (Long.parseLong(listLotNo.get(0))
										- Long.parseLong(lotNo.getValue().toString()) == 0)) {
							saveChecker = false;
							break;
						}
					}
				}

				if (saveChecker) {
					CampaignFormDataDto dataDto = FacadeProvider.getCampaignFormDataFacade()
							.getCampaignFormDataByUuid(uuidForm);

					// maybe we want to check the name of the updating user here
					dataDto.setCreatingUser(userProvider.getUserReference());

					// dataDto.setSource(PlatformEnum.WEB);
					dataDto.setFormValues(entries);

					dataDto = FacadeProvider.getCampaignFormDataFacade().saveCampaignFormData(dataDto);

					Notification.show(I18nProperties.getString(Strings.dataSavedSuccessfully));
					return true;
				} else {
					Notification notification = new Notification();
					notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
					notification.setPosition(Position.MIDDLE);
					Button closeButton = new Button(new Icon("lumo", "cross"));
					closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
					closeButton.getElement().setAttribute("aria-label", "Close");
					closeButton.addClickListener(event -> {
						notification.close();
					});

					Paragraph text = new Paragraph("This Cluster Number or Lot Cluster Number exist");

					HorizontalLayout layout = new HorizontalLayout(text, closeButton);
					layout.setAlignItems(Alignment.CENTER);

					notification.add(layout);
					notification.open();
				}
			} else {
				boolean saveChecker = true;
				boolean ccodeChecker = true;
				UserProvider userProvider = new UserProvider();
				List<CampaignFormDataEntry> entries = getFormValues();

				CampaignFormDataEntry lotNo = new CampaignFormDataEntry();
				CampaignFormDataEntry lotClusterNo = new CampaignFormDataEntry();

				for (CampaignFormDataEntry sdxc : getFormValues()) {
//					logger.debug(sdxc.getId() + "____values____ " + sdxc.getValue());
					if (sdxc.getId().equalsIgnoreCase("LotNo")) {
						lotNo = sdxc;
					}
					if (sdxc.getId().equalsIgnoreCase("LotClusterNo")) {
						lotClusterNo = sdxc;
					}
				}

				List<CampaignFormDataIndexDto> lotchecker = FacadeProvider.getCampaignFormDataFacade()
						.getCampaignFormDataByCampaignandFormMeta(campaignReferenceDto.getUuid(),
								campaignFormMeta.getUuid(), cbDistrict.getValue().getCaption(),
								cbCommunity.getValue().getCaption());

				List<String> listLotNo = new ArrayList();
				List<String> listLotClusterNo = new ArrayList();

				if (lotchecker.size() > 0) {
					for (CampaignFormDataIndexDto campaignFormDataIndexDto : lotchecker) {
						List<CampaignFormDataEntry> lotOwnSec = campaignFormDataIndexDto.getFormValues();
						if (lotOwnSec.contains(lotNo)) {
							listLotNo.add(lotOwnSec.get(lotOwnSec.indexOf(lotNo)).getValue().toString());
						}

						if (lotOwnSec.contains(lotClusterNo) && lotOwnSec.contains(lotNo)) {
							listLotClusterNo.add(lotOwnSec.get(lotOwnSec.indexOf(lotClusterNo)).getValue().toString());
						}
					}
				}

				for (String string : listLotClusterNo) {
					if (listLotNo.size() > 0) {
						if ((Long.parseLong(string) - Long.parseLong(lotClusterNo.getValue().toString()) == 0)
								&& (Long.parseLong(listLotNo.get(0))
										- Long.parseLong(lotNo.getValue().toString()) == 0)) {
							saveChecker = false;
							break;
						}
					}
				}

				if (saveChecker) {
					CampaignFormDataDto dataDto = CampaignFormDataDto.build(campaignReferenceDto, campaignFormMeta,
							cbArea.getValue(), cbRegion.getValue(), cbDistrict.getValue(), cbCommunity.getValue());

					Date dateData = Date.from(formDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

					dataDto.setFormDate(dateData);
					dataDto.setCreatingUser(userProvider.getUserReference());
					dataDto.setFormValues(entries);
					dataDto.setSource("WEB");
//					if (dataDto.getFormType())

					dataDto = FacadeProvider.getCampaignFormDataFacade().saveCampaignFormData(dataDto);

					Notification.show(I18nProperties.getString(Strings.dataSavedSuccessfully));
					return true;

				} else {
					Notification notification = new Notification();
					notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
					notification.setPosition(Position.MIDDLE);
					Button closeButton = new Button(new Icon("lumo", "cross"));
					closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
					closeButton.getElement().setAttribute("aria-label", "Close");
					closeButton.addClickListener(event -> {
						notification.close();
					});

					Paragraph text = new Paragraph("This Cluster Number or Lot Cluster Number exist");

					HorizontalLayout layout = new HorizontalLayout(text, closeButton);
					layout.setAlignItems(Alignment.CENTER);

					notification.add(layout);
					notification.open();
				}

			}
		}

		return false;
	}

	public void resetFormValues() {

		fields.keySet().forEach(key -> {
			Component field = fields.get(key);
			((AbstractField) field).setValue(formValuesMap.get(key));
		});
	}

	public void setFormValues(List<CampaignFormDataEntry> formValuex) {
		Map<String, Object> formValuesMapSet = new HashMap<>();
		if (formValuex != null) {
			formValuex.forEach(formValue -> formValuesMapSet.put(formValue.getId(), formValue.getValue()));
		}

		fields.keySet().forEach(key -> {
			Component field = fields.get(key);
			if (field instanceof NumberField) {

			}
			((AbstractField) field).setValue(formValuesMapSet.get(key));
		});
	}

	public List<CampaignFormElement> getFormElements() {
		return formElements;
	}

	public Map<String, Component> getFields() {
		return fields;
	}

	// Expression Logics

	private void checkExpression() {
		// logger.debug("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");

		EvaluationContext context = refreshEvaluationContext(getFormValues());
		final List<CampaignFormElement> formElements = getFormElements();
		formElements.stream().filter(element -> element.getExpression() != null).forEach(e -> {
			try {
				final Expression expression = expressionParser.parseExpression(e.getExpression());
				// logger.debug("------: "+expression.getExpressionString());
				final Class<?> valueType = expression.getValueType(context);
				final Object value = expression.getValue(context, valueType);
				// final Object valx = Precision.round((double) value, 3);
				// final List <String> opt = null;
				// logger.debug(value + "| range?
				// "+e.getType().toString().equals("range")+ " value:
				// "+expression.getValue(context));
				String valuex = value + "";

				if (!valuex.isBlank() && value != null) {
					if (e.getType().toString().equals("range")) {

						if (value.toString().equals("0")) {
							setFieldValue(getFields().get(e.getId()), CampaignFormElementType.fromString(e.getType()),
									null, null, null, false,
									e.getErrormessage() != null ? e.getCaption() + " : " + e.getErrormessage() + ".."
											: "..");
							// return;
						} else {

							Boolean isErrored = value.toString().endsWith(".0");

							setFieldValue(getFields().get(e.getId()), CampaignFormElementType.fromString(e.getType()),
									value.toString().endsWith(".0") ? value.toString().replace(".0", "") : value, null,
									null, isErrored,
									e.getErrormessage() != null ? e.getCaption() + " : " + e.getErrormessage() + ".."
											: "..");
							// return;
						}

					} else if (valueType.isAssignableFrom(Double.class)) {
						// logger.debug("yes double detected "+Double.isFinite((double) value) +"
						// = "+ value);
						setFieldValue(getFields().get(e.getId()), CampaignFormElementType.fromString(e.getType()),
								!Double.isFinite((double) value) ? 0
										: value.toString().endsWith(".0") ? value.toString().replace(".0", "")
												: Precision.round((double) value, 2),
								null, null, false,
								e.getErrormessage() != null ? e.getCaption() + " : " + e.getErrormessage() : null);
						// return;
					} else if (valueType.isAssignableFrom(Boolean.class)) {
						logger.debug(e.getCaption() + " : " + value + " = = = = ");
						setFieldValue(getFields().get(e.getId()), CampaignFormElementType.fromString(e.getType()),
								value, null, null, false,
								e.getErrormessage() != null ? e.getCaption() + " : " + e.getErrormessage() : null);
						// return;
						//
					} else {

						setFieldValue(getFields().get(e.getId()), CampaignFormElementType.fromString(e.getType()),
								value, null, null, false,
								e.getErrormessage() != null ? e.getCaption() + " : " + e.getErrormessage() : null);

					}
				} else if (e.getType().toString().equals("range") && valuex == null && e.getDefaultvalue() != null) {

					// logger.debug("++++++++++++++++++++++++++++++++++++++++++++++============================");

				}
			} catch (SpelEvaluationException evaluationException) {
				// LOG.error("Error evaluating expression: {} / {}",
				// evaluationEx0rception.getMessageCode(), evaluationException.getMessage());
			}
		});

	}

	public void disableExpressionFieldsForEditing() {
		final Map<String, Component> fields_ = getFields();
		getFormElements().stream().filter(formElement -> formElement.getExpression() != null)
				.filter(formElement -> fields_.get(formElement.getId()) != null)
				.filter(formElement -> !formElement.getType().equals("range"))
				.filter(formElement -> !formElement.isIgnoredisable())
				.forEach(formElement -> ((AbstractField) fields_.get(formElement.getId())).setEnabled(false));
		addExpressionListener();
	}

	public void addExpressionListener() {
		final Map<String, Component> fields_ = getFields();
		final List<CampaignFormElement> formElements = getFormElements();
		formElements.stream()
				// .filter(formElement -> formElement.getExpression() == null)
				.filter(formElement -> fields_.get(formElement.getId()) != null).forEach(formElement -> {
					((AbstractField) fields_.get(formElement.getId()))
							.addValueChangeListener(valueChangeEvent -> checkExpression());
				});
		configureExpressionFieldsWithTooltip();
	}

	public void configureExpressionFieldsWithTooltip() {
		final Map<String, Component> fields = getFields();
		getFormElements().stream().filter(formElement -> formElement.getExpression() != null)
				.filter(formElement -> fields.get(formElement.getId()) != null)
				.filter(formElement -> fields.get(formElement.getId()) instanceof Component)
				.forEach(this::buildTooltipDescription);
	}

	private void buildTooltipDescription(CampaignFormElement formElement) {
		final Set<String> fieldNamesInExpression = new HashSet<>();
		final String tooltip = formElement.getExpression();
		final Map<String, Component> fields = getFields();
		final Component field = fields.get(formElement.getId());
		getFormElements().forEach(element -> {
			if (tooltip.contains(element.getId())) {
				fieldNamesInExpression.add(get18nCaption(element.getId(), element.getCaption()));
			}
		});
		Tooltip tooltipx = Tooltip.forComponent(field)
				.withText(
						String.format("%s: %s", I18nProperties.getDescription(Descriptions.Campaign_calculatedBasedOn),
								StringUtils.join(fieldNamesInExpression, ", ")))
				.withPosition(Tooltip.TooltipPosition.TOP_START);

		// field.set .setDescription();
	}

}
