Feature: Daily purge

  Scenario: There is no more codes older than two months
    Given generate long code older than two months
    When purge old codes
    Then then there is no more codes older than two months
