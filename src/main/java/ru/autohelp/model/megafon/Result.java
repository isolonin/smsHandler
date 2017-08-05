package ru.autohelp.model.megafon;

/**
 *
 * @author Ivan
 */
public class Result {
    private Status status;
    private String msg_id;

    public Result() {
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }
}
