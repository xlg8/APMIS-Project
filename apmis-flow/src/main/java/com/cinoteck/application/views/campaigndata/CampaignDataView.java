package com.cinoteck.application.views.campaigndata;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.System.Logger;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sound.midi.SysexMessage;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Form;

import org.jsoup.select.Evaluator.ContainsData;
import org.slf4j.LoggerFactory;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.UserProvider.HasUserProvider;
import com.cinoteck.application.ViewModelProviders;
import com.cinoteck.application.ViewModelProviders.HasViewModelProviders;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.campaign.CampaignDataImportDialog;
import com.cinoteck.application.views.campaign.CampaignForm;
import com.cinoteck.application.views.campaign.ImportPopulationDataDialog;
import com.cinoteck.application.views.utils.DownloadTransposedDaywiseDataUtility;
import com.cinoteck.application.views.utils.IdleNotification;
import com.cinoteck.application.views.utils.gridexporter.GridExporter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridMultiSelectionModel.SelectAllCheckboxVisibility;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.Descriptions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.InfrastructureType;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.SortProperty;

@PageTitle("APMIS-Campaign Data")
@Route(value = "campaigndata", layout = MainLayout.class)
@CssImport(value = "./styles/custom-grid-styles.css", themeFor = "vaadin-grid")
@CssImport(value = "./styles/vaadin-text-field.css", themeFor = "vaadin-text-field")
@CssImport(value = "./styles/vaadin-number-field.css", themeFor = "vaadin-number-field")

