FROM java

ADD target/smirnp-debs17-1.0-SNAPSHOT.jar /smirnp-debs17/smirnp-debs17-1.0-SNAPSHOT.jar

WORKDIR /smirnp-debs17

CMD java -Xms100G -Xmx200G -cp smirnp-debs17-1.0-SNAPSHOT.jar org.hobbit.core.run.ComponentStarter smirnp.debs17.SmirnpSystemAdapter


