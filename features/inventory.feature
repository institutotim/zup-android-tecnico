Feature: Inventory feature

Scenario: As a logged user with permissions to see Inventory List I can go to the InventoryList Activity
    Given I wait for the "LoginActivity" screen to appear
    Then I enter text "test@example.com" into field with id "txt_login"
    Then I enter text "123456" into field with id "txt_senha"
    Then I hide the keyboard
    Then I press view with id "login_button"
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I wait
    Then I take a picture

Scenario: As a logged user with permissions to see Inventory List I can go see the inventory list in a map
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press the menu key
    Then I press "Ver Mapa"
    Then I wait for 3 seconds
    Then I take a picture

Scenario: As a logged user with permissions to see Inventory List I can see a inventory item
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I wait
    Then I press list item number 2
    Then I wait for the "InventoryItemDetailsActivity" screen to appear
    Then I wait for 3 seconds
    Then I take a picture

Scenario: As a logged user with permissions to edit a Inventory I can edit a inventory item
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I wait
    Then I press list item number 2
    Then I wait for the "InventoryItemDetailsActivity" screen to appear
    Then I wait for 2 seconds
    Then I press "Editar"
    Then I wait for the "CreateInventoryItemActivity" screen to appear
    Then I wait for 3 seconds
    Then I take a picture

Scenario: As a logged user with permissions to delete a Inventory I can delete a inventory item
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I wait
    Then I press list item number 2
    Then I wait for the "InventoryItemDetailsActivity" screen to appear
    Then I wait for a second
    Then I press "Excluir"
    Then I take a picture

Scenario: As a logged user with permissions to create a Inventory I can create a inventory item
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I wait
    Then I press view with id "inventory_create"
    Then I wait for a second
    Then I take a picture

Scenario: As a logged user with permissions to see Inventory List I can search for a inventory item
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press "Buscar"
    Then I wait for the "SearchInventoryByQueryActivity" screen to appear
    Then I enter text "Rua" into field with id "search_src_text"
    Then I wait
    Then I take a picture

Scenario: As a logged user with permissions to see Inventory List I can order the list
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press the menu key
    Then I press "Ordenar por data de criação"
    Then I wait
    Then I take a picture
    Then I press the menu key
    Then I press "Ordenar por nome"
    Then I wait
    Then I take a picture
    Then I press the menu key
    Then I press "Ordenar por última modificação"
    Then I wait
    Then I take a picture

Scenario: As a logged user with permissions to see Inventory List I can filter the list by category
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press the menu key
    Then I press "Filtrar inventário"
    Then I wait for the "FilterInventoryItemsActivity" screen to appear
    Then I take a picture
    Then I press view with id "layout_category"
    Then I wait for a second
    Then I press list item number 2
    Then I go back
    Then I press view with id "action_submit"
    Then I wait for the "InventoryListActivity" screen to appear
    Then I wait for a second
    Then I take a picture
    
Scenario: As a logged user with permissions to see Inventory List I can filter the list by status
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press the menu key
    Then I press "Filtrar inventário"
    Then I wait for the "FilterInventoryItemsActivity" screen to appear
    Then I take a picture
    Then I press view with id "layout_status"
    Then I wait for a second
    Then I press list item number 1
    Then I go back
    Then I press view with id "action_submit"
    Then I wait for the "InventoryListActivity" screen to appear
    Then I wait for a second
    Then I take a picture

Scenario: As a logged user with permissions to see Inventory List I can filter the list by inventory related to user
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press the menu key
    Then I press "Filtrar inventário"
    Then I wait for the "FilterInventoryItemsActivity" screen to appear
    Then I take a picture
    Then I press view with id "layout_created_by"
    Then I wait
    Then I click on screen 60% from the left and 60% from the top
    Then I press view with id "confirm"
    Then I wait for a second
    Then I press view with id "action_submit"
    Then I wait for the "InventoryListActivity" screen to appear
    Then I wait for a second
    Then I take a picture