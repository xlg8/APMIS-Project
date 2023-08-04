package com.cinoteck.application.views.campaigndata;

import java.awt.Panel;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;


import com.cinoteck.application.UserProvider;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserType;

public class CamapignDataFilter extends VerticalLayout {

//	private final TextField filterr = new TextField();
//
//	private final Button addNewBtnn = new Button();
//
//	private TextField searchField = new TextField();
	public ComboBox<String> campaignYear;
	public ComboBox<CampaignReferenceDto> campaign = new ComboBox<>();
	ComboBox campaignPhase = new ComboBox<>("");
	public Button newForm = new Button(I18nProperties.getCaption(Captions.actionNewForm));
	public Button importData = new Button(I18nProperties.getCaption(Captions.actionImport));
	public Button exportData = new Button(I18nProperties.getCaption(Captions.export));

	List<String> campaignsYears;
	List<CampaignReferenceDto> campaigns;
	List<CampaignPhase> campaignPhases;
	List<String> camYearList;

	public ComboBox<CampaignFormMetaReferenceDto> campaignForm = new ComboBox<>();
	public ComboBox<AreaReferenceDto> region = new ComboBox<>();
	public ComboBox<RegionReferenceDto> province = new ComboBox<>();
	public ComboBox<DistrictReferenceDto> district = new ComboBox<>();
	public ComboBox cluster = new ComboBox<>();
	public Button resetHandler = new Button();
	public Button applyHandler = new Button();

	List<CampaignFormMetaReferenceDto> campaignForms;
	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	List<CommunityReferenceDto> communities;

//	private CampaignFormPhaseSelector campaignFormPhaseSelector;

	public final TextField filterr = new TextField();

	public final Button addNewBtnn = new Button();

	public TextField searchField = new TextField();
	
	CampaignFormDataCriteria criteria; 

	public CamapignDataFilter() {
		HorizontalLayout level1Filters = new HorizontalLayout();
		level1Filters.getStyle().set("margin-left", "12px");
		level1Filters.setAlignItems(Alignment.END);
//		
//		for(CampaignReferenceDto camdreg :  FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference()) {
//			campaignsYears.add(camdreg.getCampaignYear());
//			System.out.println(camdreg +"campaign hyear ");
//			
//			campaignYear.setItems(campaignsYears);
//		}
//		
		
		criteria = new CampaignFormDataCriteria();
		
		
		campaignYear = new ComboBox<>();
		campaignYear.setLabel(I18nProperties.getCaption(Captions.campaignYear));
		campaignYear.setId("jgcjgcjgcj");
		List<CampaignReferenceDto> campaigns = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference();

		List<String> camYearList = new ArrayList<>();
		for (CampaignReferenceDto camdreg : campaigns) {
			camYearList.add(camdreg.getCampaignYear());
		}
		campaignYear.setItems(camYearList.stream().distinct().collect(Collectors.toList()));
		campaignYear.addValueChangeListener(e->{
			boolean isCampaignChangedx = false;
			campaign.clear();
			List<CampaignReferenceDto> stf = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference();
			stf.removeIf(ee -> (!ee.getCampaignYear().equals(campaignYear.getValue())));
			campaign.setItems(stf);
			campaign.setValue(stf.get(0));
		});
//		

		campaign.setLabel(I18nProperties.getCaption(Captions.Campaigns));
		campaign.setId("jgcjgcjgcj");
//		campaigns = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference();
//		campaign.setItems(campaigns);
		campaign.addValueChangeListener(e->{
			campaignPhase.setItems(CampaignPhase.values());
			campaignPhase.setValue(CampaignPhase.INTRA);
//			criteria.setCampaign(campaign.getValue());
			
		});

		campaignPhase.setLabel(I18nProperties.getCaption(Captions.Campaign_phase));
//		campaignPhases = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference()
		campaignPhase.setItems(CampaignPhase.values().toString());
//		campaignPhase.setValue("");
		campaignPhase.getStyle().set("padding-top", "0px");
		campaignPhase.setClassName("col-sm-6, col-xs-6");

		newForm.setIcon(new Icon(VaadinIcon.PLUS_CIRCLE));

		importData.setIcon(new Icon(VaadinIcon.PLUS_CIRCLE));

		exportData.setIcon(new Icon(VaadinIcon.DOWNLOAD));

		level1Filters.add(campaignYear, campaign, campaignPhase, newForm, importData, exportData);
		
		

		Button displayFilters = new Button(I18nProperties.getCaption(Captions.showFilters));
		displayFilters.getStyle().set("margin-left", "12px");
		displayFilters.getStyle().set("margin-top", "12px");
		displayFilters.setIcon(new Icon(VaadinIcon.SLIDERS));

		HorizontalLayout campaignDataFilterLayout = new HorizontalLayout();
		campaignDataFilterLayout.getStyle().set("margin-left", "12px");
		campaignDataFilterLayout.setAlignItems(Alignment.END);

		campaignForm.setLabel(I18nProperties.getCaption(Captions.campaignCampaignForm));
		campaignForm.setPlaceholder(I18nProperties.getCaption(Captions.campaignCampaignForm));
		campaignForms = FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferences();
		campaignForm.setItems(campaignForms);

		region.setLabel(I18nProperties.getCaption(Captions.area));
		region.setPlaceholder(I18nProperties.getCaption(Captions.area));

		regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
		region.setItems(regions);
		region.addValueChangeListener(e -> {
			provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
			province.setItems(provinces);
		});

		province.setLabel(I18nProperties.getCaption(Captions.region));
		province.setPlaceholder(I18nProperties.getCaption(Captions.region));
		provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
		province.setItems(provinces);
		province.addValueChangeListener(e -> {
			districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
			district.setItems(districts);
		});
		province.getStyle().set("padding-top", "0px");
		province.setClassName("col-sm-6, col-xs-6");

		district.setLabel(I18nProperties.getCaption(Captions.district));
		district.setPlaceholder(I18nProperties.getCaption(Captions.district));
		districts = FacadeProvider.getDistrictFacade().getAllActiveAsReference();
		district.setItems(districts);
		district.addValueChangeListener(e -> {
			communities = FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid());
//			cluster.setItemLabelGenerator(CommunityReferenceDto::getCaption);
			cluster.setItems(communities);
		});
		district.getStyle().set("padding-top", "0px");
		district.setClassName("col-sm-6, col-xs-6");

