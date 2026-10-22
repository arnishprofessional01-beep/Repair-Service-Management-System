FROM tomcat:10.1-jdk17

# Tomcat ke default folders saaf karo
RUN rm -rf /usr/local/tomcat/webapps/*

# Frontend aur backend structure ko ROOT application me copy karo
COPY frontend/ /usr/local/tomcat/webapps/ROOT/frontend/
COPY backend/WEB-INF/ /usr/local/tomcat/webapps/ROOT/WEB-INF/

# Default entry page redirect
RUN echo '<% response.sendRedirect("frontend/index.html"); %>' > /usr/local/tomcat/webapps/ROOT/index.jsp

EXPOSE 8080
CMD ["catalina.sh", "run"]