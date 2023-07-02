package com.cinoteck.application.views.testview;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;

//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.ClickEvent;
//import com.vaadin.flow.component.ComponentEventListener;
//import com.vaadin.flow.component.HasEnabled;
//import com.vaadin.flow.component.grid.Grid;
//import com.vaadin.flow.component.grid.Grid.Column;
//import com.vaadin.flow.component.grid.GridVariant;
//import com.vaadin.flow.component.html.Label;
//import com.vaadin.flow.component.icon.VaadinIcon;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.data.binder.BeanValidationBinder;
//import com.vaadin.flow.data.binder.Binder;
//import com.vaadin.flow.data.provider.DataCommunicator;
//import com.vaadin.flow.data.provider.ListDataProvider;
//import com.vaadin.flow.router.Route;
//
//import de.symeda.sormas.api.i18n.Captions;
//import de.symeda.sormas.api.i18n.I18nProperties;
//import de.symeda.sormas.api.i18n.Strings;
//
//public abstract class AbstractEditableGridx<T> extends VerticalLayout {
//
//	public static final String DELETE = "delete";
//
//	protected List<T> savedItems;
//	protected List<T> allItems;
//	protected Grid<T> grid;
//	protected Binder<T> binder;
//	protected Button addButton;
//
//	public AbstractEditableGridx(List<T> savedItems, List<T> allItems) {
//
//		setSizeFull();
//
//		this.savedItems = new ArrayList<>(savedItems);
//		this.allItems = new ArrayList<>(allItems);
//
//		grid = new Grid<>();
//		binder = new BeanValidationBinder<>(getBeanType());
//		addButton = new Button("Add", event -> addNewRow());
//
//		final HorizontalLayout gridLayout = new HorizontalLayout();
//		gridLayout.setMargin(false);
//		gridLayout.setSpacing(true);
//		gridLayout.setWidthFull();
//
//		grid.setDataProvider(new ListDataProvider<>(this.savedItems));
//		grid.setSelectionMode(Grid.SelectionMode.NONE);
//		grid.addThemeVariants(GridVariant.LUMO_COMPACT);
//
//		add(gridLayout);
//
//		gridLayout.add(grid);
//		gridLayout.expand(grid);
//
//		grid.setWidthFull();
//
//		grid.setId("formidx");
//
//		grid.getColumns().forEach(col -> {
//			col.setSortable(false);
//		});
//
//		grid.addItemClickListener(e -> {
//			final List<T> items = getItems();
//			int i = items.indexOf(e.getItem());
//
//			if (e.getColumn() != null && DELETE.equals(e.getColumn().getKey())) {
////				showGridRowRemoveConfirmation(items, i);
//			} else if (i > -1) {
//				grid.getEditor().editItem(e.getItem());
//			}
//		});
//
//		grid.getEditor().setBinder(binder);
//		grid.getEditor().setBuffered(true);
////		((HasEnabled) grid.getEditor()).setEnabled(true);
//
//		grid.getEditor().addSaveListener(e -> {
//			final List<T> items = getItems();
//			grid.setItems(items);
//		});
//
//		final HorizontalLayout buttonLayout = new HorizontalLayout();
//		buttonLayout.setMargin(false);
//		buttonLayout.setSpacing(true);
//		buttonLayout.setWidthFull();
//		final Button additionalRow = createButton(getAdditionalRowCaption(), newRowEvent());
////		buttonLayout.add(additionalRow);
//		buttonLayout.setJustifyContentMode(JustifyContentMode.END);
//		add(buttonLayout);
//
//		setFlexGrow(1, gridLayout);
//		setFlexGrow(0, buttonLayout);
//	}
//
//	protected abstract Button createButton(String additionalRowCaption,
//			ComponentEventListener<ClickEvent<Button>> newRowEvent);
//
//	public void discardGrid() {
//		this.grid.setItems(new ArrayList<>(this.savedItems));
//		reorderGrid();
//	}
//
//	protected abstract Class<T> getBeanType();
//
//	protected abstract void configureColumns();
//
//	protected abstract void configureBindings();
//
//	protected abstract T createNewItem();
//
//	protected abstract void reorderGrid();
//
//	protected abstract String getHeaderString();
//
//	protected abstract String getAdditionalRowCaption();
//
//	protected abstract ComponentEventListener<ClickEvent<Button>> newRowEvent();
//
//	protected abstract Binder<T> addColumnsBinder(List<T> allElements);
//
//	public void setSavedItems(List<T> savedItems) {
//		this.savedItems = savedItems;
//		this.grid.setItems(new ArrayList<>(savedItems));
//	}
//
//	@SuppressWarnings("unchecked")
//	public ArrayList<T> getItems() {
//		return (ArrayList<T>) ((ListDataProvider<T>) ((DataCommunicator<T>) ((Collection<?>) this.grid
//				.getListDataView()).iterator().next()).getDataProvider()).getItems();
//	}
//
//	protected void addNewRow() {
//		T newItem = createNewItem();
//		savedItems.add(newItem);
//		grid.getDataProvider().refreshAll();
//	}
//}

