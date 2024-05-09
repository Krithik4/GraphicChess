import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * This is mouse class that enable mouse input to move and select pieces
 */
public class Mouse extends MouseAdapter {
    private int x, y;
    private boolean pressed;

    /**
     * This returns whether the mouse is pressed or not
     * @return if the mouse is pressed or not
     */
    public boolean isPressed(){
        return this.pressed;
    }

    /**
     * This sets the pressed to true
     * @param e a mouse event
     */
    public void mousePressed(MouseEvent e){
        this.pressed = true;
    }

    /**
     * This sets the pressed to false
     * @param e a mouse event
     */
    public void mouseReleased(MouseEvent e){
        this.pressed = false;
    }

    /**
     * This updates the mouse's x and y value based on the mouse event
     * @param e a mouse event
     */
    public void mouseDragged(MouseEvent e){
        this.x = e.getX();
        this.y = e.getY();
    }

    /**
     * This updates the mouse's x and y value based on the mouse event
     * @param e a mouse event
     */
    public void mouseMoved(MouseEvent e){
        this.x = e.getX();
        this.y = e.getY();
    }

    /**
     * This gets the x value of the mouse
     * @return the x value
     */
    public int getX(){
        return this.x;
    }

    /**
     * This gets the y value of the mouse
     * @return the y value
     */
    public int getY(){
        return this.y;
    }
}
