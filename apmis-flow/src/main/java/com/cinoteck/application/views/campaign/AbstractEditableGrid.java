package com.cinoteck.application.views.campaign;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;

//public abstract class AbstractEditableGrid<T> extends VerticalLayout {
//
//	public static final String DELETE = "delete";
//
//	protected Grid<T> grid = new Grid<>();
//	protected List<T> savedItems = new ArrayList<>();
//
//	public AbstractEditableGrid(List<T> savedElements, List<T> allElements) {
//
//		setSizeFull();
//		addClassName("editable-grid");
//
//		final HorizontalLayout gridLayout = new HorizontalLayout();
//		gridLayout.setMargin(false);
//		gridLayout.setSpacing(true);
//		gridLayout.setWidthFull();
//	add(gridLayout);
//
//		gridLayout.add(grid);
//		gridLayout.expand(grid);
//
//		grid.setWidthFull();
//
//		savedItems.addAll(savedElements);
//		grid.setItems(new ArrayList<>(savedElements));
//		reorderGrid();
//		grid.setSelectionMode(Grid.SelectionMode.NONE);
//		grid.setId("formidx");
//
////		final GridRowDragger<T> gridRowDragger = new GridRowDragger<>(grid);
////		gridRowDragger.getGridDragSource().addGridDragEndListener(gridDragEndListener());
//
//		final Binder<T> binder = addColumnsBinder(allElements);
//
//		Grid.Column<T> deleteColumn = grid.addColumn(new ComponentRenderer<>(item -> {
//			if (item != null) {
//				return VaadinIcon.TRASH.create();
//			}
//			return VaadinIcon.TRASH.create();
//		})).setKey(DELETE).setHeader(I18nProperties.getCaption(Captions.remove)).setWidth("50px").setFlexGrow(0)
//				.setResizable(true).setSortable(true);
//
//		grid.getColumns().forEach(col -> {
//			col.setSortable(true);
//		});
//
//		grid.addItemClickListener(e -> {
//			final List<T> items = getItems();
//			int i = items.indexOf(e.getItem());
//
//			if (e.getColumn() != null && DELETE.equals(e.getColumn().getKey())) {
//				showGridRowRemoveConfirmation(items, i);
//			} else if (i > -1) {
//				grid.getEditor().editItem(e.getItem());
//			}
//		});
//
//		grid.getEditor().setBinder(binder);
//		grid.getEditor().setBuffered(true);
////		grid.getEditor().addEditableRenderer();
////
////		grid.getEditor().setEnabled(true);
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
//		Button additionalRow = new Button("Add Form");
//
//		additionalRow = createButton(newRowEvent());
//
//		buttonLayout.setJustifyContentMode(JustifyContentMode.END);
//		add(buttonLayout);
//
//
//	}
//	
//	   // Method to add a new row to the grid
//    protected void addNewRow() {
//        T newItem = createNewItem(); // Implement this method to create a new item of type T
//        grid.getEditor().cancel();
//        grid.getEditor().editItem(newItem);
//        grid.getEditor().setEnabled(true);
//    }
//    
//  
//
//
//
//	protected abstract Button createButton(ComponentEventListener<ClickEvent<Button>> newRowEvent);
//
//	public void discardGrid() {
//		this.grid.setItems(new ArrayList<>(this.savedItems));
//		reorderGrid();
//	}
//
//	private void showGridRowRemoveConfirmation(List<T> items, int index) {
//
//		Dialog confirmationDialog = new Dialog();
//		confirmationDialog.setWidth("400px");
//		confirmationDialog.setResizable(false);
//		confirmationDialog.setModal(true);
//		confirmationDialog.setCloseOnEsc(true);
//		confirmationDialog.setCloseOnOutsideClick(false);
//
//		Label confirmationMessage = new Label(I18nProperties.getString(Strings.confirmationRemoveGridRowMessage));
//
//		Button confirmButton = new Button(I18nProperties.getString(Strings.confirmationRemoveGridRowConfirm), event -> {
//			items.remove(index);
//			grid.setItems(items);
//			confirmationDialog.close();
//		});
//
//		Button cancelButton = new Button(I18nProperties.getString(Strings.confirmationRemoveGridRowCancel),
//				event -> confirmationDialog.close());
//
//		VerticalLayout confirmationLayout = new VerticalLayout(confirmationMessage, confirmButton, cancelButton);
//		confirmationDialog.add(confirmationLayout);
//
//		confirmationDialog.open();
//
//
//		VerticalLayout confirmationLayout1 = new VerticalLayout(
//				new Label(I18nProperties.getString(Strings.confirmationRemoveGridRowMessage)), confirmationDialog);
//		Dialog confirmationPopup = new Dialog();
//		confirmationPopup.setWidth("400px");
//		confirmationPopup.setHeight("auto");
//		confirmationPopup.setResizable(false);
//		confirmationPopup.setModal(true);
//		confirmationPopup.setCloseOnEsc(true);
//		confirmationPopup.setCloseOnOutsideClick(false);
//
//		getUI().ifPresent(ui -> ui.add(confirmationPopup));
//	}
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
//   
//
//	public void setSavedItems(List<T> savedItems) {
//		this.savedItems = savedItems;
//		this.grid.setItems(new ArrayList<>(savedItems));
//	}
//
//	@SuppressWarnings("unchecked")
//	public ArrayList<T> getItems() {
//		return (ArrayList<T>) grid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
//	}
//}


