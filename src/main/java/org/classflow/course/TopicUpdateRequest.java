
package org.classflow.course;

public class TopicUpdateRequest {

    private String topicName;
    private Boolean topicCompleted;
    private String topicNotes;

    public TopicUpdateRequest() {
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Boolean getTopicCompleted() {
        return topicCompleted;
    }

    public void setTopicCompleted(Boolean topicCompleted) {
        this.topicCompleted = topicCompleted;
    }

    public String getTopicNotes() {
        return topicNotes;
    }

    public void setTopicNotes(String topicNotes) {
        this.topicNotes = topicNotes;
    }
}
