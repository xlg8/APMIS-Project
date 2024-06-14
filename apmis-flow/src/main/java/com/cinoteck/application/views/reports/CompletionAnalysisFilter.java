package com.cinoteck.application.views.reports;

import java.util.List;

import com.cinoteck.application.UserProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;

public class CompletionAnalysisFilter extends VerticalLayout {

	private ComboBox<CampaignReferenceDto> campaign = new ComboBox<>();
	private ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>();
	private ComboBox<RegionReferenceDto> provinceFilter = new ComboBox<>();
	private ComboBox<DistrictReferenceDto> districtFilter = new ComboBox<>();
	private Button resetButton;
	
	private UserProvider userProvider = new UserProvider();

	List<CampaignReferenceDto> campaigns;

	public CompletionAnalysisFilter() {
		HorizontalLayout filterLayout = new HorizontalLayout();
		filterLayout.setPadding(false);
		filterLayout.setVisible(false);
		filterLayout.setAlignItems(Alignment.END);

		campaign.setLabel(I18nProperties.getCaption(Captions.Campaigns));
		campaign.setPlaceholder(I18nProperties.getCaption(Captions.campaignAllCampaigns));
		campaigns = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference();
		campaign.setItems(campaigns);

		regionFilter.setLabel(I18nProperties.getCaption(Captions.area));
		regionFilter.setPlaceholder(I18nProperties.getCaption(Captions.areaAllAreas));
		
		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferencePashto());
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {	
			regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferenceDari());
		} else {
			regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());			
		}

		regionFilter.addValueChangeListener(e -> {			
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReferencePashto());
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {	
				provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReferenceDari());
			} else {
				provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());
			}
		});

		provinceFilter.setLabel(I18nProperties.getCaption(Captions.region));
		provinceFilter.setPlaceholder(I18nProperties.getCaption(Captions.regionAllRegions));
		
		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReferencePashto());
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {	
			provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReferenceDari());
		} else {
			provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());
		}
		provinceFilter.addValueChangeListener(e -> {
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveAsReferencePashto());
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {			
				districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveAsReferenceDari());
			} else {
				districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveAsReference());
			}
		});
		
		districtFilter.setLabel(I18nProperties.getCaption(Captions.district));
		districtFilter.setPlaceholder(I18nProperties.getCaption(Captions.districtAllDistricts));
		
		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveAsReferencePashto());
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {		
			districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveAsReferenceDari());
		} else {
			districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveAsReference());
		}
		
		resetButton =  new Button(I18nProperties.getCaption(Captions.actionResetFilters));

		Button displayFilters = new Button(I18nProperties.getCaption(Captions.showFilters), new Icon(VaadinIcon.SLIDERS));
		displayFilters.addClickListener(e->{
			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			if(filterLayout.isVisible() == false) {
				filterLayout.setVisible(true);
				displayFilters.setText(I18nProperties.getCaption(Captions.hideFilters));
			}else {
				filterLayout.setVisible(false);
				displayFilters.setText(I18nProperties.getCaption(Captions.showFilters));
			}
		});
		
		filterLayout.add(campaign, regionFilter, provinceFilter, districtFilter, resetButton);
		
		add(displayFilters,filterLayout);

	}

}
