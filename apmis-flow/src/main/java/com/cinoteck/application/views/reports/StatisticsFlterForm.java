package com.cinoteck.application.views.reports;


import com.cinoteck.application.UserProvider;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.combobox.ComboBox;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.campaign.statistics.CampaignStatisticsCriteria;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserType;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

public class StatisticsFlterForm  {

    private Consumer<CampaignFormMetaReferenceDto> formMetaChangedCallback;
    private ComboBox<CampaignFormMetaReferenceDto> cbCampaignForm;
    private ComboBox<AreaReferenceDto> areaFilter;
    private ComboBox<RegionReferenceDto> regionFilter;
    private ComboBox<DistrictReferenceDto> districtFilter;

    @Autowired
    public StatisticsFlterForm() {
//        super(CampaignStatisticsCriteria.class, CampaignStatisticsDto.I18N_PREFIX);
//        formActionButtonsComponent.style(CssStyles.FORCE_CAPTION);
//        formActionButtonsComponent.setSpacing(false);
//        formActionButtonsComponent.setSizeFull();
//        formActionButtonsComponent.setMargin(new MarginInfo(false, false, false, true));
        initializeForm();
    }

    private void initializeForm() {
        cbCampaignForm = new ComboBox();
        cbCampaignForm.setLabel(I18nProperties.getPrefixCaption(CampaignFormDataDto.I18N_PREFIX, CampaignFormDataDto.CAMPAIGN_FORM_META));
        cbCampaignForm.setWidth("200px");
//        cbCampaignForm.setItemLabelGenerator(CampaignFormMetaReferenceDto::getName);
        cbCampaignForm.setItems(FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferences());
//        FieldHelper.addSoftRequiredStyle(cbCampaignForm);

        cbCampaignForm.addValueChangeListener(event -> {
            if (formMetaChangedCallback != null) {
                formMetaChangedCallback.accept(event.getValue());
            }
        });

        areaFilter = new ComboBox<>();
        areaFilter.setLabel(I18nProperties.getCaption(Captions.Campaign_area));
        areaFilter.setWidth("200px");
        areaFilter.setPlaceholder(I18nProperties.getString(Strings.promptAllAreas));
        areaFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());

        regionFilter = new ComboBox<>();
        regionFilter.setLabel(I18nProperties.getCaption(Captions.Campaign_region));
        regionFilter.setWidth("200px");
        regionFilter.setPlaceholder(I18nProperties.getString(Strings.promptAllRegions));
        regionFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveByServerCountry());

        districtFilter = new ComboBox<>();
        districtFilter.setLabel(I18nProperties.getCaption(Captions.Campaign_district));
        districtFilter.setWidth("200px");
        districtFilter.setPlaceholder(I18nProperties.getString(Strings.promptAllDistricts));

        UserProvider user = new UserProvider();
        final AreaReferenceDto userArea = user.getUser().getArea();
        final RegionReferenceDto userRegion = user.getUser().getRegion();
        final DistrictReferenceDto userDistrict = user.getUser().getDistrict();
        if (userArea != null) {
            areaFilter.setEnabled(false);
            areaFilter.setItems((AreaReferenceDto)FacadeProvider.getRegionFacade().getAllActiveByArea(userArea.getUuid()));
            if (userRegion != null) {
                regionFilter.setEnabled(false);
                districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveByRegion(userRegion.getUuid()));
                if (userDistrict != null) {
                    districtFilter.setEnabled(false);
                }
            }
        }
    }

    
    protected String[] getMainFilterLocators() {
        return new String[]{
                CampaignStatisticsCriteria.CAMPAIGN_FORM_META,
                CampaignStatisticsCriteria.AREA,
                CampaignStatisticsCriteria.REGION,
                CampaignStatisticsCriteria.DISTRICT
        };
    }

    protected void applyDependenciesOnFieldChange(String propertyId, ValueChangeEvent event) {
//        super.applyDependenciesOnFieldChange(propertyId, event);

        switch (propertyId) {
            case CampaignFormDataDto.REGION:
                RegionReferenceDto region = (RegionReferenceDto) event.getValue();
                if (region != null) {
                    districtFilter.clear();
                    districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveByRegion(region.getUuid()));
                } else {
                    districtFilter.clear();
                }
                break;
            case CampaignFormDataDto.DISTRICT:
                // Implement the necessary logic when the district filter changes
                break;
        }
    }

    protected void applyDependenciesOnNewValue(CampaignStatisticsCriteria criteria) {
        cbCampaignForm.clear();
        if (criteria.getCampaign() != null) {
            if (UserProvider.getCurrent().hasUserType(UserType.EOC_USER)) {
                cbCampaignForm.setItems(FacadeProvider.getCampaignFormMetaFacade()
                        .getCampaignFormMetaAsReferencesByCampaignPostCamapaign(criteria.getCampaign().getUuid()));
            } else {
                cbCampaignForm.setItems(FacadeProvider.getCampaignFormMetaFacade()
                        .getCampaignFormMetasAsReferencesByCampaign(criteria.getCampaign().getUuid()));
            }
        } else {
            cbCampaignForm.setItems(FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferences());
        }
    }

    public void setFormMetaChangedCallback(Consumer<CampaignFormMetaReferenceDto> formMetaChangedCallback) {
        this.formMetaChangedCallback = formMetaChangedCallback;
        cbCampaignForm.addValueChangeListener(e -> formMetaChangedCallback.accept(e.getValue()));
    }
}
