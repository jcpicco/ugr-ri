package com.mkyong.io.csv.opencsv;
import com.opencsv.CSVReader;
import java.io.FileReader;
import com.opencsv.exceptions.CsvException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class IndiceSimple {
    String indexPath = "./index";
    String docPath = "./DataSet";
    boolean create = true;
    private IndexWriter writer;

    public void configurarIndice(Analyzer analyzer, Similarity similarity) throws IOException {
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        
        iwc.setSimilarity(similarity);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        // iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        Directory dir = FSDirectory.open(Pathss.get(indexPath));
        writer = new IndexWriter(dir, iwc);
    }

    public void indexarDocumentos() {
        CSVReader reader = new CSVReader(new FileReader("scopus1.csv"));
        List<String[]> r = reader.readAll();
        r.forEach(x -> System.out.println(Arrays.toString(x)));
        Document doc = new Document();
        Integer start = ...;
        Integer end = ...;
        String aux = cadena.substring(start, end);
        Integer valor = Integer.decode(aux);

        doc.add(new IntPoint("ID", valor));
        doc.add(new StoreField("ID", valor));

        String cuerpo = cadena.substring(start, end);

        doc.add(new TextField("Body", cuerpo, Field.Store.YES));

        writer.addDocument(doc);
    }

    public void close() {
        try {
            writer.commit();
            writer.close();
        } catch (IOException e) {
            System.out.println("Error closing the index.");
        }
    }

    public static void main (String[] args) {
        Analyzer analyzer = new StandardAnalyzer();
        Similarity similarity = new ClassicSimilarity();
        IndiceSimple baseline = new IndiceSimple( ... );

        baseline.configurarIndice(analyzer, similarity);
        IndiceSimple.indexarDocumentos();
        baseline.close();

    }
}


