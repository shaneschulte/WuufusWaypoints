package com.github.jarada.waypoints.data;

public enum MenuSize {

    COMPACT(9, 18),
    SMALL(18, 27),
    MEDIUM(27, 36),
    LARGE(36, 45),
    MAX(45, 54),
    RESIZE(0, 54);

    public static final int STEP_SIZE = 9;

    private int smallSize;
    private int largeSize;

    MenuSize(int smallSize, int largeSize) {
        this.smallSize = smallSize;
        this.largeSize = largeSize;
    }

    public int getDataSize(int size) {
        if (smallSize > 0)
            return smallSize;

        int smallSizeAdj = (size % STEP_SIZE) * STEP_SIZE;
        if (smallSizeAdj > (largeSize - STEP_SIZE)) {
            smallSizeAdj = largeSize - STEP_SIZE;
        }
        return smallSizeAdj;
    }

    public int getFullSize(int size, boolean bottomRow) {
        if (smallSize > 0) {
            return bottomRow ? largeSize : smallSize;
        }

        int smallSizeAdj = getDataSize(size);
        return bottomRow ? smallSizeAdj + STEP_SIZE : smallSizeAdj;
    }

}
