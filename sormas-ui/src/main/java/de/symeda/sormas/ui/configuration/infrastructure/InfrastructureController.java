/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.ui.configuration.infrastructure;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.HasUuid;
import de.symeda.sormas.api.infrastructure.facility.FacilityDto;
import de.symeda.sormas.api.infrastructure.facility.FacilityIndexDto;
import de.symeda.sormas.api.infrastructure.facility.FacilityType;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.InfrastructureType;
import de.symeda.sormas.api.infrastructure.pointofentry.PointOfEntryDto;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.continent.ContinentDto;
import de.symeda.sormas.api.infrastructure.country.CountryDto;
import de.symeda.sormas.api.infrastructure.country.CountryIndexDto;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.infrastructure.subcontinent.SubcontinentDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.ui.SormasUI;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent.CommitListener;
import de.symeda.sormas.ui.utils.VaadinUiUtil;

public class InfrastructureController {

	public InfrastructureController() {

	}
	public void createArea() {
		CommitDiscardWrapperComponent<AreaEditForm> createComponent = getAreaEditComponent(null);
		VaadinUiUtil.showModalPopupWindow(createComponent, I18nProperties.getString(Strings.headingCreateEntry));
	}

	public void editArea(String uuid) {
		AreaDto area = FacadeProvider.getAreaFacade().getByUuid(uuid);
		CommitDiscardWrapperComponent<AreaEditForm> editComponent = getAreaEditComponent(area);
		String caption = I18nProperties.getString(Strings.edit) + " " + area.getName();
		VaadinUiUtil.showModalPopupWindow(editComponent, caption);
	}

	public void createRegion() {
		CommitDiscardWrapperComponent<RegionEditForm> createComponent = getRegionEditComponent(null);
		VaadinUiUtil.showModalPopupWindow(createComponent, I18nProperties.getString(Strings.headingCreateEntry));
	}

	public void editRegion(String uuid) {
		RegionDto region = FacadeProvider.getRegionFacade().getByUuid(uuid);
		CommitDiscardWrapperComponent<RegionEditForm> editComponent = getRegionEditComponent(region);
		String caption = I18nProperties.getString(Strings.edit) + " " + region.getName();
		VaadinUiUtil.showModalPopupWindow(editComponent, caption);
	}

	public void createDistrict() {
		CommitDiscardWrapperComponent<DistrictEditForm> createComponent = getDistrictEditComponent(null);
		VaadinUiUtil.showModalPopupWindow(createComponent, I18nProperties.getString(Strings.headingCreateEntry));
	}

	public void editDistrict(String uuid) {
		DistrictDto district = FacadeProvider.getDistrictFacade().getDistrictByUuid(uuid);
		CommitDiscardWrapperComponent<DistrictEditForm> editComponent = getDistrictEditComponent(district);
		String caption = I18nProperties.getString(Strings.edit) + " " + district.getName();
		VaadinUiUtil.showModalPopupWindow(editComponent, caption);
	}

	public void createCommunity() {
		CommitDiscardWrapperComponent<CommunityEditForm> createComponent = getCommunityEditComponent(null);
		VaadinUiUtil.showModalPopupWindow(createComponent, I18nProperties.getString(Strings.headingCreateEntry));
	}

	public void editCommunity(String uuid) {
		CommunityDto community = FacadeProvider.getCommunityFacade().getByUuid(uuid);
		CommitDiscardWrapperComponent<CommunityEditForm> editComponent = getCommunityEditComponent(community);
		String caption = I18nProperties.getString(Strings.edit) + " " + community.getName();
		VaadinUiUtil.showModalPopupWindow(editComponent, caption);
	}

	

	

