#!/usr/bin/env sh

tmp_dir=$(mktemp -d -t mvn-XXXXXXXXXX)
cp javacppdummy.pom $tmp_dir/pom.xml
cd $tmp_dir
mvn -Djavacpp.platform=$1 --update-snapshots compile
rm -rf $tmp_dir
