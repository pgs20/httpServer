package server;

import org.apache.http.NameValuePair;

import java.util.List;

public class Request {
    private String method;
    private String path;
    private List<NameValuePair> params;
    private String protocol;

    public Request(String method, String path, List<NameValuePair> params, String protocol) {
        this.method = method;
        this.path = path;
        this.params = params;
        this.protocol = protocol;
    }

    public String getQueryParam(String name) {
        for (NameValuePair param : this.params) {
            if (param.getName().equals(name)) return param.getValue();
        }
        return null;
    }

    public List<NameValuePair> getQueryParams() {
        return this.params;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getProtocol() {
        return protocol;
    }
}
