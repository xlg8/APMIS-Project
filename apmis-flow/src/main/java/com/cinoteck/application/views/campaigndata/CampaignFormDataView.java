//package com.cinoteck.application.views.campaigndata;
//
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.dependency.JsModule;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.router.PageTitle;
//import com.vaadin.flow.router.Route;
//import com.vaadin.flow.server.VaadinSession;
//
//import de.symeda.sormas.api.FacadeProvider;
//import de.symeda.sormas.api.campaign.CampaignReferenceDto;
//import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
//import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
//import de.symeda.sormas.api.i18n.I18nProperties;
//import de.symeda.sormas.api.i18n.Strings;
////import de.symeda.sormas.ui.ControllerProvider;
////import de.symeda.sormas.ui.SormasUI;
////import de.symeda.sormas.ui.components.CommitDiscardWrapperComponent;
////import de.symeda.sormas.ui.components.DetailSubComponentWrapper;
//
//@Route(value = "campaign/dataform", layout = MainLayout.class)
//@PageTitle("Campaign Form Data")
//@JsModule("./jquerymini.js")
//public class CampaignFormDataView extends VerticalLayout {
//    private CommitDiscardWrapperComponent<CampaignFormDataEditForm> editComponent;
//
//    public CampaignFormDataView() {
//        initView();
//    }
//
//    private void initView() {
//        DetailSubComponentWrapper container = new DetailSubComponentWrapper();
//        container.setWidthFull();
//        container.setMargin(true);
//        add(container);
//
//        String params = VaadinSession.getCurrent().getAttribute("campaignFormDataViewParams").toString();
//        if (params.contains(",")) {
//            String[] paraObj = params.split(",");
//            CampaignReferenceDto camref = FacadeProvider.getCampaignFacade()
//                    .getReferenceByUuid(paraObj[0]);
//            CampaignFormMetaReferenceDto amformmeta = FacadeProvider.getCampaignFormMetaFacade()
//                    .getCampaignFormMetaReferenceByUuid(paraObj[1]);
//
//            editComponent = ControllerProvider.getCampaignController().getCampaignFormDataComponent(
//                    null,
//                    camref,
//                    amformmeta,
//                    false,
//                    false,
//                    () -> {
//                        SormasUI.refreshCampaignView();
//                        Notification.show(String.format(I18nProperties.getString(Strings.messageCampaignFormSaved),
//                                amformmeta.getCaption()));
//                    },
//                    () -> {
//                    },
//                    () -> {
//                        SormasUI.refreshCampaignView();
//                        Notification.show(String.format(I18nProperties.getString(Strings.messageCampaignFormSaved),
//                                amformmeta.getCaption()));
//                    },
//                    true
//            );
//            editComponent.setMargin(false);
//            editComponent.getWrappedComponent().setWidthFull();
//            editComponent.setHeightUndefined();
//            editComponent.addClassName(CssStyles.ROOT_COMPONENT);
//            editComponent.setWidthFull();
//
//            container.addComponent(editComponent);
//
//            getPage().executeJs("$0.style.display = 'none';", getElement());
//        } else {
//            CampaignFormDataDto campaignFormData = FacadeProvider.getCampaignFormDataFacade()
//                    .getCampaignFormDataByUuid(getReference().getUuid());
//
//            editComponent = ControllerProvider.getCampaignController().getCampaignFormDataComponent(
//                    campaignFormData,
//                    campaignFormData.getCampaign(),
//                    campaignFormData.getCampaignFormMeta(),
//                    true,
//                    true,
//                    () -> {
//                        SormasUI.refreshView();
//                        Notification.show(String.format(I18nProperties.getString(Strings.messageCampaignFormSaved),
//                                campaignFormData.getCampaignFormMeta().toString()));
//                    },
//                    null
//            );
//            editComponent.setMargin(false);
//            editComponent.getWrappedComponent().setWidthFull();
//            editComponent.setHeightUndefined();
//            editComponent.addClassName(CssStyles.ROOT_COMPONENT);
//            editComponent.setWidthFull();
//
//            container.addComponent(editComponent);
//
//            getPage().setTitle(campaignFormData.getCampaignFormMeta().toString());
//        }
//    }
//}
