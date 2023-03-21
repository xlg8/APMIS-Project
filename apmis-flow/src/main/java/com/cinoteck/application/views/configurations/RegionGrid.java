package com.cinoteck.application.views.configurations;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;

import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;

//TODO: Change use of Region to Province where appropriate in line with the new naming rules
public class RegionGrid extends Grid<RegionIndexDto> {
    
	//TODO: Create static String variables for all Strings
	public RegionGrid() {
		 Grid<RegionIndexDto> grid = new Grid<>(RegionIndexDto.class, false);
		 Editor<RegionIndexDto> editor = grid.getEditor();
		 
		 
		 	Grid.Column<RegionIndexDto> NameColumn = grid
	                .addColumn(RegionIndexDto::getName).setHeader("Province")
	                .setFlexGrow(0).setSortable(true);
	        Grid.Column<RegionIndexDto> externalIdColumn = grid.addColumn(RegionIndexDto::getArea.getexternalId)
	                .setHeader("PCode").setFlexGrow(0).setSortable(true);
	        
	        Grid.Column<RegionIndexDto> editColumn = grid.addComponentColumn(person -> {
	            Button editButton = new Button("Edit");
	            editButton.addClickListener(e -> {
	                if (editor.isOpen())
	                    editor.cancel();
	                grid.getEditor().editItem(person);
	            });
	            return editButton;
	        }).setWidth("150px").setFlexGrow(0);

	        Binder<Person> binder = new Binder<>(Person.class);
	        editor.setBinder(binder);
	        editor.setBuffered(true);

	        TextField firstNameField = new TextField();
	        firstNameField.setWidthFull();
	        binder.forField(firstNameField)
	                .asRequired("First name must not be empty")
	                .withStatusLabel(firstNameValidationMessage)
	                .bind(Person::getFirstName, Person::setFirstName);
	        firstNameColumn.setEditorComponent(firstNameField);

	        TextField lastNameField = new TextField();
	        lastNameField.setWidthFull();
	        binder.forField(lastNameField).asRequired("Last name must not be empty")
	                .withStatusLabel(lastNameValidationMessage)
	                .bind(Person::getLastName, Person::setLastName);
	        lastNameColumn.setEditorComponent(lastNameField);

	        EmailField emailField = new EmailField();
	        emailField.setWidthFull();
	        binder.forField(emailField).asRequired("Email must not be empty")
	                .withValidator(
	                        new EmailValidator("Enter a valid email address"))
	                .withStatusLabel(emailValidationMessage)
	                .bind(Person::getEmail, Person::setEmail);
	        emailColumn.setEditorComponent(emailField);

	        Button saveButton = new Button("Save", e -> editor.save());
	        Button cancelButton = new Button(VaadinIcon.CLOSE.create(),
	                e -> editor.cancel());
	        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON,
	                ButtonVariant.LUMO_ERROR);
	        HorizontalLayout actions = new HorizontalLayout(saveButton,
	                cancelButton);
	        actions.setPadding(false);
	        editColumn.setEditorComponent(actions);

	        editor.addCancelListener(e -> {
	            firstNameValidationMessage.setText("");
	            lastNameValidationMessage.setText("");
	            emailValidationMessage.setText("");
	        });

	        List<Person> people = DataService.getPeople();
	        grid.setItems(people);

	        getThemeList().clear();
	        getThemeList().add("spacing-s");
	        add(grid, firstNameValidationMessage, lastNameValidationMessage,
	                emailValidationMessage);
	    }
    }


}
