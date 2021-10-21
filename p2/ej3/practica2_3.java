import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.ngram.NGramFilterFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import java.util.*;

public class practica2_3{
	public static void main(String[] args) throws IOException{
		Tika tika = new Tika();
        Metadata metadata = new Metadata();
		File archivo = new File("../test/hamlet.txt");

		String text = new String();

		try{
			text = tika.parseToString(archivo);
		}catch (Exception e){ 
			System.out.println("No se puede parsear...\n\n");
		}

        tika.parse(archivo,metadata); //Parseamos el fichero de texto plano
        String nombre_archivo = archivo.getName();

		Analyzer analyzer = CustomAnalyzer.builder()
   			.withTokenizer("standard")
   			.addTokenFilter("lowercase")
   			.addTokenFilter("apostrophe")
   			.addTokenFilter("nGram","minGramSize","4","maxGramSize","4")
   			.build();

		TokenStream stream = analyzer.tokenStream(null, text);

		stream.reset(); //Se le llama antes de usar incrementToken()
		while(stream.incrementToken())
		    System.out.print(" || "+stream.getAttribute(CharTermAttribute.class).toString());

		System.out.println();
			
		stream.end(); //Se le llama cuando se termina de iterar
		stream.close(); //Liberas los recursos asociados al stream
	}
}