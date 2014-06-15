#!/bin/sh

# * * * * * $HOME/[PATH]/tomcat-autostart.sh

export CATALINA_HOME=/opt/apache-tomcat-8.0.8/
export JAVA_HOME=/opt/jdk1.7.0_51/
export JAVA_OPTS="-Djava.awt.headless=true -Dfile.encoding=UTF-8 -server -Xms768m -Xmx768m -XX:NewSize=320m -XX:MaxNewSize=320m -XX:PermSize=320m -XX:MaxPermSize=320m -XX:+DisableExplicitGC"

PROCS=`ps eww | grep java | grep -v grep | grep -o -F org.apache.catalina.startup.Bootstrap | wc -l` 

case $PROCS in 0) 
    echo "Tomcat does not appear to be running. Starting Tomcat..."

    cd $CATALINA_HOME/bin
    ./catalina.sh start

exit 1 ;; 1) 

exit 0 ;; *) 
    echo "More than one Tomcat appears to be running. Restarting Tomcat..." $CATALINA_HOME/bin/catalina.sh stop && $CATALINA_HOME/bin/catalina.sh start
    exit 2 
    ;; 
    esac
