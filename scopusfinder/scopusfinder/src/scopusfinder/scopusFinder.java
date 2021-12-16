package scopusfinder;

import java.util.ArrayList;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.*;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.*;

import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyFacetSumValueSource;

import java.nio.file.Paths;
import java.nio.charset.*;
import java.io.IOException;
import java.util.List;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.search.Query;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;

public class scopusFinder {
    String indexPath = "./index";
    String taxoPath = "./taxo";
    String busquedaCampos[] = new String[]  {   "author",
                                                "author",
                                                "title",
                                                "title",
                                                "source_title",
                                                "source_title",
                                                "affiliations",
                                                "affiliations",
                                                "abstract",
                                                "abstract",
                                                "author_keywords",
                                                "author_keywords",
                                                "index_keywords",
                                                "index_keywords"
                                            };

    public Query multifieldSearch(String[] campos) throws ParseException{
        BooleanQuery.Builder bqbuilder = new BooleanQuery.Builder();
        BooleanClause bc;
        Query qaux;
        Query query = new MatchAllDocsQuery();

        for(int i = 0; i < campos.length; i++){
            if(i%2==0){
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
            } else{
                if(!campos[i].isEmpty()){
                    if(i == 5){
                        QueryParser parser = new QueryParser(busquedaCampos[i], new EnglishAnalyzer());
                        qaux = parser.parse(campos[i]);
                        bc = new BooleanClause(qaux, BooleanClause.Occur.MUST_NOT);
                        System.out.println(qaux.toString());
                        bqbuilder.add(bc);
                    }
                    else{
                        QueryParser parser = new QueryParser(busquedaCampos[i], new SimpleAnalyzer());
                        qaux = parser.parse(campos[i]);
                        bc = new BooleanClause(qaux, BooleanClause.Occur.MUST_NOT);
                        System.out.println(qaux.toString());
                        bqbuilder.add(bc);
                    }

                    query = bqbuilder.build();
                }
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

    public String indexSearch(Analyzer analyzer, Similarity similarity, String[] campos, String[] facets) throws ParseException{
        IndexReader reader = null;
        String output = "";

        try{
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher searcher = new IndexSearcher(reader);
            TaxonomyReader taxoReader = new DirectoryTaxonomyReader(FSDirectory.open(Paths.get(taxoPath)));

            searcher.setSimilarity(similarity);

            BufferedReader in = null;
            in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            Query query;
            String line = "";

            //Analyzer abstract_analyzer = new EnglishAnalyzer(ENGLISH_STOP_WORDS);

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
                else if(longitud == 1)
                    query = singlefieldSearch(campo_actual, campos[id], id);
                else
                    query = new MatchAllDocsQuery();

                FacetsConfig fconfig = new FacetsConfig();
                DrillDownQuery ddq = new DrillDownQuery(fconfig,query);

                for(String aux : facets){
                    System.out.println(aux);
                    String faux[] = aux.split("=");

                    ddq.add(faux[0],faux[1]);
                }

                FacetsCollector fc = new FacetsCollector(true);
                TopDocs results = FacetsCollector.search(searcher, ddq, 100, fc);
                long numTotalHits = results.totalHits.value;
                ScoreDoc[] hits = results.scoreDocs;
                Facets facetas = new FastTaxonomyFacetCounts(taxoReader, fconfig, fc);
                Facets facetas_score = new TaxonomyFacetSumValueSource(taxoReader,fconfig,fc,DoubleValuesSource.SCORES.fromIntField("cited_by"));

                List<FacetResult> lista = facetas.getAllDims(100);
                List<FacetResult> scores = facetas_score.getAllDims(100);
                System.out.println(lista);
                System.out.println("------------------------------------------");
                System.out.println(scores);

                System.out.println(numTotalHits+" documentos encontrados");

                for(int i=0 ; i<hits.length ; i++){
                    Document doc = searcher.doc(hits[i].doc);
                    output += "Documento "+(i+1);
                    output += "\n\tAuthor: " + doc.get("author");
                    if(!doc.get("title").equals(""))
                        output += "\n\tTitle: " + doc.get("title");
                    if(!doc.get("category").equals(""))
                        output += "\n\tCategory: " + doc.get("category");
                    if(!doc.get("year").equals(""))
                        output += "\n\tyear: " + doc.get("year");
                    if(!doc.get("volume").equals(""))
                        output += "\n\tVolume: " + doc.get("volume");
                    if(!doc.get("issue").equals(""))
                        output += "\n\tIssue: " + doc.get("issue");
                    if(!doc.get("doc_type").equals(""))
                        output += "\n\tDocument type: " + doc.get("doc_type");
                    if(!doc.get("article_number").equals(""))
                        output += "\n\tArticle number: " + doc.get("article_number");
                    if(!doc.get("page_start").equals(""))
                        output += "\n\tPage start: " + doc.get("page_start");
                    if(!doc.get("page_end").equals(""))
                        output += "\n\tPage end: " + doc.get("page_end");
                    if(!doc.get("page_count").equals(""))
                        output += "\n\tPage count: " + doc.get("page_count");
                    if(!doc.get("doi").equals(""))
                        output += "\n\tDOI: " + doc.get("doi");
                    if(!doc.get("link").equals(""))
                        output += "\n\tLink: " + doc.get("link");
                    if(!doc.get("affiliations").equals(""))
                        output += "\n\tAffiliations: " + doc.get("affiliations");
                    if(!doc.get("abstract").equals(""))
                        output += "\n\tAbstract: " + doc.get("abstract");
                    if(!doc.get("public_status").equals(""))
                        output += "\n\tPublic status: " + doc.get("public_status");
                    if(!doc.get("eid").equals(""))
                        output += "\n\tEID: " + doc.get("eid") + "\n\n";
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
        
        return output;
    }

    public static String main(String[] args, String[] args2) throws ParseException{
        Analyzer analyzer = new SimpleAnalyzer();
        Similarity similarity = new ClassicSimilarity();
        // Similarity similarity = new LMDirichletSimilarity();
        // Similarity similarity = new BM25Similarity();

        scopusFinder busqueda = new scopusFinder();
        String output = busqueda.indexSearch(analyzer, similarity, args, args2);
        
        return output;
    }
}
