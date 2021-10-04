/***********************************************************************
*****************************RI: PRÁCTICA 1*****************************
Autores:  Óscar Pérez Tobarra ; Juan Manuel Consigliere Picco
Exec:  java -cp tika-app-1.24.jar:. EjemploSimple ./test/ -d 2>/dev/null
***********************************************************************/
import java.io.File;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import java.util.ArrayList;

public class EjemploSimple {
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

    if(args[1].equals("-d")){
      for (File archivo : archivos) {
        ArrayList<String> info = new ArrayList<>();
        info.add(archivo.getName());
        info.add(tika.detect(archivo));

        String text = tika.parseToString(archivo);
        tika.parse(archivo,metadata);
        
        info.add(detectarLenguaje(text));

        if(metadata.get(Metadata.CONTENT_ENCODING) != null ){
          info.add(metadata.get(Metadata.CONTENT_ENCODING));
        } else{
          info.add(detectarCodificacion(text));
        }

        // for( String n : info){
        //   System.out.println(n);
        // }

        System.out.println(info);
      }
    }

    else if(args[1] == "-l"){

    }

    else if(args[1] == "-t"){

    }

    else{
      System.out.println("Opciones de uso válidas:");
      System.out.println("-d: muestra el título, el tipo de archivo, la codificación y el idioma de los archivos del directorio proporcionado.");
      System.out.println("-l: muestra todos los enlaces extraíbles de los documentos pertenecientes al directorio proporcionado.");
      System.out.println("-t: genera un archivo CSV con las ocurrencias de cada término en los documentos del directorio proporcionado.");
    }
  }
}