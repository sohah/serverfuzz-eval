#!#!/bin/sh

#this script is used to search github for repos with specific criteria

CRITERIA="aws lambda s3"


gh search repos --language=java ${CRITERIA}
