package org.example.service;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class AvatarGeneratorService {
    private static AvatarGeneratorService instance;
    private static final int SIZE = 400;
    private static final String DIR = "user_data/avatars";
    private static final String[][] PALETTES = {
        {"#667eea","#764ba2"},{"#f093fb","#f5576c"},{"#4facfe","#00f2fe"},
        {"#43e97b","#38f9d7"},{"#fa709a","#fee140"},{"#a18cd1","#fbc2eb"},
        {"#ffecd2","#fcb69f"},{"#a1c4fd","#c2e9fb"},{"#d4fc79","#96e6a1"},{"#84fab0","#8fd3f4"}
    };

    private AvatarGeneratorService() {
        try { Files.createDirectories(Paths.get(DIR)); } catch (IOException e) { e.printStackTrace(); }
    }

    public static AvatarGeneratorService getInstance() {
        if (instance == null) instance = new AvatarGeneratorService();
        return instance;
    }

    public String generateAvatarFromName(int userId, String name) {
        String initials = getInitials(name);
        int idx = Math.abs(name.hashCode()) % PALETTES.length;
        Color c1 = Color.web(PALETTES[idx][0]), c2 = Color.web(PALETTES[idx][1]);
        AtomicReference<String> result = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(1);
        Runnable task = () -> {
            try {
                Canvas canvas = new Canvas(SIZE, SIZE);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                RadialGradient grad = new RadialGradient(0,0,0.5,0.5,0.7,true,CycleMethod.NO_CYCLE,new Stop(0,c1),new Stop(1,c2));
                gc.setFill(grad); gc.fillOval(0,0,SIZE,SIZE);
                gc.setGlobalAlpha(0.15); gc.setFill(Color.WHITE);
                gc.fillOval(SIZE*0.55,-SIZE*0.15,SIZE*0.5,SIZE*0.5);
                gc.fillOval(-SIZE*0.1,SIZE*0.6,SIZE*0.4,SIZE*0.4);
                gc.fillOval(-SIZE*0.05,SIZE*0.15,SIZE*0.3,SIZE*0.3);
                gc.setGlobalAlpha(0.2);
                for (int i=0;i<8;i++) {
                    double a=2*Math.PI*i/8-Math.PI/2, r=SIZE*0.42, cx=SIZE/2.0, cy=SIZE/2.0;
                    gc.fillOval(cx+r*Math.cos(a)-6,cy+r*Math.sin(a)-6,12,12);
                }
                gc.setGlobalAlpha(1.0); gc.setFill(Color.WHITE);
                gc.setFont(Font.font("System",FontWeight.BOLD,SIZE*0.35));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setGlobalAlpha(0.3); gc.setFill(Color.rgb(0,0,0));
                gc.fillText(initials,SIZE/2.0+2,SIZE/2.0+SIZE*0.13+2);
                gc.setGlobalAlpha(1.0); gc.setFill(Color.WHITE);
                gc.fillText(initials,SIZE/2.0,SIZE/2.0+SIZE*0.13);
                gc.setGlobalAlpha(0.3); gc.setStroke(Color.WHITE); gc.setLineWidth(3);
                gc.strokeOval(4,4,SIZE-8,SIZE-8);
                WritableImage wi = new WritableImage(SIZE,SIZE);
                SnapshotParameters sp = new SnapshotParameters(); sp.setFill(Color.TRANSPARENT);
                canvas.snapshot(sp,wi);
                result.set(saveImage(wi,userId));
            } catch (Exception e) { e.printStackTrace(); } finally { latch.countDown(); }
        };
        if (Platform.isFxApplicationThread()) task.run(); else Platform.runLater(task);
        try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return result.get();
    }

    public String generateAvatarFromPhoto(int userId, String photoPath, String name) {
        int idx = Math.abs(name.hashCode()) % PALETTES.length;
        Color c1 = Color.web(PALETTES[idx][0]), c2 = Color.web(PALETTES[idx][1]);
        AtomicReference<String> result = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(1);
        Runnable task = () -> {
            try {
                Image src = new Image(new File(photoPath).toURI().toString(),SIZE,SIZE,true,true);
                Canvas canvas = new Canvas(SIZE,SIZE);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                double bw = 12;
                RadialGradient bg = new RadialGradient(0,0,0.5,0.5,0.7,true,CycleMethod.NO_CYCLE,new Stop(0,c1),new Stop(1,c2));
                gc.setFill(bg); gc.fillOval(0,0,SIZE,SIZE);
                double inner = SIZE - bw*2;
                gc.save(); gc.beginPath();
                gc.arc(SIZE/2.0,SIZE/2.0,inner/2.0,inner/2.0,0,360);
                gc.closePath(); gc.clip();
                double iw=src.getWidth(),ih=src.getHeight();
                double sc=Math.max(inner/iw,inner/ih);
                double dw=iw*sc,dh=ih*sc,dx=(SIZE-dw)/2.0,dy=(SIZE-dh)/2.0;
                gc.drawImage(src,dx,dy,dw,dh); gc.restore();
                gc.setGlobalAlpha(0.15); gc.setFill(Color.WHITE);
                for (int i=0;i<12;i++) {
                    double a=2*Math.PI*i/12, r=SIZE*0.47, cx=SIZE/2.0, cy=SIZE/2.0;
                    gc.fillOval(cx+r*Math.cos(a)-4,cy+r*Math.sin(a)-4,8,8);
                }
                gc.setGlobalAlpha(1.0);
                WritableImage wi = new WritableImage(SIZE,SIZE);
                SnapshotParameters sp = new SnapshotParameters(); sp.setFill(Color.TRANSPARENT);
                canvas.snapshot(sp,wi);
                result.set(saveImage(wi,userId));
            } catch (Exception e) { e.printStackTrace(); } finally { latch.countDown(); }
        };
        if (Platform.isFxApplicationThread()) task.run(); else Platform.runLater(task);
        try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return result.get();
    }

    private String saveImage(WritableImage image, int userId) throws IOException {
        Path dir = Paths.get(DIR); Files.createDirectories(dir);
        String fn = "avatar_"+userId+"_"+System.currentTimeMillis()+".png";
        File f = dir.resolve(fn).toFile();
        BufferedImage bi = SwingFXUtils.fromFXImage(image,null);
        ImageIO.write(bi,"png",f);
        System.out.println("[AvatarGenerator] Saved: "+f.getAbsolutePath());
        return f.getAbsolutePath();
    }

    private String getInitials(String name) {
        if (name==null||name.trim().isEmpty()) return "?";
        String[] p = name.trim().split("\\s+");
        if (p.length>=2) return (p[0].substring(0,1)+p[p.length-1].substring(0,1)).toUpperCase();
        return name.substring(0,Math.min(2,name.length())).toUpperCase();
    }
}
