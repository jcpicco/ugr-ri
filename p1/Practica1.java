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

    System.out.format("+----------------------------------------+----------------------------+-------------------------+--------------------+%n");
    System.out.format("| Nombre del fichero                     | Tipo de fichero            | Codificacion            | Idioma             |%n");
    System.out.format("+----------------------------------------+----------------------------+-------------------------+--------------------+%n");

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

        String formato = "| %-36s   |  %-25s | %-23s | %-18s |%n";
        System.out.format(formato, info.get(0), info.get(1), info.get(3), info.get(2));
      }

      System.out.format("+----------------------------------------+----------------------------+-------------------------+--------------------+%n");
    }

    else if(args[1].equals("-l")){

    }

    else if(args[1].equals("-t")){

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
