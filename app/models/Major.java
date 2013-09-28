package models;

import com.mongodb.BasicDBObject;
import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.ObjectId;
import play.modules.mongodb.jackson.MongoDB;

import java.util.List;

public class Major {
    private static JacksonDBCollection<Major, String> coll = MongoDB.getCollection("major", Major.class, String.class);

    @Id
    @ObjectId
    public String id;              //MongoDB Object ID
    public String major;           //전공명

    public static List<Major> all() {
        return Major.coll.find().sort(new BasicDBObject("major", 1)).toArray();
    }
}
