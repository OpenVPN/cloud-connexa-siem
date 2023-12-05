# CloudConnexa to SIEM forwarder functions for Log Streaming

Log Streaming collects events from various sources inside Wide-area Private Cloud and stores them in an AWS S3 bucket.
These events can be of different types such as security, audit, or activity. Once the events are collected, they can be
passed to external SIEM systems through AWS S3 bucket integration. This allows the SIEM systems to analyze the events
and generate alerts or reports based on predefined rules.

This repository contains serverless functions that process CloudConnexa's Log Streaming files on your AWS S3 bucket and
push them to a SIEM system.

The list of forwarders:

1. For DataDog, [go here](./datadog/README.md)

## Documentation

Every forwarder should have it's own dedicated folder with description of the forwarder in README.md file.
Every forwarder is going to have a dedicated release branch with it's version, like - datadog/release/1.0.0. It contains
v1.0.x release, bugfix/security updates for DataDog.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) file.

## LICENSING

See [COPYRIGHT.GPL](COPYRIGHT.GPL) file.
