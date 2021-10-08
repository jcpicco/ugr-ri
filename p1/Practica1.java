import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.io.InputStream;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.parser.AutoDetectParser;
import org.xml.sax.ContentHandler;
import org.apache.tika.sax.Link;
import org.apache.tika.parser.ParseContext;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.io.PrintWriter;


public class Practica1 {
  public static String detectarLenguaje (String text) {
      LanguageDetector identifier = new OptimaizeLangDetector().loadModels();
      LanguageResult idioma = identifier.detect(text);
      return idioma.getLanguage();
  }

  public static String detectarCodificacion (String text) {
      CharsetDetector detector = new CharsetDetector();
      detector.setText(text.getBytes());
      CharsetMatch encoding = detector.detect();
      return encoding.getName();
  }

  public static void main(String[] args) throws Exception {
    Tika tika = new Tika();
    Metadata metadata = new Metadata();
    File dir = new File(args[0]);
    File[] archivos = dir.listFiles();

    if(args.length != 2){
      System.out.println("Número incorrecto de parámetros de entrada.");
      System.exit(0);
    }

    if(args[1].equals("-d")){
      System.out.format("| Título                                 | Tipo de fichero            | Codificacion            | Idioma             |%n"+
                        "|----------------------------------------|----------------------------|-------------------------|--------------------|%n");
      
      for (File archivo : archivos) {
        ArrayList<String> info = new ArrayList<>(); //Estructura para almacenar los datos que se buscan
        info.add(archivo.getName()); //Obtenemos el nombre del fichero
        info.add(tika.detect(archivo)); //Obtenemos el tipo de fichero

        String text = tika.parseToString(archivo); //Se parsea el fichero a texto plano
        tika.parse(archivo,metadata); //Parseamos el fichero de texto plano
        
        info.add(detectarLenguaje(text)); //Detectamos el lenguaje escrito del documento (implementado arriba)

        /*
        Obtenemos la codificación a partir de los metadatos ó, en caso de que no se incluya la codificación en los metadatos, con
        la función detectarCodificación() (implementada arriba)
        */
        if(metadata.get(Metadata.CONTENT_ENCODING) != null ){
          info.add(metadata.get(Metadata.CONTENT_ENCODING)); 
        } else{
          info.add(detectarCodificacion(text));
        }

        String formato = "| %-36s   |  %-25s | %-23s | %-18s |%n";
        System.out.format(formato, info.get(0), info.get(1), info.get(3), info.get(2));
      }
    }

    else if(args[1].equals("-l")){
      for (File archivo : archivos) {
        InputStream input = new FileInputStream(archivo); // Convertimos el archivo File a InputStream (Admitido por parse())
        LinkContentHandler link = new LinkContentHandler(); // Estructura de almacenamiento de enlaces
        ParseContext contexto = new ParseContext(); // Indica el contexto en que trabaja
        AutoDetectParser parser = new AutoDetectParser(); // Detecta el tipo de documento y parsea en consecuencia
      
        parser.parse(input, link, metadata, contexto); // Lee el documento, extrayendo los enlaces, los metadatos y el contexto.
        List<Link> links = link.getLinks();

        System.out.println("Archivo: "+archivo.getName());
        
        if(links.isEmpty())
          System.out.println("No se han encontrado enlaces.");
        else
          for(Link i : links){
            if(!i.getUri().toString().contains("@") && !i.getUri().toString().equals(""))
              System.out.println("\t"+i.getUri());
          }
        
        System.out.println();
      }
    }

    else if(args[1].equals("-t")){
      for (File archivo : archivos) {
        String text = tika.parseToString(archivo).toLowerCase(); //Se parsea el fichero a texto plano
        tika.parse(archivo,metadata); //Parseamos el fichero de texto plano

        /*
        Split: Añade a un array de String las palabras, separándolas por los caracteres que hemos especificado.
        */
        String[] split = text.split("\\s+|\\.|\\,|\\;|\\?|\\!|\\¿|\\¡|\\(|\\)|\\{|\\}|\\[|\\]|\\:|\\|"+
                                    "|\\-|\\–|\\−|\\+|\\_|\\<|\\>|\\/|\\=|\"|\'|\\#|\\$|\\%|\\—|\\\\|\\•|\\’"); 
        Map<String, Integer> ocurrencias = new HashMap<String, Integer>();

        for(String palabra: split){
          int contador = 0;
          if(!ocurrencias.containsKey(palabra)){
            for(int i = 0; i < split.length; i++){
              if(palabra.equals(split[i]))
                contador++;
            }

            ocurrencias.put(palabra, contador);
          }
        }

        List<Map.Entry<String, Integer>> words = ocurrencias.entrySet().stream().collect(Collectors.toList());
        Collections.sort(words, new Comparator<Map.Entry<String, Integer>>(){
          public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2){
            return o2.getValue().compareTo(o1.getValue());
          }
        });

        ArrayList<Map.Entry<String, Integer>> aborrar = new ArrayList<>();

        for(Map.Entry<String, Integer> i : words){
          if(i.getKey().matches(".*\\d.*") || i.getKey().equals("")){
            aborrar.add(i);
          }
        }
        
        for(Map.Entry<String, Integer> i : aborrar){
          words.remove(i);
        }


        System.out.println("Archivo: "+archivo.getName());
        System.out.println("Text;Size");

        for(Map.Entry<String, Integer> i : words){
          System.out.println(i.getKey()+";"+i.getValue()); 
        }

        System.out.println();

        PrintWriter writer = new PrintWriter(new File("./csv/"+archivo.getName().substring(0, archivo.getName().lastIndexOf("."))+".csv"));
        writer.write("Text;Size\n");

        for(Map.Entry<String, Integer> i : words){
          writer.write(i.getKey()+";"+i.getValue()+"\n");
        }
        
        writer.close();
      }
    }

    else{
      System.out.println("Error, opción introducida no válida.");
      System.out.println("Opciones de uso válidas:");
      System.out.println("-d: muestra el título, el tipo de archivo, la codificación y el idioma de los archivos del directorio proporcionado.");
      System.out.println("-l: muestra todos los enlaces extraíbles de los documentos pertenecientes al directorio proporcionado.");
      System.out.println("-t: genera un archivo CSV con las ocurrencias de cada término en los documentos del directorio proporcionado.");
    }
  }
}
