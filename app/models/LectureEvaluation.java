package models;

import com.mongodb.*;
import net.vz.mongodb.jackson.*;
import net.vz.mongodb.jackson.ObjectId;
import org.joda.time.DateTime;
import play.data.validation.Constraints.Required;
import play.modules.mongodb.jackson.MongoDB;
import securesocial.core.Identity;
import services.MongoDBHelper;

import java.util.List;

public class LectureEvaluation {
    private static JacksonDBCollection<LectureEvaluation, String> coll = MongoDB.getCollection("lecture_simple", LectureEvaluation.class, String.class);

    @Id
    @ObjectId
    public String id;                       //MongoDB Object ID
    @Required
    public String comment;                  //수강후기
    @Required
    public int rating;                      //평가점수
    @Required
    public int year;                        //수강년도
    @Required
    public int semester;                    //수강학기
    @ObjectId
    public String user;                     //유저 아이디
    public DateTime dateTime;               //작성 시간
    public List<org.bson.types.ObjectId> likes;            //좋아요
    public List<org.bson.types.ObjectId> dislikes;         //싫어요

    public static LectureEvaluation prepareToAdd(LectureEvaluation lectureEvaluation, Identity user) {
        //TODO: 시스템 시간이 4시간 더 크게 들어감...
        //TODO: save할 때 중복확인 할 것인가? 한 유저가 같은 강의를 여러번 평가하면...안되는데
        lectureEvaluation.dateTime = DateTime.now();
        lectureEvaluation.user = new org.bson.types.ObjectId(User.findByIdentity(user).id).toString();
        lectureEvaluation.id = new org.bson.types.ObjectId().toString();

        return lectureEvaluation;
    }

    public static List<LectureSimple> findByIdentity(Identity user) {
        return LectureSimple.coll.find(new BasicDBObject("evaluations.user", new org.bson.types.ObjectId(User.findByIdentity(user).id))).toArray();
    }

    /**
     * 작성된 강의평가 카운트 반환
     * @return 강의평가 개수
     */
    public static int getEvaluationsCount() {
        DB mongoDB = MongoDBHelper.getDB();
        DBCollection coll = mongoDB.getCollection("lecture_simple");

        BasicDBObject unwindQuery = new BasicDBObject("$unwind", "$evaluations");
        BasicDBObject groupQuery = new BasicDBObject("$group", new BasicDBObject("_id", "$evaluations"));
        BasicDBObject countQuery = new BasicDBObject("$group", new BasicDBObject("_id", "count").append("count", new BasicDBObject("$sum", 1)));

        AggregationOutput aggregationOutput = coll.aggregate(unwindQuery, groupQuery, countQuery);

        for(DBObject obj : aggregationOutput.results()) {
            int count = Integer.parseInt(String.valueOf(obj.get("count")));
            if(count > 0) {
                return count;
            }
        }
        return 0;
    }

    /**
     * 강의평가를 작성한 유저 카운트 반환
     * @return 유저 카운트
     */
    public static int getUsersCount() {
        DB mongoDB = MongoDBHelper.getDB();
        DBCollection coll = mongoDB.getCollection("lecture_simple");

        BasicDBObject unwindQuery = new BasicDBObject("$unwind", "$evaluations");
        BasicDBObject groupQuery = new BasicDBObject("$group", new BasicDBObject("_id", "$evaluations.user"));
        BasicDBObject countQuery = new BasicDBObject("$group", new BasicDBObject("_id", "count").append("count", new BasicDBObject("$sum", 1)));

        AggregationOutput aggregationOutput = coll.aggregate(unwindQuery, groupQuery, countQuery);

        for(DBObject obj : aggregationOutput.results()) {
            int count = Integer.parseInt(String.valueOf(obj.get("count")));
            if(count > 0) {
                return count;
            }
        }
        return 0;
    }

    /**
     * likes 리스트의 사이즈를 반환한다
     * @return
     */
    public int getLikeSize() {
        if(likes != null && !likes.isEmpty()) { return likes.size(); }
        else { return 0; }
    }

    /**
     * dislikes 리스트의 사이즈를 반환한다
     * @return
     */
    public int getDisLikeSize() {
        if(dislikes != null && !dislikes.isEmpty()) { return dislikes.size(); }
        else { return 0; }
    }

    public static boolean vote(String id, String userId, String operator) {
        boolean isVoted;
        DB mongoDB = MongoDBHelper.getDB();
        DBCollection coll = mongoDB.getCollection("lecture_simple");

        BasicDBObject findQuery = new BasicDBObject("evaluations._id", new org.bson.types.ObjectId(id));
        BasicDBObject object = new BasicDBObject(String.format("evaluations.$.%s",operator), new org.bson.types.ObjectId(userId));
        String dbQuery;

        //like 기록이 있는지 확인
        BasicDBObject existQuery = new BasicDBObject("evaluations", new BasicDBObject("$elemMatch",
                new BasicDBObject("_id", new org.bson.types.ObjectId(id))
                        .append(operator, new org.bson.types.ObjectId(userId))));
        DBObject one = coll.findOne(existQuery);

        //like 기록이 있으면 pull 없으면 push
        if(one != null) {
            dbQuery = "$pull";
            isVoted = false;
        }
        else {
            dbQuery = "$push";
            isVoted = true;
        }

        BasicDBObject query = new BasicDBObject(dbQuery, object);
        findQuery.remove(object);

        coll.setWriteConcern(WriteConcern.SAFE);
        coll.update(findQuery, query);

        return isVoted;
    }

    public static List<org.bson.types.ObjectId> getVoteList(String id, String operator) {
        BasicDBObject findQuery = new BasicDBObject("evaluations._id", new org.bson.types.ObjectId(id));

        LectureSimple lectureSimple = LectureSimple.coll.findOne(findQuery);

        if(lectureSimple != null) {
            for(LectureEvaluation lectureEvaluation : lectureSimple.evaluations) {
                if(lectureEvaluation.id.equals(id)) {
                    if(operator.equals("likes")) {
                        return lectureEvaluation.likes;
                    }
                    else if(operator.equals("dislikes")) {
                        return lectureEvaluation.dislikes;
                    }
                }
            }
        }
        return null;
    }
}