	private CommitDiscardWrapperComponent<AreaEditForm> getAreaEditComponent(AreaDto area) {
		boolean isNew = area == null;
		AreaEditForm editForm = new AreaEditForm(isNew);
		if (isNew) {
			area = AreaDto.build();
		}

		editForm.setValue(area);

		final CommitDiscardWrapperComponent<AreaEditForm> editComponent = new CommitDiscardWrapperComponent<>(
			editForm,
			UserProvider.getCurrent().hasUserRight(isNew ? UserRight.INFRASTRUCTURE_CREATE : UserRight.INFRASTRUCTURE_EDIT),
			editForm.getFieldGroup());

		editComponent.addCommitListener(() -> {
			FacadeProvider.getAreaFacade().save(editForm.getValue());
			Notification.show(I18nProperties.getString(Strings.messageEntryCreated), Type.ASSISTIVE_NOTIFICATION);
			SormasUI.get().getNavigator().navigateTo(AreasView.VIEW_NAME);
		});

		if (!isNew) {
			extendEditComponentWithArchiveButton(editComponent, area.isArchived(), area.getUuid(), InfrastructureType.AREA, null);
		}

		return editComponent;
	}

	
	private CommitDiscardWrapperComponent<RegionEditForm> getRegionEditComponent(RegionDto region) {

		boolean isNew = region == null;
		RegionEditForm editForm = new RegionEditForm(isNew);
		if (isNew) {
			region = RegionDto.build();
		}

		editForm.setValue(region);

		final CommitDiscardWrapperComponent<RegionEditForm> editView = new CommitDiscardWrapperComponent<RegionEditForm>(
			editForm,
			UserProvider.getCurrent().hasUserRight(isNew ? UserRight.INFRASTRUCTURE_CREATE : UserRight.INFRASTRUCTURE_EDIT),
			editForm.getFieldGroup());

		editView.addCommitListener(() -> {
			FacadeProvider.getRegionFacade().save(editForm.getValue());
			Notification.show(I18nProperties.getString(Strings.messageEntryCreated), Type.ASSISTIVE_NOTIFICATION);
			SormasUI.get().getNavigator().navigateTo(RegionsView.VIEW_NAME);
		});

		if (!isNew) {
			extendEditComponentWithArchiveButton(editView, region.isArchived(), region.getUuid(), InfrastructureType.REGION, null);
		}

		return editView;
	}

	private CommitDiscardWrapperComponent<DistrictEditForm> getDistrictEditComponent(DistrictDto district) {

		boolean isNew = district == null;
		DistrictEditForm editForm = new DistrictEditForm(isNew);
		if (isNew) {
			district = DistrictDto.build();
		}

		editForm.setValue(district);

		final CommitDiscardWrapperComponent<DistrictEditForm> editView = new CommitDiscardWrapperComponent<DistrictEditForm>(
			editForm,
			UserProvider.getCurrent().hasUserRight(isNew ? UserRight.INFRASTRUCTURE_CREATE : UserRight.INFRASTRUCTURE_EDIT),
			editForm.getFieldGroup());

		editView.addCommitListener(new CommitListener() {

			@Override
			public void onCommit() {
				FacadeProvider.getDistrictFacade().save(editForm.getValue());
				Notification.show(I18nProperties.getString(Strings.messageEntryCreated), Type.ASSISTIVE_NOTIFICATION);
				SormasUI.get().getNavigator().navigateTo(DistrictsView.VIEW_NAME);
			}
		});

		if (!isNew) {
			extendEditComponentWithArchiveButton(editView, district.isArchived(), district.getUuid(), InfrastructureType.DISTRICT, null);
		}

		return editView;
	}

	private CommitDiscardWrapperComponent<CommunityEditForm> getCommunityEditComponent(CommunityDto community) {

		boolean isNew = community == null;
		CommunityEditForm editForm = new CommunityEditForm(isNew);
		if (isNew) {
			community = CommunityDto.build();
		}

		editForm.setValue(community);

		final CommitDiscardWrapperComponent<CommunityEditForm> editView = new CommitDiscardWrapperComponent<CommunityEditForm>(
			editForm,
			UserProvider.getCurrent().hasUserRight(isNew ? UserRight.INFRASTRUCTURE_CREATE : UserRight.INFRASTRUCTURE_EDIT),
			editForm.getFieldGroup());

		editView.addCommitListener(new CommitListener() {

			@Override
			public void onCommit() {
				FacadeProvider.getCommunityFacade().save(editForm.getValue());
				Notification.show(I18nProperties.getString(Strings.messageEntryCreated), Type.ASSISTIVE_NOTIFICATION);
				SormasUI.get().getNavigator().navigateTo(CommunitiesView.VIEW_NAME);
			}
		});

		if (!isNew) {
			extendEditComponentWithArchiveButton(editView, community.isArchived(), community.getUuid(), InfrastructureType.COMMUNITY, null);
		}

		return editView;
	}


