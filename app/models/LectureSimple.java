package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.Id;
import org.joda.time.DateTime;
import play.Logger;
import play.modules.mongodb.jackson.MongoDB;
import securesocial.core.Identity;

import java.util.*;
import java.util.regex.Pattern;

public class LectureSimple {
    public static JacksonDBCollection<LectureSimple, String> coll = MongoDB.getCollection("lecture_simple", LectureSimple.class, String.class);

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

    public static boolean addEvaluation(LectureEvaluation lectureEvaluation, Identity user, String id, Boolean isEdit) {
        LectureSimple lectureSimple = LectureSimple.findById(id);
        User dbUser = User.findByIdentity(user);

        //그런 강의가 없다
        if(lectureSimple==null) {
            return false;
        } else {
            //강의평 추가 준비
            lectureEvaluation = LectureEvaluation.prepareToAdd(lectureEvaluation, user);

            //기존의 강의평을 업데이트
            if(isEdit) {
                BasicDBObject findQuery = new BasicDBObject("_id", new org.bson.types.ObjectId(id))
                        .append("evaluations.user",new org.bson.types.ObjectId(dbUser.id));

                //set query
                BasicDBObject setObject = new BasicDBObject("evaluations.$.year",lectureEvaluation.year)
                        .append("evaluations.$.comment",lectureEvaluation.comment)
                        .append("evaluations.$.rating",lectureEvaluation.rating)
                        .append("evaluations.$.semester",lectureEvaluation.semester)
                        .append("evaluations.$.dateTime",lectureEvaluation.dateTime.getMillis());

                BasicDBObject setQuery = new BasicDBObject("$set", setObject);

                LectureSimple.coll.setWriteConcern(WriteConcern.SAFE);
                LectureSimple.coll.update(findQuery, setQuery);
                return true;
            }
            //새로운 강의평
            else {
                BasicDBObject findQuery = new BasicDBObject("_id", new org.bson.types.ObjectId(id));

                //push query
                BasicDBObject pushObject = new BasicDBObject("evaluations",
                        new BasicDBObject("_id",new org.bson.types.ObjectId(lectureEvaluation.id))
                                .append("user",new org.bson.types.ObjectId(lectureEvaluation.user))
                                .append("year",lectureEvaluation.year)
                                .append("comment",lectureEvaluation.comment)
                                .append("rating",lectureEvaluation.rating)
                                .append("semester",lectureEvaluation.semester)
                                .append("dateTime",lectureEvaluation.dateTime.getMillis())
                );

                BasicDBObject pushQuery = new BasicDBObject("$push", pushObject);

                LectureSimple.coll.setWriteConcern(WriteConcern.SAFE);
                LectureSimple.coll.update(findQuery, pushQuery);

                return true;
            }
        }
    }

    public float getAvgRating() {
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

    public float getAvgRatingPercentage() {
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

    public float[] getRatingsToPercentage() {
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

    public float[][] getRatingsToPercentageByGender() {
        if(this.evaluations==null) {
            return null;
        }
        else {
            //[0][]:남자, [1][]:여자
            float ratings[][] = new float[2][5];
            float maleMax = Float.MIN_VALUE;
            float femaleMax = Float.MIN_VALUE;

            //[유저 아이디 : 강의평가 객체]의 맵 생성
            Map<org.bson.types.ObjectId, LectureEvaluation> lectureEvaluationMap = new HashMap<>();
            for(LectureEvaluation lectureEvaluation : this.evaluations) {
                lectureEvaluationMap.put(new org.bson.types.ObjectId(lectureEvaluation.user), lectureEvaluation);
                Logger.debug(new org.bson.types.ObjectId(lectureEvaluation.user).toString());
            }

            BasicDBObject inQuery = new BasicDBObject("$in", lectureEvaluationMap.keySet());

            //find 쿼리 한번 호출
            for(UserDetail userDetail : UserDetail.coll.find(new BasicDBObject("user", inQuery))) {
                if(userDetail.gender==null) {
                    continue;
                }
                else if(userDetail.gender.equals("남자")){
                    switch (lectureEvaluationMap.get(new org.bson.types.ObjectId(userDetail.user)).rating) {
                        case 1: ratings[0][0]++; break;
                        case 2: ratings[0][1]++; break;
                        case 3: ratings[0][2]++; break;
                        case 4: ratings[0][3]++; break;
                        case 5: ratings[0][4]++; break;
                        default : break;
                    }
                } else if(userDetail.gender.equals("여자")){
                    switch (lectureEvaluationMap.get(new org.bson.types.ObjectId(userDetail.user)).rating) {
                        case 1: ratings[1][0]++; break;
                        case 2: ratings[1][1]++; break;
                        case 3: ratings[1][2]++; break;
                        case 4: ratings[1][3]++; break;
                        case 5: ratings[1][4]++; break;
                        default : break;
                    }
                } else {
                    continue;
                }
                Logger.debug(
                        String.format("%s : %s : %s",
                            userDetail.user,
                            userDetail.gender,
                            String.valueOf(lectureEvaluationMap.get(new org.bson.types.ObjectId(userDetail.user)).rating)
                        )
                );
            }

            for(float rating : ratings[0]) {
                if(rating>maleMax) maleMax = rating;
            }

            for(float rating : ratings[1]) {
                if(rating>femaleMax) femaleMax = rating;
            }

            ratings[0][0] = ratings[0][0] / maleMax * 100;
            ratings[0][1] = ratings[0][1] / maleMax * 100;
            ratings[0][2] = ratings[0][2] / maleMax * 100;
            ratings[0][3] = ratings[0][3] / maleMax * 100;
            ratings[0][4] = ratings[0][4] / maleMax * 100;

            ratings[1][0] = ratings[1][0] / femaleMax * 100;
            ratings[1][1] = ratings[1][1] / femaleMax * 100;
            ratings[1][2] = ratings[1][2] / femaleMax * 100;
            ratings[1][3] = ratings[1][3] / femaleMax * 100;
            ratings[1][4] = ratings[1][4] / femaleMax * 100;

            return ratings;
        }
    }
}
