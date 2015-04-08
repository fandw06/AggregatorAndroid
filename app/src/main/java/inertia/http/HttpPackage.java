package inertia.http;

import org.json.JSONObject;

/**
 * This is a class holding http request data, including url, JSON data, and command.
 *
 * @author David
 *
 */
public class HttpPackage {
    public String url;
    public JSONObject data;
    public byte command;

    /**
     * Command patterns include HTTP_POST and HTTP_GET,
     */
    public static final byte HTTP_POST = 0x01;
    public static final byte HTTP_GET = 0x02;

    public HttpPackage(String s, JSONObject d, byte c){
        url = s;
        data = d;
        command = c;
    }
}
