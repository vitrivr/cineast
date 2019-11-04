package org.vitrivr.cineast.core.features.neuralnet.tf.models.yolo.util;

/**
 * Model to store the position of the bounding boxes
 */
public class BoxPosition {
    private float left;
    private float top;
    private float right;
    private float bottom;
    private float width;
    private float height;

    public BoxPosition(float left, float top, float width, float height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;

        init();
    }

    public BoxPosition(final BoxPosition boxPosition) {
        this.left = boxPosition.left;
        this.top = boxPosition.top;
        this.width = boxPosition.width;
        this.height = boxPosition.height;

        init();
    }

    public BoxPosition(final BoxPosition boxPosition, final float scaleX, final float scaleY) {
        this.left = boxPosition.left * scaleX;
        this.top = boxPosition.top * scaleY;
        this.width = boxPosition.width * scaleX;
        this.height = boxPosition.height * scaleY;

        init();
    }

    public void init() {
        float tmpLeft = this.left;
        float tmpTop = this.top;
        float tmpRight = this.left + this.width;
        float tmpBottom = this.top + this.height;

        this.left = Math.min(tmpLeft, tmpRight); // left should have lower value as right
        this.top = Math.min(tmpTop, tmpBottom);  // top should have lower value as bottom
        this.right = Math.max(tmpLeft, tmpRight);
        this.bottom = Math.max(tmpTop, tmpBottom);
    }

    public static boolean overlaps(BoxPosition primary, BoxPosition secondary) {
        return primary.getLeft() < secondary.getRight() && primary.getRight() > secondary.getLeft()
            && primary.getTop() < secondary.getBottom() && primary.getBottom() > secondary.getTop();
    }

    public float getLeft() {
        return left;
    }

    public int getLeftInt() {
        return (int) left;
    }

    public float getTop() {
        return top;
    }

    public int getTopInt() {
        return (int) top;
    }

    public float getWidth() {
        return width;
    }

    public int getWidthInt() {
        return (int) width;
    }

    public float getHeight() {
        return height;
    }

    public int getHeightInt() {
        return (int) height;
    }

    public float getRight() {
        return right;
    }

    public int getRightInt() {
        return (int) right;
    }

    public float getBottom() {
        return bottom;
    }

    public int getBottomInt() {
        return (int) bottom;
    }

    @Override
    public String toString() {
        return "BoxPosition{" +
                "left=" + left +
                ", top=" + top +
                ", width=" + width +
                ", height=" + height +
                '}';
    }


}
