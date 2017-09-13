package com.oneops.cms.ds;

public class DataTypeHolder {

    private static ThreadLocal<DataType> dataType = new ThreadLocal<DataType>();

    public static void setReadOnlyData() {
        dataType.set(DataType.READ_ONLY);
    }

    public static void unsetReadOnlyData() {
        dataType.set(DataType.DEFAULT);
    }

    public static DataType get() {
        return dataType.get();
    }

    public static void clear() {
        dataType.remove();
    }

}
