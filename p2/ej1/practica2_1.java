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
import java.io.PrintWriter;

public class practica2_1{
	public static void Analizador(Analyzer analyzer, String nombre_analizador, String texto, String nombre_archivo) throws IOException{
		TokenStream stream = analyzer.tokenStream(null, texto);
		Map<String, Integer> ocurrencias = new HashMap<String, Integer>();

		stream.reset(); //Se le llama antes de usar incrementToken()
		while(stream.incrementToken()){ //Itera de token en token
			String palabra = stream.getAttribute(CharTermAttribute.class).toString(); //Pasamos el apartado de texto del token a String
		    if(!ocurrencias.containsKey(palabra))
		        	ocurrencias.put(palabra, 1);
		    else{
		    	int valor = ocurrencias.get(palabra);
		        ocurrencias.replace(palabra, valor, valor+1);
		    } 
		}
			
		stream.end(); //Se le llama cuando se termina de iterar
		stream.close(); //Liberas los recursos asociados al stream

		List<Map.Entry<String, Integer>> words = ocurrencias.entrySet().stream().collect(Collectors.toList());
        Collections.sort(words, new Comparator<Map.Entry<String, Integer>>(){
         	public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2){
           		return o2.getValue().compareTo(o1.getValue());
          	}
        });

        PrintWriter writer = new PrintWriter(new File("./csv/" + nombre_archivo.substring(0, nombre_archivo.lastIndexOf("."))+"-" + nombre_analizador + ".csv"));
        writer.write("Text;Size\n");

        for(Map.Entry<String, Integer> i : words){
          writer.write(i.getKey()+";"+i.getValue()+"\n");
        }
        
        writer.close();

        System.out.println("Finalizado procesamiento del analizador " + nombre_analizador + "analyzer");
	}

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

        Analizador(new WhitespaceAnalyzer(), "whitespace", text, nombre_archivo);
        Analizador(new SimpleAnalyzer(), "simple", text, nombre_archivo);
        Analizador(new StopAnalyzer(), "stop", text, nombre_archivo);
        Analizador(new StandardAnalyzer(), "standard", text, nombre_archivo);
	}
}