	private void extendEditComponentWithArchiveButton(
		CommitDiscardWrapperComponent<?> component,
		boolean isArchived,
		String uuid,
		InfrastructureType infrastructureType,
		FacilityType facilityType) {

		if (UserProvider.getCurrent().hasUserRight(UserRight.INFRASTRUCTURE_ARCHIVE)) {
			Button archiveButton = ButtonHelper.createButton(isArchived ? Captions.actionDearchive : Captions.actionArchive, e -> {
				if (!isArchived) {
					if (InfrastructureType.AREA.equals(infrastructureType)
							&& FacadeProvider.getAreaFacade().isUsedInOtherInfrastructureData(Arrays.asList(uuid))
						|| InfrastructureType.REGION.equals(infrastructureType)
							&& FacadeProvider.getRegionFacade().isUsedInOtherInfrastructureData(Arrays.asList(uuid))
						|| InfrastructureType.DISTRICT.equals(infrastructureType)
							&& FacadeProvider.getDistrictFacade().isUsedInOtherInfrastructureData(Arrays.asList(uuid))
						|| InfrastructureType.COMMUNITY.equals(infrastructureType)
							&& FacadeProvider.getCommunityFacade().isUsedInOtherInfrastructureData(Arrays.asList(uuid))) {
						showArchivingNotPossibleWindow(infrastructureType, false);
						return;
					}
				} else {
					if ( InfrastructureType.DISTRICT.equals(infrastructureType)
							&& FacadeProvider.getDistrictFacade().hasArchivedParentInfrastructure(Arrays.asList(uuid))
						|| InfrastructureType.COMMUNITY.equals(infrastructureType)
							&& FacadeProvider.getCommunityFacade().hasArchivedParentInfrastructure(Arrays.asList(uuid)))
					{
						showDearchivingNotPossibleWindow(infrastructureType, false);
						return;
					}
				}

				component.commit();
				archiveOrDearchiveInfrastructure(!isArchived, uuid, infrastructureType);
			}, ValoTheme.BUTTON_LINK);

			component.getButtonsPanel().addComponentAsFirst(archiveButton);
			component.getButtonsPanel().setComponentAlignment(archiveButton, Alignment.BOTTOM_LEFT);
		}
	}

	private void showArchivingNotPossibleWindow(InfrastructureType infrastructureType, boolean bulkArchiving) {

		final String contentText;
		switch (infrastructureType) {
		
		case AREA:
			contentText =
				I18nProperties.getString(bulkArchiving ? Strings.messageAreasArchivingNotPossible : Strings.messageAreaArchivingNotPossible);
			break;
		case REGION:
			contentText =
				I18nProperties.getString(bulkArchiving ? Strings.messageRegionsArchivingNotPossible : Strings.messageRegionArchivingNotPossible);
			break;
		case DISTRICT:
			contentText =
				I18nProperties.getString(bulkArchiving ? Strings.messageDistrictsArchivingNotPossible : Strings.messageDistrictArchivingNotPossible);
			break;
		case COMMUNITY:
			contentText = I18nProperties
				.getString(bulkArchiving ? Strings.messageCommunitiesArchivingNotPossible : Strings.messageCommunityArchivingNotPossible);
			break;
		default:
			throw new IllegalArgumentException(infrastructureType.name());
		}
		VaadinUiUtil.showSimplePopupWindow(I18nProperties.getString(Strings.headingArchivingNotPossible), contentText);
	}

	private void showDearchivingNotPossibleWindow(InfrastructureType infrastructureType, boolean bulkArchiving) {

		final String contentText;
		switch (infrastructureType) {
		
		case DISTRICT:
			contentText = I18nProperties
				.getString(bulkArchiving ? Strings.messageDistrictsDearchivingNotPossible : Strings.messageDistrictDearchivingNotPossible);
			break;
		case COMMUNITY:
			contentText = I18nProperties
				.getString(bulkArchiving ? Strings.messageCommunitiesDearchivingNotPossible : Strings.messageCommunityDearchivingNotPossible);
			break;
		default:
			throw new IllegalArgumentException(infrastructureType.name());
		}
		VaadinUiUtil.showSimplePopupWindow(I18nProperties.getString(Strings.headingDearchivingNotPossible), contentText);
	}

