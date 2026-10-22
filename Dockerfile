# Stage 1: Build Java backend
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Download servlet API and MySQL connector
RUN mkdir -p /app/lib && \
    curl -o /app/lib/jakarta.servlet-api-6.0.0.jar https://repo1.maven.org/maven2/jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar && \
    curl -o /app/lib/mysql-connector-j-8.4.0.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar

COPY backend/src /app/backend/src

# Compile all classes
RUN mkdir -p /app/classes && \
    javac -cp "/app/lib/*" -d /app/classes $(find /app/backend/src -name "*.java")

# Stage 2: Run Tomcat
FROM tomcat:10.1-jdk17
RUN rm -rf /usr/local/tomcat/webapps/*

# Setup ROOT application structure
COPY frontend/ /usr/local/tomcat/webapps/ROOT/frontend/
COPY --from=builder /app/classes/ /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/
COPY --from=builder /app/lib/mysql-connector-j-8.4.0.jar /usr/local/tomcat/webapps/ROOT/WEB-INF/lib/

# Entry redirect to frontend
RUN echo '<% response.sendRedirect("frontend/index.html"); %>' > /usr/local/tomcat/webapps/ROOT/index.jsp

EXPOSE 8080
CMD ["catalina.sh", "run"]
