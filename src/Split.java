public class Split {
    private String name;
    private long splitTime;
    private String imagePath;

    public Split(String name) {
        this.name = name;
        this.splitTime = 0;
        this.imagePath = null;
    }

    public String getName() {
        return name;
    }

    public long getSplitTime() {
        return splitTime;
    }

    public void setSplitTime(long time) {
        splitTime = time;
    }

    public String getImagePath() { return imagePath; }

    public void setImagePath(String path) { imagePath = path; }
}