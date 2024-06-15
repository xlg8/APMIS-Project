package com.cinoteck.application.views.utils;

import com.cinoteck.application.views.utils.ExportEntityName;
import com.opencsv.CSVWriter;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.server.StreamResource;

import de.symeda.sormas.api.AgeGroup;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.report.CampaignDataExtractDto;
import de.symeda.sormas.api.utils.CSVUtils;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.api.utils.SortProperty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

public final class DownloadTransposedDaywiseDataUtility {

	public StreamResource writeDataToCSV(List<CampaignFormDataIndexDto> formDatafromIndexList) {
		String exportFileName = createFileNameWithCurrentDateandEntityNameString("transposeddata", ".csv");

		return new StreamResource(exportFileName, () -> {
			try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
				try (CSVWriter writer = CSVUtils.createCSVWriter(
						new OutputStreamWriter(byteStream, StandardCharsets.UTF_8.name()),
						FacadeProvider.getConfigFacade().getCsvSeparator())) {

					// Prepare list to hold headers
					List<String> headers = new ArrayList<>();
//                	  Prepare list to hold days
					List<String> daysList = new ArrayList<>();

					Set<String> fieldIDsFromFormValuesforColumnHeadersSet = new HashSet<>();
					Set<String> uniqueVariablePartsWithoutDaySuffixForColumnHeaderxt = new HashSet<>();


					headers.add("Day");

					// Process form data to populate daysList and headers
//                for (CampaignFormDataIndexDto formData : formDatafromIndexList) {
					for (CampaignFormDataEntry formValue : formDatafromIndexList.get(0).getFormValues()) {// formData.getFormValues())
																											// {

						String id = formValue.getId();
						String[] parts = id.split("_");

						String daySuffix = parts[parts.length - 1];
						String header = id.replace("_" + daySuffix, "");

						// Extract the field IDs from the form values and add them to the set to remove
						// duplicates
						fieldIDsFromFormValuesforColumnHeadersSet.add(formValue.getId());

						// Convert the set back to a list
						List<String> fieldIDsFromFormValuesforColumnHeadersx = new ArrayList<>(
								fieldIDsFromFormValuesforColumnHeadersSet);

						for (String uniqueVariable : extractUniqueVariableParts(
								fieldIDsFromFormValuesforColumnHeadersx)) {
							String variableID = uniqueVariable.replaceAll("(_day\\d+|_days\\d+)$", "");
							uniqueVariablePartsWithoutDaySuffixForColumnHeaderxt.add(variableID);
						}

						System.out
								.println(id + "<<<<ID>> Day Suffix >>>>" + daySuffix + ">>>>>>>Final Header " + header);
						
						System.out
						.println( ">>>>>>>Final Header " + header);

						
						System.out
						.println(">>>>>>>Final Header " + uniqueVariablePartsWithoutDaySuffixForColumnHeaderxt);


						// Add day suffix to days list
						if (!daysList.contains(daySuffix)) {
							daysList.add(daySuffix);
						}

						// Add header to headers list
						if (!headers.contains(header)) {
							headers.add(header);
						}

					}
//                }
					System.out.println(daysList + "<<<< Day List >>>>" + ">>>>>>>Final Header " + headers);

					writer.writeNext(headers.toArray(new String[0]));

//                    writer.writeNext(columnNames.toArray(new String[0]));
//
//                    // Write each row for day values from dayValueMap
//                    for (String day : dayValueMap.keySet()) {
//                        Map<String, String> dayValues = dayValueMap.get(day);
//
//                        List<String> row = columnNames.stream().map(columnName -> {
//                            if (columnName.equals("DAY")) {
//                                return day;
//                            }
//                            return dayValues.getOrDefault(columnName, "");
//                        }).collect(Collectors.toList());
//
//                        writer.writeNext(row.toArray(new String[0]));
//                    }
				}
				return new ByteArrayInputStream(byteStream.toByteArray());
			} catch (IOException e) {
				// Handle exceptions and show a notification if needed
				return null;
			}
		});
		// Prepare list to hold days
//            List<String> daysList = new ArrayList<>();
//
//            // Prepare list to hold headers
//            List<String> headers = new ArrayList<>();
//
//            // Process form data to populate daysList and headers
//            for (CampaignFormDataIndexDto formData : formDatafromIndexList) {
//                for (CampaignFormDataEntry formValue : formData.getFormValues()) {
//                    String id = formValue.getId();
//                    String[] parts = id.split("_");
//                    String daySuffix = parts[parts.length - 1];
//                    String header = id.replace("_" + daySuffix, "");
//
//                    // Add day suffix to days list
//                    if (!daysList.contains(daySuffix)) {
//                        daysList.add(daySuffix);
//                    }
//
//                    // Add header to headers list
//                    if (!headers.contains(header)) {
//                        headers.add(header);
//                    }
//                }
//            }
//
//            // Write headers to CSV
//            writer.writeNext(headers.toArray(new String[0]));
//
//            // Write form values to CSV
//            for (String day : daysList) {
//                String[] row = new String[headers.size()];
//                for (CampaignFormDataIndexDto formData : formDatafromIndexList) {
//                    for (CampaignFormDataEntry formValue : formData.getFormValues()) {
//                        String id = formValue.getId();
//                        String[] parts = id.split("_");
//                        String daySuffix = parts[parts.length - 1];
//                        String header = id.replace("_" + daySuffix, "");
//
//                        if (day.equals(daySuffix)) {
//                            int index = headers.indexOf(header);
//                            row[index] = formValue.getValue().toString();
//                        }
//                    }
//                }
//                writer.writeNext(row);
//            }
//
//            try {
//				writer.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
//        });
	}

	public static StreamResource createTransposedDataFromIndexListDemo(CampaignFormDataCriteria criteria) {
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
		columnNames.add("DAY");
		// Generate and write columns to CSV writer
		Map<String, Integer> fieldIdPositions = new HashMap<>();
		int ageGroupIndex = 1;
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

					// Write each row for day values from dayValueMap
					for (String day : dayValueMap.keySet()) {
						Map<String, String> dayValues = dayValueMap.get(day);

						List<String> row = columnNames.stream().map(columnName -> {
							if (columnName.equals("DAY")) {
								return day;
							}
							return dayValues.getOrDefault(columnName, "");
						}).collect(Collectors.toList());

						writer.writeNext(row.toArray(new String[0]));
					}
				}
				return new ByteArrayInputStream(byteStream.toByteArray());
			} catch (IOException e) {
				// Handle exceptions and show a notification if needed
				return null;
			}
		});
	}

