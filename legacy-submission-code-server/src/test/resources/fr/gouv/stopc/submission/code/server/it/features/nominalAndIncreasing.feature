Feature: Normal scheduler execution and increasing volumetry on day 5

  Background:
    Given scheduler generate 300 code per days since J 0 and J 10

  Scenario: Nominal execution and increasing
    Then sftp contains 16 files
    Then in db there is 300 codes each days between j 0 and j 10
    Given purge sftp
    Given scheduler generate 400 code per days since J 5 and J 10
    Then sftp contains 6 files
    Then in db there is 300 codes each days between j 0 and j 4
    Then in db there is 400 codes each days between j 5 and j 10