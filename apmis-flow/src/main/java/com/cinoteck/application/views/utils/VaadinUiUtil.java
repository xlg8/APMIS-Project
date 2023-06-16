
package com.cinoteck.application.views.utils;

import java.util.function.Consumer;
import java.util.function.Function;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;

public final class VaadinUiUtil {

	private VaadinUiUtil() {
		// Hide Utility Class Constructor
	}

	public static Dialog createPopupWindow() {
		Dialog window = new Dialog(null);
		window.setModal(true);
		window.setSizeUndefined();
		window.setResizable(false);
//		window .center();

		return window;
	}

	public static VerticalLayout showDynamicPopup(String caption, String contentText) {
		return showDynamicPopup(caption, contentText, JustifyContentMode.CENTER);
	}

	public static VerticalLayout showDynamicPopup(String caption, String contentText, JustifyContentMode contentMode) {
		return showDynamicPopup(caption, contentText, contentMode, null);
	}
	
	
	 public static VerticalLayout showDynamicPopup(String caption, String contentText, JustifyContentMode contentMode, Integer width) {
	        Dialog dialog = new Dialog();
	        VerticalLayout popupLayout = new VerticalLayout();

	        dialog.setHeaderTitle(caption);
	        dialog.add(contentText);

	        Button deleteButton = new Button(new Icon(VaadinIcon.CHECK), (e) -> dialog.close());
	        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
	                ButtonVariant.LUMO_ERROR);
	        deleteButton.getStyle().set("flex", "auto");
	        dialog.getFooter().add(deleteButton);

	        Button cancelButton = new Button("Cancel", (e) -> dialog.close());
	        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
	        dialog.getFooter().add(cancelButton);

	        Button button = new Button("Show dialog", e -> dialog.open());
	        popupLayout.add(dialog, button);
	        
	       return popupLayout;
	    }
	 
		public static void showDynamicPopup(String caption, Label label, String caption2, String caption3, int i,
				Object object) {

			
			
			
		}
		
		
