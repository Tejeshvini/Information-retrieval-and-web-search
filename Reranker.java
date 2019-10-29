package infs7410.project1;

import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.*;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.querying.*;

import java.io.IOException;
import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Reranker {

    private Index index;

    public Reranker(Index index) {
        this.index = index;
    }

    public TrecResults rerank(String topic, String[] queryTerms, String[] docIds, WeightingModel wm) throws IOException {


        File qrels = new File("/mnt/c/Users/tejes/UQ/Sem-2/INFS7410/Project-Part1/tar/tar/2018-TAR/testing/qrels/2018-qrel_abs_test.qrels");
        HashMap<String, String> inner = new HashMap<String, String>();
        HashMap<String, HashMap<String, String>> outer = new HashMap<String, HashMap<String, String>>();

        int N =1;
        int R = 0;



        Lexicon lex = index.getLexicon();
        PostingIndex invertedIndex = index.getInvertedIndex();
        MetaIndex meta = index.getMetaIndex();
        wm.setCollectionStatistics(index.getCollectionStatistics());
        HashSet<String> docIdSet = new HashSet<>(Arrays.asList(docIds));

        HashMap<String, Double> scores = new HashMap<>();

        // Iterate over all query terms.
        for (String queryTerm : queryTerms) {
            System.out.println(queryTerm);
            // Get the lexicon entry for the term.
            LexiconEntry entry = lex.getLexiconEntry(queryTerm);
            if (entry == null) {
                continue; // This term is not in the index, go to next document.
            }

            // Obtain entry statistics.
            wm.setEntryStatistics(entry.getWritableEntryStatistics());

            // Set the number of times the query term appears in the query.
            double kf = 0.0;
            for (String otherTerm : queryTerms) {
                if (otherTerm.equals(queryTerm)) {
                    kf++;
                }
            }
            wm.setKeyFrequency(kf);

            // Prepare the weighting model for scoring.
            wm.prepare();
            BufferedReader br_qrels = new BufferedReader(new FileReader(qrels));
            String topic_qrels = null;
            String pmid_qrels = null;
            String line_qrels = br_qrels.readLine();
            while ( line_qrels != null) {
                if (line_qrels.startsWith(topic)) {
                    String[] parts_qrels = line_qrels.split(" +", 4);
                    if (parts_qrels.length >= 4) {
                        if (parts_qrels[3].contains("1")) {
                            inner.put(parts_qrels[2], "1");
                            outer.put(parts_qrels[1],inner);
                        }
                    }
                }
                line_qrels=br_qrels.readLine();
            }


            IterablePosting ip = invertedIndex.getPostings(entry);
            double score = 0.0;

            while (ip.next() != IterablePosting.EOL) {
                //System.out.println("Loop1");
                String docId = meta.getItem("docno", ip.getId());
                int ri = 0;
                int ni = 0;
                if (docIdSet.contains(docId)) {
                    for (String pmid : inner.keySet()) {
                        if (inner.get(pmid) == docId) {
                            ri++;
                        }
                        ni++;
                    }
                    double RSJ = Math.log(((ri + 0.5) / (R - ri + 0.5)) / ((ni - ri + 0.5) / (N - ni - R + ri + 0.5)));
                    score = RSJ * wm.score(ip);
                    if (!scores.containsKey(docId)) {
                        scores.put(docId, score);
                    } else {
                        scores.put(docId, scores.get(docId) + score);
                    }
                }
                for (String pmid2 : inner.keySet()) {
                    if (inner.get(pmid2) == docId) {
                        R++;
                    }
                    N++;

                }
            }
        }
// Set score to 0 for docs that do not contain any term.

        for (String id : docIdSet) {
            if (!scores.containsKey(id)) {
                scores.put(id, 0.0);
            }
        }

        // Create a results list from the scored documents.
        TrecResults results = new TrecResults();
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            results.getTrecResults().add(new TrecResult(
                    topic,
                    entry.getKey(),
                    0,
                    entry.getValue(),
                    null
            ));
        }

        // Sort the documents by the score assigned by the weighting model.
        Collections.sort(results.getTrecResults());
        Collections.reverse(results.getTrecResults());

        // Assign the rank to the documents.
        for (int i = 0; i < results.getTrecResults().size(); i++) {
            results.getTrecResults().get(i).setRank(i + 1);
        }

        return results;
    }
}

        // Create a results list from the scored documents.


