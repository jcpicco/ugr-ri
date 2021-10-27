package com.mkyong.io.csv.opencsv;
import com.opencsv.CSVReader;
import java.io.FileReader;
import com.opencsv.exceptions.CsvException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ejemplo {
    String indexPath = "./index";
    String docPath = "./docs";
    boolean create = true;
    private IndexWriter writer;

    public void configurarIndice(Analyzer analyzer, Similarity similarity) throws IOException {
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        
        iwc.setSimilarity(similarity);
        // iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        Directory dir = FSDirectory.open(Paths.get(indexPath));
        writer = new IndexWriter(dir, iwc);
    }

    public void indexarDocumentos() throws IOException, CsvException {
        CSVReader reader = new CSVReader(new FileReader("./scopus1.csv"));
        List<String[]> r = reader.readAll();
        r.forEach(x -> System.out.println(Arrays.toString(x)));

        Document doc = new Document();

        for(int i = 1 ; i < r.size() ; i++){
            doc.add(new TextField(r.get(0)[0], r.get(i)[0], Field.Store.YES));
            doc.add(new TextField(r.get(0)[1], r.get(i)[1], Field.Store.YES));
            doc.add(new TextField(r.get(0)[2], r.get(i)[2], Field.Store.YES));
            doc.add(new NumericDocValuesField(r.get(0)[3], Long.parseLong(r.get(i)[3])));
        }

        writer.addDocument(doc);
    }

    public void close() throws IOException, CsvException {
        try {
            writer.commit();
            writer.close();
        } catch (IOException e) {
            System.out.println("Error closing the index.");
        }
    }

    public static void main(String[] args) throws IOException, CsvException {
        Analyzer analyzer = new StandardAnalyzer();
        Similarity similarity = new ClassicSimilarity();
        ejemplo baseline = new ejemplo();

        baseline.configurarIndice(analyzer, similarity);
        baseline.indexarDocumentos();
        baseline.close();
    }
}


