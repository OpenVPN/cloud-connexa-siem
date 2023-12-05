# CloudConnexa Log Streaming to DataDog forwarder

This forwarder is designed to read CloudConnexa logs from your AWS S3 bucket and push them into DataDog.

## Installation

The instalation is performed by using the CloudFormation template that is going to request and setup the required AWS Lambda and DataDog paramters. See the [DataDog setup instructions](https://openvpn.net/cloud-docs/tutorials/configuration-tutorials/log-streaming/tutorial--configure-datadog-for-cloudconnexa-log-streaming.html?_gl=1*15oayum*_ga*ODY3ODEyMjAxLjE2NzE3MTI2ODM.*_ga_SPGM8Y8Y79*MTcwMTc5MjgxMC4xODguMC4xNzAxNzkyODEwLjAuMC4w&_ga=2.109488518.2006116977.1701715471-867812201.1671712683) for more details.

## Build command

    `./gradlew build -i`
