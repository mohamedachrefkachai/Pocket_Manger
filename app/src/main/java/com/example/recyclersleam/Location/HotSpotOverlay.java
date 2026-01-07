package com.example.recyclersleam.Location;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.List;

public class HotSpotOverlay extends Overlay {

    private List<HotSpot> hotSpots;
    private Paint circlePaint;
    private MapView mapView;

    public HotSpotOverlay(MapView mapView, List<HotSpot> hotSpots) {
        super();
        this.mapView = mapView;
        this.hotSpots = hotSpots;
        this.circlePaint = new Paint();
        this.circlePaint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (shadow || hotSpots == null || hotSpots.isEmpty()) {
            return;
        }

        for (HotSpot hotSpot : hotSpots) {
            drawHotSpot(canvas, hotSpot, mapView);
        }
    }

    private void drawHotSpot(Canvas canvas, HotSpot hotSpot, MapView mapView) {
        // Convertir les coordonnées GPS en pixels d'écran
        org.osmdroid.util.GeoPoint geoPoint = new org.osmdroid.util.GeoPoint(
                hotSpot.latitude, hotSpot.longitude);

        android.graphics.Point screenPoint = new android.graphics.Point();
        mapView.getProjection().toPixels(geoPoint, screenPoint);

        // Définir la couleur et l'opacité
        circlePaint.setColor(hotSpot.color);
        circlePaint.setAlpha(80); // Plus transparent
        circlePaint.setStyle(Paint.Style.FILL);

        // Taille du rayon basée sur le montant total (FIXE en pixels, pas en degrés)
        // Rayon entre 30 et 150 pixels selon le montant
        float baseRadius = 30f;
        float maxRadius = 150f;
        float radius = (float) (baseRadius + Math.min(Math.sqrt(hotSpot.totalAmount) * 3, maxRadius - baseRadius));

        // Dessiner le cercle principal
        canvas.drawCircle(screenPoint.x, screenPoint.y, radius, circlePaint);

        // Dessiner le contour
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(3);
        circlePaint.setAlpha(150);
        canvas.drawCircle(screenPoint.x, screenPoint.y, radius, circlePaint);

        // Dessiner le texte du montant
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(20);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);

        // Ajouter une ombre pour le texte
        textPaint.setShadowLayer(3, 0, 0, Color.BLACK);

        String label = String.format("%.0f DT", hotSpot.totalAmount);
        canvas.drawText(label, screenPoint.x, screenPoint.y + 8, textPaint);
    }

    public void updateHotSpots(List<HotSpot> newHotSpots) {
        this.hotSpots = newHotSpots;
    }
}
