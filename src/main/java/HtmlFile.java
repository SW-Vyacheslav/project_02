import com.hp.gagawa.java.elements.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HtmlFile {
    private String filePath;
    private Div rootBlock;

    public HtmlFile(String filePath) {
        this.filePath = filePath;
        rootBlock = new Div();
    }

    public void addBoldText(String text) {
        addBoldText(text, "black");
    }

    public void addBoldText(String text, String color) {
        Div boldBlock = new Div();
        boldBlock.setStyle(String.format("font-weight: bold; color:%s", color));
        boldBlock.appendText(text);
        rootBlock.appendChild(boldBlock);
    }

    public void addText(String text) {
        Div textBlock = new Div();
        textBlock.appendText(text);
        rootBlock.appendChild(textBlock);
    }

    public void addLink(String text, String link) {
        Div wrapBlock = new Div();
        A linkBlock = new A();
        linkBlock.setHref(link);
        linkBlock.setAttribute("target", "_blank");
        linkBlock.appendText(text);
        wrapBlock.appendChild(linkBlock);
        rootBlock.appendChild(wrapBlock);
    }

    public void addLink(String link) {
        addLink(link, link);
    }

    public void save() throws IOException {
        Files.write(Paths.get(filePath), rootBlock.write().getBytes());
    }
}
