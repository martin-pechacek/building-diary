package cz.mp.construction_site_diary.enums;

public enum ExportFormat {

    CSV(Values.CSV),
    PDF(Values.PDF);

    private final String value;

    ExportFormat(String value) {
        this.value = value;
    }

    public static class Values {
        public static final String CSV = "CSV";
        public static final String PDF = "PDF";
    }
}