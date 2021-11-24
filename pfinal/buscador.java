import com.opencsv.CSVReader;
import java.io.FileReader;
import com.opencsv.exceptions.CsvException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.*;

import java.nio.file.Paths;
import java.nio.charset.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;

import org.apache.lucene.search.Query;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;



public class buscador {
    String indexPath = "./index";
    Boolean busquedaCampos[] = new Boolean[]{   false, //author
                                        false, //title
                                        false, //source_title
                                        false, //affiliations
                                        false, //abstract
                                        false, //author_keywords
                                        false  //index_keywords
                                    };

    public void indexSearch(Analyzer analyzer, Similarity similarity){
        IndexReader reader = null;

        try{
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher searcher = new IndexSearcher(reader);

            searcher.setSimilarity(similarity);

            BufferedReader in = null;
            in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            QueryParser parser = new QueryParser("Cuerpo", analyzer);

            while(true){
                // System.out.println("Consulta por campos?: ");
                // if(in.readLine()=="si"){
                
                //     String campos = in.readLine();
                //     String camposArray[] = campos.split(" ");

                //     for(String c : camposArray){
                //         if(c.equals("author")) busquedaCampos[0] = true;

                //         if(c.equals("title")) busquedaCampos[1] = true;

                //         if(c.equals("source_title")) busquedaCampos[2] = true;

                //         if(c.equals("affiliations")) busquedaCampos[3] = true;

                //         if(c.equals("abstract")) busquedaCampos[4] = true;

                //         if(c.equals("author_keywords")) busquedaCampos[5] = true;

                //         if(c.equals("index_keywords")) busquedaCampos[6] = true;
                //     }
                // }

                String line = in.readLine();

                // if(line==null || line.length()==-1) break;
                
                // line = line.trim();

                // if(line.length()==0) break;

                // String lineArray[] = line.split(" ");

                Query query = new MatchAllDocsQuery();


                
                // try{
                //     query = parser.parse(line);
                // } catch(org.apache.lucene.queryparser.classic.ParseException e){
                //     System.out.println("Error en la cadena consulta.");
                //     continue;
                // }

                TopDocs results = searcher.search(query, 100);
                ScoreDoc[] hits = results.scoreDocs;

                long numTotalHits = results.totalHits.value;
                System.out.println(numTotalHits+" documentos encontrados");
                for(int j=0 ; j<hits.length ; j++){
                    Document doc = searcher.doc(hits[j].doc);
                    String author = doc.get("author");
                    String title = doc.get("title");
                    System.out.println("Estoy aquí");

                    System.out.println("−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−");
                    System.out.println("Título: "+title);
                    System.out.println("Autor: "+author);
                    System.out.println();
                }

                if(line.equals("")) break;
            }
        reader.close();
        } catch (IOException e){
            try{
                reader.close();
            } catch (IOException e1){
                e1.printStackTrace();
            }

        e.printStackTrace();
        }
    }

    public static void main(String[] args){
        Analyzer analyzer = new StandardAnalyzer();
        Similarity similarity = new ClassicSimilarity();
        // Similarity similarity = new LMDirichletSimilarity();
        // Similarity similarity = new BM25Similarity();

        buscador busqueda = new buscador();
        busqueda.indexSearch(analyzer, similarity);
    }
}
