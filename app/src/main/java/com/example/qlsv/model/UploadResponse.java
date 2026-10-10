package com.example.qlsv.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response cho upload-avatar. Server có thể trả về nhiều dạng:
 * - { url, fileName, ... }
 * - { anhURI, ... }
 * Code sẽ đọc cả `url` và `anhURI` để linh hoạt.
 */
public class UploadResponse {

    @SerializedName("url")
    private String url;

    @SerializedName("anhURI")
    private String anhURI;

    @SerializedName("fileName")
    private String fileName;

    public UploadResponse() {}

    /** Trả về URL ảnh hợp lệ, ưu tiên `url` rồi đến `anhURI`. */
    public String getImageUrl() {
        if (url != null && !url.isEmpty()) return url;
        return anhURI;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getAnhURI() { return anhURI; }
    public void setAnhURI(String anhURI) { this.anhURI = anhURI; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
