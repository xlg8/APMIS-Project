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
package com.cinoteck.application.views.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import com.cinoteck.application.views.about.AboutView;
import com.opencsv.CSVWriter;
import com.vaadin.data.Container.Indexed;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.server.ClassResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseListener;

import de.symeda.sormas.api.AgeGroup;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.CaseExportType;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.importexport.ExportConfigurationDto;
import de.symeda.sormas.api.importexport.ExportTarget;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.utils.CSVUtils;
import de.symeda.sormas.api.utils.CsvStreamUtils;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.api.visit.VisitExportType;

public final class DownloadUtil {

	private DownloadUtil() {
		// Hide Utility Class Constructor
	}

	public static final int DETAILED_EXPORT_STEP_SIZE = 50;

	public static StreamResource createGridExportStreamResource(
		Indexed container,
		List<Column> columns,
		ExportEntityName entityName,
		String... ignoredPropertyIds) {

		String fileName = createFileNameWithCurrentDate(entityName, ".csv");
		return new V7GridExportStreamResource(container, columns, fileName, ignoredPropertyIds);
	}

	public static StreamResource createFileStreamResource(String filePath, String fileName, String mimeType, String errorTitle, String errorText) {

		StreamResource streamResource = new StreamResource(() -> {
			try {
				return new BufferedInputStream(Files.newInputStream(new File(filePath).toPath()));
			} catch (IOException e) {
				// TODO This currently requires the user to click the "Export" button again or reload the page as the UI
				// is not automatically updated; this should be changed once Vaadin push is enabled (see #516)
				new Notification(errorTitle, errorText, Type.ERROR_MESSAGE, false).show(Page.getCurrent());
				return null;
			}
		}, fileName);
		streamResource.setMIMEType(mimeType);
		streamResource.setCacheTime(0);
		return streamResource;
	}

	public static StreamResource createStringStreamResource(String content, String fileName, String mimeType) {
		return createByteArrayStreamResource(content.getBytes(StandardCharsets.UTF_8), fileName, mimeType);
	}

	public static StreamResource createByteArrayStreamResource(byte[] content, String fileName, String mimeType) {
		StreamResource streamResource = new StreamResource(() -> new ByteArrayInputStream(content), fileName);
		streamResource.setMIMEType(mimeType);
		return streamResource;
	}

