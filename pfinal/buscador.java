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
import org.apache.lucene.index.Term;

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

            Query query = new MatchAllDocsQuery();
            String line = "";

            while(true){
                System.out.println("\nConsulta por campos?: ");

                if(in.readLine().equals("si")){
                
                    String campos = in.readLine();
                    String camposArray[] = campos.split(" ");
                }
                else{
                    // line = in.readLine();
                    line = "occlusal stress,due";
                    String lineArray[] = line.split(",");

                    if(lineArray.length>1){
                        BooleanQuery.Builder bqbuilder = new BooleanQuery.Builder();
                        BooleanClause bc;
                        PhraseQuery pq;

                        for(String qaux : lineArray){
                            System.out.println(qaux);
                            String aux[] = qaux.split(" ");
                            PhraseQuery.Builder builder = new PhraseQuery.Builder();

                            for(String q2aux : aux){
                                builder.add(new Term("abstract", q2aux));
                            }

                            pq = builder.build();
                            Term[] terms = pq.getTerms();

                            for(Term term : terms)
                                System.out.println(term.toString());

                            bc = new BooleanClause(pq, BooleanClause.Occur.MUST);
                            bqbuilder.add(bc);
                        }

                        query = bqbuilder.build();
                        
                    } else {
                        String aux[] = lineArray[0].split(" ");
                        PhraseQuery.Builder builder = new PhraseQuery.Builder();

                        for(String qaux : aux){
                            builder.add(new Term("abstract", qaux));
                        }

                        query = builder.build();
                    }
                }

                TopDocs results = searcher.search(query, 100);
                ScoreDoc[] hits = results.scoreDocs;

                long numTotalHits = results.totalHits.value;
                System.out.println(numTotalHits+" documentos encontrados");
                // for(int j=0 ; j<hits.length ; j++){
                //     Document doc = searcher.doc(hits[j].doc);
                //     String author = doc.get("author");
                //     String title = doc.get("title");
                //     System.out.println("Estoy aquí");

                //     System.out.println("−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−");
                //     System.out.println("Título: "+title);
                //     System.out.println("Autor: "+author);
                //     System.out.println();
                // }

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
