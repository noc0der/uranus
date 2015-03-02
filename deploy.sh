#! /bin/sh
if [ -x /usr/libexec/java_home ]; then
	export JAVA_HOME=`/usr/libexec/java_home -v 1.7`
fi
mvn clean deploy -s settings_osc.xml