		cluster.setPlaceholder(I18nProperties.getCaption(Captions.community));
		cluster.setItems(FacadeProvider.getDistrictFacade().getAllActiveAsReference());

		resetHandler.setText(I18nProperties.getCaption(Captions.resetFilters));

		applyHandler.setText(I18nProperties.getCaption(Captions.applyFilters));

		campaignDataFilterLayout.add(campaignForm, region, province, district, cluster, resetHandler, applyHandler);

		campaignDataFilterLayout.setVisible(false);

		displayFilters.addClickListener(e -> {
			if (!campaignDataFilterLayout.isVisible()) {
				campaignDataFilterLayout.setVisible(true);
				displayFilters.setText(I18nProperties.getCaption(Captions.hideFilters));

			} else {
				campaignDataFilterLayout.setVisible(false);
				displayFilters.setText(I18nProperties.getCaption(Captions.showFilters));
			}

		});

		add(level1Filters, displayFilters, campaignDataFilterLayout);

	}

	
	private void fillNewFormDropdown(Panel containerPanel) {
		

		CampaignReferenceDto campaignReferenceDtx = campaign.getValue();
		Object phase = campaignPhase.getValue();
		Set<FormAccess> userFormAccess = UserProvider.getCurrent().getFormAccess();
		
		CampaignDto campaignDto = FacadeProvider.getCampaignFacade().getByUuid(campaignReferenceDtx.getUuid());

//		((VerticalLayout) containerPanel.getComponent()).removeAllComponents();

		 SimpleDateFormat DateFor = new SimpleDateFormat("yyyy-MM-dd");
		 String stringDate= DateFor.format(campaignDto.getStartDate());
		
		 	LocalDate date1 = LocalDate.parse(stringDate);
	        LocalDate date2 = LocalDate.now();
//	        int days = Days.daysBetween(date1, date2).getDays();

		if (phase != null && campaignReferenceDtx != null) {
			
			List<CampaignFormMetaReferenceDto> campagaignFormReferences = FacadeProvider.getCampaignFormMetaFacade()
					.getAllCampaignFormMetasAsReferencesByRoundandCampaignandForm(phase.toString().toLowerCase(),
							campaignReferenceDtx.getUuid(), userFormAccess);

			Collections.sort(campagaignFormReferences);

			for (CampaignFormMetaReferenceDto campaignForm : campagaignFormReferences) {
				
//				int isShown = days - campaignForm.getDaysExpired();
//				boolean hideFromList = isShown < 0;
				
//				Button campaignFormButton = ButtonHelper.createButton(campaignForm.toString(), el -> {
//					if(hideFromList) {
//					ControllerProvider.getCampaignController().navigateToFormDataView(campaignReferenceDtx.getUuid(),
//							campaignForm.getUuid());
//					newFormButton.setPopupVisible(false);
//					}
//				});

//				campaignFormButton.setWidth(100, Unit.PERCENTAGE);
				// campaignFormButton.removeStyleName(VIEW_NAME);
//				campaignFormButton.removeStyleName("v-button");
//				campaignFormButton.setStyleName("nocapitalletter");
				
//				if(!hideFromList) {
//					//campaignFormButton.setEnabled(false);
//					campaignFormButton.addClickListener(e -> {
//						Notification notf = new Notification(campaignForm.getCaption() +" is now closed for data entry");
////						notf.setPosition(Notification.);
////						notf.setDelayMsec(3000);
////						notf.show(UI.getCurrent().getPage());
//					});
//				}
//				
//				((VerticalLayout) containerPanel.getContent()).addComponent(campaignFormButton);
			}
			
			if (campagaignFormReferences.size() >= 10) {
//				containerPanel.setHeight(400, Unit.PIXELS);
//				containerPanel.setWidth(containerPanel.getContent().getWidth() + 20.0f, Unit.PIXELS);
			} else {
//				containerPanel.setHeightUndefined();
//				containerPanel.setWidth(containerPanel.getContent().getWidth(), Unit.PIXELS);
			}
		}
	}

}