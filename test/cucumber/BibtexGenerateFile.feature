@i9n
Feature: all bibtex
  As a member
  I want to view all publications i have published in format of bibtex

  Scenario: show all bibtex
    Given I am at the publications
    When I select the export bibtex file option at the publications menu
    And I select Generate All BibTex option at the export bibtex page
    Then I can see the bibtex details

 #if ($InvalidEntryOfBibtex)
  Scenario: Tags are not separated by commas
    Given: I am logged into the system
    And: I am at the main menu
    And: A BibTeX entry is "@article{mrx05
                            auTHor = "Mr. X",
                            Title = {Something Great},
                            publisher = "nob" # "ody",
                            YEAR = 2005,
                            }"
    When: I click to "generate BibTex"
    Then: the request is not done by the system because are not separated by commas

  Scenario: Incompatible tags for type of publication chosen
    Given: I am logged into the system
    And: I am at the main menu
    And: A BibTeX entry is "@article{mrx05,
                            auTHor = "Mr. X",
                            Title = {Something Great},
                            publisher = "nob" # "ody",
                            YEAR = 2005,
                            chapter = 8,
                            }"
    When: I click to "generate BibTex"
    Then: the request is not done by the system because these tags are incompatible

  Scenario: Lack mandatory tags for type of publication chosen
    Given: I am logged into the system
    And: I am at the main menu
    And: A BibTeX entry is "@article{mrx05,
                            auTHor = "Mr. X",
                            Title = {Something Great},
                            publisher = "nob" # "ody",
                            }"
    When: I click to "generate BibTex"
    Then: the request is not done by the system because mandatory tags are not complete
 #end

 #if ($CorrectEntryOfBibtex)
  Scenario: Bibtex file is generated
    Given: I am logged into the system
    And: I am at the main menu
    And: A Bibtex entry is "@article{mrx05,
                            auTHor = "Mr. X",
                            Title = {Something Great},
                            publisher = "nob" # "ody",
                            YEAR = 2005,
                            }"
    When: I click to "generate BibTex"
    Then: the request is done by the system
    And: a BibTex file is generated
 #end