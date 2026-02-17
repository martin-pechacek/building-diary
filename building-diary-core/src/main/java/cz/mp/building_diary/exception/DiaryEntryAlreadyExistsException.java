package cz.mp.building_diary.exception;

public class DiaryEntryAlreadyExistsException extends RuntimeException {

    public DiaryEntryAlreadyExistsException(String message) {
        super(message);
    }
}