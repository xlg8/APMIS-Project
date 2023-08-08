package com.cinoteck.application.views.utils;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

@JsModule("/date-picker-pattern.js")
@NpmPackage(value = "date-fns", version = "2.16.0")
public class DatePickerWithPattern extends DatePicker {
    private String pattern;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
        applyPattern();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        applyPattern();
    }

    private void applyPattern() {
        UI
            .getCurrent()
            .beforeClientResponse(
                this,
                ctx -> {
                    this.getElement().executeJs("window._setDatePickerPattern(this, $0)", pattern);
                }
            );
    }
}