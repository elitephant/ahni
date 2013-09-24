package models;

import com.mongodb.BasicDBObject;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.ObjectId;
import play.modules.mongodb.jackson.MongoDB;
import securesocial.core.Identity;

import javax.persistence.Id;
import java.util.List;

public class User {
    private static JacksonDBCollection<User, String> coll = MongoDB.getCollection("user", User.class, String.class);

    @Id
    @ObjectId
    public String id;                   //MongoDB Object ID
    public String UserIdAndProviderId;
    public Object authMethod;
    public String firstName;
    public String fullName;
    public String lastName;
    public Object identityId;
    public String email;
    public String avatarUrl;
    public Object oAuth1Info;
    public Object oAuth2Info;
    public Object passwordInfo;

    public static List<User> all() {
        return User.coll.find().toArray();
    }

    public static User findByIdentity(Identity user) {
        return User.coll.findOne(new BasicDBObject("UserIdAndProviderId", user.identityId().userId() + user.identityId().providerId()));
    }
}
