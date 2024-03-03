#!/bin/sh

mvn clean package
docker build -t reactive-file-downloader .
docker-compose up