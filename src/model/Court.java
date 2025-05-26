package model;

public class Court {
    private int courtId;
    private int courtTypeId;
    private String description;

    public Court(int courtId, int courtTypeId, String description) {
        this.courtId = courtId;
        this.courtTypeId = courtTypeId;
        this.description = description;
    }

    public int getCourtId() {
        return courtId;
    }

    public void setCourtId(int courtId) {
        this.courtId = courtId;
    }

    public int getCourtTypeId() {
        return courtTypeId;
    }

    public void setCourtTypeId(int courtTypeId) {
        this.courtTypeId = courtTypeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
} 