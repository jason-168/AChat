package com.sl.achat.recyleradapter;

/**
 * 页面描述：
 * <p>
 * Created by ditclear on 2016/12/17.
 */

public class LineItem {

    private String content;
    private boolean isTitle;

    private String file;
    private int stype;
    private int ofs;

    public LineItem(String content, boolean isTitle) {
        this.content = content;
        this.isTitle = isTitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isTitle() {
        return isTitle;
    }

    public void setTitle(boolean title) {
        isTitle = title;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getStype() {
        return stype;
    }

    public void setStype(int stype) {
        this.stype = stype;
    }

    public int getOfs() {
        return ofs;
    }

    public void setOfs(int ofs) {
        this.ofs = ofs;
    }
}
