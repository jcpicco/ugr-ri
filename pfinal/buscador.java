import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

public class buscador {
    String indexPath = "./index";

    public void indexSearch(Analyzer analyzer, Similarity similarity){
        IndexReader reader = null;

        try{
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher searcher = new IndexSearcher(reader);

            Searcher.setSimilarity(similarity);

            BufferedReader in = null;
            in = new BufferedReader(new InputStreamReader(System.in.StandardCharsets.UTF_8));
            QueryParser parser = new QueryParser("Cuerpo", analyzer);

            while(true){
                System.out.println("Consulta?: ");

                String line = in.readLine();

                if(line==null || line.length()==-1) break;

                line = line.trim();

                if(line.length()==0) break;

                Query query;
                try{
                    query = parser.parse(line);
                } catch(org.apache.lucene.queryparser.classic.ParseException e){
                    System.out.println("Error en la cadena consulta.");
                    continue;
                }

                TopDocs results = searcher.search(query, 100);
                ScoreDoc[] hits = results.scoreDocs;

                int numTotalHits = results.totalHits;
                System.out.println(numTotalHits+" documentos encontrados");

                for(int j=0 ; j<hits.length ; j++){
                    Document doc = searcher.doc(hits[j].doc);
                    String cuerpo = doc.get("Cuerpo");
                    Integer id = doc.getField("ID").numericValue().intValue();

                    System.out.println("−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−");
                    System.out.println("ID: "+id);
                    System.out.println("Cuerpo: "+cuerpo);
                    System.out.println();
                }

                if(line.equals("")) break;
            }
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

    public static void main(String[] args){
        Analyzer analyzer = new StandardAnalyzer();
        Similarity similarity = new ClassicSimilarity();
        // Similarity similarity = new LMDirichletSimilarity();
        // Similarity similarity = new BM25Similarity();

        indexSearch(analyzer, similarity);
    }
}
