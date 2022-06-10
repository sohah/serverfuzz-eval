#!#!/bin/sh

#assumes relative location to the serverfuzz jar location.
#attempts to install the jars into local maven repo.

echo "installing serverfuzz jar into .m2 local rep"

mvn install:install-file -o \
 -Dfile=/media/soha/DATA/git/serverless/serverfuzz/target/serverfuzz-1.0-SNAPSHOT.jar \
 -DgroupId=edu.umn.cs \
 -DartifactId=serverfuzz \
 -Dversion=1.0-SNAPSHOT \
 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true

echo "compiling maven project"

mvn clean test-compile