public class CampaignDataView extends VerticalLayout
		implements HasUserProvider, HasViewModelProviders, BeforeEnterObserver {

	private static final long serialVersionUID = 7588118851062483372L;

	private Grid<CampaignFormDataIndexDto> grid = new Grid<>(CampaignFormDataIndexDto.class, false);
	private CampaignFormDataCriteria criteria;
	private CampaignFormMetaDto formMetaReference;
	private CampaignFormDataDto campaignFormDatadto;

	ComboBox<String> campaignYear = new ComboBox<>();
	ComboBox<CampaignReferenceDto> campaignz = new ComboBox<>();
	ComboBox<CampaignPhase> campaignPhase = new ComboBox<>();
	ComboBox<CampaignFormMetaReferenceDto> campaignFormCombo = new ComboBox<>();
	ComboBox<AreaReferenceDto> regionCombo = new ComboBox<>();
	ComboBox<RegionReferenceDto> provinceCombo = new ComboBox<>();
	ComboBox<DistrictReferenceDto> districtCombo = new ComboBox<>();
	ComboBox<CommunityReferenceDto> clusterCombo = new ComboBox<>();
	ComboBox<String> verifiedStatusCombo = new ComboBox<>();
	ComboBox<String> publishedStatusCombo = new ComboBox<>();

	ComboBox<CampaignFormElementImportance> importanceSwitcher = new ComboBox<>();
	ComboBox<String> timelinessFilter = new ComboBox<>();
	Button resetHandler = new Button();
//	Button applyHandler = new Button();
	List<AreaReferenceDto> regions;
	List<AreaReferenceDto> regionsx;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	List<CommunityReferenceDto> communities;
	List<CampaignFormMetaReferenceDto> campaignForms;
	Anchor anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
	Anchor transposdeDataAnchor = new Anchor("", I18nProperties.getCaption(Captions.export) + "Transposed Data");

	Icon icon = VaadinIcon.UPLOAD_ALT.create();
	Paragraph countRowItems;
	UserProvider userProvider = new UserProvider();

	CampaignFormDataEditForm campaignFormDataEditForm;

	Button enterBulkEdit = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode));
	Button leaveBulkEdit = new Button(I18nProperties.getCaption(Captions.actionLeaveBulkEditMode));
	MenuBar dropdownBulkOperations = new MenuBar();
	ConfirmDialog confirmationDialog;
	Checkbox checkboxx = new Checkbox();
	Button selectAllButton = new Button();
	Button selectAllButtonpLACEHOLDER = new Button();
	private String exportFileName;
	private GridExporter<CampaignFormDataIndexDto> exporter;

	private DataProvider<CampaignFormDataIndexDto, CampaignFormDataCriteria> dataProvider;

	NumberFormat arabicFormat = NumberFormat.getInstance();

	Column<CampaignFormDataIndexDto> clusterNumberColumn, ccodeColumn, formNameColumn, clusterNameColumn,
			publishedColumn, verifiedColumn;

	ComboBox<CampaignFormMetaReferenceDto> newForm = new ComboBox<>();
	ComboBox<CampaignFormMetaReferenceDto> importFormData = new ComboBox<>();

	HorizontalLayout actionButtonlayout = new HorizontalLayout();
	Button exportTransposedDataButton = new Button(I18nProperties.getCaption(Captions.export) + " Transposed Data");

	// Counters for tracking creation
	private int transposdeDataAnchorCreationCount = 0;
	private int exportTransposedDataButtonCreationCount = 0;
	// Initialize a flag to check if the click listener is already attached
	boolean isClickListenerAttached;
	private boolean callbackRunning = false;
	private Timer timer;
	protected final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

	public CampaignDataView() {

		if (I18nProperties.getUserLanguage() == null) {

			I18nProperties.setUserLanguage(Language.EN);
		} else {

			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();
		}
		FacadeProvider.getI18nFacade().setUserLanguage(userProvider.getUser().getLanguage());
		setSizeFull();
		setSpacing(false);
		criteria = new CampaignFormDataCriteria();
		criteria.setUsertype(userProvider.getUser().getUsertype().toString());
		criteria.setUsertypeEnum(userProvider.getUser().getUsertype());
		createCampaignDataFilter();

		configureGrid(criteria);
//		configureGridDataRestricttion(criteria);
		configureColumnStyles(criteria);
	}

	private String getLabelForEnum(CampaignPhase campaignPhase) {
		if (campaignPhase != null) {
			switch (campaignPhase) {
			case PRE:
				return "Pre-Campaign";

			case POST:
				return "Post-Campaign";

			case INTRA:
				return "Intra-Campaign";

			default:
				return campaignPhase.toString();
			}
		} else {
			return "Intra-Campaign";
		}

	}

	private void createCampaignDataFilter() {
		setMargin(true);

		newForm.setLabel(I18nProperties.getCaption(Captions.actionNewForm));
		newForm.setPlaceholder(I18nProperties.getCaption(Captions.dataEntry));
		newForm.setTooltipText(I18nProperties.getDescription(Descriptions.campaign_dataEntry));
		newForm.setClearButtonVisible(true);
		newForm.getStyle().set("padding-top", "0px !important");

		importFormData.setLabel(I18nProperties.getCaption(Captions.actionImport));
		importFormData.setPlaceholder(I18nProperties.getCaption(Captions.dataImport));
		importFormData.setTooltipText(I18nProperties.getDescription(Descriptions.campaign_dataImport));
		importFormData.setClearButtonVisible(true);
		importFormData.getStyle().set("padding-top", "0px !important");

		VerticalLayout filterBlock = new VerticalLayout();
		filterBlock.setSpacing(true);
		filterBlock.setMargin(true);
		filterBlock.setClassName("campaignDataFilterParent");

		HorizontalLayout layout = new HorizontalLayout();
		layout.setAlignItems(Alignment.END);
		I18nProperties.setDefaultLanguage(userProvider.getUser().getLanguage());
		Button displayFilters = new Button(I18nProperties.getCaption(Captions.hideFilters),
				new Icon(VaadinIcon.SLIDERS));

		actionButtonlayout.setClassName("row pl-3");
		actionButtonlayout.setVisible(true);
		actionButtonlayout.setAlignItems(Alignment.END);

		Button exportButton = new Button(I18nProperties.getCaption(Captions.export));
		exportButton.setIcon(new Icon(VaadinIcon.UPLOAD));

		exportTransposedDataButton.setIcon(new Icon(VaadinIcon.UPLOAD));

		exportButton.addClickListener(e -> {
			anchor.getElement().callJsFunction("click");
		});

		actionButtonlayout.add(campaignYear, campaignz, campaignPhase, newForm, importFormData, exportButton, anchor);
		anchor.getStyle().set("display", "none");

		HorizontalLayout level1Filters = new HorizontalLayout();
		level1Filters.setPadding(false);
		level1Filters.setVisible(true);
		level1Filters.setWidth("98%");
		level1Filters.setAlignItems(Alignment.END);
		level1Filters.setClassName("row pl-3");

		publishedStatusCombo.setVisible(false);
		verifiedStatusCombo.setVisible(false);

		HorizontalLayout rightFloat = new HorizontalLayout();
		rightFloat.setWidth("100%");
		rightFloat.setJustifyContentMode(JustifyContentMode.END);
		level1Filters.add(campaignFormCombo, regionCombo, provinceCombo, districtCombo, clusterCombo,
				verifiedStatusCombo, publishedStatusCombo, importanceSwitcher, resetHandler, rightFloat);

		displayFilters.addClickListener(e -> {
			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			if (!level1Filters.isVisible()) {
				actionButtonlayout.setVisible(true);
				level1Filters.setVisible(true);
				displayFilters.setText(I18nProperties.getCaption(Captions.hideFilters));
			} else {
				actionButtonlayout.setVisible(false);
				level1Filters.setVisible(false);
				displayFilters.setText(I18nProperties.getCaption(Captions.showFilters));
			}
		});

		TextArea resultField = new TextArea();
		resultField.setWidth("100%");

		campaignYear.setLabel(I18nProperties.getCaption(Captions.campaignYear));
		campaignYear.getStyle().set("padding-top", "0px !important");
		campaignYear.setClassName("col-sm-6, col-xs-6");

		campaignz.setLabel(I18nProperties.getCaption(Captions.Campaigns));
		campaignz.getStyle().set("padding-top", "0px !important");
		campaignz.setClassName("col-sm-6, col-xs-6");

		campaignPhase.setLabel(I18nProperties.getCaption(Captions.Campaign_phase));
		campaignPhase.getStyle().set("padding-top", "0px !important");
		campaignz.setClassName("col-sm-6, col-xs-6");

		campaignFormCombo.setLabel(I18nProperties.getCaption(Captions.campaignCampaignForm));
		campaignFormCombo.getStyle().set("padding-top", "0px !important");
		campaignFormCombo.getStyle().set("--vaadin-combo-box-overlay-width", "350px");
		campaignFormCombo.setClassName("col-sm-6, col-xs-6");

		regionCombo.setLabel(I18nProperties.getCaption(Captions.area));
		regionCombo.getStyle().set("padding-top", "0px !important");
		regionCombo.setClassName("col-sm-6, col-xs-6");
		regionCombo.setPlaceholder(I18nProperties.getCaption(Captions.area));

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			regionsx = FacadeProvider.getAreaFacade().getAllActiveAsReferencePashto();
			regionCombo.setItems(regionsx);
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
			regionsx = FacadeProvider.getAreaFacade().getAllActiveAsReferenceDari();
			regionCombo.setItems(regionsx);
		} else {
			regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
			regionCombo.setItems(regions);
		}

		if (userProvider.getUser().getArea() != null) {
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				AreaReferenceDto singleArea = userProvider.getUser().getArea();
				AreaDto hgsghsag = FacadeProvider.getAreaFacade().getByUuid(singleArea.getUuid());
				AreaReferenceDto singleAreatw0 = new AreaReferenceDto(hgsghsag.getUuid(), hgsghsag.getFa_af());
				regionCombo.setValue(singleAreatw0);
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				regionCombo.setValue(userProvider.getUser().getArea());
			} else {
				regionCombo.setValue(userProvider.getUser().getArea());// rda56kGbCAja
			}
			regionCombo.setEnabled(false);
		}

		provinceCombo.setLabel(I18nProperties.getCaption(Captions.region));
		provinceCombo.getStyle().set("padding-top", "0px !important");
		provinceCombo.setClassName("col-sm-6, col-xs-6");
		provinceCombo.setPlaceholder(I18nProperties.getCaption(Captions.region));

		provinceCombo.setEnabled(false);
		districtCombo.setEnabled(false);
		clusterCombo.setEnabled(false);

		if (userProvider.getUser().getUserRoles().contains(UserRole.AREA_SURVEILLANCE_SUPERVISOR)
				&& userProvider.getUser().getArea() != null) {

			regionCombo.setValue(userProvider.getUser().getArea());
			regionCombo.setEnabled(false);
			if (regionCombo.getValue() != null) {
				provinces = FacadeProvider.getRegionFacade()
						.getAllActiveByArea(userProvider.getUser().getArea().getUuid());
				provinceCombo.setItems(provinces);
				provinceCombo.setEnabled(true);
			}
		}

		else if (!userProvider.getUser().getUserRoles().contains(UserRole.AREA_SURVEILLANCE_SUPERVISOR)
				&& userProvider.getUser().getArea() != null) {

			regionCombo.setValue(userProvider.getUser().getArea());
			regionCombo.setEnabled(false);

			if (userProvider.getUser().getRegion() != null
					&& userProvider.getUser().getUserRoles().contains(UserRole.SURVEILLANCE_SUPERVISOR)) {

				provinces = FacadeProvider.getRegionFacade()
						.getAllActiveByArea(userProvider.getUser().getArea().getUuid());
				provinceCombo.setItems(provinces);
				provinceCombo.setValue(userProvider.getUser().getRegion());
				provinceCombo.setEnabled(false);

				districts = FacadeProvider.getDistrictFacade()
						.getAllActiveByRegion(userProvider.getUser().getRegion().getUuid());
				districtCombo.setItems(districts);
				districtCombo.setEnabled(true);

			} else if (userProvider.getUser().getRegion() != null
					&& !userProvider.getUser().getUserRoles().contains(UserRole.SURVEILLANCE_SUPERVISOR)) {

				provinces = FacadeProvider.getRegionFacade()
						.getAllActiveByArea(userProvider.getUser().getArea().getUuid());
				provinceCombo.setItems(provinces);
				provinceCombo.setValue(userProvider.getUser().getRegion());
				provinceCombo.setEnabled(false);

				if (userProvider.getUser().getDistrict() != null
						&& userProvider.getUser().getUserRoles().contains(UserRole.SURVEILLANCE_OFFICER)) {

					districts = FacadeProvider.getDistrictFacade()
							.getAllActiveByRegion(userProvider.getUser().getRegion().getUuid());
					districtCombo.setItems(districts);
					districtCombo.setValue(userProvider.getUser().getDistrict());
					districtCombo.setEnabled(false);
					if (districtCombo.getValue() != null) {

						communities = FacadeProvider.getCommunityFacade()
								.getAllActiveByDistrict(districtCombo.getValue().getUuid());
						clusterCombo.clear();
						clusterCombo.setItems(communities);
						clusterCombo.setEnabled(true);
					}
				} else if (userProvider.getUser().getDistrict() != null
						&& !userProvider.getUser().getUserRoles().contains(UserRole.SURVEILLANCE_OFFICER)) {

					provinces = FacadeProvider.getRegionFacade()
							.getAllActiveByArea(userProvider.getUser().getArea().getUuid());
					provinceCombo.setItems(provinces);
					provinceCombo.setValue(userProvider.getUser().getRegion());
					provinceCombo.setEnabled(false);

					districts = FacadeProvider.getDistrictFacade()
							.getAllActiveByRegion(userProvider.getUser().getRegion().getUuid());
					districtCombo.setItems(districts);
					districtCombo.setValue(userProvider.getUser().getDistrict());
					districtCombo.setEnabled(false);

					communities = FacadeProvider.getCommunityFacade()
							.getAllActiveByDistrict(userProvider.getUser().getDistrict().getUuid());
					clusterCombo.setItems(communities);
					clusterCombo.setEnabled(true);

					if (userProvider.getUser().getCommunity() != null
							&& userProvider.getUser().getUserRoles().contains(UserRole.COMMUNITY_OFFICER)) {

						communities = FacadeProvider.getCommunityFacade()
								.getAllActiveByDistrict(userProvider.getUser().getDistrict().getUuid());
						clusterCombo.setItems(communities);
//					clusterCombo.setValue(userProvider.getUser().getCommunity());
						clusterCombo.setEnabled(false);

					} else if (userProvider.getUser().getCommunity() != null
							&& !userProvider.getUser().getUserRoles().contains(UserRole.COMMUNITY_OFFICER)) {

						communities = FacadeProvider.getCommunityFacade()
								.getAllActiveByDistrict(userProvider.getUser().getDistrict().getUuid());
						clusterCombo.setItems(communities);
//					clusterCombo.setValue(userProvider.getUser().getCommunity());
						clusterCombo.setEnabled(false);
					} else {

						communities = FacadeProvider.getCommunityFacade()
								.getAllActiveByDistrict(userProvider.getUser().getDistrict().getUuid());
						clusterCombo.setItems(communities);
						clusterCombo.setEnabled(true);
					}

				} else {

					districts = FacadeProvider.getDistrictFacade()
							.getAllActiveByRegion(userProvider.getUser().getRegion().getUuid());
					districtCombo.setItems(districts);
					districtCombo.setEnabled(true);

				}

			} else {

				provinces = FacadeProvider.getRegionFacade()
						.getAllActiveByArea(userProvider.getUser().getArea().getUuid());
				provinceCombo.setItems(provinces);
				provinceCombo.setEnabled(true);
			}
		}

		else {

			provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
			provinceCombo.setItems(provinces);
		}

		dropdownBulkOperations = new MenuBar();
		MenuItem bulkActionsItem = dropdownBulkOperations.addItem(I18nProperties.getCaption(Captions.bulkActions));
		SubMenu subMenu = bulkActionsItem.getSubMenu();

		final MenuItem deleteBulkItem = subMenu.addItem(I18nProperties.getCaption(Captions.actionDelete),
				e -> handleDeleteAction());
		final MenuItem verifyDataBulkItem = subMenu.addItem(I18nProperties.getCaption(Captions.actionVerify),
				e -> handleDataVerificationAction());

		final MenuItem publishDataBulkItem = subMenu.addItem(I18nProperties.getCaption(Captions.actionPublishData),
				e -> handleDataPublishingAction());

		if (userProvider.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			enterBulkEdit = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode));
			leaveBulkEdit = new Button(I18nProperties.getCaption(Captions.actionLeaveBulkEditMode));

			deleteBulkItem.setVisible(true);

			if (userProvider.getUser().getUsertype() == UserType.WHO_USER && campaignPhase.getValue() != null
					&& userProvider.getUser().getUserRoles().contains(UserRole.PUBLISH_USER)) {
				if (campaignPhase.getValue().toString().equalsIgnoreCase("post-campaign")) {
					verifyDataBulkItem.setVisible(true);
					publishDataBulkItem.setVisible(true);
				} else {
					verifyDataBulkItem.setVisible(false);
					publishDataBulkItem.setVisible(false);

				}
			} else {
				verifyDataBulkItem.setVisible(false);
				publishDataBulkItem.setVisible(false);

			}
			selectAllButton = new Button("");

			selectAllButton.addClickListener(event -> {
				if (!grid.getSelectedItems().isEmpty()) {
					grid.deselectAll();
					selectAllButtonpLACEHOLDER.setText("Select All");
					checkboxx.setValue(false);

				} else {
					selectAllButtonpLACEHOLDER.setText("Deselect All");
					checkboxx.setValue(true);
				}
			});

		}

		provinceCombo.getStyle().set("padding-top", "0px");
		provinceCombo.setClassName("col-sm-6, col-xs-6");

		districtCombo.setLabel(I18nProperties.getCaption(Captions.district));
		districtCombo.getStyle().set("padding-top", "0px !important");
		districtCombo.setClassName("col-sm-6, col-xs-6");
		districtCombo.setPlaceholder(I18nProperties.getCaption(Captions.district));

		clusterCombo.setLabel(I18nProperties.getCaption(Captions.community));
		clusterCombo.getStyle().set("padding-top", "0px !important");
		clusterCombo.setPlaceholder(I18nProperties.getCaption(Captions.community));
		clusterCombo.setClassName("col-sm-6, col-xs-6");
		clusterCombo.getStyle().set("--vaadin-combo-box-overlay-width", "350px");

		verifiedStatusCombo.setLabel(I18nProperties.getCaption("Verified Status"));
		verifiedStatusCombo.getStyle().set("padding-top", "0px !important");
		verifiedStatusCombo.setPlaceholder(I18nProperties.getCaption("All Data"));
		verifiedStatusCombo.setClassName("col-sm-6, col-xs-6");
		verifiedStatusCombo.getStyle().set("--vaadin-combo-box-overlay-width", "350px");
		verifiedStatusCombo.setItems("All Data", "Verified", "Unverified");

		publishedStatusCombo.setLabel(I18nProperties.getCaption("Publish Status"));
		publishedStatusCombo.getStyle().set("padding-top", "0px !important");
		publishedStatusCombo.setPlaceholder(I18nProperties.getCaption("All Data"));
		publishedStatusCombo.setClassName("col-sm-6, col-xs-6");
		publishedStatusCombo.getStyle().set("--vaadin-combo-box-overlay-width", "350px");
		publishedStatusCombo.setItems("All Data", "Published", "Unpublished");

		// Initialize Item lists
		List<CampaignReferenceDto> campaigns = FacadeProvider.getCampaignFacade().getAllCampaignByStartDate();
		CampaignReferenceDto lastStarted = FacadeProvider.getCampaignFacade().getLastStartedCampaign();
		List<String> camYearList = campaigns.stream().map(CampaignReferenceDto::getCampaignYear).distinct()
				.collect(Collectors.toList());

		campaignYear.setItems(camYearList);
		campaignYear.setItemLabelGenerator(item -> {

			switch (userProvider.getUser().getLanguage().toString()) {
			case "Pashto":
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
				return String.valueOf(arabicFormat.format(Long.parseLong(item))).replace(",", "");
			case "Dari":
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
				return String.valueOf(arabicFormat.format(Long.parseLong(item))).replace(",", "");
			default:
				arabicFormat = NumberFormat.getInstance(new Locale("en"));
				return String.valueOf(arabicFormat.format(Long.parseLong(item))).replace(",", "");
			}
		});
		campaignYear.setValue(lastStarted.getCampaignYear());

		List<CampaignReferenceDto> allCampaigns = campaigns.stream()
				.filter(c -> c.getCampaignYear().equals(campaignYear.getValue())).collect(Collectors.toList());

		campaignz.setItems(allCampaigns);
		campaignz.setValue(lastStarted);

		campaignPhase.setItemLabelGenerator(this::getLabelForEnum);
		if (userProvider.getUser().getUsertype() == UserType.EOC_USER) {
			campaignPhase.setItems(CampaignPhase.values());
			campaignPhase.setValue(CampaignPhase.INTRA);
		} else {
			campaignPhase.setItems(CampaignPhase.values());
			campaignPhase.setValue(CampaignPhase.PRE);
		}

		configureFormNameTranslations();

		campaignFormCombo.getStyle().set("--vaadin-combo-box-overlay-width", "350px");
		if (campaignForms.size() > 0) {
			campaignFormCombo.setValue(campaignForms.get(0));

		}

		criteria.campaign(lastStarted);
		criteria.setFormType(campaignPhase.getValue().toString());
		criteria.setCampaignFormMeta(campaignFormCombo.getValue());

		int numberOfRows = (int) FacadeProvider.getCampaignFormDataFacade().count(criteria);
		countRowItems = new Paragraph(I18nProperties.getCaption(Captions.rows) + numberOfRows);
		countRowItems.setId("rowCount");
		rightFloat.add(countRowItems);

		// Configure Comboboxes Value Change Listeners
		campaignYear.addValueChangeListener(e -> {
			campaignz.clear();
			List<CampaignReferenceDto> allCampaigns_ = campaigns.stream()
					.filter(c -> c.getCampaignYear().equals(campaignYear.getValue())).collect(Collectors.toList());
			campaignz.setItems(allCampaigns_);
			campaignz.setValue(allCampaigns_.get(0));
			importanceSwitcher.setReadOnly(false);

		});

		campaignz.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				campaignFormCombo.clear();
				newForm.clear();
				importFormData.clear();
				configureFormNameTranslations();
				campaignFormCombo.setValue(campaignForms.get(0));
				exportFileName = e.getValue().toString() + "_"
						+ campaignFormCombo.getValue().toString().replaceAll("[^a-zA-Z0-9]+", " ") + "_"
						+ new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime());
				exporter.setFileName(exportFileName);
				anchor.setHref(exporter.getCsvStreamResource());
				importanceSwitcher.clear();
				importanceSwitcher.setReadOnly(false);
				reload();
				updateRowCount();
			}
		});

		campaignPhase.addValueChangeListener(e -> {

			System.out.println(e.getValue() + " form phase vaue ");
			importanceSwitcher.clear();
			importanceSwitcher.setReadOnly(false);
			if (e.getValue() != null) {
				
				if (e.getValue().toString().equalsIgnoreCase("post-campaign")) {
					verifiedStatusCombo.setVisible(true);
					publishedStatusCombo.setVisible(true);

//					System.out.println("post campaign selected ");

					if (userProvider.getUser().getUsertype() == UserType.WHO_USER) {
						if (userProvider.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
							if (userProvider.getUser().getUserRoles().contains(UserRole.PUBLISH_USER)) {
								verifyDataBulkItem.setVisible(true);
								publishDataBulkItem.setVisible(true);
								System.out.println("user is a publish user ");
							} else {
								verifyDataBulkItem.setVisible(false);
								publishDataBulkItem.setVisible(false);
								System.out.println("user is NOT a publish user ");
							}
//							System.out.println("user ca n do bulk peration an is who  2");
						} else {
							verifyDataBulkItem.setVisible(false);
							publishDataBulkItem.setVisible(false);
//							System.out.println("can either not don bvulk and  is  who   ");
						}

						verifiedColumn.setVisible(true);
						publishedColumn.setVisible(true);
//						System.out.println("user ca n do bulk peration an is who  ");
					} else {
						verifiedStatusCombo.setVisible(false);
						publishedStatusCombo.setVisible(false);
						if (verifiedColumn != null || publishedColumn != null) {
							verifiedColumn.setVisible(false);
							publishedColumn.setVisible(false);
						}
						verifyDataBulkItem.setVisible(false);
						publishDataBulkItem.setVisible(false);
//						System.out.println("can either not don bvulk or is not who   ");
					}

				} else {
					verifiedStatusCombo.setVisible(false);
					publishedStatusCombo.setVisible(false);
					if (verifiedColumn != null || publishedColumn != null) {
						verifiedColumn.setVisible(false);
						publishedColumn.setVisible(false);
					}
//					verifiedColumn.setVisible(false);
//					publishedColumn.setVisible(false);
					verifyDataBulkItem.setVisible(false);
					publishDataBulkItem.setVisible(false);
//					System.out.println("non - post campaign selected ");

				}

			}

			campaignFormCombo.clear();
			newForm.clear();
			importFormData.clear();
			configureFormNameTranslations();
			campaignFormCombo.setValue(campaignForms.get(0));
			remove(grid);
			configureGrid(criteria);
			updateRowCount();

		});

		campaignFormCombo.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				formMetaReference = FacadeProvider.getCampaignFormMetaFacade()
						.getCampaignFormMetaByUuid(e.getValue().getUuid());
				exportFileName = campaignz.getValue().toString() + "_"
						+ campaignFormCombo.getValue().toString().replaceAll("[^a-zA-Z0-9]+", " ") + "_"
						+ new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime());
				exporter.setFileName(exportFileName);
				anchor.setHref(exporter.getCsvStreamResource());
				importanceSwitcher.clear();
				importanceSwitcher.setReadOnly(false);

				reload();
				configureColumnStyles(criteria);

				// Remove the exportTransposedDataButton and transposdeDataAnchor if they exist
				if (actionButtonlayout.getChildren()
						.anyMatch(component -> component.equals(exportTransposedDataButton))) {
					actionButtonlayout.remove(exportTransposedDataButton);
				}
				if (actionButtonlayout.getChildren().anyMatch(component -> component.equals(transposdeDataAnchor))) {
					actionButtonlayout.remove(transposdeDataAnchor);
				}

				// Check if the value contains "Day 1" and add new components
				if (e.getValue().toString().contains("Day 1")) {
					generateTransposeDataFunctions(actionButtonlayout, campaignFormCombo.getValue().toString());
				}
			} else {
				importanceSwitcher.clear();
				importanceSwitcher.setReadOnly(true);
			}
			updateRowCount();
			configureColumnStyles(criteria);
		});

