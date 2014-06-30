package models;

import com.mongodb.BasicDBObject;
import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.ObjectId;
import play.modules.mongodb.jackson.MongoDB;

import java.util.List;

public class Restaurant {
    public static JacksonDBCollection<Restaurant, String> coll = MongoDB.getCollection("restaurant", Restaurant.class, String.class);

    @Id
    @ObjectId
    public String id;                 //MongoDB Object ID
    public String name;               //식당이름
    public double[] loc;              //구글맵 좌표
    public List<Object> evaluations;  //식당평

    /**
     * 최근에 식당평가가 등록된 순서대로 3개를 가져온다.
     * @return
     */
    public static List<Restaurant> all() {
        return Restaurant.coll.find().sort(new BasicDBObject("evaluations.dateTime", -1)).limit(3).toArray();
    }
}
