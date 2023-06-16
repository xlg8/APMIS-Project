package com.cinoteck.application.views.utils;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import de.symeda.sormas.api.utils.criteria.BaseCriteria;

public class FilteredGrid<T, C extends BaseCriteria> extends Grid<T> {

	public static final String EDIT_BTN_ID = "edit";
	
	public static final String CLONE_BTN_ID = "clone";

	private static final long serialVersionUID = 8116377533153377424L;

	/**
	 * For lazy loading: Defines how many entries are loaded into the grid when new data needs to be loaded for the visible range.
	 */
	private static final int LAZY_BATCH_SIZE = 100;

	private C criteria;
	private C criteriaPhase;
	private boolean inEagerMode;

	public FilteredGrid(Class<T> beanType) {
		super(beanType);
		getDataCommunicator().setPageSize(LAZY_BATCH_SIZE);
	}

	public C getCriteria() {
		return criteria;
	}

	public void setCriteria(C criteria) {
		setCriteria(criteria, false);
	}

	public void setCriteria(C criteria, boolean ignoreDataProvider) {
		this.criteria = criteria;
		if (!ignoreDataProvider && !inEagerMode) {
			getFilteredDataProvider().setFilter(criteria);
		}
	}

	
	public boolean isInEagerMode() {
		return inEagerMode;
	}

	public void setInEagerMode(boolean inEagerMode) {
		this.inEagerMode = inEagerMode;
	}

	@SuppressWarnings("unchecked")
	public ConfigurableFilterDataProvider<T, Void, C> getFilteredDataProvider() {
		return (ConfigurableFilterDataProvider<T, Void, C>) super.getDataProvider();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setDataProvider(DataProvider<T, ?> dataProvider) {
		if (!inEagerMode && !(dataProvider instanceof ConfigurableFilterDataProvider)) {
			dataProvider = (ConfigurableFilterDataProvider<T, Void, C>) dataProvider.withConfigurableFilter();
		}
		super.setDataProvider(dataProvider);
	}

}
