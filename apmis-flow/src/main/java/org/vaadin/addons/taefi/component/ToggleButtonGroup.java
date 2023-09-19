package org.vaadin.addons.taefi.component;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializableFunction;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@CssImport("./styles/toggle-button-group.css")
public class ToggleButtonGroup<T> extends CustomField<T> {

    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    private List<T> items;
    private T selected;
    private SerializableFunction<T, String> itemLabelGenerator = Object::toString;
    private SerializableFunction<T, String> selectedItemClassNameGenerator = item -> "";
    private SerializableFunction<T, Icon> itemIconGenerator;
    private SerializableFunction<T, String> itemTooltipTextGenerator;
    private SerializableFunction<T, Boolean> itemEnabledProvider = item -> Boolean.TRUE;
    private SerializableFunction<T, Serializable> itemIdGenerator = Objects::hashCode;
    private SerializableFunction<T, Integer> itemOrderProvider;
    private boolean enabled = true;
    private boolean toggleable = true;
    private Orientation orientation = Orientation.HORIZONTAL;
    // internals:
    private final Map<Serializable, Integer> originalOrderMap = new HashMap<>();
    private final Map<Serializable, Button> idToButtonMap = new HashMap<>();
    private final Map<Button, T> buttonToItemMap = new HashMap<>();
    private final HorizontalLayout hLayout = new HorizontalLayout();
    private final VerticalLayout vLayout = new VerticalLayout();

    public ToggleButtonGroup() {
        addClassName("toggle-button-group");
        items = new ArrayList<>();
    }

    public ToggleButtonGroup(String label) {
        this();
        setLabel(label);
    }

    public ToggleButtonGroup(List<T> items) {
        this();
        setItems(items);
    }

    public ToggleButtonGroup(String label, List<T> items) {
        this(items);
        setLabel(label);
    }

    public ToggleButtonGroup(String label, T... items) {
        this(Arrays.asList(items));
        setLabel(label);
    }

    public ToggleButtonGroup(String label, List<T> items, SerializableFunction<T, String> itemLabelGenerator) {
        this(label, items);
        this.itemLabelGenerator = itemLabelGenerator;
        init();
    }

    private void init() {

        idToButtonMap.clear();
        buttonToItemMap.clear();

        items.sort(getDefaultComparator());

        Button[] buttons = new Button[items.size()];

        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            buttons[i] = createButton(item);
            applyButtonStyles(buttons[i], i);
            buttonToItemMap.put(buttons[i], item);
            idToButtonMap.put(itemIdGenerator.apply(item), buttons[i]);
        }

