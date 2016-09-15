Feature: User feature
    
Scenario: As a logged user I can see my info
    Given I wait for the "LoginActivity" screen to appear
    Then I enter text "test@example.com" into field with id "txt_login"
    Then I enter text "123456" into field with id "txt_senha"
    Then I hide the keyboard
    Then I press view with id "login_button"
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_profile"
    Then I take a picture
    Then I wait for the "ProfileActivity" screen to appear
    Then I wait for 2 seconds
    Then I take a picture
    

Scenario: As a logged user I can logout
    Given I wait for the "LoginActivity" screen to appear
    Then I wait up to 60 seconds for the "InventoryListActivity" screen to appear
    Then I press view with id "sidebar_drawer"
    Then I press view with id "sidebar_cell_profile"
    Then I wait for the "ProfileActivity" screen to appear
    Then I press view with id "logoff_button"
    Then I press "Sair"
    Then I wait for the "LoginActivity" screen to appear
    Then I take a picture
