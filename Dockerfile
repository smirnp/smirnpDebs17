FROM java
RUN mkdir -p /usr/src/debs
WORKDIR /usr/src/debs
ADD /smirnp-debs17-1.0-SNAPSHOT.jar /usr/src/debs
CMD ["java", "-cp", "smirnp-debs17-1.0-SNAPSHOT.jar", "smirnp.debs17.system.SmirnpDebsSystemRunner"]
