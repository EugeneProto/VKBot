package bot.handler.model;

public class EmojiDataModel {
    /**
     * Parameters for text convert request.
     */
    private String text, background, foreground,

    /**
     * Marker.
     */
    whatIsNext;

    public EmojiDataModel() {
        whatIsNext="text";
    }

    public void setText(String text) {
        this.text = text;
        whatIsNext="background";
    }

    public void setBackground(String background) {
        this.background = background;
        whatIsNext="foreground";
    }

    public void setForeground(String foreground) {
        this.foreground = foreground;
    }

    public String getText() {
        return text;
    }

    public String getBackground() {
        return background;
    }

    public String getForeground() {
        return foreground;
    }

    public String whatIsNext() {
        return whatIsNext;
    }
}
