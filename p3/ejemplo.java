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
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        Directory dir = FSDirectory.open(Paths.get(indexPath));
        writer = new IndexWriter(dir, iwc);
    }

    public void indexarDocumentos() throws IOException, CsvException {
        CSVReader reader = new CSVReader(new FileReader("./scopus2.csv"));
        reader.skip(1);
        String[] r = reader.readNext();

        do{
            Document doc = new Document();
            doc.add(new TextField("author", r[0], Field.Store.YES));
            doc.add(new TextField("author_id", r[1], Field.Store.YES));
            doc.add(new TextField("title", r[2], Field.Store.YES));
            doc.add(new IntPoint("year", Integer.parseInt(r[3])));
            doc.add(new StoredField("year", Integer.parseInt(r[3])));
            doc.add(new TextField("title_source", r[4], Field.Store.YES));
            doc.add(new StoredField("volume", r[5]));
            doc.add(new StoredField("issue", r[6]));
            doc.add(new StoredField("article_number", r[7]));
            doc.add(new StoredField("page_start", r[8]));
            doc.add(new StoredField("page_end", r[9]));
            doc.add(new StoredField("page_count", r[10]));
            if(!r[11].equals("")){
                doc.add(new SortedNumericDocValuesField("cited_by", Long.parseLong(r[11])));
                doc.add(new StoredField("cited_by", Long.parseLong(r[11])));   
            }
            doc.add(new StringField("doi", r[12], Field.Store.YES));
            doc.add(new StringField("link", r[13], Field.Store.YES));
            doc.add(new TextField("affiliations", r[14], Field.Store.YES));
            doc.add(new TextField("description", r[16], Field.Store.YES));
            doc.add(new TextField("author_keywords", r[17], Field.Store.NO));
            doc.add(new TextField("index_keywords", r[18], Field.Store.NO));
            doc.add(new StringField("doc_type", r[19], Field.Store.NO));
            doc.add(new StoredField("public_status", r[20]));
            doc.add(new StringField("eid", r[23], Field.Store.YES));
            writer.addDocument(doc);
            r = reader.readNext();
        }while(r!=null);
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


