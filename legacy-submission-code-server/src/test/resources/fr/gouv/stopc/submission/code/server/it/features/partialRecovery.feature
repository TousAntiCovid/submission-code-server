Feature: Test resumption of activity following a partial execution yesterday

  Background:
    Given scheduler generate codes and stop after the first batch of j8

  Scenario: We relaunch the scheduler
    Then sftp contains 16 files
    Then in db there is 300 codes each days between j 0 and j 7
    Then in db there is 40 codes each days between j 8 and j 8
    Then in db there is 0 codes each days between j 9 and j 10
    Given purge sftp
    Given scheduler generate 300 code per days since J 0 and J 10
    Then in db there is 300 codes each days between j 0 and j 10
    Then sftp contains 16 files