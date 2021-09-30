Feature: Test code generation

  Scenario: Can I generate a "test" submission code
    When I request a "test" submission code
    Then I received a 12 characters code valid until now plus 3 days

  Scenario: I Can verify a "test" submission code
    Given A "test" submission code has been generated
    When I request it's verification
    Then The verification response is successful

  Scenario: A test code can't be used twice
    Given A "test" submission code has been generated
    And It's verification has already been requested
    When I request it's verification
    Then The verification response is failed

  Scenario: An unknown test code verification must fail
    Given I generate an INVALID "test" submission code
    When I request it's verification
    Then The verification response is failed