//		campaignFormCombo.addValueChangeListener(e -> {
//
//			if (e.getValue() != null) {
//				formMetaReference = FacadeProvider.getCampaignFormMetaFacade()
//						.getCampaignFormMetaByUuid(e.getValue().getUuid());
//				exportFileName = campaignz.getValue().toString() + "_"
//						+ campaignFormCombo.getValue().toString().replaceAll("[^a-zA-Z0-9]+", " ") + "_"
//						+ new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime());
//				exporter.setFileName(exportFileName);
////				System.out.println(exportFileName + "Export file name on form change ");
//				anchor.setHref(exporter.getCsvStreamResource());
//				importanceSwitcher.clear();
//				importanceSwitcher.setReadOnly(false);
//
//				reload();
//				configureColumnStyles(criteria);
//
//				// Known problem around here , depending on the number of times this filter
//				// value changes
//				// the export button would download a file with the numbe rof criteria that has
//				// baaen send out
//				// i.e if this filter value changes 10 times, the next time you click the
//				// button, it downloads 10 files
//				// if it chnges again and you click the button, it down loads 11 times
//				
//				
//				if (transposdeDataAnchorCreationCount > 0 ) {
//					actionButtonlayout.remove(exportTransposedDataButton, transposdeDataAnchor);
//					
//					System.out.println("i just kicked ---------------------------------------- ");
//
//				}
//				checkIfExportTransposedDataButtonIsAttached();
//				
//				if (e.getValue().toString().contains("Day 1")) {
//					
//					CampaignFormDataCriteria  campaignFormDataCriteria =  new  CampaignFormDataCriteria();
//					
//					campaignFormDataCriteria = criteria;
//					
//
//					actionButtonlayout.add(exportTransposedDataButton, transposdeDataAnchor);
//
//					DownloadTransposedDaywiseDataUtility downloadTransposedDaywiseDataUtility = new DownloadTransposedDaywiseDataUtility();
//					transposdeDataAnchor.setHref(downloadTransposedDaywiseDataUtility.createTransposedDataFromIndexListDemox2(campaignFormDataCriteria));
//					transposdeDataAnchor.getElement().setAttribute("download", true);
//					exportTransposedDataButton.addClickListener(ex -> {
//						
//	                    System.out.println("transposdeDataAnchor created ---------------------------------" + criteria);
//
//						transposdeDataAnchor.getElement().callJsFunction("click");
//					});
//					
//					// Increment counters when components are created
//                    transposdeDataAnchorCreationCount++;
//                    exportTransposedDataButtonCreationCount++;
//
//                    System.out.println("transposdeDataAnchor created " + transposdeDataAnchorCreationCount + " times");
//                    System.out.println("exportTransposedDataButton created " + exportTransposedDataButtonCreationCount + " times");
//				}
//				;
//
//			} else {
//
//				importanceSwitcher.clear();
//				importanceSwitcher.setReadOnly(true);
//
//			}
//			updateRowCount();
//			configureColumnStyles(criteria);
//
//		});

		regionCombo.setClearButtonVisible(true);
		regionCombo.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					provinces = FacadeProvider.getRegionFacade().getAllActiveByAreaPashto(e.getValue().getUuid());
					provinceCombo.setItems(provinces);
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					provinces = FacadeProvider.getRegionFacade().getAllActiveByAreaDari(e.getValue().getUuid());
					provinceCombo.setItems(provinces);
				} else {
					provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
					provinceCombo.setItems(provinces);
				}
				provinceCombo.setEnabled(true);
			} else {
				if (provinceCombo.getValue() != null) {
					provinceCombo.clear();
				}
				provinceCombo.setEnabled(false);
			}
			reload();
			updateRowCount();
		});

		provinceCombo.setClearButtonVisible(true);

		provinceCombo.addValueChangeListener(e -> {
			if (e.getValue() != null) {

				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					districts = FacadeProvider.getDistrictFacade().getAllActiveByRegionPashto(e.getValue().getUuid());
					districtCombo.setItems(districts);
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					districts = FacadeProvider.getDistrictFacade().getAllActiveByRegionDari(e.getValue().getUuid());
					districtCombo.setItems(districts);
				} else {
					districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
					districtCombo.setItems(districts);
				}
				districtCombo.setEnabled(true);
			} else {
				if (districtCombo.getValue() != null) {
					districtCombo.clear();
				}
				districtCombo.setEnabled(false);
			}
			reload();
			updateRowCount();
		});

		districtCombo.setClearButtonVisible(true);

		districtCombo.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				communities = FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid());
				clusterCombo.setItemLabelGenerator(itm -> {
					CommunityReferenceDto dcfv = (CommunityReferenceDto) itm;
					return dcfv.getNumber() + " | " + dcfv.getCaption();
				});
				clusterCombo.setItems(communities);

				clusterCombo.setEnabled(true);
			} else {
				if (clusterCombo.getValue() != null) {
					clusterCombo.clear();
				}
				clusterCombo.setEnabled(false);
			}

			reload();
			updateRowCount();
		});

		clusterCombo.setClearButtonVisible(true);
		clusterCombo.addValueChangeListener(e -> {
			reload();
			updateRowCount();
		});

		newForm.addValueChangeListener(e -> {
			if (e.getValue() != null && campaignz != null) {
				CampaignFormMetaDto formDatax = FacadeProvider.getCampaignFormMetaFacade()
						.getCampaignFormMetaByUuid(e.getValue().getUuid());

				boolean fff = formDatax.isDistrictentry();

				CampaignFormDataEditForm cam = new CampaignFormDataEditForm(e.getValue(), campaignz.getValue(), false,
						null, grid, fff);
				// add(cam);

				newForm.setValue(null);
			}
		});

		importFormData.addValueChangeListener(e -> {
			CampaignDto campaignUuid = FacadeProvider.getCampaignFacade().getByUuid(campaignz.getValue().getUuid());

			if (importFormData.getValue() != null) {

				startIntervalCallback();
				UI.getCurrent().addPollListener(event -> {
					if (callbackRunning) {
						UI.getCurrent().access(this::pokeFlow);
					} else {
						stopPullers();
					}
				});

				// CampaignReferenceDto camapigndto, CampaignFormMetaDto campaignFormMetaDto
				ImportCampaignsFormDataDialog dialogx = new ImportCampaignsFormDataDialog(campaignz.getValue(),
						importFormData.getValue(), campaignUuid);
				startIntervalCallback();

				dialogx.open();
			}
		});

		// TODO Importance filter switcher should be visible only on the change of form

		importanceSwitcher.setPlaceholder(I18nProperties.getCaption(Captions.importance));
		importanceSwitcher.setItems(CampaignFormElementImportance.values());
		importanceSwitcher.getStyle().set("padding-top", "0px !important");
		importanceSwitcher.setClassName("col-sm-6, col-xs-6");
		importanceSwitcher.setClearButtonVisible(true);
		importanceSwitcher.setTooltipText(I18nProperties.getDescription(Descriptions.campaign_importance));
		importanceSwitcher.addValueChangeListener(e -> {
			formMetaReference = FacadeProvider.getCampaignFormMetaFacade()
					.getCampaignFormMetaByUuid(campaignFormCombo.getValue().getUuid());

			reload();
			if (formMetaReference != null) {
				remove(grid);

				configureGrid(criteria);

				final boolean allAndImportantFormElements = e.getValue() == CampaignFormElementImportance.ALL;
				final boolean onlyImportantFormElements = e.getValue() == CampaignFormElementImportance.IMPORTANT;

				final List<CampaignFormElement> campaignFormElements = formMetaReference.getCampaignFormElements();

				for (CampaignFormElement element : campaignFormElements) {

					if (element.isImportant() && onlyImportantFormElements) {
						String caption = null;
						if (caption == null) {
							caption = element.getCaption();
						}

						if (caption != null) {
							addCustomColumn(element.getId(), caption);
						}
					} else if (allAndImportantFormElements) {
						String caption = null;
						if (caption == null) {
							caption = element.getCaption();
						}
						if (caption != null) {
							addCustomColumn(element.getId(), caption);
						}
					}
				}
			}

			configureColumnStyles(criteria);

		});

		verifiedStatusCombo.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				if (e.getValue().equalsIgnoreCase("unverified")) {
					criteria.setIsVerified(false);

				} else if (e.getValue().equalsIgnoreCase("verified")) {
					criteria.setIsVerified(true);

				} else {
					criteria.setIsVerified(null);

				}

			} else {
				criteria.setIsVerified(null);
			}

			reload();
			updateRowCount();
		});

		publishedStatusCombo.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				if (e.getValue().equalsIgnoreCase("unpublished")) {
					criteria.setIsPublished(false);

				} else if (e.getValue().equalsIgnoreCase("published")) {
					criteria.setIsPublished(true);

				} else {
					criteria.setIsPublished(null);

				}

			} else {
				criteria.setIsPublished(null);
			}

			reload();
			updateRowCount();
		});

		enterBulkEdit.getStyle().set("margin-top", "5px");

		enterBulkEdit.addClassName("bulkActionButton");
		Icon bulkModeButtonnIcon = new Icon(VaadinIcon.CLIPBOARD_CHECK);
		enterBulkEdit.setIcon(bulkModeButtonnIcon);
		actionButtonlayout.add(enterBulkEdit);

		enterBulkEdit.addClickListener(e -> {

			selectAllButtonpLACEHOLDER.setText("Select All");
			dropdownBulkOperations.setVisible(true);
			selectAllButtonpLACEHOLDER.setVisible(true);

			grid.setSelectionMode(Grid.SelectionMode.MULTI);

			ComponentRenderer<Checkbox, CampaignFormDataIndexDto> checkboxRenderer = new ComponentRenderer<>(item -> {
				checkboxx.setValue(grid.getSelectedItems().contains(item)); // Set the initial value
				checkboxx.addValueChangeListener(event -> {
					if (event.getValue()) {
						grid.select(item);
					} else {
						grid.deselect(item);
					}
				});
				return checkboxx;
			});

			grid.addColumn(checkboxRenderer).setHeader(selectAllButton).setSortable(false).setResizable(true)
					.setAutoWidth(true).setVisible(false);

			enterBulkEdit.setVisible(false);
			leaveBulkEdit.setVisible(true);
		});

		selectAllButtonpLACEHOLDER.addClickListener(e -> {
			selectAllButton.click();
		});

		leaveBulkEdit.setText(I18nProperties.getCaption(Captions.actionLeaveBulkEditMode));
		leaveBulkEdit.addClassName("leaveBulkActionButton");
		leaveBulkEdit.setVisible(false);
		Icon leaveBulkModeButtonnIcon = new Icon(VaadinIcon.CLIPBOARD_CHECK);
		leaveBulkEdit.setIcon(leaveBulkModeButtonnIcon);
		actionButtonlayout.add(leaveBulkEdit);

		leaveBulkEdit.addClickListener(e -> {
			grid.setSelectionMode(Grid.SelectionMode.SINGLE);
			enterBulkEdit.setVisible(true);
			leaveBulkEdit.setVisible(false);
			selectAllButtonpLACEHOLDER.setVisible(false);

			dropdownBulkOperations.setVisible(false);
		});
		dropdownBulkOperations.setVisible(false);
		actionButtonlayout.add(dropdownBulkOperations);
		selectAllButtonpLACEHOLDER.setText("Select All");
		selectAllButtonpLACEHOLDER.setVisible(false);
		actionButtonlayout.add(selectAllButtonpLACEHOLDER);

		resetHandler.setText(I18nProperties.getCaption(Captions.resetFilters));
		resetHandler.addClickListener(e -> {
			UI.getCurrent().getPage().reload();
			criteria.campaign(lastStarted);
			criteria.setFormType(campaignPhase.getValue().toString());
			updateRowCount();
		});

		layout.add(displayFilters, actionButtonlayout);

		filterBlock.add(layout, level1Filters);

		add(filterBlock);
	}

	public void checkIfExportTransposedDataButtonIsAttached() {
		boolean exportButtonExists = actionButtonlayout.getChildren()
				.anyMatch(component -> component.equals(exportTransposedDataButton));

		boolean transposdeDataAnchorExists = actionButtonlayout.getChildren()
				.anyMatch(component -> component.equals(transposdeDataAnchor));

//		System.out.println("exportButtonExists" + exportButtonExists +  "transposdeDataAnchorExists" + transposdeDataAnchorExists + "0=====================");

		if (exportButtonExists && transposdeDataAnchorExists) {
			actionButtonlayout.remove(exportTransposedDataButton, transposdeDataAnchor);
		} else {

		}
	}

	public void generateTransposeDataFunctions(HorizontalLayout actionButionLayout, String formName) {
		transposdeDataAnchor = new Anchor("", "TransposeDaywiseData");
		transposdeDataAnchor.getStyle().set("display", "none");

		exportTransposedDataButton = new Button(I18nProperties.getCaption(Captions.export) + " Transposed Data");

		if (transposdeDataAnchor.getElement().getAttribute("href") != "") {
			transposdeDataAnchor.setHref("");
		}

		CampaignFormDataCriteria transposedDataCriteria = new CampaignFormDataCriteria();
		transposedDataCriteria = criteria;
		actionButionLayout.add(exportTransposedDataButton, transposdeDataAnchor);
		DownloadTransposedDaywiseDataUtility downloadTransposedDaywiseDataUtility = new DownloadTransposedDaywiseDataUtility();
		transposdeDataAnchor.setHref(downloadTransposedDaywiseDataUtility
				.createTransposedDataFromIndexList(transposedDataCriteria, formName, campaignz.getValue().toString()));

		exportTransposedDataButton.addClickListener(ex -> {
			transposdeDataAnchor.getElement().setAttribute("download", true);
			transposdeDataAnchor.getElement().callJsFunction("click");
		});

	}

	public void removeColumnsSelectionn() {
		grid.setSelectionMode(SelectionMode.NONE);
	}

	public void setgridVisibility() {
		grid.getColumnByKey(CampaignFormDataIndexDto.ISVERIFIED).setVisible(true);
//		grid.getColumnByKey("Cam").setVisible(true);
	}

	public void setgridVisibilityx() {
		grid.getColumnByKey(CampaignFormDataIndexDto.ISPUBLISHED).setVisible(true);
//		grid.getColumnByKey("Cam").setVisible(true);
	}

	public void configureFormNameTranslations() {

		String language = userProvider.getUser().getLanguage().toString();

		Set<FormAccess> xx = new HashSet<FormAccess>();
		xx = userProvider.getUser().getFormAccess();

		List<CampaignFormMetaReferenceDto> filterdList = new ArrayList<>();

		switch (language) {
		case "Pashto":
			campaignForms = FacadeProvider.getCampaignFormMetaFacade()
					.getCampaignFormMetasAsReferencesByCampaignandRoundAndPashto(
							campaignPhase.getValue().toString().toLowerCase(), campaignz.getValue().getUuid());

			for (FormAccess n : xx) {
				boolean yn = campaignForms.stream().filter(e -> !e.getFormCategory().equals(null))
						.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()).size() > 0;
				if (yn) {
					filterdList.addAll(campaignForms.stream().filter(e -> !e.getFormCategory().equals(null))
							.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()));
				}
			}

			filterdList.sort(Comparator.comparing(CampaignFormMetaReferenceDto::getCaption));

