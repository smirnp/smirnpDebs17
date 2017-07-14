package smirnp.debs17;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 7/8/17.
 */
public class Emitter {

    //private List<String> lines;
    private final Processor processor;
    private final Path path;
    private final int linesPerSend;

    public Emitter(Processor processor1, Path _path, int _linesPerSend){
        processor = processor1;
        path = _path;
        linesPerSend = _linesPerSend;
    }

    public void start(){
        try {
            //byte[] bytes = Files.readAllBytes(path);
            List<String> lines = Files.readAllLines(path);

            int totalSent=0;
            while(totalSent<lines.size()){
                List<String> toSend = new ArrayList<>();
                for (int i = totalSent; i < totalSent+linesPerSend; i++){
                    toSend.add(lines.get(i));
                }
                String joinedLines = String.join("",toSend);
                emit(joinedLines);
                //byte[] bytes = String.join("\n",toSend).getBytes();
                //emitBytes(bytes);
                totalSent+=linesPerSend;
            }


            String test="123";
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            lines = Files.readAllLines(path);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        for(String line : lines)
//            emitBytes(line);
    }

    private void emit(String line){
        processor.processTuple(line);
    }

    private void emitBytes(byte[] bytes){
        processor.processTupleInBytes(bytes);
    }



}
