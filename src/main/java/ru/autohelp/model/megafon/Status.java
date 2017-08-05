package ru.autohelp.model.megafon;

/**
 *
 * @author Ivan
 */
public class Status {
    private String description;
    private Integer code;

    public Status() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
