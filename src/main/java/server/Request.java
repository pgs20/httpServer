package server;

import org.apache.http.NameValuePair;

import java.util.List;

public class Request {
    private String method;
    private String path;
    private List<NameValuePair> queryParams;
    private List<NameValuePair> postParams;

    public Request(String method, String path, List<NameValuePair> queryParams, List<NameValuePair> postParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.postParams = postParams;
    }

    public Request(String method, String path, List<NameValuePair> queryParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.postParams = null;
    }

    public String getPostParam(String name) {
        for (NameValuePair param : this.postParams) {
            if (param.getName().equals(name)) return param.getValue();
        }
        return null;
    }

    public List<NameValuePair> getPostParams() {
        return this.postParams;
    }

    public String getQueryParam(String name) {
        for (NameValuePair param : this.queryParams) {
            if (param.getName().equals(name)) return param.getValue();
        }
        return null;
    }

    public List<NameValuePair> getQueryParams() {
        return this.queryParams;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}