public abstract class AbstractEditableGridx<T> extends Composite<Div> {

	private static final String HEADING_LOC = "headingLoc";
	private static final String GRID_LOC = "gridLoc";
	private static final String ADDITIONAL_ROW_LOC = "additionalRowLoc";

	private static final String HTML_LAYOUT = "<div id='" + HEADING_LOC + "'></div><div id='" + GRID_LOC
			+ "'></div><div id='" + ADDITIONAL_ROW_LOC + "'></div>";
	public static final String DELETE = "delete";

	protected Grid<T> grid = new Grid<>();
	protected List<T> savedItems = new ArrayList<>();

	public AbstractEditableGridx(List<T> savedElements, List<T> allElements) {
		getContent().getElement().setProperty("innerHTML", HTML_LAYOUT);

//        setSizeFull();

		final HorizontalLayout headingLayout = new HorizontalLayout();
		headingLayout.setSpacing(true);
		headingLayout.setWidthFull();
//        headingLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
//        headingLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		getContent().getElement().appendChild(headingLayout.getElement());

		final Label headingLabel = new Label(getHeaderString());
		headingLabel.addClassName("h3");
		headingLayout.add(headingLabel);

		final HorizontalLayout gridLayout = new HorizontalLayout();
		gridLayout.setSpacing(true);
		gridLayout.setWidthFull();
		getContent().getElement().appendChild(gridLayout.getElement());

		gridLayout.add(grid);
		gridLayout.setFlexGrow(1, grid);

		grid.setHeightByRows(true);
		grid.setWidthFull();

		savedItems.addAll(savedElements);
		grid.setItems(new ArrayList<>(savedElements));
		reorderGrid();
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setId("formidx");

//        final GridRowDragger<T> gridRowDragger = new GridRowDragger<>(grid);
//        gridRowDragger.getGridDragSource().addGridDragEndListener(gridDragEndListener());

		final Binder<T> binder = addColumnsBinder(allElements);

//        Grid.Column<T> deleteColumn = grid.addColumn(ComponentRenderer.<T>create(item -> createDeleteButton(item))).setKey(DELETE).setHeader(I18nProperties.getCaption(Captions.remove));

		grid.getColumns().forEach(col -> col.setSortable(false));

		grid.addItemClickListener(e -> {
			final List<T> items = getItems();
			int i = items.indexOf(e.getItem());

//            if (e.getColumn() != null && DELETE.equals(e.getColumn().getKey())) {
//                showGridRowRemoveConfirmation(items, i);
//            } else
			if (i > -1) {
				grid.getEditor().editItem(e.getItem());
			}
		});

		grid.getEditor().setBinder(binder);
		grid.getEditor().setBuffered(true);
//        grid.getEditor().setEnabled(true);

		grid.getEditor().addSaveListener(e -> {
			final List<T> items = getItems();
			grid.setItems(items);
		});

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setWidthFull();
		final Button additionalRow = createButton(getAdditionalRowCaption(), newRowEvent());
//		buttonLayout.add(additionalRow);
//        buttonLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END, additionalRow);
		getContent().getElement().appendChild(buttonLayout.getElement());
	}

	
//    protected GridDragEndListener<T> gridDragEndListener() {
//        return gridDragEndEvent -> reorderGrid();
//    }

	private Button createButton(String additionalRowCaption, ComponentEventListener<ClickEvent<Button>> newRowEvent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void discardGrid() {
		this.grid.setItems(new ArrayList<>(this.savedItems));
		reorderGrid();
	}

//    private void showGridRowRemoveConfirmation(List<T> items, int index) {
//        Dialog confirmationDialog = new Dialog(
//                I18nProperties.getString(Strings.confirmationRemoveGridRowTitle),
//                I18nProperties.getString(Strings.confirmationRemoveGridRowMessage),
//                I18nProperties.getString(Strings.confirmationRemoveGridRowConfirm),
//                I18nProperties.getString(Strings.confirmationRemoveGridRowCancel)
//        );
//        confirmationDialog.open();
////        confirmationDialog.addDialogCloseActionListener(event -> {
////            if (1=1) {
////                items.remove(index);
////                grid.setItems(items);
////            }
////        });
//    }
	
	protected abstract ComponentEventListener<ClickEvent<Button>> newRowEvent();


	protected abstract void reorderGrid();

	protected abstract String getHeaderString();

	protected abstract String getAdditionalRowCaption();

//    protected abstract Button.ClickListener newRowEvent();

	protected abstract Binder<T> addColumnsBinder(List<T> allElements);

	public void setSavedItems(List<T> savedItems) {
		this.savedItems = savedItems;
		this.grid.setItems(new ArrayList<>(savedItems));
	}

	public ArrayList<T> getItems() {
		return new ArrayList<>(((ListDataProvider<T>) grid.getDataProvider()).getItems());
	}
}
