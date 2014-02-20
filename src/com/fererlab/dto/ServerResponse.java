package com.fererlab.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * acm
 */
@XStreamAlias("response")
public class ServerResponse {

    @XStreamAlias("header")
    private Header<String, Object> header = new Header<String, Object>();

    @XStreamAlias("content")
    private Content<String, Object> content = new Content<String, Object>();

    public ServerResponse() {
    }

    public ServerResponse(String status, String message) {
        header.add("status", status);
        header.add("message", message);
    }

    public ServerResponse(Header<String, Object> header, Content<String, Object> content) {
        this.header = header;
        this.content = content;
    }

    public ServerResponse add(String key, Object value) {
        content.add(key, value);
        return this;
    }

    public Header<String, Object> getHeader() {
        return header;
    }

    public void setHeader(Header<String, Object> header) {
        this.header = header;
    }

    public Content<String, Object> getContent() {
        return content;
    }

    public void setContent(Content<String, Object> content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ServerResponse{" +
                "header=" + header +
                ", content=" + content +
                '}';
    }
}
