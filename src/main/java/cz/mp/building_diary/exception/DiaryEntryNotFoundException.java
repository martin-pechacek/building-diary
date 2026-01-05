package cz.mp.building_diary.exception;

public class DiaryEntryNotFoundException extends RuntimeException {

    public DiaryEntryNotFoundException(String message) {
        super(message);
    }
}