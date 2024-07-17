package com.cinoteck.application.views.utils;

import com.opencsv.CSVWriter;
import com.vaadin.flow.server.StreamResource;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.utils.CSVUtils;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DateHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DownloadTransposedDaywiseDataUtility {

	public static StreamResource createTransposedDataFromIndexListDemox2(CampaignFormDataCriteria criteria) {
		String exportFileName = createFileNameWithCurrentDateandEntityNameString("transposeddata", ".csv");

		// Using the index list method to get the day-wise form data since it already
		// used the criteria on the grid to get the data
		List<CampaignFormDataIndexDto> formDatafromIndexList = FacadeProvider.getCampaignFormDataFacade()
				.getIndexList(criteria, null, null, null);

		// Initialize the set to store unique variable parts without the day suffix
		Set<String> uniqueVariablePartsWithoutDaySuffixForColumnHeaderxt = new HashSet<>();

		// Iterate through all elements in the formDatafromIndexList to build column
		// headers
		for (CampaignFormDataIndexDto formDataIndex : formDatafromIndexList) {
			if (formDataIndex.getFormValues() != null) {
				Set<String> fieldIDsFromFormValuesforColumnHeadersSet = new HashSet<>();

				// Extract the field IDs from the form values and add them to the set to remove
				// duplicates
				for (CampaignFormDataEntry formValues : formDataIndex.getFormValues()) {
					fieldIDsFromFormValuesforColumnHeadersSet.add(formValues.getId());
				}

				// Convert the set back to a list
				List<String> fieldIDsFromFormValuesforColumnHeadersx = new ArrayList<>(
						fieldIDsFromFormValuesforColumnHeadersSet);

				// Extract unique variable parts and remove the day suffix
				for (String uniqueVariable : extractUniqueVariableParts(fieldIDsFromFormValuesforColumnHeadersx)) {
					String variableID = uniqueVariable.replaceAll("(_day\\d+|_days\\d+)$", "");
					uniqueVariablePartsWithoutDaySuffixForColumnHeaderxt.add(variableID);
				}
			}
		}

		List<String> columnNames = new ArrayList<>();
//	    columnNames.add(I18nProperties.getPrefixCaption(CampaignFormDataIndexDto.I18N_PREFIX, CampaignFormDataIndexDto.COMMUNITY));
		columnNames.add("Campaign");
		columnNames.add("Form");
		columnNames.add("Region");
		columnNames.add("RCode");
		columnNames.add("Province");
		columnNames.add("PCode");
		columnNames.add("District");
		columnNames.add("Dcode");
		columnNames.add("Cluster");
		columnNames.add("Cluster Number");
		columnNames.add("CCode");
		columnNames.add("Form Phase");
		columnNames.add("Source");
		columnNames.add("Creating User");
		columnNames.add("Day");

		// Generate and write columns to CSV writer
		Map<String, Integer> fieldIdPositions = new HashMap<>();
		int ageGroupIndex = 15;
		for (String fieldGroup : uniqueVariablePartsWithoutDaySuffixForColumnHeaderxt) {
			columnNames.add(fieldGroup);
			fieldIdPositions.put(fieldGroup, ageGroupIndex);
			ageGroupIndex += 1;
		}

		Map<String, Map<String, String>> dayValueMap = new HashMap<>();

		for (CampaignFormDataIndexDto individualTransposedFormData : formDatafromIndexList) {
			if (individualTransposedFormData.getFormValues() != null) {
				Map<String, String> formDataMaxp = new HashMap<>();
				Set<String> fieldIDsFromFormValuesforColumnHeadersSet = new HashSet<>();

				for (CampaignFormDataEntry formValues : individualTransposedFormData.getFormValues()) {
					formDataMaxp.put(formValues.getId(), formValues.getValue().toString());
					fieldIDsFromFormValuesforColumnHeadersSet.add(formValues.getId());
				}

				List<String> fieldIDsFromFormValuesforColumnHeadersx = new ArrayList<>(
						fieldIDsFromFormValuesforColumnHeadersSet);

				Set<String> uniqueVariablePartsWithoutDaySuffixForColumnHeaderx = new HashSet<>();

				for (String uniqueVariable : extractUniqueVariableParts(fieldIDsFromFormValuesforColumnHeadersx)) {
					String variableID = uniqueVariable.replaceAll("(_day\\d+|_days\\d+)$", "");
					uniqueVariablePartsWithoutDaySuffixForColumnHeaderx.add(variableID);
				}

				for (String day : extractUniqueDayValues(fieldIDsFromFormValuesforColumnHeadersx)) {
					for (String variable : uniqueVariablePartsWithoutDaySuffixForColumnHeaderx) {
						String key = variable + "_" + day;

						if (formDataMaxp.containsKey(key)) {
							String keyValue = formDataMaxp.get(key);
							dayValueMap.computeIfAbsent(day, k -> new HashMap<>()).put(variable, keyValue);
						}
					}
				}
			}
		}

		return new StreamResource(exportFileName, () -> {
			try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
				try (CSVWriter writer = CSVUtils.createCSVWriter(
						new OutputStreamWriter(byteStream, StandardCharsets.UTF_8.name()),
						FacadeProvider.getConfigFacade().getCsvSeparator())) {

					writer.writeNext(columnNames.toArray(new String[0]));

					for (CampaignFormDataIndexDto individualTransposedFormData : formDatafromIndexList) {
						if (individualTransposedFormData.getFormValues() != null) {
							Map<String, String> formDataMaxp = new HashMap<>();
							Set<String> fieldIDsFromFormValuesforColumnHeadersSet = new HashSet<>();

							for (CampaignFormDataEntry formValues : individualTransposedFormData.getFormValues()) {
								formDataMaxp.put(formValues.getId(), formValues.getValue().toString());
								fieldIDsFromFormValuesforColumnHeadersSet.add(formValues.getId());
							}

							List<String> fieldIDsFromFormValuesforColumnHeadersx = new ArrayList<>(
									fieldIDsFromFormValuesforColumnHeadersSet);

							Set<String> uniqueVariablePartsWithoutDaySuffixForColumnHeaderx = new HashSet<>();

							for (String uniqueVariable : extractUniqueVariableParts(
									fieldIDsFromFormValuesforColumnHeadersx)) {
								String variableID = uniqueVariable.replaceAll("(_day\\d+|_days\\d+)$", "");
								uniqueVariablePartsWithoutDaySuffixForColumnHeaderx.add(variableID);
							}

							for (String day : extractUniqueDayValues(fieldIDsFromFormValuesforColumnHeadersx)) {
								List<String> row = new ArrayList<>(Collections.nCopies(columnNames.size(), ""));

								row.set(0, individualTransposedFormData.getCampaign().toString());

								row.set(1, individualTransposedFormData.getForm().toString());

								row.set(2, individualTransposedFormData.getArea().toString());

								row.set(3, individualTransposedFormData.getRcode().toString());

								row.set(4, individualTransposedFormData.getRegion().toString());

								row.set(5, individualTransposedFormData.getPcode() + "");

								row.set(6, individualTransposedFormData.getDistrict().toString());

								row.set(7, individualTransposedFormData.getDcode() + "");

								if (individualTransposedFormData.getCommunity() == null) {
									row.set(8, "");

									row.set(9, "");

									row.set(10, "");

								} else {
									
									row.set(8, individualTransposedFormData.getCommunity().toString());

									row.set(9, individualTransposedFormData.getClusternumber().toString());

									row.set(10, individualTransposedFormData.getCcode().toString());
									

								}

								row.set(11, individualTransposedFormData.getFormType().toString());

								if (individualTransposedFormData.getSource() == null) {
									row.set(12, "");

								} else {
									row.set(12, individualTransposedFormData.getSource().toString());

								}

								if (individualTransposedFormData.getCreatingUser() == null) {
									row.set(13, "");

								} else {
									row.set(13, individualTransposedFormData.getCreatingUser().toString());


								}

								row.set(14, day);

								for (String variable : uniqueVariablePartsWithoutDaySuffixForColumnHeaderx) {
									String key = variable + "_" + day;
									if (formDataMaxp.containsKey(key)) {
										String keyValue = formDataMaxp.get(key);
										int colIndex = columnNames.indexOf(variable);
										if (colIndex >= 0) {
											row.set(colIndex, keyValue);
										}
									}
								}

								writer.writeNext(row.toArray(new String[0]));
							}
						}
					}

					writer.flush();

				}
				return new ByteArrayInputStream(byteStream.toByteArray());
			} catch (IOException e) {
				// Handle exceptions and show a notification if needed
				return null;
			}
		});
	}

	public static List<String> extractUniqueVariableParts(List<String> formEntriesID) {
		Set<String> variablePartsSet = new HashSet<>();
		Pattern pattern = Pattern.compile("(^(.+)day\\d+|^(.+)days\\d+)$");

		for (String id : formEntriesID) {
			Matcher matcher = pattern.matcher(id);
			if (matcher.find()) {
				variablePartsSet.add(matcher.group(1));
			}
		}
		System.out.println("extractUniqueVariableParts-----" + variablePartsSet);
		return new ArrayList<>(variablePartsSet);
	}

	private static List<String> extractUniqueDayValues(List<String> formEntriesID) {
		Set<String> dayValuesSet = new HashSet<>();
		Pattern pattern = Pattern.compile("(day\\d+|days\\d+)$");

		for (String id : formEntriesID) {
			Matcher matcher1 = pattern.matcher(id);
			if (matcher1.find()) {
				dayValuesSet.add(matcher1.group());
			}

		}

		return new ArrayList<>(dayValuesSet);
	}

	public static String extractDay(String id) {
		Pattern pattern = Pattern.compile("_day\\d+$");
		Matcher matcher = pattern.matcher(id);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}

	public static String createFileNameWithCurrentDate(ExportEntityName entityName, String fileExtension) {
		String instanceName = FacadeProvider.getConfigFacade().getSormasInstanceName().toLowerCase(); // The export is
																										// being
																										// prepared
		String processedInstanceName = DataHelper.cleanStringForFileName(instanceName);
		String processedEntityName = DataHelper.cleanStringForFileName(entityName.getLocalizedNameInSystemLanguage());
		String exportDate = DateHelper.formatDateForExport(new Date());
		return String.join("_", processedInstanceName, processedEntityName, exportDate, fileExtension);
	}

	public static String createFileNameWithCurrentDateandEntityNameString(String entityName, String fileExtension) {
		String instanceName = FacadeProvider.getConfigFacade().getSormasInstanceName().toLowerCase(); // The export is
																										// being
																										// prepared
		String processedInstanceName = DataHelper.cleanStringForFileName(instanceName);
		String processedEntityName = DataHelper.cleanStringForFileName(entityName);
		String exportDate = DateHelper.formatDateForExport(new Date());
		return String.join("_", processedInstanceName, processedEntityName, exportDate, fileExtension);
	}

}
