#!/bin/bash
# Smart Emergency Response System (ERAS) Console Runner

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Locate a working Java executable
JAVA_CMD=""

if [ -x "/Applications/IntelliJ IDEA.app/Contents/jbr/Contents/Home/bin/java" ]; then
    JAVA_CMD="/Applications/IntelliJ IDEA.app/Contents/jbr/Contents/Home/bin/java"
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
elif java -version &>/dev/null; then
    JAVA_CMD="java"
else
    # Check standard macOS JVM directories
    for jvm in /Library/Java/JavaVirtualMachines/*/Contents/Home/bin/java; do
        if [ -x "$jvm" ]; then
            JAVA_CMD="$jvm"
            break
        fi
    done
fi

if [ -z "$JAVA_CMD" ]; then
    echo "[!] Error: No working Java runtime found. Please install a JDK or set JAVA_HOME."
    exit 1
fi

CLASSPATH="out/production/Smart-Emergency-Response-System:lib/mysql-connector-j-26.10.0.jar:lib/mysql-connector-j.jar"

"$JAVA_CMD" -cp "$CLASSPATH" com.emergency.Main "$@"
