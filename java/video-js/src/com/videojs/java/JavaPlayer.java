/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.videojs.java;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DurationUpdateEvent;
import javax.media.EndOfMediaEvent;
import javax.media.GainChangeEvent;
import javax.media.GainChangeListener;
import javax.media.GainControl;
import javax.media.Manager;
import javax.media.MediaTimeSetEvent;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.PrefetchCompleteEvent;
import javax.media.RateChangeEvent;
import javax.media.RealizeCompleteEvent;
import javax.media.RestartingEvent;
import javax.media.StartEvent;
import javax.media.StopAtTimeEvent;
import javax.media.StopByRequestEvent;
import javax.media.StopEvent;
import javax.media.StopTimeChangeEvent;
import javax.media.Time;
import javax.swing.JOptionPane;

/**
 *
 * @author Maxime
 */
public class JavaPlayer extends java.applet.Applet {

    public enum Status {
        UNKNOWN(0),
        INITIALIZED(1),
        PLAYING(2),
        PAUSED(3),
        ENDED(4);
        
        private final int value;
        private Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    
    /**
     * Initializes the applet JavaPlayer
     */
    @Override
    public void init() {
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    initComponents();
                    initJMF();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    } 
    
    protected void initJMF() {
        Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, true);
    }
    
    @Override
    public void paint(Graphics g) {
        String bgcolor = this.getParameter("bgcolor");
        if (bgcolor != null) {
            setBackground(Color.decode(bgcolor));
        }
        super.paint(g);
    }
    
    @Override
    public void start() {
    }
    
    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.deallocate();
        }
    }
    
    protected void initPlayer(URL url) {
        try {
            // clear all previous player and associated controls
            if (mediaPlayer != null) {
                removeAll();
                
                mediaPlayer.stop();
                mediaPlayer.deallocate();
                
                mediaPlayer = null;
                gain = null;
            }
            
            sendEvent("loadstart");
            mediaPlayer = Manager.createPlayer(url);
            mediaPlayer.addControllerListener(new ControllerListener() {
                @Override
                public void controllerUpdate(ControllerEvent e) {
                    if (e instanceof RealizeCompleteEvent) {
                        Component video = mediaPlayer.getVisualComponent();
                        if (video != null) {
                            add(video, BorderLayout.CENTER);
                        }
                        
                        Component controls = null;
                        String showControls = getParameter("controls");
                        if (showControls != null && showControls.equals("true")) {
                            controls = mediaPlayer.getControlPanelComponent();
                        }
                        if (controls != null) {
                            add(controls, BorderLayout.SOUTH);
                        }
                        
                        gain = mediaPlayer.getGainControl();
                        gain.addGainChangeListener(new GainChangeListener() {
                            @Override
                            public void gainChange(GainChangeEvent e) {
                                sendEvent("volumechange");
                            }
                        });
                        
                        validate();
                    }
                    else if (e instanceof PrefetchCompleteEvent) {
                        sendEvent("loadeddata");
                        sendEvent("durationchange");
                    }
                    else if (e instanceof DurationUpdateEvent) {
                        sendEvent("durationchange");
                    }
                    else if (e instanceof MediaTimeSetEvent) {
                        sendEvent("timeupdate");
                    }
                    else if (e instanceof RateChangeEvent) {
                        sendEvent("ratechange");
                    }
                    else if (e instanceof StopTimeChangeEvent) {
                        // Do something?
                    }
                    else if (e instanceof StartEvent) {
                        playerStatus = Status.PLAYING;
                        sendEvent("play");
                        sendEvent("playing");
                    }
                    else if (e instanceof EndOfMediaEvent) {
                        playerStatus = Status.ENDED;
                        sendEvent("pause");
                        sendEvent("ended");
                    }
                    else if (e instanceof ControllerClosedEvent) {
                        playerStatus = Status.ENDED;
                        sendEvent("pause");
                        sendEvent("ended");
                    }
                    else if (e instanceof RestartingEvent) {
                        // Do something?
                    }
                    else if (e instanceof StopAtTimeEvent) {
                        playerStatus = Status.PAUSED;
                        sendEvent("pause");
                    }
                    else if (e instanceof StopByRequestEvent) {
                        playerStatus = Status.PAUSED;
                        sendEvent("pause");
                    }
                    else if (e instanceof StopEvent) {
                        playerStatus = Status.PAUSED;
                        sendEvent("pause");
                    }
                }
            });
            
            playerStatus = Status.INITIALIZED;
        }
        catch (NoPlayerException ex) {
            System.err.println("No media player found: " + ex.getMessage());
        }
        catch (IOException ex) {
            System.err.println("Error reading from the source: " + ex.getMessage());
        }
    }
    
    public static void infoBox(String infoMessage) {
        JOptionPane.showMessageDialog(null, infoMessage, "Video.js Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void setSrc(String url) throws MalformedURLException {
        initPlayer(new URL(url));
        
        String autoplay = getParameter("autoplay");
        String preload = getParameter("preload");
        
        if (autoplay != null && autoplay.equals("true")) {
            playMedia();
        }
        else if (preload != null && preload.equals("true")) {
            loadMedia();
        }
    }
    
    public void loadMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.prefetch();
        }
    }
    
    public void playMedia() {
        if (mediaPlayer != null) {
            if (playerStatus == Status.ENDED) {
                mediaPlayer.setMediaTime(new Time(0));
                mediaPlayer.setStopTime(new Time(getDuration()));
            }
            mediaPlayer.start();
        }
    }
    
    public void pauseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
    
    public void stopMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
    
    public void setCurrentTime(double seconds) {
        if (mediaPlayer != null) {
            mediaPlayer.setMediaTime(new Time(seconds));
        }
    }
    
    public double getCurrentTime() {
        if (mediaPlayer != null) {
            return mediaPlayer.getMediaTime().getSeconds();
        }
        return 0;
    }
    
    public double getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration().getSeconds();
        }
        return 0;
    }
    
    public void setVolume(float volume) {
        if (gain != null) {
            gain.setLevel(volume);
        }
    }
    
    public float getVolume() {
        if (gain != null) {
            return gain.getLevel();
        }
        return 0;
    }
    
    public void setMute(boolean mute) {
        if (gain != null) {
            gain.setMute(mute);
        }
    }
    
    public boolean getMute() {
        if (gain != null) {
            return gain.getMute();
        }
        return false;
    }
    
    public void setRate(float rate) {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(rate);
        }
    }
    
    public float getRate() {
        if (mediaPlayer != null) {
            return mediaPlayer.getRate();
        }
        return 0;
    }
    
    public void enterFullscreeen() {
        if (mediaPlayer != null) {
            // Not supported
        }
    }
    
    public void leaveFullscreeen() {
        if (mediaPlayer != null) {
            // Not supported
        }
    }
    
    public int getPlayerStatus() {
        return playerStatus.getValue();
    }
    
    protected void sendEvent(String event) {
        String jsCallback = getParameter("jscallbackfunction");
        String objId = getParameter("id");
        
        if (jsCallback != null && objId != null) {
            try {
                getAppletContext().showDocument(new URL("javascript:" + jsCallback + "(\"" + objId + "\", \"" + event +"\")"));
            }
            catch(MalformedURLException ex) { }
        }
    }
    
    Player mediaPlayer;
    GainControl gain;
    Status playerStatus = Status.UNKNOWN;

    /**
     * This method is called from within the init() method to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
