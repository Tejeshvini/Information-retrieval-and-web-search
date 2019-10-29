package infs7410.project1;

import infs7410.project1.ranking.BM25;
import infs7410.project1.reduction.IDFReduction;
import org.apache.log4j.Logger;
import org.terrier.matching.models.WeightingModel;
import org.terrier.querying.IndexRef;
import org.terrier.structures.Index;
import org.terrier.terms.PorterStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.stream.Collectors;


//import org.terrier.matching.models.BM25;


public class Project1 {
    public static void main(String[] args) throws IOException {
        // ------------------------------------------------------------- //
        // The following code shows how you could use the code provided. //
        // It is not commented here because it should be fairly obvious. //
        // The classes themselves, however have detailed comments which  //
        // should provide some insights into the inner workings of what  //
        // is going on.                                                  //
        // Feel free to edit these classes to complete this project.     //
        // You are not marked on the beauty of your code, just that it   //
        // produces the expected results in a reasonable amount of time! //
        // ------------------------------------------------------------- //
        Logger logger = Logger.getLogger(Project1.class);
        Index index = Index.createIndex("./var/index", "pubmed");

        Reranker reranker = new Reranker(index);
        File folder = new File("/mnt/c/Users/tejes/UQ/Sem-2/INFS7410/Project-Part1/tar/tar/2018-TAR/testing/topics/");
        //File BM25_file = new File("/mnt/c/Users/tejes/UQ/Sem-2/INFS7410/Project-Part1/project/BM25_2018_test.res");
        //File qrels = new File("/mnt/c/Users/tejes/UQ/Sem-2/INFS7410/Project-Part1/tar/tar/2018-TAR/testing/qrels/2018-qrel_abs_test.qrels");


        WeightingModel wm = new BM25();
        IDFReduction reduction = new IDFReduction();

        File[] files = folder.listFiles();
        //IDFReduction reduction = new IDFReduction();


        //File[] files = folder.listFiles();
        for (File file : files) {
            //HashMap<String, String> bm25_hashmap = new HashMap<String, String>();
            //HashMap<String, String[]> qrels_hashmap = new HashMap<String, String[]>();
            System.out.println("Reading files");
            BufferedReader br = new BufferedReader(new FileReader(file));
            IndexRef ref = IndexRef.of(String.valueOf(index));
            String topic = null;
            ArrayList<String> queryTerm = new ArrayList<>();
            ArrayList<String> docID = new ArrayList<String>();
            String thisLine_file = br.readLine();

            PorterStemmer stemmer = new PorterStemmer();
            while (thisLine_file != null) {
                //System.out.println("Reading the topic file");
                if (thisLine_file.startsWith("Topic:")) {
                    //System.out.println("Saving the topic file");
                    String token = "Topic:";
                    //System.out.println(token);
                    //System.out.println(thisLine_file.indexOf(token));
                    thisLine_file = thisLine_file.substring(thisLine_file.indexOf(token)+token.length());
                    //System.out.println(thisLine_file);
                    StringTokenizer st = new StringTokenizer(thisLine_file);
                    while (st.hasMoreElements()) {
                        topic = st.nextToken();
                        System.out.println(topic);
                    }
                }
                else if (thisLine_file.startsWith("Title: ")) {
                    //System.out.println("Saving the query terms");
                    String token = "Title: ";
                    thisLine_file = thisLine_file.substring(thisLine_file.indexOf(token) + token.length());
                    String[] queryterms = thisLine_file.split("\\s+");
                    int numberOfQueryTerms = queryterms.length;
                    double k = (0.85) * numberOfQueryTerms;
                    k = Math.round(k);
                    String idfrQuery = reduction.reduce(thisLine_file, (int) k, ref);
                    System.out.println(thisLine_file);
                    System.out.println(idfrQuery);
                    StringTokenizer st = new StringTokenizer(idfrQuery);
                    while (st.hasMoreElements()) {
                        stemmer.stem(String.valueOf(st));
                        queryTerm.add(st.nextToken().toLowerCase());
                    }
                    System.out.println(queryTerm);
                }
                else if (thisLine_file.startsWith("  ")) {
                    StringTokenizer st = new StringTokenizer(thisLine_file);
                    while (st.hasMoreElements()) {
                        docID.add(st.nextToken());
                    }
                }
                thisLine_file = br.readLine();
            }
            String[] queryTerms = queryTerm.toArray(new String[queryTerm.size()]);
            String[] docIds = docID.toArray(new String[docID.size()]);
            TrecResults results = reranker.rerank(topic, queryTerms, docIds, wm);
            results.setRunName(wm.toString());
            results.write("BM25_rerank_2018_test" +".res");



            }

        }
    }









