package de.symeda.sormas.backend.common;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

import de.symeda.sormas.backend.util.AbstractDomainObjectJoins;

public abstract class QueryContext<T, ADO extends AbstractDomainObject> {

	private CriteriaQuery<?> query;
	private CriteriaBuilder criteriaBuilder;
	private From<?, ADO> root;
	private AbstractDomainObjectJoins<T, ADO> joins;
	private Map<String, Expression<?>> subqueryExpressions;

	public QueryContext(CriteriaBuilder cb, CriteriaQuery<?> query, From<?, ADO> root, AbstractDomainObjectJoins<T, ADO> joins) {
		this.root = root;
		this.joins = joins;
		this.subqueryExpressions = new HashMap<>();
		this.query = query;
		this.criteriaBuilder = cb;
	}

	protected Expression addSubqueryExpression(String name, Expression<?> expression) {
		subqueryExpressions.put(name, expression);
		return expression;
	}

	public <E> Expression<E> getSubqueryExpression(String name) {
		if (subqueryExpressions.containsKey(name)) {
			return (Expression<E>) subqueryExpressions.get(name);
		}
		return (Expression<E>) createExpression(name);
	}

	protected abstract Expression<?> createExpression(String name);

	public From<?, ADO> getRoot() {
		return root;
	}

	public CriteriaQuery<?> getQuery() {
		return query;
	}

	public CriteriaBuilder getCriteriaBuilder() {
		return criteriaBuilder;
	}

	public AbstractDomainObjectJoins<T, ADO> getJoins() {
		return joins;
	}

}