public abstract class AbstractEditableGrid<T> extends VerticalLayout {

    public static final String DELETE = "delete";

    protected Grid<T> grid = new Grid<>();
    protected List<T> savedItems = new ArrayList<>();

    public AbstractEditableGrid(List<T> savedElements, List<T> allElements) {
        setSizeFull();
        addClassName("editable-grid");

        HorizontalLayout gridLayout = new HorizontalLayout();
        gridLayout.setMargin(false);
        gridLayout.setSpacing(true);
        gridLayout.setWidthFull();
        add(gridLayout);

        gridLayout.add(grid);
        gridLayout.expand(grid);

        grid.setWidthFull();

        savedItems.addAll(savedElements);
        grid.setItems(new ArrayList<>(savedElements));
        reorderGrid();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setId("formidx");

        final Binder<T> binder = addColumnsBinder(allElements);

        Grid.Column<T> deleteColumn = grid.addColumn(new ComponentRenderer<>(item -> VaadinIcon.TRASH.create()))
                .setKey(DELETE)
                .setHeader(I18nProperties.getCaption(Captions.remove))
                .setWidth("50px")
                .setFlexGrow(0)
                .setResizable(true)
                .setSortable(true);

        grid.getColumns().forEach(col -> col.setSortable(true));

        grid.addItemClickListener(e -> {
            List<T> items = getItems();
            int index = items.indexOf(e.getItem());

            if (e.getColumn() != null && DELETE.equals(e.getColumn().getKey())) {
                showGridRowRemoveConfirmation(items, index);
            } else if (index > -1) {
                grid.getEditor().editItem(e.getItem());
            }
        });

        grid.getEditor().setBinder(binder);
        grid.getEditor().setBuffered(true);

        grid.getEditor().addSaveListener(e -> {
            List<T> items = getItems();
            grid.setItems(items);
        });

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setMargin(false);
        buttonLayout.setSpacing(true);
        buttonLayout.setWidthFull();

        Button additionalRow = createButton(newRowEvent());
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonLayout.add(additionalRow);
        add(buttonLayout);
    }

    protected abstract Button createButton(ComponentEventListener<ClickEvent<Button>> newRowEvent);

	// Method to add a new row to the grid
    protected void addNewRow() {
        T newItem = createNewItem();
        ListDataProvider<T> dataProvider = (ListDataProvider<T>) grid.getDataProvider();
        dataProvider.getItems().add(newItem);
        grid.getDataProvider().refreshAll();

        grid.getEditor().cancel();
        grid.getEditor().editItem(newItem);
    }
    protected abstract T createNewItem();

	public void discardGrid() {
        grid.setItems(new ArrayList<>(savedItems));
        reorderGrid();
    }

    private void showGridRowRemoveConfirmation(List<T> items, int index) {
        Dialog confirmationDialog = new Dialog();
        confirmationDialog.setWidth("400px");
        confirmationDialog.setResizable(false);
        confirmationDialog.setModal(true);
        confirmationDialog.setCloseOnEsc(true);
        confirmationDialog.setCloseOnOutsideClick(false);

        Label confirmationMessage = new Label(I18nProperties.getString(Strings.confirmationRemoveGridRowMessage));

        Button confirmButton = new Button(I18nProperties.getString(Strings.confirmationRemoveGridRowConfirm), event -> {
            items.remove(index);
            grid.setItems(items);
            confirmationDialog.close();
        });

        Button cancelButton = new Button(I18nProperties.getString(Strings.confirmationRemoveGridRowCancel), event -> confirmationDialog.close());

        VerticalLayout confirmationLayout = new VerticalLayout(confirmationMessage, confirmButton, cancelButton);
        confirmationDialog.add(confirmationLayout);

        confirmationDialog.open();
    }

    protected abstract void reorderGrid();

    protected abstract String getHeaderString();

    protected abstract String getAdditionalRowCaption();

    protected abstract ComponentEventListener<ClickEvent<Button>> newRowEvent();

    protected abstract Binder<T> addColumnsBinder(List<T> allElements);

    public void setSavedItems(List<T> savedItems) {
        this.savedItems = savedItems;
        this.grid.setItems(new ArrayList<>(savedItems));
    }

    @SuppressWarnings("unchecked")
    public ArrayList<T> getItems() {
        return (ArrayList<T>) grid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
    }
}