	private void archiveOrDearchiveInfrastructure(boolean archive, String entityUuid, InfrastructureType infrastructureType) {

		Label contentLabel = new Label();
		final String notificationMessage;
		switch (infrastructureType) {
		
		case AREA:
			contentLabel.setValue(I18nProperties.getString(archive ? Strings.confirmationArchiveArea : Strings.confirmationDearchiveArea));
			notificationMessage = I18nProperties.getString(archive ? Strings.messageAreaArchived : Strings.messageAreaDearchived);
			break;
		case REGION:
			contentLabel.setValue(I18nProperties.getString(archive ? Strings.confirmationArchiveRegion : Strings.confirmationDearchiveRegion));
			notificationMessage = I18nProperties.getString(archive ? Strings.messageRegionArchived : Strings.messageRegionDearchived);
			break;
		case DISTRICT:
			contentLabel.setValue(I18nProperties.getString(archive ? Strings.confirmationArchiveDistrict : Strings.confirmationDearchiveDistrict));
			notificationMessage = I18nProperties.getString(archive ? Strings.messageDistrictArchived : Strings.messageDistrictDearchived);
			break;
		case COMMUNITY:
			contentLabel.setValue(I18nProperties.getString(archive ? Strings.confirmationArchiveCommunity : Strings.confirmationDearchiveCommunity));
			notificationMessage = I18nProperties.getString(archive ? Strings.messageCommunityArchived : Strings.messageCommunityDearchived);
			break;
		default:
			throw new IllegalArgumentException(infrastructureType.name());
		}

		VaadinUiUtil.showConfirmationPopup(
			I18nProperties.getString(archive ? Strings.headingConfirmArchiving : Strings.headingConfirmDearchiving),
			contentLabel,
			I18nProperties.getString(Strings.yes),
			I18nProperties.getString(Strings.no),
			640,
			e -> {
				if (e.booleanValue()) {
					switch (infrastructureType) {
					
					case AREA:
						if (archive) {
							FacadeProvider.getAreaFacade().archive(entityUuid);
						} else {
							FacadeProvider.getAreaFacade().dearchive(entityUuid);
						}
						SormasUI.get().getNavigator().navigateTo(AreasView.VIEW_NAME);
						break;
					
					case REGION:
						if (archive) {
							FacadeProvider.getRegionFacade().archive(entityUuid);
						} else {
							FacadeProvider.getRegionFacade().dearchive(entityUuid);
						}
						SormasUI.get().getNavigator().navigateTo(RegionsView.VIEW_NAME);
						break;
					case DISTRICT:
						if (archive) {
							FacadeProvider.getDistrictFacade().archive(entityUuid);
						} else {
							FacadeProvider.getDistrictFacade().dearchive(entityUuid);
						}
						SormasUI.get().getNavigator().navigateTo(DistrictsView.VIEW_NAME);
						break;
					case COMMUNITY:
						if (archive) {
							FacadeProvider.getCommunityFacade().archive(entityUuid);
						} else {
							FacadeProvider.getCommunityFacade().dearchive(entityUuid);
						}
						SormasUI.get().getNavigator().navigateTo(CommunitiesView.VIEW_NAME);
						break;
					
					default:
						throw new IllegalArgumentException(infrastructureType.name());
					}

					Notification.show(notificationMessage, Type.ASSISTIVE_NOTIFICATION);
				}
			});
	}

