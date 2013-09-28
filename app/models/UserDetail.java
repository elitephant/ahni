package models;

import com.mongodb.BasicDBObject;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.Id;
import play.modules.mongodb.jackson.MongoDB;
import securesocial.core.Identity;

import java.util.List;

public class UserDetail {
    private static JacksonDBCollection<UserDetail, String> coll = MongoDB.getCollection("user_detail", UserDetail.class, String.class);

    @Id
    @ObjectId
    public String id;               //MongoDB Object ID
    public String userId;           //userId
    public Object validatedTime;    //학교 인증 시간
    public String validatedEmail;   //학교 인증 메일
    public String gender;           //성별
    public String major;            //전공
    public String entrance;         //입학년도

    public static List<UserDetail> all() {
        return UserDetail.coll.find().toArray();
    }

    public static UserDetail findByUserId(String userId) {
        return UserDetail.coll.findOne(new BasicDBObject("userId", userId));
    }

    public boolean save(Identity user) {
        User dbUser = User.findByIdentity(user);
        UserDetail userDetail = UserDetail.findByUserId(dbUser.id);

        if(userDetail==null) {
            return false;
        }
        else {
            this.validatedEmail = userDetail.validatedEmail;
            this.validatedTime = userDetail.validatedTime;
            this.userId = userDetail.userId;

            coll.update(userDetail, this);

            return true;
        }
    }
}
