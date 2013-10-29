package models;

import java.util.*;

public class LectureEvaluationStat {
    private float[] maleRating;
    private float[] femaleRating;
    private Map<String, Integer> majorCountMap;
    private Map<LectureSimple, Float> lectureSimpleFloatMap;

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

    public Map<LectureSimple, Float> getLectureSimpleFloatMap() {
        return lectureSimpleFloatMap;
    }

    public void setLectureSimpleFloatMap(Map<LectureSimple, Float> lectureSimpleFloatMap) {
        this.lectureSimpleFloatMap = lectureSimpleFloatMap;
    }

    public List<LectureSimple> getLectureSimpleFloatMapToSubList(int limit) {
        if(this.lectureSimpleFloatMap != null && this.lectureSimpleFloatMap.size() > 0) {
            Iterator<LectureSimple> it = this.lectureSimpleFloatMap.keySet().iterator();
            List<LectureSimple> list = new ArrayList<>();

            int i = 0;
            while(it.hasNext()) {
                list.add(it.next());
                if(++i >= limit) break;
            }
            return list;
        }
        else {
            return null;
        }
    }
}