        addButtonsToLayout(buttons);
    }

    protected void addButtonsToLayout(Button[] buttons) {
        if (orientation == Orientation.HORIZONTAL) {
            remove(vLayout);

            hLayout.setSpacing(false);
            hLayout.removeAll();
            hLayout.add(buttons);
            hLayout.setFlexGrow(1.0, buttons);
            add(hLayout);
        } else {
            remove(hLayout);

            vLayout.setSpacing(false);
            vLayout.removeAll();
            vLayout.add(buttons);
            vLayout.setFlexGrow(1.0, buttons);
            vLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
            add(vLayout);
        }
    }

    protected Button createButton(T item) {
        Button button = new Button(itemLabelGenerator.apply(item));
        button.addClickListener(this::buttonsActionListener);
        button.setEnabled(getEnabled() && itemEnabledProvider.apply(item));
        if (itemIconGenerator != null) {
            button.setIcon(itemIconGenerator.apply(item));
        }
        if (itemTooltipTextGenerator != null) {
            button.setTooltipText(itemTooltipTextGenerator.apply(item));
        }
        return button;
    }

    protected void applyButtonStyles(Button button, int index) {
        getButtonsBaseClass().ifPresent(button::addClassName);
        if (index == 0) {
            getFirstButtonClass().ifPresent(button::addClassName);
        } else if (index == items.size() - 1) {
            getLastButtonClass().ifPresent(button::addClassName);
        } else {
            getMiddleButtonClass().ifPresent(button::addClassName);
        }
    }

    protected Optional<String> getButtonsBaseClass() {
        return Optional.of("toggle-button-group-button-" + getOrientationStylePostfix());
    }

    protected Optional<String> getFirstButtonClass() {
        return Optional.of("toggle-button-group-first-button-" + getOrientationStylePostfix());
    }

    protected Optional<String> getMiddleButtonClass() {
        return Optional.of("toggle-button-group-middle-button-" + getOrientationStylePostfix());
    }

    protected Optional<String> getLastButtonClass() {
        return Optional.of("toggle-button-group-last-button-" + getOrientationStylePostfix());
    }

    private String getOrientationStylePostfix() {
        return orientation == Orientation.HORIZONTAL ? "h" : "v";
    }

    private Comparator<T> getDefaultComparator() {
        return (item1, item2) -> {
            Integer item1Order;
            Integer item2Order;
            if (itemOrderProvider != null) {
                item1Order = itemOrderProvider.apply(item1);
                item2Order = itemOrderProvider.apply(item2);
            } else {
                item1Order = originalOrderMap.get(itemIdGenerator.apply(item1));
                item2Order = originalOrderMap.get(itemIdGenerator.apply(item2));
            }
            return item1Order.compareTo(item2Order);
        };
    }

    protected void buttonsActionListener(ClickEvent<Button> event) {
        if (isReadOnly()) {
            return;
        }
        T selectedValue = buttonToItemMap.get(event.getSource());
        if (isToggleable()) {
            Serializable selectedValueId = itemIdGenerator.apply(selectedValue);
            Serializable previousValueId = itemIdGenerator.apply(getValue());
            if (Objects.equals(selectedValueId, previousValueId)) {
                setValue(null, event.isFromClient());
                return;
            }
        }
        setValue(selectedValue, event.isFromClient());
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setReadOnly(!enabled);

        Stream<Component> componentStream;
        if (Orientation.HORIZONTAL == this.orientation) {
            componentStream = this.hLayout.getChildren();
        } else {
            componentStream = this.vLayout.getChildren();
        }
        componentStream.filter(Button.class::isInstance)
                .map(Button.class::cast)
                .forEach(button -> button.setEnabled(enabled));
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public boolean isToggleable() {
        return toggleable;
    }

    public void setToggleable(boolean toggleable) {
        this.toggleable = toggleable;
    }

    public T getSelected() {
        return selected;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = new ArrayList<>(items);
        initializeOriginalOrderMap();
        init();
    }

    public void setItems(T... items) {
        setItems(Arrays.asList(items));
    }

    private void initializeOriginalOrderMap() {
        originalOrderMap.clear();
        IntStream.range(0, items.size()).forEach(i ->
                originalOrderMap.put(itemIdGenerator.apply(items.get(i)), i));
    }

    public SerializableFunction<T, String> getSelectedItemClassNameGenerator() {
        return selectedItemClassNameGenerator;
    }

    public void setSelectedItemClassNameGenerator(SerializableFunction<T, String> selectedItemClassNameGenerator) {
        this.selectedItemClassNameGenerator = selectedItemClassNameGenerator;
        init();
    }

    public Function<T, String> getItemLabelGenerator() {
        return itemLabelGenerator;
    }

    public void setItemLabelGenerator(SerializableFunction<T, String> itemLabelGenerator) {
        this.itemLabelGenerator = itemLabelGenerator;
        init();
    }

    public SerializableFunction<T, Icon> getItemIconGenerator() {
        return itemIconGenerator;
    }

    public void setItemIconGenerator(SerializableFunction<T, Icon> itemIconGenerator) {
        this.itemIconGenerator = itemIconGenerator;
        init();
    }

    public Function<T, Serializable> getItemIdGenerator() {
        return itemIdGenerator;
    }

    public void setItemIdGenerator(SerializableFunction<T, Serializable> itemIdGenerator) {
        this.itemIdGenerator = itemIdGenerator;
        init();
    }

    public SerializableFunction<T, String> getItemTooltipTextGenerator() {
        return itemTooltipTextGenerator;
    }

    public void setItemTooltipTextGenerator(SerializableFunction<T, String> itemTooltipTextGenerator) {
        this.itemTooltipTextGenerator = itemTooltipTextGenerator;
        init();
    }

    public SerializableFunction<T, Boolean> getItemEnabledProvider() {
        return itemEnabledProvider;
    }

    public void setItemEnabledProvider(SerializableFunction<T, Boolean> itemEnabledProvider) {
        this.itemEnabledProvider = itemEnabledProvider;
        init();
    }

    public SerializableFunction<T, Integer> getItemOrderProvider() {
        return itemOrderProvider;
    }

    public void setItemOrderProvider(SerializableFunction<T, Integer> itemOrderProvider) {
        this.itemOrderProvider = itemOrderProvider;
        init();
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
        init();
    }

    @Override
    public T getValue() {
        return selected;
    }

    @Override
    public void setValue(T selected) {
        setValue(selected, false);
    }

    public void setValue(T selected, boolean isFromClient) {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot set a value before possible options have initialized. " +
                    "Use one of the existing setItems(...) methods or the proper constructor to initialize the available items before calling setValue().");
        }
        T oldValue = getValue();
        this.selected = selected;
        fireValueChangeEvent(oldValue, this.selected, isFromClient);
    }

    protected void fireValueChangeEvent(T oldValue, T newValue, boolean fromClient) {
        updateStyles(oldValue, newValue);

        if (!Objects.equals(itemIdGenerator.apply(oldValue), itemIdGenerator.apply(newValue))) {
            fireEvent(new AbstractField.ComponentValueChangeEvent<>(this, this, oldValue, fromClient));
        }
    }

    private void updateStyles(T oldValue, T newValue) {
        if (oldValue != null) {
            Serializable oldSelectedId = itemIdGenerator.apply(oldValue);
            Button oldSelected = idToButtonMap.get(oldSelectedId);
            String oldSelectedCustomClass = selectedItemClassNameGenerator.apply(oldValue);
            if (StringUtils.isNotBlank(oldSelectedCustomClass)) {
                oldSelected.removeClassName(oldSelectedCustomClass);
            } else {
                oldSelected.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }
        }

        Serializable newSelectedId = itemIdGenerator.apply(newValue);
        Optional.ofNullable(idToButtonMap.get(newSelectedId)).ifPresent(newSelected -> {
            String newSelectedCustomClass = selectedItemClassNameGenerator.apply(newValue);
            if (StringUtils.isNotBlank((newSelectedCustomClass))) {
                newSelected.addClassName(newSelectedCustomClass);
            } else {
                newSelected.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }
        });
    }

    @Override
    protected T generateModelValue() {
        return null;
    }

    @Override
    protected void setPresentationValue(T t) {

    }
}