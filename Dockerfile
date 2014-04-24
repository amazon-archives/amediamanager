# The base image on which this image is based
FROM jolokia/tomcat-7.0

# Add Application WAR
RUN rm -rf /opt/tomcat/webapps/docs /opt/tomcat/webapps/examples /opt/tomcat/webapps/ROOT
ADD target/amediamanager.war /opt/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["/opt/tomcat/bin/catalina.sh", "run"]