package com.cinoteck.application;


import java.util.Collection;


import com.vaadin.flow.function.ValueProvider;
import de.symeda.sormas.api.utils.HtmlHelper;

/**
 * A ValueProvider that allows displaying a collection as a comma separated list of
 * strings.
 */
@SuppressWarnings({
        "serial",
        "rawtypes" })
public class CollectionValueProvider<T extends Collection> implements ValueProvider<T, String> {

    @Override
    public String apply(T source) {

        if (source == null) {
            return "";
        }

        StringBuilder b = new StringBuilder();
        for (Object o : source) {
            b.append(o.toString());
            b.append(", ");
        }
        if (b.length() >= 2) {
            return HtmlHelper.cleanHtml(b.substring(0, b.length() - 2));
        }
        return "";
    }
}