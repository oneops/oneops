#!/bin/sh

curl -X POST -H "Content-Type: text/plain" --data-binary @assembly.yaml http://localhost:9000/api/boo
