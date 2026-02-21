package cz.mp.construction_site_diary.exception;

public class DiaryEntryNotFoundException extends RuntimeException {

    public DiaryEntryNotFoundException(String message) {
        super(message);
    }
}