	@SuppressWarnings("serial")
	public static StreamResource createPopulationDataExportResource(String campaignUuid) {

		String exportFileName = createFileNameWithCurrentDate(ExportEntityName.POPULATION_DATA, ".csv");
		StreamResource populationDataStreamResource = new StreamResource(new StreamSource() {

			@Override
			public InputStream getStream() {
				try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
					try (CSVWriter writer = CSVUtils.createCSVWriter(
						new OutputStreamWriter(byteStream, StandardCharsets.UTF_8.name()),
						FacadeProvider.getConfigFacade().getCsvSeparator())) {
						// Generate and write columns to CSV writer
						List<String> columnNames = new ArrayList<>();
						columnNames.add(I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX, PopulationDataDto.REGION));
						columnNames.add(I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX, PopulationDataDto.DISTRICT));
						columnNames.add(I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX, PopulationDataDto.COMMUNITY));
						//add campaign uuid to the last row
						columnNames.add(I18nProperties.getCaption(Captions.Campaign));
						columnNames.add(I18nProperties.getString(Strings.total));
						columnNames.add(I18nProperties.getCaption(Captions.populationDataMaleTotal));
						columnNames.add(I18nProperties.getCaption(Captions.populationDataFemaleTotal));
						

						Map<AgeGroup, Integer> ageGroupPositions = new HashMap<>();
						int ageGroupIndex = 6;
						for (AgeGroup ageGroup : AgeGroup.values()) {
							columnNames.add(DataHelper.getSexAndAgeGroupString(ageGroup, null));
							columnNames.add(DataHelper.getSexAndAgeGroupString(ageGroup, Sex.MALE));
							columnNames.add(DataHelper.getSexAndAgeGroupString(ageGroup, Sex.FEMALE));
							columnNames.add(DataHelper.getSexAndAgeGroupString(ageGroup, Sex.OTHER));
							ageGroupPositions.put(ageGroup, ageGroupIndex);
							ageGroupIndex += 4;
						}
						

						writer.writeNext(columnNames.toArray(new String[columnNames.size()]));

						List<Object[]> populationExportDataList = FacadeProvider.getPopulationDataFacade().getPopulationDataForExport(campaignUuid);

						String[] exportLine = new String[columnNames.size()];
						String regionName = "";
						String districtName = "";
						String communityName = "";
						String campaignName = "";
						for (Object[] populationExportData : populationExportDataList) {
							String dataRegionName = (String) populationExportData[0];
							String dataDistrictName = populationExportData[1] == null ? "" : (String) populationExportData[1];
							String dataCommunityName = populationExportData[2] == null ? "" : (String) populationExportData[2];
							String dataCampaignName = populationExportData[3] == null ? "" : (String) populationExportData[3];
							if (exportLine[0] != null
								&& (!dataRegionName.equals(regionName)
									|| !dataDistrictName.equals(districtName)
									|| !dataCampaignName.equals(campaignName)
									|| !dataCommunityName.equals(communityName))) {
								// New region or district reached; write line to CSV
								writer.writeNext(exportLine);
								exportLine = new String[columnNames.size()];
							}
							regionName = dataRegionName;
							districtName = dataDistrictName;
							communityName = dataCommunityName;
							campaignName = dataCampaignName;

							// Region
							if (exportLine[0] == null) {
								exportLine[0] = (String) populationExportData[0];
							}
							// District
							if (exportLine[1] == null) {
								exportLine[1] = (String) populationExportData[1];
							}
							// Community
							if (exportLine[2] == null) {
								exportLine[2] = (String) populationExportData[2];
							}
							
							//campaign
							if (exportLine[3] == null) {
								exportLine[3] = (String) populationExportData[3];
							}

							if (populationExportData[4] == null) {
								// Total population
								String sexString = (String) populationExportData[5];
								if (Sex.MALE.getName().equals(sexString)) {
									exportLine[5] = String.valueOf((int) populationExportData[6]);
								} else if (Sex.FEMALE.getName().equals(sexString)) {
									exportLine[6] = String.valueOf((int) populationExportData[6]);
								} else if (Sex.OTHER.getName().equals(sexString)) {
									exportLine[7] = String.valueOf((int) populationExportData[6]);
								} else {
									exportLine[4] = String.valueOf((int) populationExportData[6]);
								}
							} else {
								// Population based on age group position and sex
								Integer ageGroupPosition = ageGroupPositions.get(AgeGroup.valueOf((String) populationExportData[4]));
								String sexString = (String) populationExportData[5];
								if (Sex.MALE.getName().equals(sexString)) {
									ageGroupPosition += 1;
								} else if (Sex.FEMALE.getName().equals(sexString)) {
									ageGroupPosition += 2;
								} else if (Sex.OTHER.getName().equals(sexString)) {
									ageGroupPosition += 3;
								}
								exportLine[ageGroupPosition] = String.valueOf((int) populationExportData[6]);
							}
							
							columnNames.add(I18nProperties.getCaption(Captions.Campaign));
						}

						// Write last line to CSV
						writer.writeNext(exportLine);
						writer.flush();
					}
					return new ByteArrayInputStream(byteStream.toByteArray());
				} catch (IOException e) {
					// TODO This currently requires the user to click the "Export" button again or reload the page as the UI
					// is not automatically updated; this should be changed once Vaadin push is enabled (see #516)
					new Notification(
						I18nProperties.getString(Strings.headingExportFailed),
						I18nProperties.getString(Strings.messageExportFailed),
						Type.ERROR_MESSAGE,
						false).show(Page.getCurrent());
					return null;
				}
			}
		}, exportFileName);
		populationDataStreamResource.setMIMEType("text/csv");
		populationDataStreamResource.setCacheTime(0);
		return populationDataStreamResource;
	}

	public static interface OutputStreamConsumer {

		void writeTo(OutputStream os) throws IOException;
	}

	/**
	 * The buffer can be used for an input stream without having to copy it
	 */
	private static class SharedByteArrayOutputStream extends ByteArrayOutputStream {

		public SharedByteArrayOutputStream() {
			super(2048);
		}

		public ByteArrayInputStream toInputStream() {
			return new ByteArrayInputStream(buf, 0, count);
		}
	}

	public static class DelayedInputStream extends FilterInputStream {

		private Supplier<InputStream> lazyInputStreamSupplier;

		protected DelayedInputStream(Supplier<InputStream> lazyInputStreamSupplier) {
			super(null);
			this.lazyInputStreamSupplier = lazyInputStreamSupplier;
		}

		public DelayedInputStream(OutputStreamConsumer osConsumer, Consumer<IOException> exceptionHandler) {
			this(() -> {
				try (SharedByteArrayOutputStream os = new SharedByteArrayOutputStream()) {
					osConsumer.writeTo(os);
					return os.toInputStream();
				} catch (IOException e) {
					exceptionHandler.accept(e);
					throw new UncheckedIOException(e);
				}
			});
		}

		private void ensureInited() {
			if (lazyInputStreamSupplier != null) {
				in = lazyInputStreamSupplier.get();
				lazyInputStreamSupplier = null;
			}
		}

		@Override
		public int read() throws IOException {
			ensureInited();
			return super.read();
		}

		@Override
		public int read(byte[] b) throws IOException {
			ensureInited();
			return super.read(b);
		}

		@Override
		public synchronized int read(byte[] b, int off, int len) throws IOException {
			ensureInited();
			return super.read(b, off, len);
		}

		@Override
		public synchronized long skip(long n) throws IOException {
			ensureInited();
			return super.skip(n);
		}

		@Override
		public synchronized int available() throws IOException {
			ensureInited();
			return super.available();
		}

		@Override
		public synchronized void mark(int readAheadLimit) {
			ensureInited();
			super.mark(readAheadLimit);
		}
	}


	public static <T> StreamResource createCsvExportStreamResource(
		Class<T> exportRowClass,
		Enum<?> exportType,
		CsvStreamUtils.SupplierBiFunction<Integer, Integer, List<T>> exportRowsSupplier,
		CsvStreamUtils.SupplierBiFunction<String, Class<?>, String> propertyIdCaptionFunction,
		ExportEntityName entityName,
		ExportConfigurationDto exportConfiguration) {

		String exportFileName = createFileNameWithCurrentDate(entityName, ".csv");
		StreamResource extendedStreamResource = new StreamResource(() -> new DelayedInputStream((out) -> {
			try {
				CsvStreamUtils.writeCsvContentToStream(
					exportRowClass,
					exportRowsSupplier,
					propertyIdCaptionFunction,
					exportConfiguration,
					(o) -> exportType == null || hasExportTarget(exportType, (Method) o),
					FacadeProvider.getConfigFacade(),
					out);
			} catch (Exception e) {
				LoggerFactory.getLogger(DownloadUtil.class).error(e.getMessage(), e);

				throw e;
			}

		},
			e -> {
				// TODO This currently requires the user to click the "Export" button again or reload the page
				//  as the UI
				// is not automatically updated; this should be changed once Vaadin push is enabled (see #516)
				VaadinSession.getCurrent()
					.access(
						() -> new Notification(
							I18nProperties.getString(Strings.headingExportFailed),
							I18nProperties.getString(Strings.messageExportFailed),
							Type.ERROR_MESSAGE,
							false).show(Page.getCurrent()));
			}), exportFileName);
		extendedStreamResource.setMIMEType("text/csv");
		extendedStreamResource.setCacheTime(0);
		return extendedStreamResource;
	}

	@SuppressWarnings("rawtypes")
	private static boolean hasExportTarget(Enum<?> exportType, Method m) {

		if (m.isAnnotationPresent(ExportTarget.class)) {
			final Class<? extends Enum> exportTypeClass = exportType.getClass();
			final ExportTarget exportTarget = m.getAnnotation(ExportTarget.class);
			Supplier<Enum[]> exportTypeSupplier = null;
			if (exportTypeClass.isAssignableFrom(CaseExportType.class)) {
				exportTypeSupplier = exportTarget::caseExportTypes;
			}
			if (exportTypeClass.isAssignableFrom(VisitExportType.class)) {
				exportTypeSupplier = exportTarget::visitExportTypes;

			}
			return exportTypeSupplier != null && containsExportType(exportType, exportTypeSupplier);
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private static boolean containsExportType(Enum<?> exportType, Supplier<Enum[]> supplier) {
		return Arrays.asList(supplier.get()).contains(exportType);
	}

	/**
	 * <p>
	 * When downloading a Resource via FileDownloader,
	 * the Component of the FileDownloader must remain visible in the UI.
	 * Otherwise the Resource is unregistered and the download may fail.
	 * </p>
	 * <p>
	 * This method display a modal dialog that includes the exportComponent without actually showing it to the user.
	 * When the dialog is closed, it up to the closeListener to decide the fate of the exportComponent.
	 * </p>
	 *
	 * @param exportComponent
	 * @param closeListener
	 */
	public static void showExportWaitDialog(AbstractComponent exportComponent, CloseListener closeListener) {

		//the button has to remain in the UI for the download to succeed, but it should not be seen 
		CustomLayout hidingLayout = new CustomLayout();
		hidingLayout.setSizeUndefined();
		hidingLayout.setTemplateContents("");
		hidingLayout.addComponent(exportComponent);
		
		Label lbl = new Label(I18nProperties.getString(Strings.infoDownloadExport), ContentMode.HTML);
		HorizontalLayout layout = new HorizontalLayout(lbl, hidingLayout);
		layout.setMargin(true);
		layout.setExpandRatio(lbl, 1);
		Window dialog = new Window();//VaadinUiUtil.showPopupWindow(layout);
		dialog.setContent(layout);
		dialog.setCaption(exportComponent.getCaption());

		dialog.addCloseListener(closeListener);
	}

	public static String createFileNameWithCurrentDatex(String formname, String campaign, ExportEntityName entityName, String fileExtension) {
		String instanceName = FacadeProvider.getConfigFacade().getSormasInstanceName().toLowerCase(); //The export is being prepared
		String processedInstanceName = DataHelper.cleanStringForFileName(campaign);
		String processedEntityName = DataHelper.cleanStringForFileName(entityName.getLocalizedNameInSystemLanguage());
		String exportDate = DateHelper.formatDateForExport(new Date());
		return String.join("_", processedInstanceName, formname, processedEntityName, exportDate, fileExtension);
	}
	
	public static String createFileNameWithCurrentDate(ExportEntityName entityName, String fileExtension) {
		String instanceName = FacadeProvider.getConfigFacade().getSormasInstanceName().toLowerCase(); //The export is being prepared
		String processedInstanceName = DataHelper.cleanStringForFileName(instanceName);
		String processedEntityName = DataHelper.cleanStringForFileName(entityName.getLocalizedNameInSystemLanguage());
		String exportDate = DateHelper.formatDateForExport(new Date());
		return String.join("_", processedInstanceName, processedEntityName, exportDate, fileExtension);
	}

	public static void attachDataDictionaryDownloader(AbstractComponent target) {
		new FileDownloader(new StreamResource(() -> new DownloadUtil.DelayedInputStream((out) -> {
			try {
				String documentPath = FacadeProvider.getInfoFacade().generateDataDictionary();
				IOUtils.copy(Files.newInputStream(new File(documentPath).toPath()), out);
			} catch (IOException e) {
				LoggerFactory.getLogger(AboutView.class).error("Failed to generate data dictionary", e);

				// fall back to pre-generated document
				InputStream preGeneratedDocumentStream = new ClassResource("/doc/SORMAS_Data_Dictionary.xlsx").getStream().getStream();
				IOUtils.copy(preGeneratedDocumentStream, out);
			}
		}, (e) -> {
		}), createFileNameWithCurrentDate(ExportEntityName.DATA_DICTIONARY, ".xlsx"))).extend(target);
	}
}