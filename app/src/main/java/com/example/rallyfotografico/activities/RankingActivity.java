package com.example.rallyfotografico.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rallyfotografico.R;
import com.example.rallyfotografico.adapters.FotoAdapter;
import com.example.rallyfotografico.models.Foto;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

/**
 * Actividad que muestra el ranking de las fotos más votadas.
 * Incluye un gráfico de barras con el top 5 de seudónimos más votados
 * y un RecyclerView con todas las fotos admitidas.
 */
public class RankingActivity extends AppCompatActivity {

    private RecyclerView rvRanking;
    private FotoAdapter fotoAdapter;
    private List<Foto> fotoList;
    private FirebaseFirestore db;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        // Inicializamos la lista de fotos y Firestore
        rvRanking = findViewById(R.id.rvRanking);
        rvRanking.setLayoutManager(new LinearLayoutManager(this));

        barChart = findViewById(R.id.barChart);
        db = FirebaseFirestore.getInstance();
        fotoList = new ArrayList<>();

        // Creamos el adaptador sin función de eliminación (solo visualización)
        fotoAdapter = new FotoAdapter(fotoList, (foto) -> {}, false);
        rvRanking.setAdapter(fotoAdapter);

        // Cargamos el ranking desde Firebase
        cargarRanking();
    }

    /**
     * Carga las fotos admitidas desde Firestore, agrupa los votos por seudónimo
     * y prepara los datos para el gráfico de barras (Top 5).
     */
    private void cargarRanking() {
        db.collection("fotos")
                .whereEqualTo("estado", "admitida")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> votosPorSeudonimo = new HashMap<>();

                    // Sumar los votos por seudónimo
                    for (var doc : queryDocumentSnapshots) {
                        Foto foto = doc.toObject(Foto.class);
                        String seudonimo = foto.getSeudonimo() != null ? foto.getSeudonimo() : "Sin nombre";
                        int votos = foto.getVotos();
                        votosPorSeudonimo.put(seudonimo, votosPorSeudonimo.getOrDefault(seudonimo, 0) + votos);
                    }

                    // Ordenar el top 5 por votos descendentes
                    List<Map.Entry<String, Integer>> top5 = new ArrayList<>(votosPorSeudonimo.entrySet());
                    top5.sort((a, b) -> b.getValue() - a.getValue());
                    if (top5.size() > 5) top5 = top5.subList(0, 5);

                    // Preparar datos para el gráfico
                    List<BarEntry> entries = new ArrayList<>();
                    List<String> labels = new ArrayList<>();

                    int index = 0;
                    for (Map.Entry<String, Integer> entry : top5) {
                        entries.add(new BarEntry(index, entry.getValue()));
                        labels.add(entry.getKey());
                        index++;
                    }

                    // Mostrar el gráfico en pantalla
                    mostrarGrafico(entries, labels);
                })
                .addOnFailureListener(e -> Toast.makeText(RankingActivity.this, "Error al cargar ranking", Toast.LENGTH_SHORT).show());
    }

    /**
     * Configura y muestra el gráfico de barras con los datos proporcionados.
     *
     * @param entries Lista de entradas (valores de votos)
     * @param labels Lista de seudónimos correspondientes a cada barra
     */
    private void mostrarGrafico(List<BarEntry> entries, List<String> labels) {
        BarDataSet dataSet = new BarDataSet(entries, "Top 5 participantes");
        dataSet.setValueTextSize(12f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Colores variados por barra

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false); // Oculta descripción por defecto
        barChart.getLegend().setEnabled(false);      // Oculta leyenda

        // Configuración del eje X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Seudónimos como etiquetas
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);

        // Configuración del eje Y (izquierdo)
        barChart.getAxisLeft().setGranularity(1f);
        barChart.getAxisLeft().setGranularityEnabled(true);
        barChart.getAxisLeft().setAxisMinimum(0f); // Desde 0
        barChart.getAxisRight().setEnabled(false); // Oculta eje derecho
        barChart.getAxisLeft().setTextSize(12f);

        // Ajuste de colores para tema oscuro o claro
        boolean isDark = (getResources().getConfiguration().uiMode & 0x30) == 0x20;
        int textColor = isDark ? Color.WHITE : Color.BLACK;

        barChart.setBackgroundColor(Color.TRANSPARENT);
        xAxis.setTextColor(textColor);
        barChart.getAxisLeft().setTextColor(textColor);
        barChart.getXAxis().setAxisLineColor(textColor);
        barChart.getAxisLeft().setAxisLineColor(textColor);
        dataSet.setValueTextColor(textColor);

        // Animación del gráfico
        barChart.animateY(1000);
        barChart.invalidate(); // Refresca el gráfico
    }

}
