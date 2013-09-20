package models;

import com.mongodb.BasicDBObject;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.ObjectId;
import play.modules.mongodb.jackson.MongoDB;

import javax.persistence.Id;
import java.util.List;

public class UserDetail {
    private static JacksonDBCollection<UserDetail, String> coll = MongoDB.getCollection("UserDetails", UserDetail.class, String.class);

    @Id
    @ObjectId
    public String id;               //MongoDB Object ID
    public String userId;           //userId
    public Object validatedTime;    //학교 인증 시간
    public String validatedEmail;   //학교 인증 메일

    public static List<UserDetail> all() {
        return UserDetail.coll.find().toArray();
    }

    public static UserDetail findByUserId(String userId) {
        return UserDetail.coll.findOne(new BasicDBObject("userId", userId));
    }
}
