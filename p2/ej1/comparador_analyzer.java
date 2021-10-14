import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import java.util.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class comparador_analyzer{
	public static void main(String[] args) throws IOException{
		Analyzer whitespace = new WhitespaceAnalyzer();
		Analyzer simple = new SimpleAnalyzer();
		Analyzer stop = new StopAnalyzer();
		Analyzer standard = new StandardAnalyzer();

		Tika tika = new Tika();
        Metadata metadata = new Metadata();
		File dir = new File(args[0]);
		File[] archivos = dir.listFiles();

		for(File archivo: archivos){
			String text = new String();

			try{
				text = tika.parseToString(archivo);
			}catch (Exception e){ 
				System.out.println("No se puede parsear...\n\n");
				continue;
			}

        	tika.parse(archivo,metadata); //Parseamos el fichero de texto plano

			TokenStream whitespaceStream = whitespace.tokenStream(null, text);
			Map<String, Integer> ocurrencias = new HashMap<String, Integer>();

			whitespaceStream.reset();
			while(whitespaceStream.incrementToken()){
				String palabra = whitespaceStream.getAttribute(CharTermAttribute.class).toString();
		        if(!ocurrencias.containsKey(palabra))
		        	ocurrencias.put(palabra, 1);
		        else{
		        	int valor = ocurrencias.get(palabra);
		            ocurrencias.replace(palabra, valor, valor+1);
		        } 
		    }
			whitespaceStream.end();
			whitespaceStream.close();

			List<Map.Entry<String, Integer>> words = ocurrencias.entrySet().stream().collect(Collectors.toList());
        	Collections.sort(words, new Comparator<Map.Entry<String, Integer>>(){
          		public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2){
            		return o2.getValue().compareTo(o1.getValue());
          		}
        	});
		}
	}
}