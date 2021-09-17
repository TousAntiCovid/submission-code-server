Feature: Test resumption of activity following a non-execution yesterday

  Background:
    Given scheduler generate 300 code per days since J 0 and J 8

  Scenario: We relaunch the scheduler
    Then sftp contains 16 files
    Then then in db there is 300 codes each days between j 0 and j 8
    Then then in db there is 0 codes each days between j 9 and j 10
    Given purge sftp
    Given scheduler generate 300 code per days since J 9 and J 10
    Then then in db there is 300 codes each days between j 0 and j 10
    Then sftp contains 16 files
    Given purge sftp and data base