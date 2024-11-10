package com.cinoteck.application.views.utils;

import com.opencsv.CSVWriter;
import com.vaadin.flow.server.StreamResource;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
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

public final class DownloadTransposedLqasDataUtility {

	CampaignFormDataCriteria criteria = new CampaignFormDataCriteria();

	public static StreamResource createTransposedLqasDataFromIndexList(CampaignFormDataCriteria criteria,
			String formName, String campaignName) {
		CampaignFormDataCriteria criteriax = new CampaignFormDataCriteria();

		criteriax = criteria;

		String exportFileName = "APMIS_" + formName + "_" + campaignName + ".csv";// createFileNameWithCurrentDateandEntityNameString(formName+
																					// "_" + campaignName, ".csv");

		// Using the index list method to get the day-wise form data since it already
		// used the criteria on the grid to get the data
		List<CampaignFormDataIndexDto> formDatafromIndexList = FacadeProvider.getCampaignFormDataFacade()
				.getIndexList(criteriax, null, null, null);

		// Initialize the set to store unique variable parts without the day suffix
		Set<String> fieldIDWithoutDaySuffix = new HashSet<>();

		// Iterate through all elements in the formDatafromIndexList to build column
		// headers
		for (CampaignFormDataIndexDto formData : formDatafromIndexList) {
			if (formData.getFormValues() != null) {
				Set<String> fieldIDsFromFormValues = new HashSet<>();

				// Extract the field IDs from the form values and add them to the set to remove
				// duplicates
				for (CampaignFormDataEntry formValues : formData.getFormValues()) {
					fieldIDsFromFormValues.add(formValues.getId());
				}

				// Convert the set back to a list
				List<String> fieldIDsforColumnHeaders = new ArrayList<>(fieldIDsFromFormValues);

				// Extract unique variable parts and remove the day suffix
				for (String uniqueVariable : extractUniqueVariableParts(fieldIDsforColumnHeaders)) {
					if (uniqueVariable.matches(".*H\\d+$")) {
					String variableID = uniqueVariable.replaceAll("(H\\d+|H\\d+)$", "");
					fieldIDWithoutDaySuffix.add(variableID);
				} else if (!uniqueVariable.startsWith("House")) {
					// Handle non-H-suffix fields
					fieldIDWithoutDaySuffix.add(uniqueVariable);
				}
				
//					String variableID = uniqueVariable.replaceAll("(H\\d+|H\\d+)$", "");
//					fieldIDWithoutDaySuffix.add(variableID);
				}

			}
		}

		List<String> columnNames = new ArrayList<>();
//		
		Map<String, String> fieldIdToCaptionMap = new HashMap<>();

		List<String> fieldCaptions = new ArrayList<>();

		CampaignFormMetaDto formMetaReference = FacadeProvider.getCampaignFormMetaFacade()
				.getCampaignFormMetaByUuid(criteria.getCampaignFormMeta().getUuid());
		if (formMetaReference != null) {
			List<CampaignFormElement> campaignFormElements = formMetaReference.getCampaignFormElements();
			fieldIdToCaptionMap = matchFieldsWithCaptions(fieldIDWithoutDaySuffix, campaignFormElements);
		}

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
		columnNames.add("House Total");

		fieldCaptions.add("Campaign");
		fieldCaptions.add("Form");
		fieldCaptions.add("Region");
		fieldCaptions.add("RCode");
		fieldCaptions.add("Province");
		fieldCaptions.add("PCode");
		fieldCaptions.add("District");
		fieldCaptions.add("Dcode");
		fieldCaptions.add("Cluster");
		fieldCaptions.add("Cluster Number");
		fieldCaptions.add("CCode");
		fieldCaptions.add("Form Phase");
		fieldCaptions.add("Source");
		fieldCaptions.add("Creating User");
		fieldCaptions.add("Day");
		fieldCaptions.add("House Total");


		// Generate and write columns to CSV writer
		// index here has to be 15 because of the number of existing column
		// remeber to increment the values when new columns are added below the defined
		// columns
		Map<String, Integer> fieldIdPositions = new HashMap<>();
		int ageGroupIndex = 15;
		for (String fieldGroup : fieldIDWithoutDaySuffix) {
			columnNames.add(fieldGroup);
			fieldIdPositions.put(fieldGroup, ageGroupIndex);
			ageGroupIndex += 1;
		}

		for (String fieldId : fieldIDWithoutDaySuffix) {
			String caption = fieldIdToCaptionMap.getOrDefault(fieldId, fieldId);
			fieldCaptions.add(caption);
			fieldIdPositions.put(caption, ageGroupIndex);
			ageGroupIndex += 1;
		}

		Map<String, Map<String, String>> dayValueMap = new HashMap<>();

		for (CampaignFormDataIndexDto individualTransposedFormData : formDatafromIndexList) {
			if (individualTransposedFormData.getFormValues() != null) {
				Map<String, String> formDataMaxp = new HashMap<>();
				Set<String> fieldIDsFromFormValues = new HashSet<>();

				for (CampaignFormDataEntry formValues : individualTransposedFormData.getFormValues()) {
					formDataMaxp.put(formValues.getId(), formValues.getValue().toString());
					fieldIDsFromFormValues.add(formValues.getId());
				}

				List<String> fieldIDsforColumnHeaders = new ArrayList<>(fieldIDsFromFormValues);

				Set<String> uniqueVariablePartsWithoutDaySuffixForColumnHeader = new HashSet<>();

				for (String uniqueVariable : extractUniqueVariableParts(fieldIDsforColumnHeaders)) {

					if (uniqueVariable.matches(".*H\\d+$")) {
						// Handle H-suffix fields
						String variableID = uniqueVariable.replaceAll("(H\\d+|H\\d+)$", "");
						uniqueVariablePartsWithoutDaySuffixForColumnHeader.add(variableID);
					} else if (!uniqueVariable.startsWith("House")) {
						// Handle non-H-suffix fields
						uniqueVariablePartsWithoutDaySuffixForColumnHeader.add(uniqueVariable);
					}

//					String variableID = uniqueVariable.replaceAll("(H\\d+|H\\d+)$", "");
//					uniqueVariablePartsWithoutDaySuffixForColumnHeader.add(variableID);
				}

				for (String day : extractUniqueDayValues(fieldIDsforColumnHeaders)) {
					for (String variable : uniqueVariablePartsWithoutDaySuffixForColumnHeader) {
						String key = variable + day;

						System.out.println("key666666666221222" + key);

						if (formDataMaxp.containsKey(key)) {
							String keyValue = formDataMaxp.get(key);
							dayValueMap.computeIfAbsent(day, k -> new HashMap<>()).put(variable, keyValue);
						}
					}
				}

				System.out.println("fieldIDsforColumnHeaders" + fieldIDsforColumnHeaders);
				System.out.println("uniqueVariablePartsWithoutDaySuffixForColumnHeader"
						+ uniqueVariablePartsWithoutDaySuffixForColumnHeader);

			}
		}

		return new StreamResource(exportFileName, () -> {
			try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
				// Write UTF-8 BOM
				byteStream.write(0xEF);
				byteStream.write(0xBB);
				byteStream.write(0xBF);

				try (CSVWriter writer = CSVUtils.createCSVWriter(
						new OutputStreamWriter(byteStream, StandardCharsets.UTF_8),
						FacadeProvider.getConfigFacade().getCsvSeparator())) {

					// Write header twice
					writer.writeNext(fieldCaptions.toArray(new String[0]));

					writer.writeNext(columnNames.toArray(new String[0]));

					for (CampaignFormDataIndexDto individualTransposedFormData : formDatafromIndexList) {
						if (individualTransposedFormData.getFormValues() != null) {
							Map<String, String> formDataMaxp = new HashMap<>();
							Set<String> fieldIDsFromFormValues = new HashSet<>();

							for (CampaignFormDataEntry formValues : individualTransposedFormData.getFormValues()) {
								String value = new String(
										formValues.getValue().toString().getBytes(StandardCharsets.UTF_8),
										StandardCharsets.UTF_8);
								formDataMaxp.put(formValues.getId(), value);
								fieldIDsFromFormValues.add(formValues.getId());
							}

							List<String> fieldIDsforColumnHeaders = new ArrayList<>(fieldIDsFromFormValues);

							Set<String> uniqueVariablePartsWithoutDaySuffixForColumnHeader = new HashSet<>();

							for (String uniqueVariable : extractUniqueVariableParts(fieldIDsforColumnHeaders)) {

								if (uniqueVariable.matches(".*H\\d+$")) {
									// Handle H-suffix fields
									String variableID = uniqueVariable.replaceAll("(H\\d+|H\\d+)$", "");
									uniqueVariablePartsWithoutDaySuffixForColumnHeader.add(variableID);
								} else if (!uniqueVariable.startsWith("House")) {
									// Handle non-H-suffix fields
									uniqueVariablePartsWithoutDaySuffixForColumnHeader.add(uniqueVariable);
								}
//								 else {
//					                    // Handle non-H-suffix fields
//					                    uniqueVariablePartsWithoutDaySuffixForColumnHeader.add(uniqueVariable);
//					                }

//								String variableID = uniqueVariable.replaceAll("(H\\d+|H\\d+)$", "");
//								uniqueVariablePartsWithoutDaySuffixForColumnHeader.add(variableID);
							}

							for (String day : extractUniqueDayValues(fieldIDsforColumnHeaders)) {
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
								row.set(12,
										individualTransposedFormData.getSource() != null
												? individualTransposedFormData.getSource().toString()
												: "");
								row.set(13,
										individualTransposedFormData.getCreatingUser() != null
												? individualTransposedFormData.getCreatingUser().toString()
												: "");
								row.set(14, day);

								String houseNumber = day.replaceAll("[^0-9]", "");

								// Add House value matching the current day's number
								String houseKey = "House" + houseNumber;
								
								  int houseColumnIndex = columnNames.indexOf("House Total");
								
							 
//if(houseColumnIndex >= 0) {
//	
//}
								
								if (formDataMaxp.containsKey(houseKey)) {
									
									
									// Add a new column for House if not already added
									if (!columnNames.contains("House")) {
										columnNames.add("House");
										 houseColumnIndex = columnNames.size() - 1;
										row.add(formDataMaxp.get(houseKey));
									} else {
									       if (houseColumnIndex >= 0 && formDataMaxp.containsKey(houseKey)) {
									            String houseValue = formDataMaxp.get(houseKey);
									            row.set(houseColumnIndex, houseValue);
									            System.out.println("Setting house value: " + houseValue + " for house key: " + houseKey);
									        } else {
									            System.out.println("Failed to set house value. House Column Index: " + houseColumnIndex + 
									                             ", House Key exists: " + formDataMaxp.containsKey(houseKey));
									        }
										
										
//										int houseColumnIndex = columnNames.indexOf("House Total");
//										row.set(houseColumnIndex, formDataMaxp.get(houseKey));
									}
								}
								
								
								
								for (String variable : uniqueVariablePartsWithoutDaySuffixForColumnHeader) {
								    // Case 1: Handle House values
								    if (variable.startsWith("House")) {
								        String houseKeyz = variable + day;
								        if (formDataMaxp.containsKey(houseKeyz)) {
								            String houseValue = formDataMaxp.get(houseKey);
								            int houseColumnIndexz = columnNames.indexOf(variable);
								            if (houseColumnIndexz >= 0) {
								                row.set(houseColumnIndexz, houseValue);
								            }
								        }
								    }
								    // Case 2: Handle all other fields (including those ending with H1, H2, etc.)
								    else {
								        // First try with the day suffix
								        String keyWithDay = variable + day;
								        if (formDataMaxp.containsKey(keyWithDay)) {
								            String keyValue = formDataMaxp.get(keyWithDay);
								            int colIndex = columnNames.indexOf(variable);
								            if (colIndex >= 0) {
								                row.set(colIndex, keyValue);
								            }
								        } 
								        // If not found with day suffix, try the original variable name
								        else if (formDataMaxp.containsKey(variable)) {
								            String keyValue = formDataMaxp.get(variable);
								            int colIndex = columnNames.indexOf(variable);
								            if (colIndex >= 0) {
								                row.set(colIndex, keyValue);
								            }
								        }
								    }
								}
								

//								for (String variable : uniqueVariablePartsWithoutDaySuffixForColumnHeader) {
//									String key = variable + day;
//
//									 // Handle House values
//								    if (variable.startsWith("House")) {
//								        String houseKeyz = variable + day;
//								        if (formDataMaxp.containsKey(houseKeyz)) {
//								            String houseValue = formDataMaxp.get(houseKeyz);
//								            int houseColumnIndexz = columnNames.indexOf(variable);
//								            if (houseColumnIndexz >= 0) {
//								                row.set(houseColumnIndexz, houseValue);
//								            }
//								        }
//								    } 
//								    // Case 2: Handle fields ending with H1, H2, etc.
//								    else if (variable.matches(".*H\\d+$")) {
//								        if (formDataMaxp.containsKey(key)) {
//								            String keyValue = formDataMaxp.get(key);
//								            int colIndex = columnNames.indexOf(variable);
//								            if (colIndex >= 0) {
//								                row.set(colIndex, keyValue);
//								            }
//								        }
//								    }
//								    // Case 3: Handle regular fields (no House prefix or H-suffix)
//								    else {
//								        // Try to get the value directly from the map
//								        if (formDataMaxp.containsKey(variable)) {
//								            String keyValue = formDataMaxp.get(variable);
//								            int colIndex = columnNames.indexOf(variable);
//								            if (colIndex >= 0) {
//								                row.set(colIndex, keyValue);
//								            }
//								        }
//								    }
//
//									if (variable.matches(".*H\\d+$")) {
//										String key = variable + day;
//
//										if (formDataMaxp.containsKey(key)) {
//											String keyValue = formDataMaxp.get(key);
//											int colIndex = columnNames.indexOf(variable);
//											if (colIndex >= 0) {
//												row.set(colIndex, keyValue);
//											}
//
//										}
//									} else if (!variable.startsWith("House")) { // Process non-House regular fields
//										if (formDataMaxp.containsKey(variable)) {
//											String keyValue = formDataMaxp.get(variable);
//											int colIndex = columnNames.indexOf(variable);
//											if (colIndex >= 0) {
//												row.set(colIndex, keyValue);
//											}
//										}
//									}else {
//										
//										String key = variable + day;
//
//										if (formDataMaxp.containsKey(key)) {
//											String keyValue = formDataMaxp.get(key);
//											int colIndex = columnNames.indexOf(variable);
//											if (colIndex >= 0) {
//												row.set(colIndex, keyValue);
//											}
//
//										}
//										
//									}


//								}

								// Ensure proper encoding for each cell in the row
								String[] rowArray = row.toArray(new String[0]);
								for (int i = 0; i < rowArray.length; i++) {
									if (rowArray[i] != null) {
										rowArray[i] = new String(rowArray[i].getBytes(StandardCharsets.UTF_8),
												StandardCharsets.UTF_8);
									}
								}

								writer.writeNext(rowArray);
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

	    for (String id : formEntriesID) {
	        if (id.startsWith("House")) {
	            // For House fields, keep as is (will be combined with day later)
	            String baseHouse = id.replaceAll("H\\d+$", "");
	            variablePartsSet.add(baseHouse);
	        } else if (id.matches(".*H\\d+$")) {
	            // For fields ending with H1, H2, etc., remove the H-suffix
	            String baseVariable = id.replaceAll("H\\d+$", "");
	            variablePartsSet.add(baseVariable);
	        } else {
	            // For regular fields, add as is
	            variablePartsSet.add(id);
	        }
	    }
	    
	    return new ArrayList<>(variablePartsSet);
	}

//	public static List<String> extractUniqueVariableParts(List<String> formEntriesID) {
//		Set<String> variablePartsSet = new HashSet<>();
//		Pattern pattern = Pattern.compile("(^(.+)H\\d+|^(.+)H\\d+)$");
//
//		for (String id : formEntriesID) {
//			Matcher matcher = pattern.matcher(id);
//			if (matcher.find()) {
//				variablePartsSet.add(matcher.group(1));
//			} else if (!id.matches(".*H\\d+$")) {
//				// Add keys that don't have H-suffix directly
//				variablePartsSet.add(id);
//			}
//		}
//		// System.out.println("extractUniqueVariableParts-----" + variablePartsSet);
//		return new ArrayList<>(variablePartsSet);
//	}

	
	private static List<String> extractUniqueDayValues(List<String> formEntriesID) {
	    Set<String> dayValuesSet = new HashSet<>();
	    Pattern pattern = Pattern.compile("H\\d+$");

	    for (String id : formEntriesID) {
	        Matcher matcher = pattern.matcher(id);
	        if (matcher.find()) {
	            dayValuesSet.add(matcher.group());
	        }
	    }
	    return new ArrayList<>(dayValuesSet);
	}

//	private static List<String> extractUniqueDayValues(List<String> formEntriesID) {
//		Set<String> dayValuesSet = new HashSet<>();
//		Pattern pattern = Pattern.compile("(H\\d+|H\\d+)$");
//
//		for (String id : formEntriesID) {
//			Matcher matcher1 = pattern.matcher(id);
//			if (matcher1.find()) {
//				dayValuesSet.add(matcher1.group());
//			}
//
//		}
//
//		return new ArrayList<>(dayValuesSet);
//	}


	private static void debugPrintFormValues(Map<String, String> formDataMaxp, String variable, String day) {
	    System.out.println("Processing variable: " + variable);
	    String keyWithDay = variable + day;
	    System.out.println("Checking key with day: " + keyWithDay);
	    System.out.println("Has value with day: " + formDataMaxp.containsKey(keyWithDay));
	    if (formDataMaxp.containsKey(keyWithDay)) {
	        System.out.println("Value: " + formDataMaxp.get(keyWithDay));
	    }
	    System.out.println("Has direct value: " + formDataMaxp.containsKey(variable));
	    if (formDataMaxp.containsKey(variable)) {
	        System.out.println("Direct value: " + formDataMaxp.get(variable));
	    }
	}

	private static void debugPrintFieldTypes(List<String> formEntriesID) {
	    System.out.println("\nField Types Analysis:");
	    for (String id : formEntriesID) {
	        System.out.println("Field: " + id);
	        System.out.println("  Starts with House: " + id.startsWith("House"));
	        System.out.println("  Ends with H\\d+: " + id.matches(".*H\\d+$"));
	        if (id.matches(".*H\\d+$")) {
	            System.out.println("  Base part: " + id.replaceAll("H\\d+$", ""));
	        }
	    }
	}
	private static boolean isHouseField(String fieldId) {
	    return fieldId.startsWith("House");
	}

	private static boolean isHSuffixField(String fieldId) {
	    return fieldId.matches(".*H\\d+$");
	}

	private static boolean isRegularField(String fieldId) {
	    return !isHouseField(fieldId) && !isHSuffixField(fieldId);
	}

	public static String extractDay(String id) {
		Pattern pattern = Pattern.compile("H\\d+$");
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
																										// prepare
		String processedInstanceName = DataHelper.cleanStringForFileName(instanceName);
		String processedEntityName = DataHelper.cleanStringForFileName(entityName);
		String exportDate = DateHelper.formatDateForExport(new Date());
		return String.join("_", processedInstanceName, processedEntityName, exportDate, fileExtension);

	}

	private static Map<String, String> matchFieldsWithCaptions(Set<String> fieldIDWithoutDaySuffix,
			List<CampaignFormElement> campaignFormElements) {

		Map<String, String> fieldIdToCaptionMap = new HashMap<>();

		for (String fieldId : fieldIDWithoutDaySuffix) {
			for (CampaignFormElement element : campaignFormElements) {
				String elementId = element.getId();
				String elementIdWithoutDay = elementId.replaceAll("(H\\d+|H\\d+)$", "");

				if (elementIdWithoutDay.equals(fieldId)) {
					fieldIdToCaptionMap.put(fieldId, element.getCaption());
					break;
				}
			}
		}

		return fieldIdToCaptionMap;
	}

}
