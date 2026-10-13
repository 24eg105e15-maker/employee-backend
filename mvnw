#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script
# ----------------------------------------------------------------------------

# If JAVA_HOME is set, use it; otherwise rely on PATH
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

if [ ! -x "$JAVACMD" ] && ! command -v java >/dev/null 2>&1; then
    echo "Error: JAVA_HOME is not set and java was not found in PATH." >&2
    exit 1
fi

# Determine the script directory
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MAVEN_PROJECTBASEDIR="$SCRIPT_DIR"
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

# Read distributionUrl from wrapper properties
if [ -f "$MAVEN_WRAPPER_PROPERTIES" ]; then
    DISTRIBUTION_URL=$(grep "^distributionUrl=" "$MAVEN_WRAPPER_PROPERTIES" | sed "s/distributionUrl=//")
fi

WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_LAUNCHER="org.apache.maven.wrapper.MavenWrapperMain"

# Download wrapper jar if missing
if [ ! -f "$WRAPPER_JAR" ]; then
    WRAPPER_URL=$(grep "^wrapperUrl=" "$MAVEN_WRAPPER_PROPERTIES" | sed "s/wrapperUrl=//")
    if command -v curl >/dev/null 2>&1; then
        curl -o "$WRAPPER_JAR" "$WRAPPER_URL" 2>/dev/null
    elif command -v wget >/dev/null 2>&1; then
        wget -O "$WRAPPER_JAR" "$WRAPPER_URL" 2>/dev/null
    fi
fi

# Determine Maven home: download if needed
MAVEN_HOME_PARENT="$HOME/.m2/wrapper/dists"
MAVEN_HOME="$MAVEN_HOME_PARENT/$(basename "$DISTRIBUTION_URL" .zip)"

if [ ! -d "$MAVEN_HOME" ]; then
    mkdir -p "$MAVEN_HOME_PARENT"
    if command -v curl >/dev/null 2>&1; then
        curl -f -o "$MAVEN_HOME_PARENT/dist.zip" "$DISTRIBUTION_URL" 2>/dev/null
    elif command -v wget >/dev/null 2>&1; then
        wget -O "$MAVEN_HOME_PARENT/dist.zip" "$DISTRIBUTION_URL" 2>/dev/null
    fi
    if [ -f "$MAVEN_HOME_PARENT/dist.zip" ]; then
        unzip -q "$MAVEN_HOME_PARENT/dist.zip" -d "$MAVEN_HOME_PARENT" 2>/dev/null
        rm -f "$MAVEN_HOME_PARENT/dist.zip"
        MAVEN_HOME="$(find "$MAVEN_HOME_PARENT" -maxdepth 1 -type d -name "apache-maven-*" | head -1)"
    fi
fi

# Try to use system Maven if wrapper setup failed
if [ -d "$MAVEN_HOME" ]; then
    MVNW_EXEC="$MAVEN_HOME/bin/mvn"
elif command -v mvn >/dev/null 2>&1; then
    MVNW_EXEC="mvn"
else
    "$JAVACMD" -classpath "$WRAPPER_JAR" "$WRAPPER_LAUNCHER" \
        -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" "$@"
    exit $?
fi

exec "$MVNW_EXEC" "$@"