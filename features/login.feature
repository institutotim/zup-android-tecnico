Feature: Login feature

Scenario: As a invalid user I can't log into my app
    Given I wait for the "LoginActivity" screen to appear
    Then take picture
    Then I enter text "test@example.com" into field with id "txt_login"
    Then I enter text "2dd611" into field with id "txt_senha"
    Then I hide the keyboard
    Then I press view with id "login_button"
    Then I wait for 2 seconds
    Then I take a picture
    Then I wait for a second
    Then I press view with id "button1"
    
Scenario: As a valid user I can log into my app
    Given I wait for the "LoginActivity" screen to appear
    Then I enter text "test@example.com" into field with id "txt_login"
    Then I enter text "123456" into field with id "txt_senha"
    Then I hide the keyboard
    Then I press view with id "login_button"
    Then I wait for the "LoadingDataActivity" screen to appear
    Then I take a picture