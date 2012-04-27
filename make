test -d build || mkdir -p build
#rm -rf build/*
export CLASSPATH=lib/p2pCore.jar:lib/protobuf-java-2.3.0.jar:lib/bcprov-jdk15on-147-ext.jar:lib/commons-codec-1.6.jar
ant compile
scalac -d build -deprecation -classpath build:$CLASSPATH src/*.scala
./makejar