//			campaignForms = FacadeProvider.getCampaignFormMetaFacade()
//					.getAllCampaignFormMetasAsReferencesByRoundUserLanguageCampaignandForm(
//							campaignPhase.getValue().toString().toLowerCase(), campaignz.getValue().getUuid(),
//							userProvider.getUser().getFormAccess(), "Dari");
//			campaignForms.sort(Comparator.comparing(CampaignFormMetaReferenceDto::getCaption));

			newForm.setItems(filterdList);
			importFormData.setItems(filterdList);
			campaignFormCombo.setItems(filterdList);
//			campaignForms.sort(Comparator.comparing(CampaignFormMetaReferenceDto::getCaption));
//			newForm.setItems(campaignForms);
//
////			campaignForms = FacadeProvider.getCampaignFormMetaFacade()
////					.getAllCampaignFormMetasAsReferencesByRoundUserLanguageCampaignandForm(
////							campaignPhase.getValue().toString().toLowerCase(), campaignz.getValue().getUuid(),
////							userProvider.getUser().getFormAccess(), "Pashto");
////			campaignForms.sort(Comparator.comparing(CampaignFormMetaReferenceDto::getCaption));
////			newForm.setItems(campaignForms);
//			importFormData.setItems(campaignForms);
//			campaignFormCombo.setItems(campaignForms);
			break;

		case "Dari":

			campaignForms = FacadeProvider.getCampaignFormMetaFacade()
					.getAllCampaignFormMetasAsReferencesByRoundandCampaignRoundAndDari(
							campaignPhase.getValue().toString().toLowerCase(), campaignz.getValue().getUuid());

			for (FormAccess n : xx) {
				boolean yn = campaignForms.stream().filter(e -> !e.getFormCategory().equals(null))
						.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()).size() > 0;
				if (yn) {
					filterdList.addAll(campaignForms.stream().filter(e -> !e.getFormCategory().equals(null))
							.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()));
				}
			}

			filterdList.sort(Comparator.comparing(CampaignFormMetaReferenceDto::getCaption));

