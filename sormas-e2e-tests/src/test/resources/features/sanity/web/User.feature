@Sanity @Users
Feature: Create user

  Scenario Outline: Create a new user
    Given I log in as a Admin User
    And I click on the Users from navbar
    And I click on the NEW USER button
    And I create a new user with <rights>
    When I select last created user
    Then I check the created data is correctly displayed on Edit User Page for selected <rights>

    Examples:
      | rights                  |
      | National User           |
#      | POE National User       |
#      | Import User             |
#      | External Visits User    |
#      | ReST User               |
#      | Sormas to Sormas Client |
#      | National Clinician      |

  Scenario Outline: Create a new user
