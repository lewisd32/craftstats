#!/bin/bash -e

dir="$(dirname "$0")"
java -classpath "$dir"/'../lib/*' com.lewisd.ksp.craftstats.PartHierarchy "$@"
