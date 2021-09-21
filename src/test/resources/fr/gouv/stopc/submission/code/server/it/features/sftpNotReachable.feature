Feature: SFTP server is not reachable

  Scenario: sftp server is not reachable
    Given sftp server is unreachable
    When scheduler generate 300 code per days since J 0 and J 10
    Then in db there is 0 codes each days between j 0 and j 10
