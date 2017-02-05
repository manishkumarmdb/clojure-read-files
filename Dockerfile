FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/clojure-read-files-0.0.1-SNAPSHOT-standalone.jar /clojure-read-files/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/clojure-read-files/app.jar"]
