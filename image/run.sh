#!/bin/sh
echo The options are: $@
java $@ -Dobjectservice.config=conf/config.yml -jar bin/ObjectService.jar