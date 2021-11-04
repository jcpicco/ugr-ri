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
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileInputStream;

public class indiceSimple {
    String indexPath = "./index";
    String docPath = "./docs";
    boolean create = true;
    private IndexWriter writer;
    Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();

    indiceSimple(){
        analyzerPerField.put("author", new SimpleAnalyzer());
        analyzerPerField.put("author_id", new ClassicAnalyzer());
        analyzerPerField.put("title", new SimpleAnalyzer());
        analyzerPerField.put("source_title", new SimpleAnalyzer());
        analyzerPerField.put("doi", new KeywordAnalyzer());
        analyzerPerField.put("affiliations", new SimpleAnalyzer());
        analyzerPerField.put("abstract", new EnglishAnalyzer());
        analyzerPerField.put("author_keywords", new SimpleAnalyzer());
        analyzerPerField.put("index_keywords", new SimpleAnalyzer());
        analyzerPerField.put("doc_type", new KeywordAnalyzer());
    }

    public void configurarIndice(Analyzer analyzer, Similarity similarity, String path) throws IOException {
        PerFieldAnalyzerWrapper analyzerWrapper = new PerFieldAnalyzerWrapper(analyzer, analyzerPerField);
        
        IndexWriterConfig iwc = new IndexWriterConfig(analyzerWrapper);
        
        iwc.setSimilarity(similarity);
        // iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        Directory dir = FSDirectory.open(Paths.get(indexPath+"/"+path));
        writer = new IndexWriter(dir, iwc);
    }

    public void indexarDocumentos(File archivo) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new FileReader(docPath+"/"+archivo.getName()));
        reader.skip(1);
        String[] r = reader.readNext();
        
        do{
            Document doc = new Document();
            doc.add(new TextField("author", r[0], Field.Store.YES));
            if(!r[3].equals("[No author id available]")){
                doc.add(new TextField("author_id", r[1], Field.Store.YES));
            }
            doc.add(new TextField("title", r[2], Field.Store.YES));
            if(!r[3].equals("")){
                doc.add(new SortedNumericDocValuesField("year", Long.parseLong(r[3])));
                doc.add(new StoredField("year", Long.parseLong(r[3])));
            }
            doc.add(new TextField("source_title", r[4], Field.Store.YES));
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
            doc.add(new StoredField("link", r[13]));
            doc.add(new TextField("affiliations", r[14], Field.Store.YES));
            doc.add(new TextField("abstract", r[16], Field.Store.YES));
            doc.add(new TextField("author_keywords", r[17], Field.Store.NO));
            doc.add(new TextField("index_keywords", r[18], Field.Store.NO));
            doc.add(new StringField("doc_type", r[19], Field.Store.NO));
            doc.add(new StoredField("public_status", r[20]));
            doc.add(new StoredField("eid", r[23]));
            writer.addDocument(doc);
            r = reader.readNext();
        }while(r!=null);

        System.out.println("Índice guardado en: "+indexPath+"/"+archivo.getName().substring(0, archivo.getName().lastIndexOf(".")));
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
        File dir = new File("./docs");
        File[] archivos = dir.listFiles();
        Analyzer analyzer = new StandardAnalyzer();
        Similarity similarity = new ClassicSimilarity();
        indiceSimple baseline = new indiceSimple();

        for(File archivo : archivos){
            baseline.configurarIndice(analyzer, similarity, archivo.getName().substring(0, archivo.getName().lastIndexOf(".")));
            baseline.indexarDocumentos(archivo);
            baseline.close();
        }
    }
}


