package com.cinoteck.application.views.utils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

//import org.hibernate.internal.build.AllowSysOut;

import com.google.common.collect.Sets;
import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.server.ErrorMessage;
//import com.vaadin.server.Page;
//import com.vaadin.server.UserError;
//import com.vaadin.server.VaadinService;
//import com.vaadin.server.Page.Styles;
//import com.vaadin.server.Sizeable.Unit;
//import com.vaadin.ui.AbstractComponent;
//import com.vaadin.ui.Component.ErrorEvent;
//import com.vaadin.ui.Notification;
//import com.vaadin.v7.ui.DateField;
//import com.vaadin.ui.TabSheet;
//import com.vaadin.ui.VerticalLayout;
//import com.vaadin.v7.data.Property.ReadOnlyException;
//import com.vaadin.v7.data.Validator.InvalidValueException;
//import com.vaadin.v7.data.Validator;
//import com.vaadin.v7.data.util.converter.Converter;
//import com.vaadin.v7.data.util.converter.Converter.ConversionException;
//import com.vaadin.v7.data.validator.RegexpValidator;
//import com.vaadin.v7.shared.ui.label.ContentMode;
//import com.vaadin.v7.ui.ComboBox;
//import com.vaadin.v7.ui.Field;
//import com.vaadin.v7.ui.Label;
//import com.vaadin.v7.ui.OptionGroup;
//import com.vaadin.v7.ui.TextArea;
//import com.vaadin.v7.ui.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.server.VaadinService;

import de.symeda.sormas.api.MapperUtil;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.data.translation.TranslationElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElementEnumOptions;
import de.symeda.sormas.api.campaign.form.CampaignFormElementStyle;
import de.symeda.sormas.api.campaign.form.CampaignFormElementOptions;
import de.symeda.sormas.api.campaign.form.CampaignFormElementType;
import de.symeda.sormas.api.campaign.form.CampaignFormTranslations;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.sormastosormas.validation.ValidationErrorMessage;
import de.symeda.sormas.api.utils.fieldaccess.UiFieldAccessCheckers;
import de.symeda.sormas.api.utils.fieldvisibility.FieldVisibilityCheckers;

public class CampaignFormBuilder extends FormLayout {

	private final List<CampaignFormElement> formElements;
	private final Map<String, Object> formValuesMap;
	private final FormLayout campaignFormLayout;
	private final Locale userLocale;
	private Map<String, String> userTranslations = new HashMap<String, String>();
	private Map<String, String> userOptTranslations = new HashMap<String, String>();
	Map<String, AbstractField> fields;
	private Map<String, String> optionsValues = new HashMap<String, String>();
	private List<String> constraints;
	private List<CampaignFormTranslations> translationsOpt;

	public CampaignFormBuilder(List<CampaignFormElement> formElements, List<CampaignFormDataEntry> formValues,
			FormLayout campaignFormLayout, List<CampaignFormTranslations> translations) {
		this.formElements = formElements;
		if (formValues != null) {
			this.formValuesMap = new HashMap<>();
			formValues.forEach(formValue -> formValuesMap.put(formValue.getId(), formValue.getValue()));
		} else {
			this.formValuesMap = new HashMap<>();
		}
		this.campaignFormLayout = campaignFormLayout;
		this.fields = new HashMap<>();
		this.translationsOpt = translations;

		this.userLocale = I18nProperties.getUserLanguage().getLocale();
		if (userLocale != null) {
			translations.stream().filter(t -> t.getLanguageCode().equals(userLocale.toString())).findFirst().ifPresent(
					filteredTranslations -> userTranslations = filteredTranslations.getTranslations().stream().collect(
							Collectors.toMap(TranslationElement::getElementId, TranslationElement::getCaption)));
		}
		
		buildForm();
	
	}

