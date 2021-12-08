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
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyFacetSumValueSource;

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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;



public class buscador {
    String indexPath = "./index";
    String taxoPath = "./taxo";
    String busquedaCampos[] = new String[]  {   "author",
                                                "title",
                                                "source_title",
                                                "affiliations",
                                                "abstract",
                                                "author_keywords",
                                                "index_keywords"
                                            };

    public Query multifieldSearch(String[] campos) throws ParseException{
        BooleanQuery.Builder bqbuilder = new BooleanQuery.Builder();
        BooleanClause bc;
        Query qaux;
        Query query = new MatchAllDocsQuery();

        for(int i = 0; i < campos.length; i++){
            if(!campos[i].isEmpty()){
                if(i == 4){
                    QueryParser parser = new QueryParser(busquedaCampos[i], new EnglishAnalyzer());
                    qaux = parser.parse(campos[i]);
                    bc = new BooleanClause(qaux, BooleanClause.Occur.MUST);
                    System.out.println(qaux.toString());
                    bqbuilder.add(bc);
                }
                else{
                    QueryParser parser = new QueryParser(busquedaCampos[i], new SimpleAnalyzer());
                    qaux = parser.parse(campos[i]);
                    bc = new BooleanClause(qaux, BooleanClause.Occur.MUST);
                    System.out.println(qaux.toString());
                    bqbuilder.add(bc);
                }

                query = bqbuilder.build();
            }
        }

        return query;
    }

    public Query singlefieldSearch(String campo_actual, String busqueda, Integer id) throws ParseException{
        Query query;

        if(campo_actual.equals("abstract")){
            QueryParser parser = new QueryParser(campo_actual, new EnglishAnalyzer());
            query = parser.parse(busqueda);
            System.out.println(query.toString());
        }
        else{
            QueryParser parser = new QueryParser(campo_actual, new SimpleAnalyzer());
            query = parser.parse(busqueda);
            System.out.println(query.toString());
        }

        return query;
    }

    public void indexSearch(Analyzer analyzer, Similarity similarity) throws ParseException{
        IndexReader reader = null;

        try{
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher searcher = new IndexSearcher(reader);
            TaxonomyReader taxoReader = new DirectoryTaxonomyReader(FSDirectory.open(Paths.get(taxoPath)));

            searcher.setSimilarity(similarity);

            BufferedReader in = null;
            in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            Query query = new MatchAllDocsQuery();
            String line = "";

            //Analyzer abstract_analyzer = new EnglishAnalyzer(ENGLISH_STOP_WORDS);

            while(true){

                String[] campos = new String[7];

                for(int i = 0; i < campos.length; i++){
                    System.out.print(busquedaCampos[i]+": ");
                    campos[i] = in.readLine();
                }

                int longitud = 0;
                String campo_actual = "";
                int id = -1;

                for(int i = 0; i < campos.length; i++){
                    if(!campos[i].isEmpty()){
                        longitud++;
                        campo_actual = busquedaCampos[i];
                        id = i;
                    }
                }

                if(longitud > 1)
                    query = multifieldSearch(campos);
                else
                    query = singlefieldSearch(campo_actual, campos[id], id);    
                
                FacetsCollector fc = new FacetsCollector(true);
                TopDocs results = FacetsCollector.search(searcher, query, 100, fc);
                ScoreDoc[] hits = results.scoreDocs;
                FacetsConfig fconfig = new FacetsConfig();
                Facets facetas = new FastTaxonomyFacetCounts(taxoReader, fconfig, fc);
                Facets facetas_score = new TaxonomyFacetSumValueSource(taxoReader,fconfig,fc,DoubleValuesSource.SCORES.fromIntField("cited_by"));
                List<FacetResult> lista = facetas.getAllDims(100);
                List<FacetResult> scores = facetas_score.getAllDims(100);
                System.out.println(lista);
                System.out.println("------------------------------------------");
                System.out.println(scores);

                long numTotalHits = results.totalHits.value;

                DrillDownQuery ddq = new DrillDownQuery(fconfig,query);

                String f = "category--software,doc_type--Article";
                String fArray[] = f.split(",");

                for(String aux : fArray){
                    String faux[] = aux.split("--");
                    
                    ddq.add(faux[0],faux[1]);
                }

                FacetsCollector fc1 = new FacetsCollector(true);;
                TopDocs results2 = FacetsCollector.search(searcher, ddq, 100, fc1);
                ScoreDoc[] hits2 = results2.scoreDocs;
                Facets facetas2 = new FastTaxonomyFacetCounts(taxoReader, fconfig, fc1);

                List<FacetResult> lista2 = facetas2.getAllDims(100);
                System.out.println("------------------------------------------");
                System.out.println(lista2);

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

    public static void main(String[] args) throws ParseException{
        Analyzer analyzer = new SimpleAnalyzer();
        Similarity similarity = new ClassicSimilarity();
        // Similarity similarity = new LMDirichletSimilarity();
        // Similarity similarity = new BM25Similarity();

        buscador busqueda = new buscador();
        busqueda.indexSearch(analyzer, similarity);
    }
}
