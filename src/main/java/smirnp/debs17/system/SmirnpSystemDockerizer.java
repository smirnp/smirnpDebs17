package smirnp.debs17.system;

import com.agtinternational.hobbit.benchmarks.sml.SMLCsvSystemNegativeRunner;
import com.agtinternational.hobbit.sdk.docker.HobbitDockersBuilder;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hobbit.core.Constants.SYSTEM_PARAMETERS_MODEL_KEY;

public class SmirnpSystemDockerizer extends HobbitDockersBuilder {
    private Class runnerClass;

    public SmirnpSystemDockerizer() {
        super("smirnp-debs17-docker");
        imageName("git.project-hobbit.eu:4567/smirnp/smirnp-debs17");
        containerName("cont_name_smirnp-debs17");
        runnerClass(SMLCsvSystemNegativeRunner.class);
    }

    HobbitDockersBuilder runnerClass(Class runnerClass) {
        this.runnerClass = runnerClass;
        return this;
    }


    public HobbitDockersBuilder parameters(String parameters) {
        addEnvironmentVariable(SYSTEM_PARAMETERS_MODEL_KEY, parameters);
        return this;
    }

    protected Reader getDockerFileContent() {
//        String content = String.format(
//                "FROM java\n" +
//                        "RUN mkdir -p /usr/src/sml\n" +
//                        "WORKDIR /usr/src/sml\n" +
//                        "ADD ./sml-benchmark-1.0-SNAPSHOT.jar /usr/src/sml\n" +
//                        "ADD ./original-wm-data-gen-1.0-SNAPSHOT.jar /usr/src/sml\n" +
//                        "CMD [\"java\", \"-Xmx250g\", \"-cp\", \"sml-benchmark-1.0-SNAPSHOT.jar:original-wm-data-gen-1.0-SNAPSHOT.jar\", \"%s\"]",
//                runnerClass.getCanonicalName());
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get("Dockerfile")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new StringReader(content);
    }
}
