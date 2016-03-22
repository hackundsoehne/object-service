# How to contribute
Please have a look at the wiki, where the general architecture and some details are explained.
 
# Getting Started
How to run the project is detailed in the README, please have a look at it.
 
# Creating Tickets
If you encounter a bug, please create a ticket. A sane and helpful description makes our life much easier, so please
in case the stacktrace is a bit longer, create a gist. Also describe what you did when you encountered the bug, preferably
also how to reproduce the bug.

# Making Changes
 * Create a topic branch from where you want to base your work.
 * Make commits of logical units.
 * Check for unnecessary whitespace with git diff --check before committing.
 * Make sure your commit messages are readable and understandable.
 * Make sure you have added the necessary tests for your changes.
 * Run all the tests to assure nothing else was accidentally broken.
 * If a commit fixed a bug, include `fixes #ISSUE` in the message (more information can be found [here](https://help.github.com/articles/closing-issues-via-commit-messages/))
 
# Submitting Changes  
 * Submit a pull request to the object-service.
 * (Optional) Update the ticket
 * Include a link to the pull request in the ticket.
 * After feedback has been given we expect responses within two weeks. After two weeks we may close the pull request if it isn't showing any activity.
 * Even if you are an admin, don't merge your own pull request. The pull requests are always merged by the reviewer.