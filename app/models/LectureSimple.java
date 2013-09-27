package models;

import com.mongodb.BasicDBObject;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.Id;
import org.joda.time.DateTime;
import play.modules.mongodb.jackson.MongoDB;
import securesocial.core.Identity;

import java.util.*;
import java.util.regex.Pattern;

public class LectureSimple {
    static JacksonDBCollection<LectureSimple, String> coll = MongoDB.getCollection("lecture_simple", LectureSimple.class, String.class);

    @Id
    @ObjectId
    public String id;                           //MongoDB Object ID
    public String lectureName;                  //강의명
    public String professorName;                //교수명
    public List<LectureEvaluation> evaluations; //강의평

    /**
     * 최근에 강의평가가 등록된 순서대로 10개를 가져온다.
     * @return
     */
    public static List<LectureSimple> all() {
        return LectureSimple.coll.find().sort(new BasicDBObject("evaluations.dateTime", -1)).limit(10).toArray();
    }

    public static List<LectureSimple> findByKeyword(String keyword) {
        List<BasicDBObject> orList = new ArrayList<>();
        orList.add(new BasicDBObject("lectureName", Pattern.compile(keyword)));
        orList.add(new BasicDBObject("professorName", Pattern.compile(keyword)));

        return LectureSimple.coll.find(new BasicDBObject("$or", orList)).toArray();
    }

    /**
     * 몽고 오브젝트 아이디로 검색하여 반환.
     * @param id
     * @return
     */
    public static LectureSimple findById(String id) {
        if(org.bson.types.ObjectId.isValid(id)){
            return LectureSimple.coll.findOne(new BasicDBObject("_id", new org.bson.types.ObjectId(id)));
        }
        else {
            return null;
        }
    }

    public static Collection<String> findLectureNameByKeyword(String keyword) {
        List<LectureSimple> lectureSimpleList = LectureSimple.coll.find(new BasicDBObject("lectureName", Pattern.compile(keyword))).toArray();

        //고유한 강의명 분류를 위한 HashMap
        HashMap<String,String> hashMap = new HashMap<>();
        for (LectureSimple lecture : lectureSimpleList) {
            hashMap.put(lecture.lectureName, lecture.lectureName);
        }

        //정렬하기 위한 TreeMap
        TreeMap<String, String> treeMap = new TreeMap<>(hashMap);

        return treeMap.values();
    }

    public static Collection<String> findProfessorNameByKeyword(String keyword) {
        List<LectureSimple> lectureSimpleList = LectureSimple.coll.find(new BasicDBObject("professorName", Pattern.compile(keyword))).toArray();

        //고유한 교수명 분류를 위한 HashMap
        HashMap<String,String> hashMap = new HashMap<>();
        for (LectureSimple lecture : lectureSimpleList) {
            hashMap.put(lecture.professorName, lecture.professorName);
        }

        //정렬하기 위한 TreeMap
        TreeMap<String, String> treeMap = new TreeMap<>(hashMap);

        return treeMap.values();
    }

    public static boolean addEvaluation(LectureEvaluation lectureEvaluation, Identity user, String id) {
        LectureSimple lectureSimple = LectureSimple.findById(id);

        if(lectureSimple==null) {
            return false;
        } else {
            lectureEvaluation = LectureEvaluation.prepareToAdd(lectureEvaluation, user);

            if(lectureSimple.evaluations==null) {
                lectureSimple.evaluations = new ArrayList<>();
                lectureSimple.evaluations.add(lectureEvaluation);
            }
            else {
                lectureSimple.evaluations.add(lectureEvaluation);
            }

            LectureSimple.coll.updateById(id, lectureSimple);

            return true;
        }
    }

    public float avgRating() {

        if(this.evaluations==null) {
            return 0f;
        }
        else {
            float sum = 0.0f;
            for(LectureEvaluation lectureEvaluation : this.evaluations) {
                sum+=lectureEvaluation.rating;
            }
            return sum/this.evaluations.size();
        }
    }

    public float avgRatingPercentage() {
        if(this.evaluations==null) {
            return 0f;
        }
        else {
            float sum = 0.0f;
            for(LectureEvaluation lectureEvaluation : this.evaluations) {
                sum+=lectureEvaluation.rating;
            }
            return sum/this.evaluations.size() / 5 * 100;
        }
    }

    public float[] ratingsToPercentage() {
        if(this.evaluations==null) {
            return null;
        }
        else {
            float ratings[] = new float[5];
            float max = Float.MIN_VALUE;

            for(LectureEvaluation lectureEvaluation : this.evaluations) {
                switch (lectureEvaluation.rating) {
                    case 1: ratings[0]++; break;
                    case 2: ratings[1]++; break;
                    case 3: ratings[2]++; break;
                    case 4: ratings[3]++; break;
                    case 5: ratings[4]++; break;
                    default : break;
                }
            }

            for(float rating : ratings) {
                if(rating>max) max = rating;
            }

            ratings[0] = ratings[0] / max * 100;
            ratings[1] = ratings[1] / max * 100;
            ratings[2] = ratings[2] / max * 100;
            ratings[3] = ratings[3] / max * 100;
            ratings[4] = ratings[4] / max * 100;

            return ratings;
        }
    }
}
