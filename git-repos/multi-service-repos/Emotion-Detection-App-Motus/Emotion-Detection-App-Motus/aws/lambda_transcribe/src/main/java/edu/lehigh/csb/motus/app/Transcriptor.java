package edu.lehigh.csb.motus.app;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

public class Transcriptor {

	String doTranscription(InputStream stream) throws IOException{
		String total = "";
		
	     Configuration configuration = new Configuration();

	        configuration
	                .setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
	        configuration
	                .setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
	        configuration
	                .setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

	        StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(
	                configuration);
	        //InputStream stream = new FileInputStream(new File("src/audio/10001-90210-01803.wav"));

	        recognizer.startRecognition(stream);
	        SpeechResult result;
	        while ((result = recognizer.getResult()) != null) {
	            System.out.format("Hypothesis: %s\n", result.getHypothesis());
	            total +=  result.getHypothesis()+" ";
	        }
	        recognizer.stopRecognition();
		return total;
		
	}
	
}
