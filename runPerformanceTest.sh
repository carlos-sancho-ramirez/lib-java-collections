#!/bin/sh
./gradlew clean compilePerformanceTestJava
java -cp build/classes/java/main:build/classes/java/performanceTest sword.collections.SimpleTest
