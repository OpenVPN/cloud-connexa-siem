#!/bin/bash

# create-bucket
set -eo pipefail
BUCKET_NAME=connexa-s3-datadog-install
echo $BUCKET_NAME > bucket-name.txt
aws s3 mb s3://$BUCKET_NAME

# build-deploy
ARTIFACT_BUCKET=$(cat bucket-name.txt)
aws s3 cp data/log.jsonl.gz s3://$ARTIFACT_BUCKET/CloudConnexa/log.jsonl.gz
./gradlew build -i
aws s3 cp build/distributions/connexa-s3-datadog-1.0.0.zip s3://$ARTIFACT_BUCKET/connexa-s3-datadog-1.0.0.zip
