package cz.mp.building_diary.exception;

public class CoordinatesNotFoundException extends RuntimeException {

    public CoordinatesNotFoundException(String message) {
        super(message);
    }
}