//
//	public static Dialog showSimplePopupWindow(String caption, String contentText, JustifyContentMode contentMode, Integer width) {
//		Dialog window = new Dialog(null);
//		window.setModal(true);
//		window.setSizeUndefined();
//		window.setResizable(false);
//
//		if (width != null) {
//			window.setWidth(width, Unit.PIXELS);
//		}
//
//		//window.center();
//
//		VerticalLayout popupLayout = new VerticalLayout();
//		popupLayout.setMargin(true);
//		popupLayout.setSpacing(true);
//		popupLayout.setSizeUndefined();
//		Label contentLabel = new Label(contentText);
//		contentLabel.setWidth(100, Unit.PERCENTAGE);
//		popupLayout.add(contentLabel);
//		
//		Button closeButton = new Button(new Icon(VaadinIcon.CHECK), (e) -> window.close());
//		closeButton.addThemeVariants(ButtonVariant.LUMO_ICON);
//		//closeButton.setAriaLabel("Close");
//		closeButton.setTooltipText("Close the dialog");
//		
//		
//		window.getFooter().add(closeButton);
//		
//		
//		
//		popupLayout.add(okayButton);
//		popupLayout.setComponentAlignment(okayButton, Alignment.BOTTOM_RIGHT);
//
//		window.setCaption(caption);
//		window.setContent(popupLayout);
//
//		UI.getCurrent().addWindow(window);
//
//		return window;
//	}
//
//	public static Window showPopupWindow(Component content) {
//
//		return showPopupWindow(content, null);
//	}
//
//	public static Window showPopupWindow(Component content, String caption) {
//
//		Window window = new Window(caption);
//		window.setModal(true);
//		window.setSizeUndefined();
//		window.setResizable(false);
//		window.center();
//		window.setContent(content);
//
//		UI.getCurrent().addWindow(window);
//
//		return window;
//	}
//
//	public static Window showModalPopupWindow(CommitDiscardWrapperComponent<?> content, String caption) {
//		final Window popupWindow = VaadinUiUtil.showPopupWindow(content);
//		popupWindow.setCaption(caption);
//		content.setMargin(true);
//
//		content.addDoneListener(popupWindow::close);
//
//		return popupWindow;
//	}
//
//	@SuppressWarnings("serial")
//	public static void addIconColumn(GeneratedPropertyContainer container, String iconPropertyId, VaadinIcons vaadinIconsIcon) {
//
//		container.addGeneratedProperty(iconPropertyId, new PropertyValueGenerator<String>() {
//
//			@Override
//			public String getValue(Item item, Object itemId, Object propertyId) {
//				return vaadinIconsIcon.getHtml();
//			}
//
//			@Override
//			public Class<String> getType() {
//				return String.class;
//			}
//		});
//	}
//
//	public static void setupEditColumn(Grid.Column column) {
//		column.setRenderer(new HtmlRenderer());
//		column.setWidth(20);
//		column.setSortable(false);
//		column.setHeaderCaption("");
//	}
//
//	public static Window showConfirmationPopup(
//		String caption,
//		Component content,
//		String confirmCaption,
//		String cancelCaption,
//		Integer width,
//		Consumer<Boolean> resultConsumer) {
//
//		return showConfirmationPopup(caption, content, popupWindow -> {
//			ConfirmationComponent confirmationComponent = new ConfirmationComponent(false) {
//
//				private static final long serialVersionUID = 1L;
//
//				@Override
//				protected void onConfirm() {
//					resultConsumer.accept(true);
//					popupWindow.close();
//				}
//
//				@Override
//				protected void onCancel() {
//					resultConsumer.accept(false);
//					popupWindow.close();
//				}
//			};
//
//			confirmationComponent.getConfirmButton().setCaption(confirmCaption);
//			confirmationComponent.getCancelButton().setCaption(cancelCaption);
//
//			return confirmationComponent;
//		}, width);
//	}
//
//	public static Window showConfirmationPopup(
//		String caption,
//		Component content,
//		Function<Window, ConfirmationComponent> confirmationComponentProvider,
//		Integer width) {
//
//		Window popupWindow = VaadinUiUtil.createPopupWindow();
//		if (width != null) {
//			popupWindow.setWidth(width, Unit.PIXELS);
//		} else {
//			popupWindow.setWidthUndefined();
//		}
//		popupWindow.setCaption(caption);
//
//		VerticalLayout layout = new VerticalLayout();
//		layout.setMargin(true);
//		content.setWidth(100, Unit.PERCENTAGE);
//		layout.addComponent(content);
//
//		ConfirmationComponent confirmationComponent = confirmationComponentProvider.apply(popupWindow);
//
//		layout.addComponent(confirmationComponent);
//		layout.setComponentAlignment(confirmationComponent, Alignment.BOTTOM_RIGHT);
//		layout.setWidth(100, Unit.PERCENTAGE);
//		layout.setSpacing(true);
//
//		popupWindow.setContent(layout);
//		popupWindow.setClosable(false);
//
//		UI.getCurrent().addWindow(popupWindow);
//
//		return popupWindow;
//	}
//
//	/**
//	 * @param resultConsumer
//	 *            TRUE: Option A, FALSE: Option B
//	 */
//	public static Window showChooseOptionPopup(
//		String caption,
//		Component content,
//		String optionACaption,
//		String optionBCaption,
//		Integer width,
//		Consumer<Boolean> resultConsumer) {
//
//		Window popupWindow = VaadinUiUtil.createPopupWindow();
//		if (width != null) {
//			popupWindow.setWidth(width, Unit.PIXELS);
//		} else {
//			popupWindow.setWidthUndefined();
//		}
//		popupWindow.setCaption(caption);
//
//		VerticalLayout layout = new VerticalLayout();
//		layout.setMargin(true);
//		content.setWidth(100, Unit.PERCENTAGE);
//		layout.addComponent(content);
//
//		ConfirmationComponent confirmationComponent = new ConfirmationComponent(false) {
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			protected void onConfirm() {
//				resultConsumer.accept(true);
//				popupWindow.close();
//			}
//
//			@Override
//			protected void onCancel() {
//				resultConsumer.accept(false);
//				popupWindow.close();
//			}
//		};
//		confirmationComponent.getConfirmButton().setCaption(optionACaption);
//		Button cancelButton = confirmationComponent.getCancelButton();
//		cancelButton.setCaption(optionBCaption);
//		cancelButton.removeStyleName(ValoTheme.BUTTON_LINK);
//		cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
//
//		layout.addComponent(confirmationComponent);
//		layout.setComponentAlignment(confirmationComponent, Alignment.BOTTOM_RIGHT);
//		layout.setWidth(100, Unit.PERCENTAGE);
//		layout.setSpacing(true);
//		popupWindow.setContent(layout);
//		popupWindow.setClosable(false);
//
//		UI.getCurrent().addWindow(popupWindow);
//		return popupWindow;
//	}
//
//	public static Window showDeleteConfirmationWindow(String content, Runnable callback) {
//		Window popupWindow = VaadinUiUtil.createPopupWindow();
//
//		VerticalLayout deleteLayout = new VerticalLayout();
//		deleteLayout.setMargin(true);
//		deleteLayout.setSizeUndefined();
//		deleteLayout.setSpacing(true);
//
//		Label description = new Label(content);
//		description.setWidth(100, Unit.PERCENTAGE);
//		deleteLayout.addComponent(description);
//
//		ConfirmationComponent deleteConfirmationComponent = new ConfirmationComponent(false) {
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			protected void onConfirm() {
//				popupWindow.close();
//				onDone();
//				callback.run();
//			}
//
//			@Override
//			protected void onCancel() {
//				popupWindow.close();
//			}
//		};
//		deleteConfirmationComponent.getConfirmButton().setCaption(I18nProperties.getString(Strings.yes));
//		deleteConfirmationComponent.getCancelButton().setCaption(I18nProperties.getString(Strings.no));
//		deleteLayout.addComponent(deleteConfirmationComponent);
//		deleteLayout.setComponentAlignment(deleteConfirmationComponent, Alignment.BOTTOM_RIGHT);
//
//		popupWindow.setCaption(I18nProperties.getString(Strings.headingConfirmDeletion));
//		popupWindow.setContent(deleteLayout);
//		UI.getCurrent().addWindow(popupWindow);
//
//		return popupWindow;
//	}
//
//	//publish 
//	
//	public static Window showPublishConfirmationWindow(String content, Runnable callback) {
//		Window popupWindow = VaadinUiUtil.createPopupWindow();
//
//		VerticalLayout publishLayout = new VerticalLayout();
//		publishLayout.setMargin(true);
//		publishLayout.setSizeUndefined();
//		publishLayout.setSpacing(true);
//
//		Label description = new Label(content);
//		description.setWidth(100, Unit.PERCENTAGE);
//		publishLayout.addComponent(description);
//
//		ConfirmationComponent publishConfirmationComponent = new ConfirmationComponent(false) {
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			protected void onConfirm() {
//				popupWindow.close();
//				onDone();
//				callback.run();
//			}
//
//			@Override
//			protected void onCancel() {
//				popupWindow.close();
//			}
//		};
//		publishConfirmationComponent.getConfirmButton().setCaption(I18nProperties.getString(Strings.yes));
//		publishConfirmationComponent.getCancelButton().setCaption(I18nProperties.getString(Strings.no));
//		publishLayout.addComponent(publishConfirmationComponent);
//		publishLayout.setComponentAlignment(publishConfirmationComponent, Alignment.BOTTOM_RIGHT);
//
//		popupWindow.setCaption(I18nProperties.getString(Strings.headingConfirmPublish));
//		popupWindow.setContent(publishLayout);
//		UI.getCurrent().addWindow(popupWindow);
//
//		return popupWindow;
//	}
//	
//	public static Window showUnPublishConfirmationWindow(String content, Runnable callback) {
//		Window popupWindow = VaadinUiUtil.createPopupWindow();
//
//		VerticalLayout unpublishLayout = new VerticalLayout();
//		unpublishLayout.setMargin(true);
//		unpublishLayout.setSizeUndefined();
//		unpublishLayout.setSpacing(true);
//
//		Label description = new Label(content);
//		description.setWidth(100, Unit.PERCENTAGE);
//		unpublishLayout.addComponent(description);
//
//		ConfirmationComponent unpublishConfirmationComponent = new ConfirmationComponent(false) {
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			protected void onConfirm() {
//				popupWindow.close();
//				onDone();
//				callback.run();
//			}
//
//			@Override
//			protected void onCancel() {
//				popupWindow.close();
//			}
//		};
//		unpublishConfirmationComponent.getConfirmButton().setCaption(I18nProperties.getString(Strings.yes));
//		unpublishConfirmationComponent.getCancelButton().setCaption(I18nProperties.getString(Strings.no));
//		unpublishLayout.addComponent(unpublishConfirmationComponent);
//		unpublishLayout.setComponentAlignment(unpublishConfirmationComponent, Alignment.BOTTOM_RIGHT);
//
//		popupWindow.setCaption(I18nProperties.getString(Strings.headingConfirmUnPublish));
//		popupWindow.setContent(unpublishLayout);
//		UI.getCurrent().addWindow(popupWindow);
//
//		return popupWindow;
//	}
//	
//	
//	public static ConfirmationComponent buildYesNoConfirmationComponent() {
//		ConfirmationComponent requestTaskComponent = new ConfirmationComponent(false) {
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			protected void onConfirm() {
//			}
//
//			@Override
//			protected void onCancel() {
//			}
//		};
//		requestTaskComponent.getConfirmButton().setCaption(I18nProperties.getString(Strings.yes));
//		requestTaskComponent.getCancelButton().setCaption(I18nProperties.getString(Strings.no));
//		return requestTaskComponent;
//	}
//
//	public static HorizontalLayout createInfoComponent(String htmlContent) {
//		return createIconComponent(htmlContent, "img/info-icon.png", 35);
//
//	}
//
//	public static HorizontalLayout createWarningComponent(String htmlContent) {
//		return createWarningComponent(htmlContent, 35);
//	}
//
//	public static HorizontalLayout createWarningComponent(String htmlContent, int iconSize) {
//		return createIconComponent(htmlContent, "img/warning-icon.png", iconSize);
//
//	}
//
//	public static HorizontalLayout createErrorComponent(String htmlContent) {
//		return createErrorComponent(htmlContent, 35);
//	}
//
//	public static HorizontalLayout createErrorComponent(String htmlContent, int iconSize) {
//		return createIconComponent(htmlContent, "img/error-icon.png", iconSize);
//
//	}
//
//	public static HorizontalLayout createIconComponent(String htmlContent, String iconName, int iconSize) {
//		HorizontalLayout infoLayout = new HorizontalLayout();
//		infoLayout.setWidth(100, Unit.PERCENTAGE);
//		infoLayout.setSpacing(true);
//		Image icon = new Image(null, new ThemeResource(iconName));
//		icon.setHeight(iconSize, Unit.PIXELS);
//		icon.setWidth(iconSize, Unit.PIXELS);
//		infoLayout.addComponent(icon);
//		infoLayout.setComponentAlignment(icon, Alignment.MIDDLE_LEFT);
//		Label infoLabel = new Label(htmlContent, ContentMode.HTML);
//		infoLabel.setWidth(100, Unit.PERCENTAGE);
//		infoLayout.addComponent(infoLabel);
//		infoLayout.setExpandRatio(infoLabel, 1);
//		CssStyles.style(infoLayout, CssStyles.VSPACE_3);
//		return infoLayout;
//	}
//
//	public static VerticalLayout createWarningLayout() {
//		VerticalLayout warningLayout = new VerticalLayout();
//		warningLayout.setMargin(true);
//		Image warningIcon = new Image(null, new ThemeResource("img/warning-icon.png"));
//		warningIcon.setHeight(35, Unit.PIXELS);
//		warningIcon.setWidth(35, Unit.PIXELS);
//		warningLayout.addComponentAsFirst(warningIcon);
//		CssStyles.style(warningLayout, CssStyles.ALIGN_CENTER);
//		return warningLayout;
//	}
//
//	public static void showWarningPopup(String message) {
//		VerticalLayout warningLayout = createWarningLayout();
//		Window popupWindow = VaadinUiUtil.showPopupWindow(warningLayout);
//		Label infoLabel = new Label(message);
//		CssStyles.style(infoLabel, CssStyles.LABEL_LARGE, CssStyles.LABEL_WHITE_SPACE_NORMAL);
//		warningLayout.addComponent(infoLabel);
//		popupWindow.addCloseListener(e -> popupWindow.close());
//		popupWindow.setWidth(400, Unit.PIXELS);
//	}


}
