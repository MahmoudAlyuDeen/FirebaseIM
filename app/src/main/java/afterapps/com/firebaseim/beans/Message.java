package afterapps.com.firebaseim.beans;

/*
 * Created by Mahmoud on 3/13/2017.
 */

public class Message {

    private long timestamp;
    private long negatedTimestamp;
    private long dayTimeStamp;
    private String body;
    private String from;
    private String to;

    public Message(long timestamp, long negatedTimestamp, long dayTimeStamp, String body, String from, String to) {
        this.timestamp = timestamp;
        this.negatedTimestamp = negatedTimestamp;
        this.dayTimeStamp = dayTimeStamp;
        this.body = body;
        this.from = from;
        this.to = to;
    }

    public Message() {
    }

    public long getNegatedTimestamp() {
        return negatedTimestamp;
    }

    public long getDayTimeStamp() {
        return dayTimeStamp;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getBody() {
        return body;
    }
}
