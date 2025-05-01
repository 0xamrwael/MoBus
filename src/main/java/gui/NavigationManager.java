/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;
import javax.swing.JFrame;
import java.util.Stack;
/**
 *
 * @author Amr
 */
public class NavigationManager {
    private static NavigationManager instance;
    private Stack<JFrame> frameStack;

    private NavigationManager() {
        frameStack = new Stack<>();
    }

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void navigateTo(JFrame newFrame) {
        if (!frameStack.isEmpty()) {
            JFrame currentFrame = frameStack.peek();
            currentFrame.setVisible(false);
        }
        frameStack.push(newFrame);
        newFrame.setVisible(true);
    }

    public void goBack() {
        if (frameStack.size() > 1) {
            JFrame currentFrame = frameStack.pop();
            currentFrame.dispose();
            JFrame previousFrame = frameStack.peek();
            previousFrame.setVisible(true);
        }
    }
}