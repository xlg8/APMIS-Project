package com.cinoteck.application.views.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.Icon;

import java.util.List;

import de.symeda.sormas.api.i18n.I18nProperties;

public class MenuBarHelper {

    public static ContextMenu createDropDown(String captionKey, MenuBarItem... items) {
        return createDropDown(captionKey, List.of(items));
    }

    public static ContextMenu createDropDown(String captionKey, List<MenuBarItem> items) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem mainItem = contextMenu.addItem(I18nProperties.getCaption(captionKey));

        for (MenuBarItem item : items) {
            MenuItem dropDownItem = mainItem.getSubMenu().addItem(item.caption);
            if (item.icon != null) {
                dropDownItem.getElement().setAttribute("icon", item.icon.getElement().getAttribute("icon"));
            }
            if (item.command != null) {
                dropDownItem.addClickListener(event -> item.command.execute());
            }
            dropDownItem.setVisible(item.visible);
        }

        return contextMenu;
    }

    public static class MenuBarItem {

        private String caption;
        private Icon icon;
        private Command command;
        private boolean visible = true;

        public MenuBarItem(String caption, Icon icon, Command command) {
            this.caption = caption;
            this.icon = icon;
            this.command = command;
        }

        public MenuBarItem(String caption, Icon icon, Command command, boolean visible) {
            this.caption = caption;
            this.icon = icon;
            this.command = command;
            this.visible = visible;
        }
    }

    @FunctionalInterface
    public interface Command {
        void execute();
    }
}
