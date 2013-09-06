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

public class Lecture {
    private static JacksonDBCollection<Lecture, String> coll = MongoDB.getCollection("Lectures", Lecture.class, String.class);

    @Id
    @ObjectId
    public String id;              //MongoDB Object ID
    public String haksoo;          //학수번호
    public String lectureName;     //강의명
    public String professorName;   //교수명
    public String credit;          //학점
    public String type;            //구분(교양필수, 전공필수, 전공선택, 전공필수 등)
    public String timeClassroom;   //시간 & 강의실

    public static List<Lecture> all() {
        return Lecture.coll.find().limit(10).toArray();
    }

    public static Collection<String> findLectureNameByKeyword(String keyword) {
        List<Lecture> lectureList = Lecture.coll.find(new BasicDBObject("lectureName", Pattern.compile(keyword))).toArray();

        //고유한 강의명 분류를 위한 HashMap
        HashMap<String,String> hashMap = new HashMap<>();
        for (Lecture lecture : lectureList) {
            hashMap.put(lecture.lectureName, lecture.lectureName);
        }

        //정렬하기 위한 TreeMap
        TreeMap<String, String> treeMap = new TreeMap<>(hashMap);

        return treeMap.values();
    }

    public static Collection<String> findProfessorNameByKeyword(String keyword) {
        List<Lecture> lectureList = Lecture.coll.find(new BasicDBObject("professorName", Pattern.compile(keyword))).toArray();

        //고유한 교수명 분류를 위한 HashMap
        HashMap<String,String> hashMap = new HashMap<>();
        for (Lecture lecture : lectureList) {
            hashMap.put(lecture.professorName, lecture.professorName);
        }

        //정렬하기 위한 TreeMap
        TreeMap<String, String> treeMap = new TreeMap<>(hashMap);

        return treeMap.values();
    }
}