//			campaignForms = FacadeProvider.getCampaignFormMetaFacade()
//					.getAllCampaignFormMetasAsReferencesByRoundUserLanguageCampaignandForm(
//							campaignPhase.getValue().toString().toLowerCase(), campaignz.getValue().getUuid(),
//							userProvider.getUser().getFormAccess(), "Dari");
//			campaignForms.sort(Comparator.comparing(CampaignFormMetaReferenceDto::getCaption));

			newForm.setItems(filterdList);
			importFormData.setItems(filterdList);
			campaignFormCombo.setItems(filterdList);
//			campaignForms.sort(Comparator.comparing(CampaignFormMetaReferenceDto::getCaption));
//			newForm.setItems(campaignForms);
////			campaignForms = FacadeProvider.getCampaignFormMetaFacade()
////					.getAllCampaignFormMetasAsReferencesByRoundUserLanguageCampaignandForm(
////							campaignPhase.getValue().toString().toLowerCase(), campaignz.getValue().getUuid(),
////							userProvider.getUser().getFormAccess(), "Dari");
////			campaignForms.sort(Comparator.comparing(CampaignFormMetaReferenceDto::getCaption));
////			newForm.setItems(campaignForms);
//			importFormData.setItems(campaignForms);
//			campaignFormCombo.setItems(campaignForms);
			break;

		default:
			campaignForms = FacadeProvider.getCampaignFormMetaFacade()
					.getAllCampaignFormMetasAsReferencesByRoundandCampaign(
							campaignPhase.getValue().toString().toLowerCase(), campaignz.getValue().getUuid());

			campaignForms.removeIf(e -> e.getFormCategory() == null);

			for (FormAccess n : xx) {
				boolean yn = campaignForms.stream().filter(e -> !e.getFormCategory().equals(null))
						.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()).size() > 0;
				if (yn) {
					filterdList.addAll(campaignForms.stream().filter(e -> !e.getFormCategory().equals(null))
							.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()));
				}
			}

			filterdList.sort(Comparator.comparing(CampaignFormMetaReferenceDto::getCaption));

//			campaignForms = FacadeProvider.getCampaignFormMetaFacade()
//					.getAllCampaignFormMetasAsReferencesByRoundUserLanguageCampaignandForm(
//							campaignPhase.getValue().toString().toLowerCase(), campaignz.getValue().getUuid(),
//							userProvider.getUser().getFormAccess(), "Dari");
//			campaignForms.sort(Comparator.comparing(CampaignFormMetaReferenceDto::getCaption));

			newForm.setItems(filterdList);
			importFormData.setItems(filterdList);
			campaignFormCombo.setItems(filterdList);
