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
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.FacetField;
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
    String taxoPath = "./taxo";
    String docPath = "./docs";
    boolean create = true;
    private IndexWriter indexWriter;
    private DirectoryTaxonomyWriter taxoWriter;
    private FacetsConfig fconfig;
    Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();

    indiceSimple(){
        analyzerPerField.put("author", new SimpleAnalyzer());
        analyzerPerField.put("title", new SimpleAnalyzer());
        analyzerPerField.put("source_title", new SimpleAnalyzer());
        analyzerPerField.put("affiliations", new SimpleAnalyzer());
        analyzerPerField.put("abstract", new EnglishAnalyzer());
        analyzerPerField.put("author_keywords", new SimpleAnalyzer());
        analyzerPerField.put("index_keywords", new SimpleAnalyzer());
    }

    public void configurarIndice(Analyzer analyzer, Similarity similarity) throws IOException {
        PerFieldAnalyzerWrapper analyzerWrapper = new PerFieldAnalyzerWrapper(analyzer, analyzerPerField);
        
        IndexWriterConfig iwc = new IndexWriterConfig(analyzerWrapper);
        
        iwc.setSimilarity(similarity);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        Directory indexDir = FSDirectory.open(Paths.get(indexPath));
        Directory taxoDir = FSDirectory.open(Paths.get(taxoPath));

        fconfig = new FacetsConfig();
        taxoWriter = new DirectoryTaxonomyWriter(taxoDir);
        indexWriter = new IndexWriter(indexDir, iwc);

        fconfig.setMultiValued("year",true);
        fconfig.setMultiValued("category",true);
        fconfig.setMultiValued("doc_type",true);
    }

    public void indexarDocumentos(File archivo) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new FileReader(docPath+"/"+archivo.getName()));    
        reader.skip(1);
        String[] r = reader.readNext();
        System.out.println("Archivo "+archivo.getName());

        do{ 
            Document doc = new Document();
            doc.add(new TextField(  "category",
                                    archivo.getName().substring(0, archivo.getName().lastIndexOf(".")),
                                    Field.Store.YES));
            doc.add(new StoredField("file_dir", docPath+"/"+archivo.getName()));
            doc.add(new TextField("author", r[0], Field.Store.YES));
            doc.add(new StoredField("author_id", r[1]));
            doc.add(new TextField("title", r[2], Field.Store.YES));
            if(!r[3].equals("")){
                doc.add(new IntPoint("year", Integer.parseInt(r[3])));
                doc.add(new StoredField("year", Integer.parseInt(r[3])));
            }
            doc.add(new TextField("source_title", r[4], Field.Store.YES));
            doc.add(new StoredField("volume", r[5]));
            doc.add(new StoredField("issue", r[6]));
            doc.add(new StoredField("article_number", r[7]));
            doc.add(new StoredField("page_start", r[8]));
            doc.add(new StoredField("page_end", r[9]));
            doc.add(new StoredField("page_count", r[10]));
            if(!r[11].equals("")){
                doc.add(new NumericDocValuesField("cited_by", Integer.parseInt(r[11])));
                doc.add(new StoredField("cited_by", Long.parseLong(r[11])));
            }
            doc.add(new StoredField("doi", r[12]));
            doc.add(new StoredField("link", r[13]));
            doc.add(new TextField("affiliations", r[14], Field.Store.YES));
            doc.add(new TextField("abstract", r[16], Field.Store.YES));
            doc.add(new TextField("author_keywords", r[17], Field.Store.NO));
            doc.add(new TextField("index_keywords", r[18], Field.Store.NO));
            doc.add(new StoredField("doc_type", r[19]));
            doc.add(new StoredField("public_status", r[20]));
            doc.add(new StoredField("eid", r[23]));

            doc.add(new FacetField("category",archivo.getName().substring(0, archivo.getName().lastIndexOf("."))));
            doc.add(new FacetField("year",r[3]));
            doc.add(new FacetField("doc_type", r[19]));
            indexWriter.addDocument(fconfig.build(taxoWriter,doc));
            r = reader.readNext();
        }while(r!=null);
    }

    public void close() throws IOException, CsvException {
        try {
            indexWriter.commit();
            indexWriter.close();
            taxoWriter.commit();
            taxoWriter.close();
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

        baseline.configurarIndice(analyzer, similarity);
        for(File archivo : archivos){
            baseline.indexarDocumentos(archivo);
        }
        baseline.close();
    }
}