	@SuppressWarnings("unchecked")
	public void archiveOrDearchiveAllSelectedItems(
		boolean archive,
		Collection<?> selectedRows,
		InfrastructureType infrastructureType,
		Runnable callback) {

		// Check that at least one entry is selected
		if (selectedRows.isEmpty()) {
			new Notification(
				I18nProperties.getString(Strings.headingNoRowsSelected),
				I18nProperties.getString(Strings.messageNoRowsSelected),
				Type.WARNING_MESSAGE,
				false).show(Page.getCurrent());
			return;
		}

		// Check if archiving/dearchiving is allowed concerning the hierarchy
		Set<String> selectedRowsUuids = selectedRows.stream().map(row -> ((HasUuid) row).getUuid()).collect(Collectors.toSet());
		if (InfrastructureType.AREA.equals(infrastructureType) && FacadeProvider.getAreaFacade().isUsedInOtherInfrastructureData(selectedRowsUuids)

			|| InfrastructureType.REGION.equals(infrastructureType)
				&& FacadeProvider.getRegionFacade().isUsedInOtherInfrastructureData(selectedRowsUuids)
			|| InfrastructureType.DISTRICT.equals(infrastructureType)
				&& FacadeProvider.getDistrictFacade().isUsedInOtherInfrastructureData(selectedRowsUuids)
			|| InfrastructureType.COMMUNITY.equals(infrastructureType)
				&& FacadeProvider.getCommunityFacade().isUsedInOtherInfrastructureData(selectedRowsUuids)) {
			showArchivingNotPossibleWindow(infrastructureType, true);
			return;
		}
		if (
			InfrastructureType.DISTRICT.equals(infrastructureType)
				&& FacadeProvider.getDistrictFacade().hasArchivedParentInfrastructure(selectedRowsUuids)
			|| InfrastructureType.COMMUNITY.equals(infrastructureType)
				&& FacadeProvider.getCommunityFacade().hasArchivedParentInfrastructure(selectedRowsUuids)
			) {
			showDearchivingNotPossibleWindow(infrastructureType, false);
			return;
		}

		final String confirmationMessage;
		final String notificationMessage;
		switch (infrastructureType) {
		
		case AREA:
			confirmationMessage =
				archive ? I18nProperties.getString(Strings.confirmationArchiveAreas) : I18nProperties.getString(Strings.confirmationDearchiveAreas);
			notificationMessage =
				archive ? I18nProperties.getString(Strings.messageAreasArchived) : I18nProperties.getString(Strings.messageAreasDearchived);
			break;
		case REGION:
			confirmationMessage = archive
				? I18nProperties.getString(Strings.confirmationArchiveRegions)
				: I18nProperties.getString(Strings.confirmationDearchiveRegions);
			notificationMessage =
				archive ? I18nProperties.getString(Strings.messageRegionsArchived) : I18nProperties.getString(Strings.messageRegionsDearchived);
			break;
		case DISTRICT:
			confirmationMessage = archive
				? I18nProperties.getString(Strings.confirmationArchiveDistricts)
				: I18nProperties.getString(Strings.confirmationDearchiveDistricts);
			notificationMessage =
				archive ? I18nProperties.getString(Strings.messageDistrictsArchived) : I18nProperties.getString(Strings.messageDistrictsDearchived);
			break;
		case COMMUNITY:
			confirmationMessage = archive
				? I18nProperties.getString(Strings.confirmationArchiveCommunities)
				: I18nProperties.getString(Strings.confirmationDearchiveCommunities);
			notificationMessage = archive
				? I18nProperties.getString(Strings.messageCommunitiesArchived)
				: I18nProperties.getString(Strings.messageCommunitiesDearchived);
			break;
		
		default:
			throw new IllegalArgumentException(infrastructureType.name());
		}

		VaadinUiUtil.showConfirmationPopup(
			I18nProperties.getString(Strings.headingConfirmArchiving),
			new Label(String.format(confirmationMessage, selectedRows.size())),
			I18nProperties.getString(Strings.yes),
			I18nProperties.getString(Strings.no),
			null,
			e -> {
				if (e.booleanValue()) {

					switch (infrastructureType) {
					
					case AREA:
						for (AreaDto selectedRow : (Collection<AreaDto>) selectedRows) {
							if (archive) {
								FacadeProvider.getAreaFacade().archive(selectedRow.getUuid());
							} else {
								FacadeProvider.getAreaFacade().dearchive(selectedRow.getUuid());
							}
						}
						break;
					
					case REGION:
						for (RegionIndexDto selectedRow : (Collection<RegionIndexDto>) selectedRows) {
							if (archive) {
								FacadeProvider.getRegionFacade().archive(selectedRow.getUuid());
							} else {
								FacadeProvider.getRegionFacade().dearchive(selectedRow.getUuid());
							}
						}
						break;
					case DISTRICT:
						for (DistrictIndexDto selectedRow : (Collection<DistrictIndexDto>) selectedRows) {
							if (archive) {
								FacadeProvider.getDistrictFacade().archive(selectedRow.getUuid());
							} else {
								FacadeProvider.getDistrictFacade().dearchive(selectedRow.getUuid());
							}
						}
						break;
					case COMMUNITY:
						for (CommunityDto selectedRow : (Collection<CommunityDto>) selectedRows) {
							if (archive) {
								FacadeProvider.getCommunityFacade().archive(selectedRow.getUuid());
							} else {
								FacadeProvider.getCommunityFacade().dearchive(selectedRow.getUuid());
							}
						}
						break;
					
					default:
						throw new IllegalArgumentException(infrastructureType.name());
					}

					callback.run();
					Notification.show(notificationMessage, Type.ASSISTIVE_NOTIFICATION);
				}
			});
	}
}
