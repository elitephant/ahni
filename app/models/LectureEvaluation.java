package models;

import com.mongodb.BasicDBObject;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.ObjectId;
import org.joda.time.DateTime;
import play.data.validation.Constraints;
import play.modules.mongodb.jackson.MongoDB;
import securesocial.core.Identity;

import javax.persistence.Id;
import java.util.List;

public class LectureEvaluation {
    @Id
    @ObjectId
    public String id;
    @Constraints.Required
    public String lectureName;
    @Constraints.Required
    public String professorName;
    @Constraints.Required
    public String comment;
    @Constraints.Required
    public int rating;
    public String userId;
    public DateTime dateTime;

    private static JacksonDBCollection<LectureEvaluation, String> coll = MongoDB.getCollection("LectureEvaluation", LectureEvaluation.class, String.class);

    public static List<LectureEvaluation> all() {
        return LectureEvaluation.coll.find().sort(new BasicDBObject("dateTime",-1)).toArray();
    }

    public static void create(LectureEvaluation lectureEvaluation, Identity user) {
        lectureEvaluation.dateTime = DateTime.now();
        lectureEvaluation.userId = user.identityId().userId();
        LectureEvaluation.coll.save(lectureEvaluation);
    }
}
