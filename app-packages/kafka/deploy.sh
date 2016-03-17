#! /bin/bash

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

APPNAME=${1:-koya}
slider install-package --replacepkg --name KOYA --package target/slider-kafka-app-package-0.90.0-incubating-SNAPSHOT.zip
slider stop $APPNAME
slider destroy $APPNAME
slider create $APPNAME --filesystem hdfs://root --queue dev --template appConfig.json --resources resources-default.json