//		        	newForm.setItems(campaignForms);
			break;

		}

	}

	private void updateGridSelectionMode() {
		if (userProvider.getUser().getUsertype() == UserType.EOC_USER) {
			boolean isPublished = FacadeProvider.getCampaignFacade().isPublished(criteria.getCampaign().getUuid());

			if (isPublished && criteria.getFormType().toString().equals("POST-CAMPAIGN")) {
				grid.setSelectionMode(SelectionMode.SINGLE);
			}
		}
	}

	private void selectAllItems(ListDataProvider<CampaignFormDataIndexDto> dataProvider) {
		MultiSelect<Grid<CampaignFormDataIndexDto>, CampaignFormDataIndexDto> multiSelect = grid.asMultiSelect();
		dataProvider.getItems().forEach(multiSelect::select);
	}

	private <T> void selectAllItems(Grid<T> grid, DataProvider<T, ?> dataProvider) {
		MultiSelect<Grid<T>, T> multiSelect = grid.asMultiSelect();
		dataProvider.fetch(new Query<>()).forEach(multiSelect::select);

		grid.addSelectionListener(event -> {
			for (T item : event.getAllSelectedItems()) {
				grid.select(item);
			}
		});
	}

	private void setBulkModeLoader() {
		grid.setItems(
				query -> FacadeProvider.getCampaignFormDataFacade()
						.getIndexList(criteria, query.getOffset(), query.getLimit(),
								query.getSortOrders().stream()
										.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
												sortOrder.getDirection() == SortDirection.ASCENDING))
										.collect(Collectors.toList()))
						.stream(),
				query -> (int) FacadeProvider.getCampaignFormDataFacade().count(criteria));

	}

	private Stream<CampaignFormDataIndexDto> fetchCampaignFormData(
			Query<CampaignFormDataIndexDto, CampaignFormDataCriteria> query) {
		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			return FacadeProvider.getCampaignFormDataFacade()
					.getIndexListPashto(criteria, query.getOffset(), query.getLimit(), query.getSortOrders().stream()
							.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
									sortOrder.getDirection() == SortDirection.ASCENDING))
							.collect(Collectors.toList()))
					.stream();
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {

//			System.out.println("Darrrrrrrrrrrrrrrrrrrrrrr");

			return FacadeProvider.getCampaignFormDataFacade()
					.getIndexListDari(criteria, query.getOffset(), query.getLimit(), query.getSortOrders().stream()
							.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
									sortOrder.getDirection() == SortDirection.ASCENDING))
							.collect(Collectors.toList()))
					.stream();
		} else {
//			System.out.println("Enggggggggggggggggggggggg");
			return FacadeProvider.getCampaignFormDataFacade()
					.getIndexList(criteria, query.getOffset(), query.getLimit(), query.getSortOrders().stream()
							.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
									sortOrder.getDirection() == SortDirection.ASCENDING))
							.collect(Collectors.toList()))
					.stream();
		}
	}

	private int countCampaignFormData(Query<CampaignFormDataIndexDto, CampaignFormDataCriteria> query) {
		return (int) FacadeProvider.getCampaignFormDataFacade().count(criteria);
	}

	private void handleDeleteAction() {

		deleteAllSelectedItems(grid.getSelectedItems());

	}

	private void handleDataVerificationAction() {

		verifyAllSelectedItems(grid.getSelectedItems());

	}

	private void handleDataPublishingAction() {

		publishAllSelectedItems(grid.getSelectedItems());

	}

	public void deleteAllSelectedItems(Collection<CampaignFormDataIndexDto> selectedRows) {
		confirmationDialog = new ConfirmDialog();

		if (selectedRows.size() == 0) {
			confirmationDialog.setCancelable(false);
			confirmationDialog.setRejectable(false);
			confirmationDialog.addCancelListener(e -> confirmationDialog.close());
			confirmationDialog.setConfirmText(I18nProperties.getCaption(Captions.actionOkay));

			confirmationDialog.setText("You have not selected any data to be deleted.");
			confirmationDialog.setHeader("Error Deleting Campaign Data");
			confirmationDialog.open();

		} else {
			confirmationDialog.setCancelable(true);
			confirmationDialog.setRejectable(true);
			confirmationDialog.setRejectText(I18nProperties.getCaption(Captions.actionNo));
			confirmationDialog.setConfirmText(I18nProperties.getCaption(Captions.actionYes));
			confirmationDialog.addCancelListener(e -> confirmationDialog.close());
			confirmationDialog.addRejectListener(e -> confirmationDialog.close());
			confirmationDialog.open();
			confirmationDialog.setHeader("Delete Campaign Data");
//TODO: Language

			confirmationDialog
					.setText("Are you sure you want to Delete " + selectedRows.size() + " selected Campaign Data?");

			confirmationDialog.addConfirmListener(e -> {
				List<String> uuids = selectedRows.stream().map(CampaignFormDataIndexDto::getUuid)
						.collect(Collectors.toList());
				FacadeProvider.getCampaignFormDataFacade().deleteCampaignData(uuids);
//				 Notification.show("Camapaign Dayta Deleted ");
				reload();
				if (leaveBulkEdit.isVisible()) {
					leaveBulkEdit.setVisible(false);
					enterBulkEdit.setVisible(true);
					grid.setSelectionMode(Grid.SelectionMode.SINGLE);

					dropdownBulkOperations.setVisible(false);
					selectAllButtonpLACEHOLDER.setVisible(false);

				}
			});

		}
	}

	public void publishAllSelectedItems(Collection<CampaignFormDataIndexDto> selectedRows) {
		confirmationDialog = new ConfirmDialog();
		boolean isDataDirty = false;
		for (CampaignFormDataIndexDto selectedItem : selectedRows) {

			System.out.println(selectedRows.size() + "<<<Size of selected items " + selectedItem.isIsverified()
					+ ">>>> Dirty data " + isDataDirty);

			if (selectedItem.isIsverified() == false) {
				isDataDirty = true;
			}

//			System.out.println(selectedRows.size() + "<<<2222222Size of selected items " + selectedItem.isIsverified()
//					+ "2222222222>>>> Dirty data " + isDataDirty);

		}
		if (selectedRows.size() == 0 || isDataDirty) {
			confirmationDialog.setCancelable(false);
			confirmationDialog.setRejectable(false);
			confirmationDialog.addCancelListener(e -> confirmationDialog.close());
			confirmationDialog.setConfirmText(I18nProperties.getCaption(Captions.actionOkay));
			if (selectedRows.size() == 0 && (isDataDirty || !isDataDirty)) {
				confirmationDialog.setText("You have not selected any data to be published.");

			} else if (selectedRows.size() > 0 && isDataDirty) {
				confirmationDialog.setText(
						"You have selected 1 or more unverified records to publish. Please verify any records you wish to publish first.");

			}
			confirmationDialog.setHeader("Error Publishing Campaign Data");
			confirmationDialog.open();

		} else {
			confirmationDialog.setCancelable(true);
			confirmationDialog.setRejectable(true);
			confirmationDialog.setRejectText(I18nProperties.getCaption(Captions.actionNo));
			confirmationDialog.setConfirmText(I18nProperties.getCaption(Captions.actionYes));
			confirmationDialog.addCancelListener(e -> confirmationDialog.close());
			confirmationDialog.addRejectListener(e -> confirmationDialog.close());
			confirmationDialog.open();
			confirmationDialog.setHeader("Publish Campaign Data");
//TODO: Language

			confirmationDialog
					.setText("Are you sure you want to Publish " + selectedRows.size() + " selected Campaign Data?");

			confirmationDialog.addConfirmListener(e -> {
				List<String> uuids = selectedRows.stream().map(CampaignFormDataIndexDto::getUuid)
						.collect(Collectors.toList());

//				System.err.println(" verification kicke from frontend");

//			FacadeProvider.getCampaignFormDataFacade().verifyCampaignData(event.getCampaign().getUuid(), true);

				FacadeProvider.getCampaignFormDataFacade().publishCampaignData(uuids, false);
//				 Notification.show("Camapaign Dayta Deleted ");
				reload();
				if (leaveBulkEdit.isVisible()) {
					leaveBulkEdit.setVisible(false);
					enterBulkEdit.setVisible(true);
					grid.setSelectionMode(Grid.SelectionMode.SINGLE);

					dropdownBulkOperations.setVisible(false);
					selectAllButtonpLACEHOLDER.setVisible(false);

				}
			});

		}
	}

	public void verifyAllSelectedItems(Collection<CampaignFormDataIndexDto> selectedRows) {
		confirmationDialog = new ConfirmDialog();

		if (selectedRows.size() == 0) {
			confirmationDialog.setCancelable(false);
			confirmationDialog.setRejectable(false);
			confirmationDialog.addCancelListener(e -> confirmationDialog.close());
			confirmationDialog.setConfirmText(I18nProperties.getCaption(Captions.actionOkay));

			confirmationDialog.setText("You have not selected any data to be verified.");
			confirmationDialog.setHeader("Error Verifying Campaign Data");
			confirmationDialog.open();

		} else {
			confirmationDialog.setCancelable(true);
			confirmationDialog.setRejectable(true);
			confirmationDialog.setRejectText(I18nProperties.getCaption(Captions.actionNo));
			confirmationDialog.setConfirmText(I18nProperties.getCaption(Captions.actionYes));
			confirmationDialog.addCancelListener(e -> confirmationDialog.close());
			confirmationDialog.addRejectListener(e -> confirmationDialog.close());
			confirmationDialog.open();
			confirmationDialog.setHeader("Verify Campaign Data");
//TODO: Language

			confirmationDialog
					.setText("Are you sure you want to Verify " + selectedRows.size() + " selected Campaign Data?");

			confirmationDialog.addConfirmListener(e -> {
				List<String> uuids = selectedRows.stream().map(CampaignFormDataIndexDto::getUuid)
						.collect(Collectors.toList());

				System.err.println(" verification kicke from frontend");

//			FacadeProvider.getCampaignFormDataFacade().verifyCampaignData(event.getCampaign().getUuid(), true);

				FacadeProvider.getCampaignFormDataFacade().verifyCampaignData(uuids, false);
//				 Notification.show("Camapaign Dayta Deleted ");
				reload();
				if (leaveBulkEdit.isVisible()) {
					leaveBulkEdit.setVisible(false);
					enterBulkEdit.setVisible(true);
					grid.setSelectionMode(Grid.SelectionMode.SINGLE);

					dropdownBulkOperations.setVisible(false);
					selectAllButtonpLACEHOLDER.setVisible(false);

				}
			});

		}
	}

	public void reload() {

		criteria.campaign(campaignz.getValue());
		criteria.setFormType(campaignPhase.getValue().toString());
		criteria.setCampaignFormMeta(campaignFormCombo.getValue());
		criteria.area(regionCombo.getValue());
		criteria.region(provinceCombo.getValue());
		criteria.district(districtCombo.getValue());
		criteria.community(clusterCombo.getValue());

		grid.getDataProvider().refreshAll();
	}

	@SuppressWarnings("deprecation")
	private void configureGrid(CampaignFormDataCriteria criteria) {
		System.out.println("Configure grid calllllleddddddddddddd");
		setMargin(false);
		grid = new Grid<>(CampaignFormDataIndexDto.class, false);
//		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setColumnReorderingAllowed(true);

		ComponentRenderer<Span, CampaignFormDataIndexDto> rCodeRender = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getRcode()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormDataIndexDto> pCodeRender = new ComponentRenderer<>(input -> {

			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getPcode()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormDataIndexDto> dCodeRender = new ComponentRenderer<>(input -> {

			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getDcode()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormDataIndexDto> cCodeRender = new ComponentRenderer<>(input -> {

			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getCcode()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormDataIndexDto> clusterNumberRender = new ComponentRenderer<>(input -> {

			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getClusternumber()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {

			grid.addColumn(CampaignFormDataIndexDto.CAMPAIGN).setHeader(I18nProperties.getCaption(Captions.Campaigns))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getCampaign());
			formNameColumn = grid.addColumn(CampaignFormDataIndexDto.FORM)
					.setHeader(I18nProperties.getCaption(Captions.campaignCampaignForm)).setSortable(true)
					.setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getForm());
			grid.addColumn(CampaignFormDataIndexDto.AREA).setHeader(I18nProperties.getCaption(Captions.area))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getArea());
			grid.addColumn(rCodeRender).setHeader(I18nProperties.getCaption(Captions.Area_externalId)).setSortable(true)
					.setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Area_externalId));
			grid.addColumn(CampaignFormDataIndexDto.REGION).setHeader(I18nProperties.getCaption(Captions.region))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getRegion());
			grid.addColumn(pCodeRender).setHeader(I18nProperties.getCaption(Captions.Region_externalID))
					.setSortable(true).setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Region_externalID));
			grid.addColumn(CampaignFormDataIndexDto.DISTRICT).setHeader(I18nProperties.getCaption(Captions.district))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getDistrict());
			grid.addColumn(dCodeRender).setHeader(I18nProperties.getCaption(Captions.District_externalID))
					.setSortable(true).setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.District_externalID));
			clusterNameColumn = grid.addColumn(CampaignFormDataIndexDto.COMMUNITY)
					.setHeader(I18nProperties.getCaption(Captions.community)).setSortable(true).setResizable(true)
					.setAutoWidth(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.community));
			clusterNumberColumn = grid.addColumn(clusterNumberRender).setKey("clusterNumber")
					.setHeader(I18nProperties.getCaption(Captions.clusterNumber)).setSortable(true).setResizable(true)
					.setAutoWidth(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.clusterNumber));
			ccodeColumn = grid.addColumn(cCodeRender)
					.setHeader(I18nProperties.getCaption(Captions.Community_externalID)).setSortable(true)
					.setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Community_externalID));
			grid.addColumn(CampaignFormDataIndexDto.FORM_TYPE).setHeader(I18nProperties.getCaption(Captions.formPhase))
					.setSortable(true).setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.formPhase));
			grid.addColumn(CampaignFormDataIndexDto.SOURCE).setHeader("Source")// I18nProperties.getCaption(Captions.formPhase))
					.setSortable(true).setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> "Source:" + e.getSource());
			grid.addColumn(CampaignFormDataIndexDto.CREATED_BY)
					.setHeader(I18nProperties.getCaption(Captions.Campaign_creatingUser)).setSortable(true)
					.setResizable(true).setAutoWidth(true).setTooltipGenerator(
							e -> I18nProperties.getCaption(Captions.Campaign_creatingUser) + e.getSource());

		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {

			grid.addColumn(CampaignFormDataIndexDto.CAMPAIGN).setHeader(I18nProperties.getCaption(Captions.Campaigns))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getCampaign());
			formNameColumn = grid.addColumn(CampaignFormDataIndexDto.FORM)
					.setHeader(I18nProperties.getCaption(Captions.campaignCampaignForm)).setSortable(true)
					.setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getForm());
			grid.addColumn(CampaignFormDataIndexDto.AREA).setHeader(I18nProperties.getCaption(Captions.area))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getArea());
			grid.addColumn(rCodeRender).setHeader(I18nProperties.getCaption(Captions.Area_externalId)).setSortable(true)
					.setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Area_externalId));
			grid.addColumn(CampaignFormDataIndexDto.REGION).setHeader(I18nProperties.getCaption(Captions.region))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getRegion());
			grid.addColumn(pCodeRender).setHeader(I18nProperties.getCaption(Captions.Region_externalID))
					.setSortable(true).setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Region_externalID));
			grid.addColumn(CampaignFormDataIndexDto.DISTRICT).setHeader(I18nProperties.getCaption(Captions.district))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getDistrict());
			grid.addColumn(dCodeRender).setHeader(I18nProperties.getCaption(Captions.District_externalID))
					.setSortable(true).setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.District_externalID));
			clusterNameColumn = grid.addColumn(CampaignFormDataIndexDto.COMMUNITY)
					.setHeader(I18nProperties.getCaption(Captions.community)).setSortable(true).setResizable(true)
					.setAutoWidth(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.community));
			clusterNumberColumn = grid.addColumn(clusterNumberRender).setKey("clusterNumber")
					.setHeader(I18nProperties.getCaption(Captions.clusterNumber)).setSortable(true).setResizable(true)
					.setAutoWidth(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.clusterNumber));
			ccodeColumn = grid.addColumn(cCodeRender)
					.setHeader(I18nProperties.getCaption(Captions.Community_externalID)).setSortable(true)
					.setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Community_externalID));
			grid.addColumn(CampaignFormDataIndexDto.FORM_TYPE).setHeader(I18nProperties.getCaption(Captions.formPhase))
					.setSortable(true).setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.formPhase));
			grid.addColumn(CampaignFormDataIndexDto.SOURCE).setHeader("Source")// I18nProperties.getCaption(Captions.formPhase))
					.setSortable(true).setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> "Source:" + e.getSource());
			grid.addColumn(CampaignFormDataIndexDto.CREATED_BY)
					.setHeader(I18nProperties.getCaption(Captions.Campaign_creatingUser)).setSortable(true)
					.setResizable(true).setAutoWidth(true).setTooltipGenerator(
							e -> I18nProperties.getCaption(Captions.Campaign_creatingUser) + e.getSource());

		} else {

			grid.addColumn(CampaignFormDataIndexDto.CAMPAIGN).setHeader(I18nProperties.getCaption(Captions.Campaigns))
//					createHeaderComponent(I18nProperties.getCaption(Captions.Campaigns),I18nProperties.getCaption(Captions.Campaigns)))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getCampaign())
					.setFooter(CampaignFormDataIndexDto.CAMPAIGN);

			formNameColumn = grid.addColumn(CampaignFormDataIndexDto.FORM)
					.setHeader(I18nProperties.getCaption(Captions.campaignCampaignForm))
//							createHeaderComponent(I18nProperties.getCaption(Captions.campaignCampaignForm),I18nProperties.getCaption(Captions.campaignCampaignForm)))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getForm())
					.setFooter(CampaignFormDataIndexDto.FORM);
			grid.addColumn(CampaignFormDataIndexDto.AREA).setHeader(I18nProperties.getCaption(Captions.area))
//					createHeaderComponent(I18nProperties.getCaption(Captions.area), I18nProperties.getCaption(Captions.area)))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getArea())
					.setFooter(CampaignFormDataIndexDto.AREA);
			grid.addColumn(CampaignFormDataIndexDto.RCODE)
					.setHeader(I18nProperties.getCaption(Captions.Area_externalId))
//							createHeaderComponent(I18nProperties.getCaption(Captions.Area_externalId), I18nProperties.getCaption(Captions.Area_externalId)))
					.setSortable(true).setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> e.getRcode().toString()).setFooter(CampaignFormDataIndexDto.RCODE);

			grid.addColumn(CampaignFormDataIndexDto.REGION).setHeader(I18nProperties.getCaption(Captions.region))
