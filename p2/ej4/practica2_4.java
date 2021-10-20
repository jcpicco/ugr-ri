import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import java.util.*;

public class practica2_4{
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

        Filtro(new FourLettersFilter(new WhitespaceAnalyzer().tokenStream(null, text)), "FourLettersFilter");
	}
}