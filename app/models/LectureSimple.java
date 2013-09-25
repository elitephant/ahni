package models;

import com.mongodb.BasicDBObject;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.ObjectId;
import play.modules.mongodb.jackson.MongoDB;

import javax.persistence.Id;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class LectureSimple {
    private static JacksonDBCollection<LectureSimple, String> coll = MongoDB.getCollection("lecture_simple", LectureSimple.class, String.class);
    @Id
    @ObjectId
    public String id;              //MongoDB Object ID
    public String lectureName;     //강의명
    public String professorName;   //교수명

    public static List<LectureSimple> all() {
        return LectureSimple.coll.find().limit(10).toArray();
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
}
