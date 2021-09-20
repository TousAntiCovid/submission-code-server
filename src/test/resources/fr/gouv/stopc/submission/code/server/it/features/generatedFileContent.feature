Feature: Test if the generated file is well formatted

  Background:
    Given scheduler generate 10 code per days since J 0 and J 0

  Scenario: Verify the name and the content of the generated files
    Then sftp contains 2 files and names are well formatted
    Then archive and csv had right filename
    Then csv contains 11 lines
    Then first csv content is correct