# Contributing to the CloudConnexa to SIEM forwarder functions

Thank you so much for your contribution!

## Branch structure

The master branch is currently tracking development for the ongoing release cycle. Please note that, at any given time, master may be broken. Feel free to create issues against master, but have patience when using the master branch. It is recommended to use a release, and priority will be given to bugs identified in the most recent release.

    feature/<feature name>          Development of a new feature. All new fucntionality should be developed in the feature branches. Create the feature branch from the 'main' branch. Is closed on the successful merge into the 'main' branch.
    main                            <- ongoing release. All feature branches are merged into it. May be unstable. QA is performed on this branch.
    datadog/release/1.0.0           v1.0.0 changes. Branch closed for commits.
    datadog/release/1.0.0HF1        v1.0.0HF1 changes. Added hotfixes and security updates.

## Submitting Issues

- First take a look at the [Troubleshooting section](https://openvpn.net/cloud-docs/tutorials/configuration-tutorials/log-streaming/tutorial--troubleshoot-cloudconnexa-log-streaming.html).
- If you can't find anything useful, please contact our Suport Team for assistance. [Go here](https://support.openvpn.com/hc/en-us/requests/new?ticket_form_id=360001597871&tf_subject=Log%20Streaming%20Issue).
- Open a GitHub issue for the confirmed cases and link them to the Support cases.

## Pull Requests

The PRs review proces takes time and to simplify and ease it, follow these checklist:

- [ ] prepared a proper commit history. (we advise you to rebase if needed).
- [ ] written tests for the code you wrote and they pass.
- [ ] all tests passed locally.
- [ ] summarized your PR with an informative title and a message describing your changes. Cross-reference any related bugs and issues.

## Commits

- Please keep each commit's changes small and focused - many changes in the same commit makes them harder to review.
- Please also combine each code-change within one commit rather than many commits. Rebase liberally.
- Please make all commit messages clear and communicative.

Following these rules keeps history cleaner, makes it easier to revert things, and it makes everyone involved happier.

## Documentation

Every forwarder should have it's own dedicated folder with description of the forwarder in README.md file.

## Code style, standards

Follow the general Java code style.
