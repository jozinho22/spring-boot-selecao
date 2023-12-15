#language: en

Feature: Test differents authorizations

    Scenario Outline: Authenticate as ADMIN and then register a new AuthorizedUser

          Given I authenticate as ADMIN
          | joss@gmail.com   | joss   |
          When I want to register a new AuthorizedUser, who will have a USER Role
          | user@gmail.com   | pwd   |
          Then I retrieve a USER token
          Then my new AuthorizedUser can access to the products list with USER token
          Then my new AuthorizedUser can not access to the products list without USER token
          Then my new AuthorizedUser can not access to the register API with USER token
          | user2@gmail.com   | pwd2   |