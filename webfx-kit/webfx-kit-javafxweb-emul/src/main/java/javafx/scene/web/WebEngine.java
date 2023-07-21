package javafx.scene.web;

/**
 * @author Bruno Salmon
 */
public class WebEngine {

    private final WebView webView;

    WebEngine(WebView webView) {
        this.webView = webView;
    }

    public void load(String url) {
        webView.setUrl(url);
    }

    public void loadContent(String content) {
        loadContent(content, "text/html");
    }

    public void loadContent(String content, String contentType) {
        webView.setLoadContent(content);
    }
}
