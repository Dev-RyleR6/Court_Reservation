package model;

public class CourtType {
    private int courtTypeId;
    private String description;

    public CourtType(int courtTypeId, String description) {
        this.courtTypeId = courtTypeId;
        this.description = description;
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