package models;

import com.mongodb.BasicDBObject;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.ObjectId;
import org.joda.time.DateTime;
import play.data.validation.Constraints;
import play.modules.mongodb.jackson.MongoDB;
import securesocial.core.Identity;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LectureEvaluation {
    private static JacksonDBCollection<LectureEvaluation, String> coll = MongoDB.getCollection("LectureEvaluations", LectureEvaluation.class, String.class);

    @Id
    @ObjectId
    public String id;               //MongoDB Object ID
    @Constraints.Required
    public String lectureName;      //강의명
    @Constraints.Required
    public String professorName;    //교수명
    @Constraints.Required
    public String comment;          //수강후기
    @Constraints.Required
    public int rating;              //평가점수
    public String userId;           //유저 아이디
    public DateTime dateTime;       //작성 시간

    public static List<LectureEvaluation> all() {
        //시간으로 내림차순 정렬하여 최근것 부터 보여줌, 10개
        return LectureEvaluation.coll.find().limit(10).sort(new BasicDBObject("dateTime", -1)).toArray();
    }

    public static List<LectureEvaluation> findByKeyword(String keyword) {
        List<BasicDBObject> orList = new ArrayList<>();
        orList.add(new BasicDBObject("lectureName", Pattern.compile(keyword)));
        orList.add(new BasicDBObject("professorName", Pattern.compile(keyword)));

        return LectureEvaluation.coll.find(new BasicDBObject("$or", orList)).toArray();
    }

    public static void create(LectureEvaluation lectureEvaluation, Identity user) {
        //현재 시스템 시간과 유저아이디를 set 해주고 MongoDB에 Insert
        //TODO: 시스템 시간이 4시간 더 크게 들어감...
        //TODO: save할 때 중복확인 할 것인가? 한 유저가 같은 강의를 여러번 평가하면...안되는데
        lectureEvaluation.dateTime = DateTime.now();
        lectureEvaluation.userId = User.findByIdentity(user).id;
        LectureEvaluation.coll.save(lectureEvaluation);
    }
}
