package com.example.fortis.ui.treino;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fortis.R;
import com.example.fortis.ui.progresso.ProgressoActivity;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class TreinoConcluidoActivity extends AppCompatActivity {

    private TextView tvTituloConcluido, tvSubtituloConcluido, tvTempoTotalValor, tvPesoTotalValor;
    private MaterialButton btnConcluirStats, btnVerProgresso;

    // --- 1. VARIÁVEL PARA GUARDAR O ID ---
    private long idDoTreino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treino_concluido); // Layout da Etapa 51

        associarViews();

        // --- 2. RECEBER O ID DO TREINO ---
        idDoTreino = getIntent().getLongExtra("TREINO_ID", -1);
        String nomeTreino = getIntent().getStringExtra("NOME_TREINO");
        String diaSemana = getIntent().getStringExtra("DIA_SEMANA");
        String tempoTotalFormatado = getIntent().getStringExtra("TEMPO_TOTAL");
        double pesoTotal = getIntent().getDoubleExtra("PESO_TOTAL", 0.0);

        // ... (popular os campos da UI)
        tvTituloConcluido.setText(nomeTreino != null ? nomeTreino.toUpperCase() : "TREINO CONCLUÍDO");
        tvSubtituloConcluido.setText(diaSemana != null ? diaSemana : "");
        tvTempoTotalValor.setText(tempoTotalFormatado != null ? tempoTotalFormatado : "00:00");
        tvPesoTotalValor.setText(String.format(Locale.getDefault(), "%.0f KG", pesoTotal));


        btnConcluirStats.setOnClickListener(v -> {
            finish();
        });

        // --- 3. AÇÃO DO BOTÃO "VER PROGRESSO" ATUALIZADA ---
        btnVerProgresso.setOnClickListener(v -> {
            Intent intent = new Intent(TreinoConcluidoActivity.this, ProgressoActivity.class);
            // Passa o ID do treino para a tela de Progresso
            intent.putExtra("TREINO_ID", idDoTreino);
            startActivity(intent);
        });
    }

    private void associarViews() {
        tvTituloConcluido = findViewById(R.id.tvTituloConcluido);
        tvSubtituloConcluido = findViewById(R.id.tvSubtituloConcluido);
        tvTempoTotalValor = findViewById(R.id.tvTempoTotalValor);
        tvPesoTotalValor = findViewById(R.id.tvPesoTotalValor);
        btnConcluirStats = findViewById(R.id.btnConcluirStats);
        btnVerProgresso = findViewById(R.id.btnVerProgresso);
    }
}