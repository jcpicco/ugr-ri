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
        CSVReader reader = new CSVReader(new FileReader("./scopus2.csv"));
        List<String[]> r = reader.readAll();
        // r.forEach(x -> System.out.println(Arrays.toString(x)));

        Document doc = new Document();

        for(int i = 1 ; i < r.size() ; i++){
            doc.add(new TextField(r.get(0)[0], r.get(i)[0], Field.Store.YES));          //autor(es)
            doc.add(new TextField(r.get(0)[1], r.get(i)[1], Field.Store.YES));          //id autor(es)
            doc.add(new TextField(r.get(0)[2], r.get(i)[2], Field.Store.YES));          //título
            doc.add(new IntPoint(r.get(0)[3], Integer.parseInt(r.get(i)[3])));          //año
            doc.add(new StoredField(r.get(0)[3], Integer.parseInt(r.get(i)[3])));
            doc.add(new TextField(r.get(0)[4], r.get(i)[4], Field.Store.YES));          //título fuente
            doc.add(new StoredField(r.get(0)[5], r.get(i)[5]));                         //volumen
            doc.add(new StoredField(r.get(0)[6], r.get(i)[6]));                         //número revistad
            doc.add(new StoredField(r.get(0)[7], r.get(i)[7]));                         //número artículo
            doc.add(new StoredField(r.get(0)[8], r.get(i)[8]));                         //página inicio
            doc.add(new StoredField(r.get(0)[9], r.get(i)[9]));                         //página fin
            doc.add(new StoredField(r.get(0)[10], r.get(i)[10]));        //número páginas
            if(!r.get(i)[11].equals("")){
                System.out.print(" || "+Integer.parseInt(r.get(i)[11]));
                doc.add(new SortedNumericDocValuesField(r.get(0)[11], Long.parseLong(r.get(i)[11])));//número citas
                // doc.add(new IntPoint(r.get(0)[11], Integer.parseInt(r.get(i)[11])));//número citas
                doc.add(new StoredField(r.get(0)[11], Long.parseLong(r.get(i)[11])));   
            }
            doc.add(new StringField(r.get(0)[12], r.get(i)[12], Field.Store.YES));      //DOI
            doc.add(new StringField(r.get(0)[13], r.get(i)[13], Field.Store.YES));      //link
            doc.add(new TextField(r.get(0)[14], r.get(i)[14], Field.Store.YES));        //afiliaciones
            doc.add(new TextField(r.get(0)[16], r.get(i)[16], Field.Store.YES));        //abstract
            doc.add(new TextField(r.get(0)[17], r.get(i)[17], Field.Store.NO));        //keywords autor
            doc.add(new TextField(r.get(0)[18], r.get(i)[18], Field.Store.NO));        //keywords índice
            doc.add(new StringField(r.get(0)[19], r.get(i)[19], Field.Store.NO));      //tipo documento
            doc.add(new StoredField(r.get(0)[20], r.get(i)[20]));                       //fase publicación
            doc.add(new StringField(r.get(0)[23], r.get(i)[23], Field.Store.YES));      //EID
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


