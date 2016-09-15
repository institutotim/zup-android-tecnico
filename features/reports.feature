Feature: Reports feature

Scenario: As a logged user with permissions to see Reports List I can go to the ReportsList Activity
    Given I wait for the "LoginActivity" screen to appear
    Then I enter text "test@example.com" into field with id "txt_login"
    Then I enter text "123456" into field with id "txt_senha"
    Then I hide the keyboard
    Then I press view with id "login_button"
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_reports"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I wait
    Then I take a picture

Scenario: As a logged user with permissions to see Reports List I can see a report
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_reports"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I wait
    Then I press list item number 2
    Then I wait for the "ReportItemDetailsActivity" screen to appear
    Then I wait for 3 seconds
    Then I take a picture

Scenario: As a logged user with permissions to see Reports List I can create a report
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_reports"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I wait for a second
    Then I press view with id "report_create_button"
    Then I wait for 2 seconds
    Then I take a picture

Scenario: As a logged user with permissions to see Reports List I can go see the reports list in a map
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_reports"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I press the menu key
    Then I press "Ver Mapa"
    Then I wait for 5 seconds
    Then I take a picture

Scenario: As a logged user with permissions to edit a Report I can edit a report
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_reports"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I wait for a second
    Then I press list item number 2
    Then I wait for the "ReportItemDetailsActivity" screen to appear
    Then I wait for a second
    Then I press "Editar"
    Then I wait for the "CreateReportItemActivity" screen to appear
    Then I wait for a second
    Then I take a picture

Scenario: As a logged user with permissions to delete a Report I can delete a report
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_reports"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I wait for a second
    Then I press list item number 2
    Then I wait for the "ReportItemDetailsActivity" screen to appear
    Then I wait for a second
    Then I press "Excluir"
    Then I take a picture

Scenario: As a logged user with permissions to see Reports List I can search for a report
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_reports"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I press "Buscar"
    Then I wait for the "SearchReportByProtocolActivity" screen to appear
    Then I enter text "Rua" into field with id "search_src_text"
    Then I wait for 3 seconds
    Then I take a picture

Scenario: As a logged user with permissions to see Reports List I can order the list
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_reports"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I press the menu key
    Then I press "Ordenar por endereço"
    Then I wait
    Then I take a picture
    Then I press the menu key
    Then I press "Ordenar por data de criação"
    Then I wait
    Then I take a picture

Scenario: As a logged user with permissions to see Reports List I can filter the list by category
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_reports"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I press the menu key
    Then I press "Filtrar itens"
    Then I wait for the "FilterReportsActivity" screen to appear
    Then I press view with id "layout_category"
    Then I wait for a second
    Then I click on screen 60% from the left and 60% from the top
    Then I press view with id "confirm"
    Then I wait for a second
    Then I press view with id "action_submit"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I wait for a second
    Then I take a picture


Scenario: As a logged user with permissions to see Reports List I can filter the list by status
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_reports"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I press the menu key
    Then I press "Filtrar itens"
    Then I wait for the "FilterReportsActivity" screen to appear
    Then I press view with id "layout_status"
    Then I wait for a second
    Then I press list item number 2
    Then I press view with id "confirm"
    Then I wait for a second
    Then I press view with id "action_submit"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I wait for a second
    Then I take a picture

Scenario: As a logged user with permissions to see Reports List I can filter the list by reports created by user
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_reports"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I press the menu key
    Then I press "Filtrar itens"
    Then I wait for the "FilterReportsActivity" screen to appear
    Then I press view with id "layout_created_by"
    Then I wait
    Then I click on screen 60% from the left and 60% from the top
    Then I press view with id "confirm"
    Then I wait for a second
    Then I press view with id "action_submit"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I wait for a second
    Then I take a picture

Scenario: As a logged user with permissions to see Reports List I can filter the list by reports requested by user
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_reports"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I press the menu key
    Then I press "Filtrar itens"
    Then I wait for the "FilterReportsActivity" screen to appear
    Then I press view with id "layout_requested_by"
    Then I wait
    Then I click on screen 60% from the left and 60% from the top
    Then I press view with id "confirm"
    Then I wait for a second
    Then I press view with id "action_submit"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I wait for a second
    Then I take a picture

Scenario: As a logged user with permissions to see Reports List I can filter the list by reports related to me
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_reports"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I press the menu key
    Then I press "Filtrar itens"
    Then I wait for the "FilterReportsActivity" screen to appear
    Then I press view with id "layout_related_to_me"
    Then I press view with id "action_submit"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I wait for a second
    Then I take a picture

Scenario: As a logged user with permissions to see Reports List I can filter the list by reports related to my group
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_reports"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I press the menu key
    Then I press "Filtrar itens"
    Then I wait for the "FilterReportsActivity" screen to appear
    Then I press view with id "layout_related_to_my_group"
    Then I press view with id "action_submit"
    Then I wait for the "ReportsListActivity" screen to appear
    Then I wait for a second
    Then I take a picture