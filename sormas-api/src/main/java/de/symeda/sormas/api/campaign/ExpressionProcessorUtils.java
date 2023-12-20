package de.symeda.sormas.api.campaign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;

public class ExpressionProcessorUtils {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private ExpressionProcessorUtils() {
	}

	public static EvaluationContext refreshEvaluationContext(List<CampaignFormDataEntry> formValues) {
		EvaluationContext context = new StandardEvaluationContext(transformFormValueListToMap(formValues));
		context.getPropertyAccessors().add(new MapAccessor()); 
		return context;
	}

	private static Map<String, Object> transformFormValueListToMap(List<CampaignFormDataEntry> formValues) {

		final Map<String, Object> formValuesMap = new HashMap<>();
		for (CampaignFormDataEntry campaignFormDataEntry : formValues) {
			formValuesMap.put(campaignFormDataEntry.getId(), parseValue(campaignFormDataEntry.getValue()));
		}

		return formValuesMap;
	}

	
	private static Object parseValue(Object value) {
//		S		qystem.out.println("int: "+(value instanceof Integer) + ", String: "+(value instanceof String)+"________expression parseValue: "+value);
		if ((value instanceof String && !((String) value).isEmpty()) || (value instanceof Integer)) {
			try {
				return Double.parseDouble(value.toString());
			} catch (NumberFormatException e) {
			//	try {
				//	Integer.parseInt(value.toString())
				//	return Double.parseDouble(value.toString());
				//} catch (NumberFormatException e1) {
			//	System.out.println("Value not parseable as double #####: "+value);
					return value;
				//}
			}
		}
		return value;
	}
}
