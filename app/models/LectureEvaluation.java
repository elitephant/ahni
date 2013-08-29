package models;

import org.joda.time.DateTime;
import play.db.ebean.Model;

public class LectureEvaluation extends Model {
    public String Name;
    public String Professor;
    public String UserID;
    public String Comment;
    public byte Rating;
    public DateTime TimeStamp;
}
