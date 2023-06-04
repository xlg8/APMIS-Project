package com.cinoteck.application.views.campaign;



import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;


public abstract class AbstractEditableGrid<T> extends VerticalLayout {

	private static final String HEADING_LOC = "headingLoc";
	private static final String GRID_LOC = "gridLoc";
	private static final String ADDITIONAL_ROW_LOC = "additionalRowLoc";

	private static final String HTML_LAYOUT = (HEADING_LOC) + (GRID_LOC) + (ADDITIONAL_ROW_LOC);
	public static final String DELETE = "delete";

	protected Grid<T> grid = new Grid<>();
	protected List<T> savedItems = new ArrayList<>();

	public AbstractEditableGrid(List<T> savedElements, List<T> allElements) {

		setSizeFull();

		final HorizontalLayout gridLayout = new HorizontalLayout();
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

//		final GridRowDragger<T> gridRowDragger = new GridRowDragger<>(grid);
//		gridRowDragger.getGridDragSource().addGridDragEndListener(gridDragEndListener());

		final Binder<T> binder = addColumnsBinder(allElements);

//		Column<T> deleteColumn =
//				grid.addColumn(t -> VaadinIcon.TRASH).setKey(DELETE).setHeader(I18nProperties.getCaption(Captions.remove))
//						.setWidth("50px").setFlexGrow(0).setResizable(false).setSortable(false);
//
//		grid.getColumns().forEach(col -> {
//			col.setSortable(false);
//		});

		grid.addItemClickListener(e -> {
			final List<T> items = getItems();
			int i = items.indexOf(e.getItem());

			if (e.getColumn() != null && DELETE.equals(e.getColumn().getKey())) {
//				showGridRowRemoveConfirmation(items, i);
			} else if (i > -1) {
				grid.getEditor().editItem(e.getItem());
			}
		});

		grid.getEditor().setBinder(binder);
		grid.getEditor().setBuffered(true);
//		((HasEnabled) grid.getEditor()).setEnabled(true);

		grid.getEditor().addSaveListener(e -> {
			final List<T> items = getItems();
			grid.setItems(items);
		});

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setMargin(false);
		buttonLayout.setSpacing(true);
		buttonLayout.setWidthFull();
		final Button additionalRow = createButton(getAdditionalRowCaption(), newRowEvent());
//		buttonLayout.add(additionalRow);
		buttonLayout.setJustifyContentMode(JustifyContentMode.END);
		add(buttonLayout);

		setFlexGrow(1, gridLayout);
		setFlexGrow(0, buttonLayout);
	}

//	protected GridDragEndListener<T> gridDragEndListener() {
//		return (GridDragEndListener<T>) gridDragEndEvent -> reorderGrid();
//	}

	protected abstract Button createButton(String additionalRowCaption,
			ComponentEventListener<ClickEvent<Button>> newRowEvent);

	public void discardGrid() {
		this.grid.setItems(new ArrayList<>(this.savedItems));
		reorderGrid();
	}

//	private void showGridRowRemoveConfirmation(List<T> items, int index) {
//		ConfirmationComponent confirmationComponent = new ConfirmationComponent(false, null) {
//
//			private static final long serialVersionUID = 3664636750443474734L;
//
//			@Override
//			protected void onConfirm() {
//				items.remove(index);
//				grid.setItems(items);
//			}
//
//			@Override
//			protected void onCancel() {
//			}
//		};
//
//		confirmationComponent.getConfirmButton().setText(I18nProperties.getString(Strings.confirmationRemoveGridRowConfirm));
//		confirmationComponent.getCancelButton().setText(I18nProperties.getString(Strings.confirmationRemoveGridRowCancel));
//
//		VerticalLayout confirmationLayout = new VerticalLayout(
//				new Label(I18nProperties.getString(Strings.confirmationRemoveGridRowMessage)), confirmationComponent);
//		Window confirmationPopup = new Window(I18nProperties.getString(Strings.confirmationRemoveGridRowTitle),
//				confirmationLayout);
//		confirmationPopup.setWidth("400px");
//		confirmationPopup.setHeight("auto");
//		confirmationPopup.setResizable(false);
//		confirmationPopup.setModal(true);
//		confirmationPopup.setCloseOnEsc(true);
//		confirmationPopup.setCloseOnOutsideClick(false);
//
//		getUI().ifPresent(ui -> ui.add(confirmationPopup));
//	}

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
		return (ArrayList<T>) ((ListDataProvider<T>) ((DataCommunicator<T>) ((Collection<?>) this.grid.getListDataView())
				.iterator().next()).getDataProvider()).getItems();
	}
}