	public void buildForm() {
		int currentCol = -1;
		// GridLayout currentLayout = campaignFormLayout;
		int sectionCount = 0;

		int ii = 0;
		// System.out.println("Got one____");
		VerticalLayout vertical = new VerticalLayout();
		vertical.setSizeFull();
		vertical.setWidthFull();
		vertical.setHeightFull();
		vertical.setSpacing(false);

		TabSheet accrd = new TabSheet();
		accrd.setHeight(750, Unit.PIXELS);

		int accrd_count = 0;

		for (CampaignFormElement formElement : formElements) {
			CampaignFormElementType type = CampaignFormElementType.fromString(formElement.getType());
			String fieldId = formElement.getId();
			List<CampaignFormElementStyle> styles;
			if (formElement.getStyles() != null) {
				styles = Arrays.stream(formElement.getStyles()).map(CampaignFormElementStyle::fromString)
						.collect(Collectors.toList());
			} else {
				styles = new ArrayList<>();
			}

			if (formElement.getOptions() != null) {
				// userOptTranslations = null;

				CampaignFormElementOptions campaignFormElementOptions = new CampaignFormElementOptions();
				optionsValues = formElement.getOptions().stream()
						.collect(Collectors.toMap(MapperUtil::getKey, MapperUtil::getCaption)); // .collect(Collectors.toList());

				if (userLocale != null) {
					
					translationsOpt.stream().filter(t -> t.getLanguageCode().equals(userLocale.toString())).findFirst()
							.ifPresent(filteredTranslations -> filteredTranslations.getTranslations().stream()
									.filter(cd -> cd.getElementId().equals(formElement.getId())).findFirst()
									.ifPresent(optionsList -> userOptTranslations = optionsList.getOptions().stream()
											.filter(c -> c.getCaption() != null)
											.collect(Collectors.toMap(MapperUtil::getKey, MapperUtil::getCaption))));

				}

				/*
				 * if (userLocale != null) {
				 * System.out.println("___userLocale.toString()_____"+userLocale.toString());
				 * System.out.println("___translationsOpt_____"+translationsOpt.size());
				 * translationsOpt.stream().filter(t ->
				 * t.getLanguageCode().equals(userLocale.toString())).findFirst()
				 * .ifPresent(filteredTranslations ->
				 * filteredTranslations.getTranslations().stream()
				 * 
				 * .filter(cd -> cd.getElementId() == formElement.getId()).findFirst()
				 * .ifPresent(optionsList -> userOptTranslations =
				 * optionsList.getOptions().stream() .filter(c -> c.getCaption() != null)
				 * .collect(Collectors.toMap(MapperUtil::getKey, MapperUtil::getCaption))));
				 * System.out.println("___userOptTranslations_____"+userOptTranslations.size());
				 * System.out.println("___formElement.getId()_____"+formElement.getId()); }
				 * 
				 * 
				 */

				if (userOptTranslations.size() == 0) {
					campaignFormElementOptions.setOptionsListValues(optionsValues);
					// get18nOptCaption(formElement.getId(), optionsValues));
				} else {
					campaignFormElementOptions.setOptionsListValues(userOptTranslations);

				}

			} else {
				optionsValues = new HashMap<String, String>();
			}

			if (formElement.getConstraints() != null) {
				CampaignFormElementOptions campaignFormElementOptions = new CampaignFormElementOptions();
				constraints = (List<String>) Arrays.stream(formElement.getConstraints()).collect(Collectors.toList());
				ListIterator<String> lstItemsx = constraints.listIterator();
				int i = 1;
				while (lstItemsx.hasNext()) {
					String lss = lstItemsx.next().toString();
					if (lss.toLowerCase().contains("max")) {
						campaignFormElementOptions.setMax(Integer.parseInt(lss.substring(lss.lastIndexOf("=") + 1)));
					} else if (lss.toLowerCase().contains("min")) {
						campaignFormElementOptions.setMin(Integer.parseInt(lss.substring(lss.lastIndexOf("=") + 1)));
					} else if (lss.toLowerCase().contains("expression")) {
						campaignFormElementOptions.setExpression(true);
					}
				}

			}
			// input:checked

			String dependingOnId = formElement.getDependingOn();
			Object[] dependingOnValues = formElement.getDependingOnValues();

			Object value = formValuesMap.get(formElement.getId());

			int occupiedColumns = getOccupiedColumns(type, styles);

			if (type == CampaignFormElementType.DAYWISE) {
				accrd_count++;
				if (accrd_count > 1) {

					final VerticalLayout layout = new VerticalLayout(vertical);
					layout.setMargin(true);
					// layout.addComponent(label);
					int temp = accrd_count;
					temp = temp - 1;
					layout.setClassName("daywise_background_" + temp); // .addStyleName(dependingOnId); sormas background: green
					accrd.add(formElement.getCaption(), layout);

					vertical = new VerticalLayout();
					vertical.setSizeFull();
					vertical.setWidthFull();
					vertical.setHeightFull();
					vertical.setSpacing(false);

				}
			} else if (type == CampaignFormElementType.SECTION) {
				sectionCount++;
			
				vertical.setId("formSectionId-" + sectionCount);

			} else if (type == CampaignFormElementType.LABEL) {
		
				Label labx = new Label();
				labx.getElement().setProperty("innerHTML", formElement.getCaption());
				labx.setId(formElement.getId());

				vertical.add(labx);

				if (dependingOnId != null && dependingOnValues != null) {
					//needed
				//	setVisibilityDependency(field, dependingOnId, dependingOnValues);
				}
			} else {
				CampaignFormElementOptions constrainsVal = new CampaignFormElementOptions();
				boolean fieldIsRequired = formElement.isImportant();

				if (type == CampaignFormElementType.YES_NO) {
					ToggleButton toggle = new ToggleButton();
					toggle.setId(formElement.getId());
					toggle.setSizeFull();
					toggle.setRequiredIndicatorVisible(formElement.isImportant());
					toggle.setLabelAsHtml(formElement.getCaption() +": No ");

					toggle.addValueChangeListener(evt -> toggle.setLabel(formElement.getCaption()+" : "+ (evt.getValue() == true ? "Yes " : "No ")));
							
							
					vertical.add(toggle);
					fields.put(formElement.getId(), toggle);
					
				} else if (type == CampaignFormElementType.TEXT) {
					TextField textField = new TextField();
					textField.setLabel(formElement.getCaption());
					//textField.setValue("Ruukinkatu 2");
					textField.setClearButtonVisible(true);
					textField.setPrefixComponent(VaadinIcon.PENCIL.create());
					textField.setId(formElement.getId());
					textField.setSizeFull();
					textField.setRequiredIndicatorVisible(formElement.isImportant());
					vertical.add(textField);
					fields.put(formElement.getId(), textField);
					
				}else if(type == CampaignFormElementType.NUMBER){
					NumberField numberField = new NumberField();
					numberField.setLabel(formElement.getCaption());
					numberField.setId(formElement.getId());
					numberField.setSizeFull();
					numberField.setRequiredIndicatorVisible(formElement.isImportant());
					vertical.add(numberField);
					fields.put(formElement.getId(), numberField);
					
					//Binder<String> binder = new Binder<>(String.class);

					
					if(fieldId.equalsIgnoreCase("Villagecode")) {
						numberField.setAllowedCharPattern("(?!.*000$).*");
//								 new RegexpValidator("(?!.*000$).*", I18nProperties.getValidationError(
//											errormsg == null ? caption + ": " + Validations.onlyDecimalNumbersAllowed : errormsg, caption) ));
						 
						numberField.addValueChangeListener(e -> {
							if (e.getValue() != null && e.getValue().toString().length() > 2 && e.getValue().toString().length() < 8) {
								if (VaadinService.getCurrentRequest().getWrappedSession()
										.getAttribute("Clusternumber") != null) {
									
									final String des = VaadinService.getCurrentRequest().getWrappedSession()
											.getAttribute("Clusternumber") + e.getValue().toString().substring(0, 3);
									
									numberField.setValue(Double.parseDouble(des));

								}
							}
						});
					
					}
					
					
					
					if(fieldId.equalsIgnoreCase("PopulationGroup_0_4")) {
						
						 
						numberField.addValueChangeListener(e -> {
								if (VaadinService.getCurrentRequest().getWrappedSession()
										.getAttribute("populationdata") != null) {
									
									final String des = VaadinService.getCurrentRequest().getWrappedSession().getAttribute("populationdata").toString();
									numberField.setValue(Double.parseDouble(des));
									numberField.setReadOnly(true);
								}
								
						});
					
					}
					
					
				}else if(type == CampaignFormElementType.RANGE){
					IntegerField integerField = new IntegerField();
					integerField.setLabel(formElement.getCaption());
//					integerField.setHelperText("Max 10 items");
					integerField.setId(formElement.getId());
					integerField.setValue(2);
					integerField.setStepButtonsVisible(true);
					integerField.setSizeFull();
					integerField.setRequiredIndicatorVisible(formElement.isImportant());
					vertical.add(integerField);
					fields.put(formElement.getId(), integerField);
					
					String validationMessageTag = "";
					Map<String, Object> validationMessageArgs = new HashMap<>();

					if (constrainsVal.isExpression()) {
						
						if (!fieldIsRequired) {
						//	ApmisNotification notification = new ApmisNotification("Application submitted!");
						}
						
						constrainsVal.setExpression(false);

					} else {

						if (constrainsVal.getMin() != null || constrainsVal.getMax() != null) {
							
							integerField.setMin(constrainsVal.getMin());
							integerField.setMax(constrainsVal.getMax());
							
							if (constrainsVal.getMin() == null) {
								validationMessageTag = Validations.numberTooBig;
								validationMessageArgs.put("value", constrainsVal.getMax());
							} else if (constrainsVal.getMax() == null) {
								validationMessageTag = Validations.numberTooSmall;
								validationMessageArgs.put("value", constrainsVal.getMin());
							} else {
								validationMessageTag = Validations.numberNotInRange;
								validationMessageArgs.put("min", constrainsVal.getMin());
								validationMessageArgs.put("max", constrainsVal.getMax());
							}
//needed
							// field.addValidator(
							// new NumberValidator(I18nProperties.getValidationError(validationMessageTag,
							// validationMessageArgs), minValue, maxValue));
							

//							((TextField) field).addValidator(new NumberNumericValueValidator(
//									caption.toUpperCase() + ": "
//											+ 
//											
//											I18nProperties.getValidationError(validationMessageTag,
//													validationMessageArgs),
//									constrainsVal.getMin(), constrainsVal.getMax(), true, isOnError));

						}else {
							
							//needed
							//This should throw error as range suppose to have min and max if not taken care by expression
						}
					}
				
				
				
					
				}else if( type == CampaignFormElementType.DECIMAL) {
					BigDecimalField bigDecimalField = new BigDecimalField();
					bigDecimalField.setLabel(formElement.getCaption());
					bigDecimalField.setWidth("240px");
					bigDecimalField.setValue(new BigDecimal("948205817.472950487"));
					bigDecimalField.setId(formElement.getId());
					bigDecimalField.setSizeFull();
					bigDecimalField.setRequiredIndicatorVisible(formElement.isImportant());
					vertical.add(bigDecimalField);
					fields.put(formElement.getId(), bigDecimalField);

				
				} else if (type == CampaignFormElementType.TEXTBOX) {
					TextArea textArea = new TextArea();
					textArea.setWidthFull();
					textArea.setLabel(formElement.getCaption());
					textArea.setId(formElement.getId());
					textArea.setSizeFull();
					textArea.setRequiredIndicatorVisible(formElement.isImportant());
					vertical.add(textArea);
					fields.put(formElement.getId(), textArea);


				} else if (type == CampaignFormElementType.RADIO) {
					RadioButtonGroup<CampaignFormElementEnumOptions> radioGroup = new RadioButtonGroup<>();
					radioGroup.setLabel(formElement.getCaption());
					radioGroup.setItems(CampaignFormElementEnumOptions.values());
					radioGroup.setId(formElement.getId());
					radioGroup.setSizeFull();
					radioGroup.setRequiredIndicatorVisible(formElement.isImportant());
					vertical.add(radioGroup);
					fields.put(formElement.getId(), radioGroup);

					

				} else if (type == CampaignFormElementType.RADIOBASIC) {
					RadioButtonGroup<String> radioGroupVert = new RadioButtonGroup<>();
					radioGroupVert.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
					radioGroupVert.setLabel(formElement.getCaption());
					radioGroupVert.setItems("Pending", "Submitted", "Confirmed");
					radioGroupVert.setValue("Pending");
					radioGroupVert.setId(formElement.getId());
					radioGroupVert.setSizeFull();
					radioGroupVert.setRequiredIndicatorVisible(formElement.isImportant());
					vertical.add(radioGroupVert);
					
				} else if (type == CampaignFormElementType.DROPDOWN) {
					
				} else if (type == CampaignFormElementType.CHECKBOX) {
					CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
					checkboxGroup.setLabel(formElement.getCaption());
					checkboxGroup.setItems("Read", "Edit", "Delete");
					checkboxGroup.setId(formElement.getId());
					checkboxGroup.setSizeFull();
					checkboxGroup.setRequiredIndicatorVisible(formElement.isImportant());
					vertical.add(checkboxGroup);
					fields.put(formElement.getId(), checkboxGroup);


				} else if (type == CampaignFormElementType.CHECKBOXBASIC) {
					CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
					checkboxGroup.setLabel(formElement.getCaption());
					checkboxGroup.setItems("Monday", "Tuesday", "Wednesday", "Thursday",
					        "Friday", "Saturday", "Sunday");
					checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
					checkboxGroup.setId(formElement.getId());
					checkboxGroup.setSizeFull();
					checkboxGroup.setRequiredIndicatorVisible(formElement.isImportant());
					vertical.add(checkboxGroup);
					fields.put(formElement.getId(), checkboxGroup);



				} else if (type == CampaignFormElementType.DATE) {
					DatePicker.DatePickerI18n singleFormatI18n = new DatePicker.DatePickerI18n();
					singleFormatI18n.setDateFormat("dd-MM-yyyy");

					DatePicker datePicker = new DatePicker(formElement.getCaption());
					datePicker.setI18n(singleFormatI18n);
					datePicker.setSizeFull();
					datePicker.setPlaceholder("DD.MM.YYYY");
					datePicker.setHelperText("Format: DD.MM.YYYY");
					datePicker.setId(formElement.getId());
					datePicker.setRequiredIndicatorVisible(formElement.isImportant());
					vertical.add(datePicker);
					fields.put(formElement.getId(), datePicker);
				}
				
				

				if (dependingOnId != null && dependingOnValues != null) {
					//needed
					//setVisibilityDependency((AbstractComponent) field, dependingOnId, dependingOnValues);
				}
				
				
				
			}
			
			if (accrd_count == 0) {

				campaignFormLayout.add(vertical);
			} else {

				campaignFormLayout.add(accrd);
			}

			userOptTranslations = new HashMap<String, String>(); 
		}
		vertical.setWidthFull();
		campaignFormLayout.setWidthFull();
		
		add(campaignFormLayout);
	}


