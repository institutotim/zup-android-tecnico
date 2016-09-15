Feature: Sync feature

Scenario: As a logged user with sync pending I can go to Sync Activity
    Given I wait for the "LoginActivity" screen to appear
    Then I enter text "test@example.com" into field with id "txt_login"
    Then I enter text "123456" into field with id "txt_senha"
    Then I hide the keyboard
    Then I press view with id "login_button"
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_sync"
    Then I wait for the "SyncActivity" screen to appear
    Then I wait
    Then I take a picture

Scenario: As a logged user with sync done I can see the sync done list
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
    Then I enter text " - Teste Calabash" into field with id "report_description"
    Then I press view with id "button2"
    Then I wait
    Then I go back
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_sync"
    Then I wait for the "SyncActivity" screen to appear
    Then I wait
    Then I take a picture

Scenario: As a logged user with sync successfull I can clear successful sync list
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_sync"
    Then I wait for the "SyncActivity" screen to appear
    Then I press "Limpar lista"
    Then I should see "Itens sincronizados com sucesso foram removidos dessa lista"
    Then I take a picture