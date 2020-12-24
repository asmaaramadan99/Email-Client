package Models;

import java.util.Date;

public class EmailHeaderImmutable {

    private Integer emailHeaderID;
    private String title;
    private String senderAddress;
    private String senderName;
    private java.util.Date date;
    private Integer priority;

    public EmailHeaderImmutable(EmailHeader emailHeader) {
        this.emailHeaderID = emailHeader.getEmailHeaderID();
        this.title = emailHeader.getTitle();
        this.senderAddress = emailHeader.getSender().getAddress();
        this.senderName = emailHeader.getSender().getUserName();
        this.date = emailHeader.getDate();
        this.priority = emailHeader.getPriority();
    }

    public Integer getEmailHeaderID() {
        return emailHeaderID;
    }

    public String getTitle() {
        return title;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public String getSenderName() {
        return senderName;
    }

    public Date getDate() {
        return date;
    }

    public Integer getPriority() {
        return priority;
    }
}