	private int getOccupiedColumns(CampaignFormElementType type, List<CampaignFormElementStyle> styles) {
		List<CampaignFormElementStyle> colStyles = styles.stream().filter(s -> s.toString().startsWith("col"))
				.collect(Collectors.toList());

		if (type == CampaignFormElementType.YES_NO && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.RADIO && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.CHECKBOX && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.DROPDOWN && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.CHECKBOXBASIC && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.RADIOBASIC && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.TEXTBOX && !styles.contains(CampaignFormElementStyle.INLINE)
				|| (type == CampaignFormElementType.TEXT || type == CampaignFormElementType.DATE
						|| type == CampaignFormElementType.NUMBER || type == CampaignFormElementType.DECIMAL
						|| type == CampaignFormElementType.RANGE)) {// && styles.contains(CampaignFormElementStyle.ROW))
																	// {
			return 12;
		}

		if (colStyles.isEmpty()) {
			switch (type) {
			case LABEL:
			case SECTION:
				return 12;
			default:
				return 4;
			}
		}

		// Multiple col styles are not supported; use the first one
		String colStyle = colStyles.get(0).toString();
		return Integer.parseInt(colStyle.substring(colStyle.indexOf("-") + 1));
	}

	private float calculateComponentWidth(CampaignFormElementType type, List<CampaignFormElementStyle> styles) {
		List<CampaignFormElementStyle> colStyles = styles.stream().filter(s -> s.toString().startsWith("col"))
				.collect(Collectors.toList());

		if (type == CampaignFormElementType.YES_NO && styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.RADIO && styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.RADIOBASIC && styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.CHECKBOX && styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.CHECKBOXBASIC && styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.DROPDOWN && styles.contains(CampaignFormElementStyle.INLINE)
				|| (type == CampaignFormElementType.TEXT || type == CampaignFormElementType.NUMBER
						|| type == CampaignFormElementType.DECIMAL || type == CampaignFormElementType.RANGE
						|| type == CampaignFormElementType.DATE || type == CampaignFormElementType.TEXTBOX)
				// && !styles.contains(CampaignFormElementStyle.ROW)
				|| type == CampaignFormElementType.LABEL || type == CampaignFormElementType.SECTION) {
			return 100f;
		}
		if (1 == 1) {
			return 100f;
		}

		if (colStyles.isEmpty()) {
			// return 33.3f;
		}

		// Multiple col styles are not supported; use the first one
		String colStyle = colStyles.get(0).toString();
		return Integer.parseInt(colStyle.substring(colStyle.indexOf("-") + 1)) / 12f * 100;
	}
//
//	public <T extends CustomField<?>> void setFieldValue(T field, CampaignFormElementType type, Object value,
//			Map<String, String> options, String defaultvalue, Boolean isErrored, Object defaultErrorMsgr) {
//
//		switch (type) {
//		case YES_NO:
//		//	System.out.println(field.getId() +"@@@@@@@@@@555555555555@@@@@@@@@@@@@@@@@@@@2 "+value);
//			if (value != null) {
//				value = value.toString().equalsIgnoreCase("YES") ? true
//						: value.toString().equalsIgnoreCase("NO") ? false : value;
//
//			}
//		//	System.out.println(Sets.newHashSet(value)+"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@2 "+value);
//			((NullableOptionGroup) field).setValue(Sets.newHashSet(value));
//			break;
//		case TEXT:
//		case NUMBER:
//		case RANGE:
//
//			Boolean isExpressionValue = false;
//			if (defaultErrorMsgr != null) {
//				if (defaultErrorMsgr.toString().endsWith("..")) {
//					isExpressionValue = true;
//					defaultErrorMsgr = defaultErrorMsgr.toString().equals("..") ? null
//							: defaultErrorMsgr.toString().replace("..", "");
//				}
//			}
//
//			if (isExpressionValue && !isErrored && value == null) {
//				
//				Object tempz = defaultErrorMsgr != null ? defaultErrorMsgr : "Data entered not in range or calculated rangexxx!";
//				((TextField) field).setCaption(
//						((TextField) field).getCaption());
//				((TextField) field).setRequiredError(defaultErrorMsgr != null ? defaultErrorMsgr.toString() : "Data entered not in range or calculated range!");
//			//	Notification.show("Error found", tempz.toString(), Notification.TYPE_TRAY_NOTIFICATION);
//			}
//
//			((TextField) field).setValue(value != null ? value.toString() : defaultvalue != null ? defaultvalue : null);
//
//			break;
//		case DECIMAL:
//			if (value != null) {
//
//				((TextField) field).setValue(value != null ? value.toString() : null);
//			}
//			break;
//		case TEXTBOX:
//
//			if (value != null) {
//
//				if (value.equals(true)) {
//					field.setEnabled(true);
//				} else if (value.equals(false)) {
//					field.setEnabled(false);
//					// Notification.show("Warning:", Title "Expression resulted in wrong value
//					// please check your data 1", Notification.TYPE_WARNING_MESSAGE);
//				}
//			}
//			;
//			((TextArea) field).setValue(value != null ? value.toString() : null);
//			break;
//		case DATE:
//			if (value != null) {
//				try {
//
//					String vc = value + "";
//					// System.out.println("@@@===@@ date to parse"+value);
//					Date dst = vc.contains("00:00:00") ? dateFormatter(value) : dateFormatterLongAndMobile(value);
//
//					((DateField) field).setValue(value != null ? dst : null);
//
//				} catch (ReadOnlyException | ConversionException e) {
//					// TODO Auto-generated catch block
//					((DateField) field).setValue(null);
//					e.printStackTrace();
//				} catch (ParseException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			;
//			break;
//		case RADIO:
//			((OptionGroup) field).select(Sets.newHashSet(value).toString().replace("[", "").replace("]", ""));
//			break;
//		case RADIOBASIC:
//			((OptionGroup) field).select(Sets.newHashSet(value).toString().replace("[", "").replace("]", ""));
//			break;
//		case CHECKBOX:
//			if (value != null) {
//				String dcs = value.toString().replace("[", "").replace("]", "").replaceAll(", ", ",");
//				String strArray[] = dcs.split(",");
//				for (int i = 0; i < strArray.length; i++) {
//					((OptionGroup) field).select(strArray[i]);
//				}
//			}
//			;
//			break;
//		case CHECKBOXBASIC:
//
//			if (value != null) {
//				String dcxs = value.toString().replace("[", "").replace("]", "").replaceAll(", ", ",");
//				String strArraxy[] = dcxs.split(",");
//				for (int i = 0; i < strArraxy.length; i++) {
//					((OptionGroup) field).select(strArraxy[i]);
//				}
//			}
//			;
//			break;
//		case DROPDOWN:
//
//			if (value != null) {
//
//				if (value.equals(true)) {
//					field.setEnabled(true);
//				} else if (value.equals(false)) {
//					field.setEnabled(false);
//				}
//			}
//			;
//			
//			if (defaultvalue != null) {
//				String dxz = options.get(defaultvalue);
//				((ComboBox) field).select(defaultvalue);
//			}
//			;
//			
//			
//			if (value != null) {
//				String dxz = options.get(value);
//				((ComboBox) field).select(value);
//			}
//			;
//			
//
//			break;
//		default:
//			throw new IllegalArgumentException(type.toString());
//		}
//	}
//
//	private Date dateFormatterLongAndMobile(Object value) {
//
//		String dateStr = value + "";
//		DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss a");
//		DateFormat formatter_ = new SimpleDateFormat("MMM d, yyyy HH:mm:ss");
//		DateFormat formatter_x = new SimpleDateFormat("MMM d, yyyy HH:mm:ss a");
//		DateFormat formattercx = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
//		DateFormat formatterx = new SimpleDateFormat("dd/MM/yyyy");
//		Date date;
//		//System.out.println("date in question " + value); delteme meeeeee  +++++++++++++++++++++++===========
//
//		try {
//			date = (Date) formatter.parse(dateStr);
//		} catch (ParseException e) {
//			System.out.println("date wont parse on " + e.getMessage());
//			try {
//				date = (Date) formatter_.parse(dateStr);
//					} catch (ParseException ex) {
//						System.out.println("date wont parse on " + ex.getMessage());
//						try {
//							date = (Date) formatter_x.parse(dateStr);
//						} catch (ParseException edz) {
//							System.out.println("date wont parse on " + edz.getMessage());
//					try {
//						date = (Date) formatterx.parse(dateStr);
//					} catch (ParseException ed) {
//						System.out.println("date wont parse on " + ed.getMessage());
//						
//						try {
//							date = (Date) formattercx.parse(dateStr);
//						} catch (ParseException edx) {
//							System.out.println("date wont parse on " + edx.getMessage());
//							date = new Date((Long) value);
//						}
//					}
//				}
//			}
//		}
//
//		return date;
//	}
//
//	private Date dateFormatter(Object value) throws ParseException {
//		// TODO Auto-generated method stub
//
//		String dateStr = value + "";
//		DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
//		Date date;
//
//		date = (Date) formatter.parse(dateStr);
//
//		//System.out.println(date);
//
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(date);
//		String formatedDate = cal.get(Calendar.DATE) + "/" + (cal.get(Calendar.MONTH) + 1) + "/"
//				+ cal.get(Calendar.YEAR);
//		//System.out.println("formatedDate : " + formatedDate);
//
//		Date res = new Date(formatedDate + "");
//
//		return res;
//	}
//
//	private void setVisibilityDependency(AbstractComponent component, String dependingOnId,
//			Object[] dependingOnValues) {
//		Field<?> dependingOnField = fields.get(dependingOnId);
//		List<Object> dependingOnValuesList = Arrays.asList(dependingOnValues);
//
//		if (dependingOnField == null) {
//			return;
//		}
////fieldValueMatchesDependingOnValuesNOTValuer
//		if (dependingOnValuesList.stream().anyMatch(v -> v.toString().contains("!"))) {
//
//			// hide on default
//			component.setVisible(dependingOnValuesList.stream().anyMatch(
//					v -> fieldValueMatchesDependingOnValuesNOTValuer(dependingOnField, dependingOnValuesList)));
//
//			// check value and determine if to hide or show
//			dependingOnField.addValueChangeListener(e -> {
//				boolean visible = fieldValueMatchesDependingOnValuesNOTValuer(dependingOnField, dependingOnValuesList);
//
//				component.setVisible(visible);
//				if (component instanceof Field) {
//					if (!visible) {
//						((Field<?>) component).setValue(null);
//					}
//				}
//			});
//		} else {
//
//			// hide on default
//			component.setVisible(dependingOnValuesList.stream()
//					.anyMatch(v -> fieldValueMatchesDependingOnValues(dependingOnField, dependingOnValuesList)));
//
//			// check value and determine if to hide or show
//			dependingOnField.addValueChangeListener(e -> {
//				boolean visible = fieldValueMatchesDependingOnValues(dependingOnField, dependingOnValuesList);
//
//				component.setVisible(visible);
//				if (component instanceof Field) {
//					if (!visible) {
//						((Field<?>) component).setValue(null);
//					}
//				}
//			});
//		}
//	}
//
//	private boolean fieldValueMatchesDependingOnValues(Field<?> dependingOnField, List<Object> dependingOnValuesList) {
//		if (dependingOnField.getValue() == null) {
//			return false;
//		}
//
////		if (dependingOnField instanceof NullableOptionGroup) {
//////			String booleanValue = Boolean.TRUE.equals(((NullableOptionGroup) dependingOnField).getNullableValue())
//////					? "true"
//////					: "false";
////			String stringValue = Boolean.TRUE.equals(((NullableOptionGroup) dependingOnField).getNullableValue())
////					? "Yes"
////					: "No";
////
////			return dependingOnValuesList.stream().anyMatch(
////					v -> 
////					//v.toString().equalsIgnoreCase(booleanValue) || 
////					v.toString().equalsIgnoreCase(stringValue));
////		} else {
//
//			return dependingOnValuesList.stream()
//					.anyMatch(v -> v.toString().equalsIgnoreCase(dependingOnField.getValue().toString()));
//	//	}
//	}
//
//	private boolean fieldValueMatchesDependingOnValuesNOTValuer(Field<?> dependingOnField,
//			List<Object> dependingOnValuesList) {
//		if (dependingOnField.getValue() == null) {
//			return false;
//		}
//
//		if (dependingOnField instanceof NullableOptionGroup) {
//			String booleanValue = Boolean.TRUE.equals(((NullableOptionGroup) dependingOnField).getNullableValue())
//					? "false"
//					: "true";
//			String stringValue = Boolean.TRUE.equals(((NullableOptionGroup) dependingOnField).getNullableValue()) ? "no"
//					: "yes";
//
//			return dependingOnValuesList.stream()
//					.anyMatch(v -> v.toString().replaceAll("!", "").equalsIgnoreCase(booleanValue)
//							|| v.toString().replaceAll("!", "").equalsIgnoreCase(stringValue));
//		} else {
//
//			return dependingOnValuesList.stream().anyMatch(
//					v -> !v.toString().replaceAll("!", "").equalsIgnoreCase(dependingOnField.getValue().toString()));
//		}
//	}
//
//	public String get18nCaption(String elementId, String defaultCaption) {
//		if (userTranslations != null && userTranslations.containsKey(elementId)) {
//			return userTranslations.get(elementId);
//		}
//
//		return defaultCaption;
//	}
//
//	public List<CampaignFormDataEntry> getFormValues() {
//		return fields.keySet().stream().map(id -> {
//			Field<?> field = fields.get(id);
//			if (field instanceof NullableOptionGroup) {
//				
//				  String valc = field.getValue() != null ? field.getValue().toString().equalsIgnoreCase("true") ||field.getValue().toString().equalsIgnoreCase("YES") || field.getValue().toString().equalsIgnoreCase("[YES]") || field.getValue().toString().equalsIgnoreCase("[true]") ? "Yes" :
//						field.getValue().toString().equalsIgnoreCase("[NO]") || field.getValue().toString().equalsIgnoreCase("NO") || field.getValue().toString().equalsIgnoreCase("false") || field.getValue().toString().equalsIgnoreCase("[false]") ? "No" : null : null;
//				 
//				return new CampaignFormDataEntry(id, valc);
//							//	: field.getValue());
//
//				// return new CampaignFormDataEntry(id, ((NullableOptionGroup)
//				// field).getNullableValue());
//			} /*
//				 * else if (field instanceof DateField) {
//				 * 
//				 * System.out.println("----xx: "+field.getValue());
//				 * 
//				 * // Sys The number you entered is not valid.
//				 * println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ field is date"
//				 * + dateFormat.format(((DateField)
//				 * field).getDateFormat().format(field.getValue()+"", null))); return new
//				 * CampaignFormDataEntry(id, ((DateField)
//				 * field).getDateFormat().format(field.getValue()+"", null)); }
//				 */ else {
//				return new CampaignFormDataEntry(id, field.getValue());
//			}
//		}).collect(Collectors.toList());
//	}
//
////let change this method to litrate through all the field, check the validity, and return ;ist of those that are not valid in a catch block
//	public void validateFields() {
//		try {
//			fields.forEach((key, value) -> {
//
//				Field formField = fields.get(key);
//
//				if (!fields.get(key).isValid()) {
//					fields.get(key).setRequiredError("Error found");
//				}
//			});
//		} finally {
//
//			fields.forEach((key, value) -> {
//
//				Field formField = fields.get(key);
//				try {
//
//					formField.validate();
//
//				} catch (Validator.InvalidValueException e) {
//
//					throw (InvalidValueException) e;
//				}
//			});
//		}
//
//	}
//
//	public void resetFormValues() {
//
//		fields.keySet().forEach(key -> {
//			Field<?> field = fields.get(key);
//			((Field<Object>) field).setValue(formValuesMap.get(key));
//		});
//	}
//
//	public List<CampaignFormElement> getFormElements() {
//		return formElements;
//	}
//
//	public Map<String, Field<?>> getFields() {
//		return fields;
//	}

}
