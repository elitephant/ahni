package services.util.ValueComparator;

import models.LectureSimple;

import java.util.Comparator;
import java.util.Map;

public class LectureSimpleFloatValueComparator implements Comparator<LectureSimple> {
    Map<LectureSimple, Float> base;

    public LectureSimpleFloatValueComparator(Map<LectureSimple, Float> base) {
        this.base = base;
    }

    @Override
    public int compare(LectureSimple o1, LectureSimple o2) {
        if(base.get(o1) >= base.get(o2)) {
            return -1;
        } else {
            return 1;
        }
    }
}