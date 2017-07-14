FROM java
#ENV HOBBIT_SYSTEM_URI=http://project-hobbit.eu/resources/debs2017/debsparrotsystemexample
ENV HOBBIT_SYSTEM_URI=http://project-hobbit.eu/resources/debs2017/smirnp-debs17
ENV HOBBIT_RABBIT_HOST=rabbit
ENV HOBBIT_SESSION_ID=exp1
ENV SYSTEM_PARAMETERS_MODEL={}
ENV HOBBIT_EXPERIMENT_URI=http://example.com/exp1
RUN mkdir -p /usr/src/debs
RUN mkdir -p /usr/src/debs/data
WORKDIR /usr/src/debs
ADD /target/smirnp-debs17-1.0-SNAPSHOT.jar /usr/src/debs
COPY /data/* /usr/src/debs/data/
CMD ["java", "-cp", "smirnp-debs17-1.0-SNAPSHOT.jar", "smirnp.debs17.SmirnpDebsSystemRunner"]