//					createHeaderComponent(I18nProperties.getCaption(Captions.region), I18nProperties.getCaption(Captions.region)))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getRegion())
					.setFooter(CampaignFormDataIndexDto.REGION);

			grid.addColumn(CampaignFormDataIndexDto.PCODE)
					.setHeader(I18nProperties.getCaption(Captions.Region_externalID))
//					createHeaderComponent(I18nProperties.getCaption(Captions.Region_externalID), I18nProperties.getCaption(Captions.Region_externalID)))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> {

						int pcode = e.getPcode();
						return "" + pcode;
					}).setFooter(CampaignFormDataIndexDto.PCODE);
			grid.addColumn(CampaignFormDataIndexDto.DISTRICT).setHeader(I18nProperties.getCaption(Captions.district))
//			createHeaderComponent(I18nProperties.getCaption(Captions.district),I18nProperties.getCaption(Captions.district)))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getDistrict())
					.setFooter(CampaignFormDataIndexDto.DISTRICT);
			grid.addColumn(CampaignFormDataIndexDto.DCODE)
					.setHeader(I18nProperties.getCaption(Captions.District_externalID))
//					.setHeaderText("Your Tooltip Text")
//					createHeaderComponent(I18nProperties.getCaption(Captions.District_externalID),I18nProperties.getCaption(Captions.District_externalID)))
					.setSortable(true).setResizable(true).setAutoWidth(true).setFooter(CampaignFormDataIndexDto.DCODE)
					.setTooltipGenerator(e -> {
						int dcode = e.getDcode();
						return "" + dcode;
//					e.getDcode().toString()
					});
			clusterNameColumn = grid.addColumn(CampaignFormDataIndexDto.COMMUNITY)
					.setHeader(I18nProperties.getCaption(Captions.community))
//					createHeaderComponent(I18nProperties.getCaption(Captions.community),I18nProperties.getCaption(Captions.community)))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getCommunity())
					.setFooter(I18nProperties.getCaption(Captions.community));

			clusterNumberColumn = grid.addColumn(CampaignFormDataIndexDto.COMMUNITYNUMBER)
					.setHeader(I18nProperties.getCaption(Captions.clusterNumber))
//							createHeaderComponent(I18nProperties.getCaption(Captions.clusterNumber),I18nProperties.getCaption(Captions.clusterNumber)))
					.setSortable(true).setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> e.getClusternumber().toString())
					.setFooter(CampaignFormDataIndexDto.COMMUNITYNUMBER);
			ccodeColumn = grid.addColumn(CampaignFormDataIndexDto.CCODE)
					.setHeader(I18nProperties.getCaption(Captions.Community_externalID)).setSortable(true)
					.setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getCcode().toString())
					.setFooter(CampaignFormDataIndexDto.CCODE);

			grid.addColumn(CampaignFormDataIndexDto.FORM_TYPE).setHeader(I18nProperties.getCaption(Captions.formPhase))
//					createHeaderComponent(I18nProperties.getCaption(Captions.formPhase),I18nProperties.getCaption(Captions.formPhase)))
					.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getFormType())
					.setFooter(CampaignFormDataIndexDto.FORM_TYPE);

			grid.addColumn(CampaignFormDataIndexDto.SOURCE).setHeader("Source")
//					createHeaderComponent("Source","Source"))// I18nProperties.getCaption(Captions.formPhase))
					.setSortable(true).setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> "Source:" + e.getSource()).setFooter(CampaignFormDataIndexDto.SOURCE);

			grid.addColumn(CampaignFormDataIndexDto.CREATED_BY)
					.setHeader(I18nProperties.getCaption(Captions.Campaign_creatingUser))
