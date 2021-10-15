import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.commongrams.CommonGramsFilter;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.standard.StandardFilter;

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
import java.io.PrintWriter;

public class practica2_2{
	public static void Filtro(TokenStream stream, String nombre_filtro) throws IOException{
		stream.reset(); //Se le llama antes de usar incrementToken()

        System.out.println("Filtro: " + nombre_filtro);
		while(stream.incrementToken()){ //Itera de token en token
			System.out.print(" || "+stream.getAttribute(CharTermAttribute.class).toString()); //Pasamos el apartado de texto del token a String
		}
		System.out.print("\n\n");

		stream.end(); //Se le llama cuando se termina de iterar
		stream.close(); //Liberas los recursos asociados al stream
	}

	public static void main(String[] args) throws IOException{
		Tika tika = new Tika();
        Metadata metadata = new Metadata();
		File archivo = new File("./test.txt");

		String text = new String();

		try{
			text = tika.parseToString(archivo);
		}catch (Exception e){ 
			System.out.println("No se puede parsear...\n\n");
		}

        tika.parse(archivo,metadata); //Parseamos el fichero de texto plano
        String nombre_archivo = archivo.getName();

        Filtro(new StandardFilter(new WhitespaceAnalyzer().tokenStream(null, text)), "StandardFilter");
		Filtro(new LowerCaseFilter(new WhitespaceAnalyzer().tokenStream(null, text)), "LowerCaseFilter");
		Filtro(new StopFilter(new WhitespaceAnalyzer().tokenStream(null, text), StandardAnalyzer.ENGLISH_STOP_WORDS_SET), "StopFilter");
		Filtro(new SnowballFilter(new WhitespaceAnalyzer().tokenStream(null, text), "English"), "SnowballFilter");
		Filtro(new ShingleFilter(new WhitespaceAnalyzer().tokenStream(null, text)), "ShingleFilter");
		Filtro(new EdgeNGramTokenFilter(new WhitespaceAnalyzer().tokenStream(null, text),2,4), "EdgeNGramTokenFilter");
		Filtro(new NGramTokenFilter(new WhitespaceAnalyzer().tokenStream(null, text),2,4), "NGramTokenFilter");
		Filtro(new CommonGramsFilter(new WhitespaceAnalyzer().tokenStream(null, text)), "CommonGramsFilter");
		Filtro(new SynonymFilter(new WhitespaceAnalyzer().tokenStream(null, text)), "SynonymFilter");
	}
}