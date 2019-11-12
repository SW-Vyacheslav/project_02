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
        Div boldBlock = new Div();
        boldBlock.setStyle("font-weight: bold;");
        boldBlock.appendText(text);
        rootBlock.appendChild(boldBlock);
    }

    public void addText(String text) {
        Div textBlock = new Div();
        textBlock.appendText(text);
        rootBlock.appendChild(textBlock);
    }

    public void addTextWithLink(String text, String link) {
        Div wrapBlock = new Div();
        A linkBlock = new A();
        linkBlock.setHref(link);
        linkBlock.appendText(text);
        wrapBlock.appendChild(linkBlock);
        rootBlock.appendChild(wrapBlock);
    }

    public void addLink(String link) {
        Div wrapBlock = new Div();
        A linkBlock = new A();
        linkBlock.setHref(link);
        linkBlock.appendText(link);
        wrapBlock.appendChild(linkBlock);
        rootBlock.appendChild(wrapBlock);
    }

    public void save() throws IOException {
        Files.write(Paths.get(filePath), rootBlock.write().getBytes());
    }
}
