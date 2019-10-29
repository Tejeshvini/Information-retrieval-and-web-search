package infs7410.project1.ranking;

import org.terrier.matching.models.WeightingModel;

public class BM25 extends WeightingModel {


    private double k1 = 1;
    private double b =0.5;

    public BM25() {
        super();
    }

    @Override
    public String getInfo() {
        return "BM25";
    }

    @Override
    public double score(double tf, double docLength) {
        // TODO: IMPLEMENT ME!
        // DO NOT LOOK AT HOW BM25 IS IMPLEMENTED IN TERRIER. THIS WILL RUIN YOUR LEARNING EXPERIENCE.
        // HINT: The `averageDocumentLength` variable can be accessed from the parent class:
        // http://terrier.org/docs/current/javadoc/org/terrier/matching/models/WeightingModel.html
        return (tf * (k1+1))/(tf + k1*(1 - b + (b* docLength/ averageDocumentLength)));
    }
}
