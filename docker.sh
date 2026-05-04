#!/bin/bash
set -e

docker-compose down -v
docker-compose up -d --build
sleep 90
docker-compose ps