//	public static StreamResource createTransposedDataFromIndexListDemo(CampaignFormDataCriteria criteria) {
//
//		String exportFileName = createFileNameWithCurrentDateandEntityNameString("transposeddata", ".csv");
//		
//		//Using the index list methos to get the daywise formdata since it already used the criteria on the grid to get the data 
//
//		List<CampaignFormDataIndexDto> formDatafromIndexList = FacadeProvider.getCampaignFormDataFacade()
//				.getIndexList(criteria, null, null, null);
//
//		
//		//i'm using an hash set that would get the values of all the ids, remove the day suffix
//		//basically implementing the power of hashset to remove duplicate id'd to build the column headers 
//		Set<String> uniqueVariablePartsWithoutDaySuffixForColumnHeaderxt = new HashSet<String>();
//
//		
//		
//		//Get the form values and extract the id'd .... .get(0) because i only want the first
//		if (formDatafromIndexList.get(0).getFormValues() != null) {
//			List<String> fieldIDsFromFormValuesforColumnHeadersx = new ArrayList<String>(); // =
//
//			for (CampaignFormDataEntry formValues : formDatafromIndexList.get(0).getFormValues()) {
//				fieldIDsFromFormValuesforColumnHeadersx.add(formValues.getId());
//			}
//
//			List<String> extractinguniquevariable = new ArrayList<String>();
//
//			for (String uniqueVariable : extractUniqueVariableParts(fieldIDsFromFormValuesforColumnHeadersx)) {
//				String variableID = uniqueVariable.replaceAll("(_day\\d+|_days\\d+)$", "");
//				uniqueVariablePartsWithoutDaySuffixForColumnHeaderxt.add(variableID);
//
//			}
//		}
//
//		List<String> columnNames = new ArrayList<>();
//		columnNames.add("dAY");
//		// Generate and write columns to CSV writer
//		Map<String, Integer> fieldIdPositions = new HashMap<>();
//		int ageGroupIndex = 1;
//		for (String fieldGroup : uniqueVariablePartsWithoutDaySuffixForColumnHeaderxt) {
//			columnNames.add(fieldGroup);
//			fieldIdPositions.put(fieldGroup, ageGroupIndex);
//			ageGroupIndex += 1;
//		}
//
////
//
//		Map<String, Map<String, String>> dayValueMap = new HashMap<>();
//
//		for (CampaignFormDataIndexDto individualTransposedFormData : formDatafromIndexList) {
//
//			System.out.println("uuuid of evaluated data--------------- " + individualTransposedFormData.getUuid());
//			if (individualTransposedFormData.getFormValues() != null) {
//				Map<String, String> formDataMaxp = new HashMap<>();
//				List<String> fieldIDsFromFormValuesforColumnHeadersx = new ArrayList<String>(); // =
//
//				for (CampaignFormDataEntry formValues : individualTransposedFormData.getFormValues()) {
//					System.out.println("formValues.getValue().toString()" + formValues.getValue().toString() + " : "
//							+ formValues.getId());
//					formDataMaxp.put(formValues.getId(), formValues.getValue().toString());
//					fieldIDsFromFormValuesforColumnHeadersx.add(formValues.getId());
//				}
//
//				System.err.println(fieldIDsFromFormValuesforColumnHeadersx.size() + " uuuu"
//						+ fieldIDsFromFormValuesforColumnHeadersx);
//				System.err.println("------" + extractUniqueVariableParts(fieldIDsFromFormValuesforColumnHeadersx).size()
//						+ extractUniqueVariableParts(fieldIDsFromFormValuesforColumnHeadersx));
//
//				List<String> extractinguniquevariable = new ArrayList<String>();
//				Set<String> uniqueVariablePartsWithoutDaySuffixForColumnHeaderx = new HashSet<String>();
//
//				for (String uniqueVariable : extractUniqueVariableParts(fieldIDsFromFormValuesforColumnHeadersx)) {
//					String variableID = uniqueVariable.replaceAll("(_day\\d+|_days\\d+)$", "");
//					uniqueVariablePartsWithoutDaySuffixForColumnHeaderx.add(variableID);
//
//				}
//
//				System.out.println("uniqueVariablePartsWithoutDaySuffixForColumnHeaderx"
//						+ uniqueVariablePartsWithoutDaySuffixForColumnHeaderx.size()
//						+ uniqueVariablePartsWithoutDaySuffixForColumnHeaderx);
//
//				for (String day : extractUniqueDayValues(fieldIDsFromFormValuesforColumnHeadersx)) {
//					for (String variable : uniqueVariablePartsWithoutDaySuffixForColumnHeaderx) {
//						String key = variable + "_" + day;
//
//						if (formDataMaxp.keySet().contains(key)) {
//
//							String keyValue = formDataMaxp.get(key);
//
//							dayValueMap.computeIfAbsent(day, k -> new HashMap<>()).put(variable, keyValue);
//
//							System.out.println("valuexxx" + variable + "_" + day + " key valuexxxx" + keyValue);
//
//						} else {
//							System.out
//									.println("valuyyyy" + variable + "_" + day + "keyset yyyy" + formDataMaxp.keySet());
//
//						}
//
//					}
//
//				}
//			}
//
//		}
//
////		for(dayValueMap) {
////			
////		}
//
//		return new StreamResource(exportFileName, () -> {
//			try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
//				try (CSVWriter writer = CSVUtils.createCSVWriter(
//						new OutputStreamWriter(byteStream, StandardCharsets.UTF_8.name()),
//						FacadeProvider.getConfigFacade().getCsvSeparator())) {
//
//					writer.writeNext(columnNames.toArray(new String[columnNames.size()]));
//
//					// Write each row for day values from dayValueMap
//					for (String day : dayValueMap.keySet()) {
//						Map<String, String> dayValues = dayValueMap.get(day);
//
//						List<String> row = columnNames.stream().map(columnName -> {
//							if (columnName.equals("dAY")) {
//								return day;
//							}
//							return dayValues.getOrDefault(columnName, "");
//						}).collect(Collectors.toList());
//
////						writer.writeNext(row.toArray(new String[0]));
//						writer.writeNext(row.toArray(new String[row.size()]));
//					}
//
//
//
//				}
//				return new ByteArrayInputStream(byteStream.toByteArray());
//			} catch (IOException e) {
//				// Handle exceptions and show a notification if needed
//				return null;
//			}
//		});
//
//	}

	public static StreamResource createTransposedDataFromIndexList(CampaignFormDataCriteria criteria) {
		String exportFileName = createFileNameWithCurrentDateandEntityNameString("transposeddata", ".csv");
		List<CampaignFormDataIndexDto> formDataValues = FacadeProvider.getCampaignFormDataFacade()
				.getIndexList(criteria, null, null, null);

		System.out.println("formDataValues Size" + formDataValues);

		// Collect all form entries
		List<CampaignFormDataEntry> formEntries = new ArrayList<>();
		for (CampaignFormDataIndexDto vv : formDataValues) {
			if (vv.getFormValues() != null) {
				formEntries.addAll(vv.getFormValues());
			}
		}

		if (formEntries.isEmpty()) {
			System.out.println("No form entries found.");
			return null;
		}

		// Collect all form entry IDs
		List<String> formEntriesID = new ArrayList<>();
		for (CampaignFormDataEntry vv : formEntries) {
			formEntriesID.add(vv.getId());
		}

		// Extract and combine _day values
		List<String> uniqueDayValues = extractUniqueDayValues(formEntriesID);
		List<String> uniqueFormIdValues = extractUniqueVariableParts(formEntriesID);

		return new StreamResource(exportFileName, () -> {
			try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
				try (CSVWriter writer = CSVUtils.createCSVWriter(
						new OutputStreamWriter(byteStream, StandardCharsets.UTF_8.name()),
						FacadeProvider.getConfigFacade().getCsvSeparator())) {

					// Generate and write columns to CSV writer
					List<String> columnNames = new ArrayList<>();
					columnNames.add("Day");

					Map<String, Integer> fieldIdPositions = new HashMap<>();

					int fieldIndex = 1;

					for (String fieldID : uniqueFormIdValues) {
						if (fieldID != null) {
							columnNames.add(fieldID);
							fieldIdPositions.put(fieldID, fieldIndex);
							fieldIndex += 1;
						}
					}
					writer.writeNext(columnNames.toArray(new String[0]));

					// Initialize rows with day values
					Map<String, String[]> rows = new LinkedHashMap<>();

					for (String day : uniqueDayValues) {
						String[] row = new String[uniqueFormIdValues.size() + 1];
						row[0] = day;
						rows.put(day, row);
					}

					// Populate the rows with values
					for (CampaignFormDataEntry entry : formEntries) {
						String id = entry.getId();
						String value = entry.getValue().toString();
						String day = extractDay(id);
						String variable = extractVariable(id);

						if (day != null && variable != null) {
							String[] row = rows.get(day);
							if (row != null) {
								int columnIndex = uniqueFormIdValues.indexOf(variable) + 1;
								System.out.println("Column index: " + columnIndex + ", row length: " + row.length);

								if (columnIndex > 0 && columnIndex < row.length) {
									row[columnIndex] = value;
								} else {
									System.out.println(
											"Invalid column index: " + columnIndex + " for variable: " + variable);
								}
							} else {
								System.out.println("No row found for day: " + day);
							}
						} else {
							System.out.println("Invalid day or variable extracted from id: " + id);
						}
					}

					// Write rows to CSV
					for (String[] row : rows.values()) {
						writer.writeNext(row);
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

	public static StreamResource createTransposedDataFromMaterializedView(CampaignFormDataCriteria criteria) {
		String exportFileName = createFileNameWithCurrentDateandEntityNameString("transposeddata", ".csv");
		List<CampaignFormDataIndexDto> formDataValues = FacadeProvider.getCampaignFormDataFacade()
				.getIndexList(criteria, null, null, null);

		System.out.println("formDataValues Size: " + formDataValues.size());

		// Collect all form entries
		List<CampaignFormDataEntry> formEntries = new ArrayList<>();
		for (CampaignFormDataIndexDto vv : formDataValues) {
			if (vv.getFormValues() != null) {
				formEntries.addAll(vv.getFormValues());
			}
		}

		if (formEntries.isEmpty()) {
			System.out.println("No form entries found.");
			return null;
		}

		// Collect all form entry IDs
		List<String> formEntriesID = new ArrayList<>();
		for (CampaignFormDataEntry vv : formEntries) {
			formEntriesID.add(vv.getId());
		}

		// Extract unique day values and form IDs
		List<String> uniqueDayValues = extractUniqueDayValues(formEntriesID);

		Set<String> uniqueFormIdValuesSet = new HashSet<>();
		for (String id : formEntriesID) {
			String cleanedId = id.replaceAll("_day\\d+", "");
			uniqueFormIdValuesSet.add(cleanedId);
		}
		List<String> uniqueFormIdValues = new ArrayList<>(uniqueFormIdValuesSet);

		return new StreamResource(exportFileName, () -> {
			try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
				try (CSVWriter writer = CSVUtils.createCSVWriter(
						new OutputStreamWriter(byteStream, StandardCharsets.UTF_8.name()),
						FacadeProvider.getConfigFacade().getCsvSeparator())) {

					// Generate and write columns to CSV writer
					List<String> columnNames = new ArrayList<>();

					columnNames.add("Campaign");
					columnNames.add("District");
					columnNames.add("Cluster");
					columnNames.add("Day");

					Map<String, Integer> fieldIdPositions = new HashMap<>();

					int fieldIndex = 4;

					for (String fieldID : uniqueFormIdValues) {
						if (fieldID != null) {
							for (String day : uniqueDayValues) {
								String dynamicFieldID = fieldID;
								columnNames.add(dynamicFieldID);
								fieldIdPositions.put(dynamicFieldID, fieldIndex);
								fieldIndex += 1;
							}
						}
					}
					writer.writeNext(columnNames.toArray(new String[0]));

					List<Object[]> populationExportDataList = FacadeProvider.getCampaignFormDataFacade()
							.getTransposedCampaignFormDataDaywiseData(criteria);

					Map<String, String[]> exportLinesMap = new HashMap<>();

					for (Object[] populationExportData : populationExportDataList) {
						String dataCampaignName = populationExportData[1] == null ? ""
								: (String) populationExportData[1];
						String dataDistrictName = populationExportData[5] == null ? ""
								: (String) populationExportData[5];
						String dataCommunityName = populationExportData[6] == null ? ""
								: (String) populationExportData[6];
						String dataDay = populationExportData[7] == null ? "" : (String) populationExportData[7];

						String key = dataCampaignName + "|" + dataDistrictName + "|" + dataCommunityName + "|"
								+ dataDay;

						String[] exportLine = exportLinesMap.getOrDefault(key, new String[columnNames.size()]);

						// Populate the static columns
						exportLine[0] = dataCampaignName;
						exportLine[1] = dataDistrictName;
						exportLine[2] = dataCommunityName;
						exportLine[3] = dataDay;

						// Populate the dynamic fields
						for (int i = 3; i < populationExportData.length; i++) {
							String fieldID = columnNames.get(i);
							String dynamicFieldID = fieldID.replaceAll("(_day\\d+)$", "") + dataDay; // Concatenate form
																										// ID with
																										// current day
																										// value
							if (fieldIdPositions.containsKey(dynamicFieldID)) {
								exportLine[fieldIdPositions.get(dynamicFieldID)] = populationExportData[i] == null ? ""
										: populationExportData[i].toString();
							}
						}

						exportLinesMap.put(key, exportLine);
					}

					// Write all lines to CSV
					for (String[] exportLine : exportLinesMap.values()) {
						writer.writeNext(exportLine);
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

	// public static StreamResource
	// createTransposedDayWiseDataExportResource(CampaignFormDataCriteria criteria)
	// {
//	    String exportFileName = createFileNameWithCurrentDateandEntityNameString("DaywiseData", ".csv");
//
//	    return new StreamResource(exportFileName, () -> {
//	        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
//	            try (CSVWriter writer = CSVUtils.createCSVWriter(
//	                    new OutputStreamWriter(byteStream, StandardCharsets.UTF_8.name()),
//	                    FacadeProvider.getConfigFacade().getCsvSeparator())) {
//
//	                // Fetch the data
//	                List<CampaignDataExtractDto> data = FacadeProvider.getCampaignFormDataFacade()
//	                        .getTransposedCampaignFormDataDaywiseData(criteria);
//
//	                // Collect unique field names
//	                Set<String> uniqueFields = data.stream()
//	                        .map(CampaignDataExtractDto::getKey)
//	                        .collect(Collectors.toSet());
//
//	                // Create a sorted list of unique field names
//	                List<String> uniqueFieldList = new ArrayList<>(uniqueFields);
//	                Collections.sort(uniqueFieldList);
//
//	                // Generate and write column headers to CSV
//	                List<String> columnNames = new ArrayList<>();
//	                columnNames.add("Day");
//	                columnNames.addAll(uniqueFieldList);
//	                writer.writeNext(columnNames.toArray(new String[0]));
//
//	                // Initialize rows with day values
//	                Map<String, String[]> rows = new LinkedHashMap<>();
//
//	                for (CampaignDataExtractDto entry : data) {
//	                    String day = entry.getFormDay();
//	                    if (!rows.containsKey(day)) {
//	                        // Initialize row with empty values
//	                        String[] row = new String[uniqueFieldList.size() + 1];
//	                        row[0] = day;
//	                        rows.put(day, row);
//	                    }
//	                    String[] row = rows.get(day);
//	                    int columnIndex = uniqueFieldList.indexOf(entry.getKey()) + 1;
//	                    row[columnIndex] = entry.getValue();
//	                }
//
//	                // Write rows to CSV
//	                for (String[] row : rows.values()) {
//	                    writer.writeNext(row);
//	                }
//
//	                writer.flush();
//	            }
//	            return new ByteArrayInputStream(byteStream.toByteArray());
//	        } catch (IOException e) {
//	            // Handle exceptions and show a notification if needed
//	            return null;
//	        }
//	    });
//	}
//	

//	public static StreamResource createTransposedDayWiseDataExportResource(CampaignFormDataCriteria criteria) {
//	    String exportFileName = createFileNameWithCurrentDateandEntityNameString("DaywiseData", ".csv");
//
//	    return new StreamResource(exportFileName, () -> {
//	        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
//	            try (CSVWriter writer = CSVUtils.createCSVWriter(
//	                    new OutputStreamWriter(byteStream, StandardCharsets.UTF_8.name()),
//	                    FacadeProvider.getConfigFacade().getCsvSeparator())) {
//
//	                // Fetch the data
////	                List<CampaignDataExtractDto> data = FacadeProvider.getCampaignFormDataFacade()
////	                        .getTransposedCampaignFormDataDaywiseData(criteria);
//
//	                
//	                System.out.println("data size ----" + data.size());
//	                // Collect unique base field names
//	                Set<String> uniqueBaseFields = data.stream()
//	                        .map(CampaignDataExtractDto::getBaseFieldName)
//	                        .collect(Collectors.toSet());
//
//	                // Create a sorted list of unique base field names
//	                List<String> uniqueBaseFieldList = new ArrayList<>(uniqueBaseFields);
//	                Collections.sort(uniqueBaseFieldList);
//
//	                // Generate and write column headers to CSV
//	                List<String> columnNames = new ArrayList<>();
//	                columnNames.add("Day");
//	                columnNames.addAll(uniqueBaseFieldList);
//	                writer.writeNext(columnNames.toArray(new String[0]));
//
//	                // Initialize rows with day values
//	                Map<String, String[]> rows = new LinkedHashMap<>();
//
//	                for (CampaignDataExtractDto entry : data) {
//	                    String day = entry.getFormDay();
//	                    if (!rows.containsKey(day)) {
//	                        // Initialize row with empty values
//	                        String[] row = new String[uniqueBaseFieldList.size() + 1];
//	                        row[0] = day;
//	                        rows.put(day, row);
//	                    }
//	                    String[] row = rows.get(day);
//	                    int columnIndex = uniqueBaseFieldList.indexOf(entry.getBaseFieldName()) + 1;
//	                    row[columnIndex] = entry.getValue();
//	               
//
//	                // Write rows to CSV
//	                for (String[] rowx : rows.values()) {
//	                    writer.writeNext(rowx);
//	                }
//	                }
//	                writer.flush();
//	            }
//	            return new ByteArrayInputStream(byteStream.toByteArray());
//	        } catch (IOException e) {
//	            // Handle exceptions and show a notification if needed
//	            return null;
//	        }
//	    });
//	}
//	
	private static void writeTableToCSV(List<String> uniqueDayValues, List<String> uniqueVariableParts,
			List<CampaignFormDataEntry> formEntries, String fileName) {
		// Sort day values and variable parts for proper order in the table
		Collections.sort(uniqueDayValues);
		Collections.sort(uniqueVariableParts);

		try (FileWriter writer = new FileWriter(fileName)) {
			// Write CSV header
			writer.append("Day");
			for (String variable : uniqueVariableParts) {
				writer.append(",").append(variable);
			}
			writer.append("\n");

			// Write CSV rows
			for (String day : uniqueDayValues) {
				writer.append(day);
				for (String variable : uniqueVariableParts) {
					String value = getValueForVariableAndDay(formEntries, variable, day);
					writer.append(",").append(value != null ? value : "");
				}
				writer.append("\n");
			}

			System.out.println("CSV file created: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	private static String getValueForVariableAndDay(List<CampaignFormDataEntry> formEntries, String variable,
			String day) {
		String idPattern = variable + day;
		for (CampaignFormDataEntry entry : formEntries) {
			if (entry.getId().equals(idPattern)) {
				return entry.getValue().toString();
			}
		}
		return null;
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

	private static String extractVariable(String id) {
		Pattern pattern = Pattern.compile("^(.+)_day\\d+$");
		Matcher matcher = pattern.matcher(id);
		if (matcher.find()) {
			return matcher.group(1);
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
