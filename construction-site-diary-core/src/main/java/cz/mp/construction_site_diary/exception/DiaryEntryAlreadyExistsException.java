package cz.mp.construction_site_diary.exception;

public class DiaryEntryAlreadyExistsException extends RuntimeException {

    public DiaryEntryAlreadyExistsException(String message) {
        super(message);
    }
}