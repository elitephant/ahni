package models;

import java.util.Map;

public class LectureEvaluationStat {
    private float[] maleRating;
    private float[] femaleRating;
    private Map<String, Integer> majorCountMap;

    public float[] getMaleRating() {
        return maleRating;
    }

    public void setMaleRating(float[] maleRating) {
        this.maleRating = maleRating;
    }

    public float[] getFemaleRating() {
        return femaleRating;
    }

    public void setFemaleRating(float[] femaleRating) {
        this.femaleRating = femaleRating;
    }

    public Map<String, Integer> getMajorCountMap() {
        return majorCountMap;
    }

    public void setMajorCountMap(Map<String, Integer> majorCountMap) {
        this.majorCountMap = majorCountMap;
    }
}