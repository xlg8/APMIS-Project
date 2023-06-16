//package com.cinoteck.application.views.configurations;
//
//import java.util.stream.Collectors;
//
//import com.cinoteck.application.UserProvider;
//import com.cinoteck.application.views.utils.FilteredGrid;
//import com.vaadin.flow.component.ComponentEventListener;
//
//import de.symeda.sormas.api.FacadeProvider;
//import de.symeda.sormas.api.feature.FeatureType;
//import de.symeda.sormas.api.i18n.I18nProperties;
//import de.symeda.sormas.api.infrastructure.area.AreaDto;
//import de.symeda.sormas.api.infrastructure.district.DistrictCriteria;
//import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;
//import de.symeda.sormas.api.user.UserRight;
//import de.symeda.sormas.api.utils.SortProperty;
//
//public class DistrictsGrid extends FilteredGrid<DistrictIndexDto, DistrictCriteria> {
//
//	private static final long serialVersionUID = -4437531618828715458L;
//
//	public DistrictsGrid(DistrictCriteria criteria) {
//
//		super(DistrictIndexDto.class);
//		setSizeFull();
//
////		ViewConfiguration viewConfiguration = ViewModelProviders.of(DistrictsView.class).get(ViewConfiguration.class);
////		setInEagerMode(viewConfiguration.isInEagerMode());
//
//		if (isInEagerMode() && UserProvider.getCurrent().hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
//			setCriteria(criteria);
//			setEagerDataProvider();
//		} else {
//			setLazyDataProvider();
//			setCriteria(criteria);
//		}
//
//		setColumns(
//				DistrictIndexDto.AREA_NAME,
//				DistrictIndexDto.AREA_EXTERNAL_ID,
//			DistrictIndexDto.REGION,
//			DistrictIndexDto.REGION_EXTERNALID,
//			DistrictIndexDto.NAME,
//			//DistrictIndexDto.EPID_CODE,
//			DistrictIndexDto.EXTERNAL_ID,
////			DistrictIndexDto.POPULATION,
//			DistrictIndexDto.RISK);
//
////		getColumn(DistrictIndexDto.POPULATION).setSortable(false);
//
//		if (FacadeProvider.getFeatureConfigurationFacade().isFeatureEnabled(FeatureType.EDIT_INFRASTRUCTURE_DATA)
//			&& UserProvider.getCurrent().hasUserRight(UserRight.INFRASTRUCTURE_EDIT)) {
//			addItemClickListener(new ComponentEventListener<>(DistrictIndexDto.NAME, e -> ControllerProvider.getInfrastructureController().editDistrict(e.getUuid())));
//			addItemClickListener(new ShowDetailsListener<>(DistrictIndexDto.REGION, e -> ControllerProvider.getInfrastructureController().editDistrict(e.getUuid())));
//			addItemClickListener(new ShowDetailsListener<>(DistrictIndexDto.EXTERNAL_ID, e -> ControllerProvider.getInfrastructureController().editDistrict(e.getUuid())));
////			addItemClickListener(new ShowDetailsListener<>(DistrictIndexDto.POPULATION, e -> ControllerProvider.getInfrastructureController().editDistrict(e.getUuid())));
//			addItemClickListener(new ShowDetailsListener<>(DistrictIndexDto.RISK, e -> ControllerProvider.getInfrastructureController().editDistrict(e.getUuid())));
//			
//		//	addEditColumn(e -> ControllerProvider.getInfrastructureController().editDistrict(e.getUuid()));
//		}
//
//		for (Column<?, ?> column : getColumns()) {
//			column.setDescriptionGenerator(DistrictIndexDto -> column.getCaption());
//			column.setCaption(I18nProperties.getPrefixCaption(DistrictIndexDto.I18N_PREFIX, column.getId(), column.getCaption()));
//			if(column.getCaption().equalsIgnoreCase("Name")) {
//				column.setCaption("District");
//			}
//			if(column.getCaption().equalsIgnoreCase("External ID")) {
//				column.setCaption("DCode");
//			}
//			if(column.getCaption().equalsIgnoreCase("Areaname")) { 
//				column.setCaption("Region");
//			}
//			if(column.getCaption().equalsIgnoreCase("Areaexternal Id")) { 
//				column.setCaption("RCode");
//			}
//			if(column.getCaption().equalsIgnoreCase("Regionexternal Id")) { 
//				column.setCaption("PCode");
//			}
//		}
//	}
//
//	public void reload() {
//		getDataProvider().refreshAll();
//	}
//
//	public void setLazyDataProvider() {
//
//		DataProvider<DistrictIndexDto, DistrictCriteria> dataProvider = DataProvider.fromFilteringCallbacks(
//			query -> FacadeProvider.getDistrictFacade()
//				.getIndexList(
//					query.getFilter().orElse(null),
//					query.getOffset(),
//					query.getLimit(),
//					query.getSortOrders()
//						.stream()
//						.map(sortOrder -> new SortProperty(sortOrder.getSorted(), sortOrder.getDirection() == SortDirection.ASCENDING))
//						.collect(Collectors.toList()))
//				.stream(),
//			query -> {
//				return (int) FacadeProvider.getDistrictFacade().count(query.getFilter().orElse(null));
//			});
//		setDataProvider(dataProvider);
//		setSelectionMode(SelectionMode.NONE);
//	}
//
//	public void setEagerDataProvider() {
//
//		ListDataProvider<DistrictIndexDto> dataProvider =
//			DataProvider.fromStream(FacadeProvider.getDistrictFacade().getIndexList(getCriteria(), null, null, null).stream());
//		setDataProvider(dataProvider);
//		setSelectionMode(SelectionMode.MULTI);
//	}
//}