//					createHeaderComponent(I18nProperties.getCaption(Captions.Campaign_creatingUser),I18nProperties.getCaption(Captions.Campaign_creatingUser)))//I18nProperties.getCaption(Captions.Campaign_creatingUser))
					.setSortable(true).setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Campaign_creatingUser) + e.getSource())
					.setFooter(CampaignFormDataIndexDto.CREATED_BY);

			if (userProvider.getUser().getUsertype() == UserType.WHO_USER) {

				verifiedColumn = grid.addColumn(CampaignFormDataIndexDto::getVerifiedStringValue)
						.setHeader(I18nProperties.getCaption("Verified Status")).setSortable(true).setResizable(true)
						.setAutoWidth(true)
						.setTooltipGenerator(
								e -> I18nProperties.getCaption("Verified Status: ") + e.getVerifiedStringValue())
						.setFooter(CampaignFormDataIndexDto.ISVERIFIED);

				publishedColumn = grid.addColumn(CampaignFormDataIndexDto::getPublishedStringValue)
						.setHeader(I18nProperties.getCaption("Published Status")).setSortable(true).setResizable(true)
						.setAutoWidth(true)
						.setTooltipGenerator(
								e -> I18nProperties.getCaption("Published Status: ") + e.getPublishedStringValue())
						.setFooter(CampaignFormDataIndexDto.ISPUBLISHED);

				if (campaignPhase.getValue() != null
						&& campaignPhase.getValue().toString().equalsIgnoreCase("post-campaign")) {
					publishedColumn.setVisible(true);
					verifiedColumn.setVisible(true);
				} else {
					publishedColumn.setVisible(false);
					verifiedColumn.setVisible(false);
				}
			}
		}

		grid.setVisible(true);
		grid.setWidthFull();
		grid.setAllRowsVisible(false);

		dataProvider = DataProvider.fromFilteringCallbacks(this::fetchCampaignFormData, this::countCampaignFormData);
		grid.setDataProvider(dataProvider);

		if (userProvider.getUser().getUsertype() == UserType.EOC_USER) {
			boolean isPublished = FacadeProvider.getCampaignFacade().isPublished(campaignz.getValue().getUuid());

//			System.out.println("1111111111111111111" + campaignPhase.getValue() + campaignz.getValue());
			if (campaignPhase.getValue().toString().equalsIgnoreCase("POST-CAMPAIGN")) {
//		        newSelectionModel = new GridSelectionModel<CampaignFormDataIndexDto>(Grid.SelectionMode.NONE);
//				System.out.println("1111111111111111111aaaaaaaaaaaaaa");
				if (isPublished) {
					grid.setSelectionMode(Grid.SelectionMode.NONE);
//					System.out.println("1111111111111111111bbbbbbbbbbbbbbb");
				}

			} else {

//				System.out.println("1111111111111111111cccccccccccccccccccc");
				grid.setSelectionMode(Grid.SelectionMode.SINGLE);
				grid.asSingleSelect().addValueChangeListener(e -> {
					if (e.getValue() != null) {
						CampaignFormDataDto formData = FacadeProvider.getCampaignFormDataFacade()
								.getCampaignFormDataByUuid(e.getValue().getUuid());

						CampaignFormDataEditForm cam = new CampaignFormDataEditForm(formData.getCampaignFormMeta(),
								campaignz.getValue(), true, formData.getUuid(), grid, false);

					}

				});
			}
		} else {

			grid.setSelectionMode(Grid.SelectionMode.SINGLE);
			grid.asSingleSelect().addValueChangeListener(e -> {
				CampaignFormDataDto formData = FacadeProvider.getCampaignFormDataFacade()
						.getCampaignFormDataByUuid(e.getValue().getUuid());

				CampaignFormDataEditForm cam = new CampaignFormDataEditForm(formData.getCampaignFormMeta(),
						campaignz.getValue(), true, formData.getUuid(), grid, false);

//				cam.verifyAndPublishButton.setText(I18nProperties.getCaption("Verify & Publish"));
				System.out.println("2222222222222222222222" + formData.getUuid());

			});
		}

		exporter = GridExporter.createFor(grid);
//		exporter.addColumnHeaderCustomizer((header, column) -> {
//		    // Customize the appearance of the column headers here
//		    header.getStyle().set("color", "blue");
//		    header.setText(header.getText() + " Customized"); // Add custom text to the header
//		});
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle(I18nProperties.getCaption(Captions.campaignDataInformation));
		exportFileName = campaignz.getValue().toString() + "_"
				+ campaignFormCombo.getValue().toString().replaceAll("[^a-zA-Z0-9]+", " ") + "_"
				+ new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime());
		exporter.setFileName(exportFileName);

		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");
		anchor.setId("campDatAnchor");
		anchor.getStyle().set("width", "100px");

		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");
		anchor.getElement().insertChild(0, icon.getElement());

		add(grid);
	}

	private void configureColumnStyles(CampaignFormDataCriteria criteria) {
		Date formExpiryDate = FacadeProvider.getCampaignFormMetaFacade().formExpiryDate(criteria);
		// TODO Auto-generated();
		grid.setClassNameGenerator((v) -> {

			if (v.getCreatingUser() != null) {
				List<CampaignFormDataIndexDto> creatingUserType = FacadeProvider.getCampaignFormDataFacade()
						.getCreatingUsersUserType(v.getCreatingUser().toString());

				for (CampaignFormDataIndexDto xx : creatingUserType) {
					return "WHODATA";
				}
			}

			if (formExpiryDate != null) {
				if (v.getFormDate() != null && v.getFormDate().after((Date) formExpiryDate)) {

					return "lateData";
				} else {
					return "";
				}
			} else {
				return "";
			}

		});

		if (campaignFormCombo.getValue() != null) {

			CampaignFormMetaDto formData = FacadeProvider.getCampaignFormMetaFacade()
					.getCampaignFormMetaByUuid(campaignFormCombo.getValue().getUuid());

			boolean isDistictLevelData = formData.isDistrictentry();

			if (isDistictLevelData) {
				clusterNameColumn.setVisible(false);
				clusterNumberColumn.setVisible(false);
				ccodeColumn.setVisible(false);

			} else {
				clusterNameColumn.setVisible(true);
				clusterNumberColumn.setVisible(true);
				ccodeColumn.setVisible(true);
			}
		}

	}

	private String clusterNumberLabelGenerator(CommunityReferenceDto communityReferenceDto) {

		return (communityReferenceDto.getNumber() + " | " + communityReferenceDto.getCaption());

	}

	private void export(Grid<CampaignFormDataIndexDto> grid, TextArea result) {
		// Fetch all data from the grid in the current sorted order
		Stream<CampaignFormDataIndexDto> persons = null;
		Set<CampaignFormDataIndexDto> selection = grid.asMultiSelect().getValue();
		if (selection != null && selection.size() > 0) {
			persons = selection.stream();
		} else {
//			persons = dataView.getItems();
		}

		StringWriter output = new StringWriter();
		StatefulBeanToCsv<CampaignFormDataIndexDto> writer = new StatefulBeanToCsvBuilder<CampaignFormDataIndexDto>(
				output).build();
		try {
			writer.write(persons);
		} catch (Exception e) {
			output.write("An error occured during writing: " + e.getMessage());
		}

		result.setValue(output.toString());
	}

	private void setDataProvider() {
		DataProvider<CampaignFormDataIndexDto, CampaignFormDataCriteria> dataProvider = DataProvider
				.fromFilteringCallbacks(
						query -> FacadeProvider.getCampaignFormDataFacade()
								.getIndexList(criteria, query.getOffset(), query.getLimit(),
										query.getSortOrders().stream()
												.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
														sortOrder.getDirection() == SortDirection.ASCENDING))
												.collect(Collectors.toList()))
								.stream(),
						query -> (int) FacadeProvider.getCampaignFormDataFacade().count(criteria));
		grid.setDataProvider(dataProvider);
	}

	public void addCustomColumn(String property, String caption) {
		if (!property.toString().contains("readonly")) {
//			System.out.println(caption + "_--------------------UUUUUUUUUUUUUUUUUUUUUUUUUUUUu");
			grid.addColumn(
					e -> e.getFormValues().stream().filter(v -> v.getId().equals(property)).findFirst().orElse(null))
					.setHeader(caption)
//							createHeaderComponent(caption, caption))
					.setFooter(property).setSortProperty(property).setSortable(false).setResizable(true)
					.setAutoWidth(true)
					.setTooltipGenerator(e -> caption + " : " + e.getFormValues().stream()
							.filter(v -> v.getId().equals(property)).findFirst().orElse(null))
					.setClassNameGenerator(item -> "full-width-column");

		}

	}

	public void updateRowCount() {
		int numberOfRows = (int) FacadeProvider.getCampaignFormDataFacade().count(criteria);
		String newText = I18nProperties.getCaption(Captions.rows) + numberOfRows;

		countRowItems.setText(newText);
		countRowItems.setId("rowCount");
	}

	private void closeEditor() {
		campaignFormDataEditForm.setVisible(false);
		grid.setVisible(true);
		removeClassName("editing");
	}

	private void pokeFlow() {
		logger.debug("runingImport...");
	}

	private void startIntervalCallback() {
		UI.getCurrent().setPollInterval(5000);
		if (!callbackRunning) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
//					stopIntervalCallback();
				}
			}, 15000); // 10 minutes

			callbackRunning = true;
		}
	}

	private void stopIntervalCallback() {
		if (callbackRunning) {
			callbackRunning = false;
			if (timer != null) {
				timer.cancel();
				timer.purge();
			}
		}
	}

	private void stopPullers() {
		UI.getCurrent().setPollInterval(-1);
	}

//	public static void printAllThreads() {
//		Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();
//
//		for (Map.Entry<Thread, StackTraceElement[]> entry : threadMap.entrySet()) {
//			Thread thread = entry.getKey();
//			StackTraceElement[] stackTrace = entry.getValue();
//
//			System.out.println("Thread Name: " + thread.getName());
//			System.out.println("Thread ID: " + thread.getId());
//			System.out.println("Thread State: " + thread.getState());
//			System.out.println("Is Daemon: " + thread.isDaemon());
//			System.out.println("Stack Trace:");
//
//			for (StackTraceElement element : stackTrace) {
//				System.out.println("\tat " + element);
//			}
//
//			System.out.println("----------------------------");
//		}
//	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public @NotNull ViewModelProviders getViewModelProviders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserProvider getUserProvider() {
		// TODO Auto-generated method stub
		return null;
	}

}