package com.example.fortis.ui.progresso;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.fortis.R;
import com.example.fortis.data.SessionManager;
import com.example.fortis.data.model.Exercicio;
import com.example.fortis.data.model.HistoricoResponseDTO;
// (Imports das activities de navegação)
import com.example.fortis.ui.home.HomeActivity;
import com.example.fortis.ui.perfil.PerfilActivity;
import com.example.fortis.ui.suporte.AjudaActivity;
import com.example.fortis.ui.treino.MeusTreinosActivity;
import com.example.fortis.ui.treino.TreinoDoDiaRouterActivity;
import com.example.fortis.viewmodel.progresso.ProgressoViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton; // Import do Botão

// Imports da Biblioteca de Gráfico (Etapa 74/76)
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

// Imports de Data (Etapa 76)
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProgressoActivity extends AppCompatActivity {

    private ProgressoViewModel viewModel;
    private SessionManager sessionManager;
    private long idDoTreino;
    private String tokenDeAutenticacao;
    private List<Exercicio> listaDeExerciciosLocal = new ArrayList<>();

    // Views
    private ImageButton ibVoltar, ibConfiguracao;
    private BottomNavigationView bottomNavigationView;
    private Spinner spinnerExercicios;
    private ProgressBar progressBar;
    private LinearLayout llConteudoProgresso;
    private LineChart lineChart; // Componente do Gráfico
    private MaterialButton btnVoltarInicio; // Botão "Voltar ao Início"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progresso); // Layout da Etapa 60

        sessionManager = new SessionManager(this);
        tokenDeAutenticacao = sessionManager.getAuthToken();
        idDoTreino = getIntent().getLongExtra("TREINO_ID", -1); // ID vindo da Etapa 57

        if (idDoTreino == -1 || tokenDeAutenticacao == null) {
            Toast.makeText(this, "Erro: Treino ou Autenticação inválida.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ProgressoViewModel.class);

        associarViews();
        configurarHeaderBotoes();
        configurarBottomNavigation();
        configurarObservadores();
        configurarEstiloGrafico(); // Estilização base (Etapa 76)

        // Inicia a primeira chamada: carregar o spinner
        viewModel.carregarExerciciosDoTreino(tokenDeAutenticacao, idDoTreino);
    }

    private void associarViews() {
        ibVoltar = findViewById(R.id.ibVoltar);
        ibConfiguracao = findViewById(R.id.ibConfiguracao);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        spinnerExercicios = findViewById(R.id.spinnerExerciciosProgresso);
        progressBar = findViewById(R.id.progressBarProgresso);
        llConteudoProgresso = findViewById(R.id.llConteudoProgresso);
        lineChart = findViewById(R.id.lineChartProgresso); // ID do Gráfico (Etapa 75)
        btnVoltarInicio = findViewById(R.id.btnVoltarInicioProgresso); // ID do Botão (Etapa 79)
    }

    private void configurarHeaderBotoes() {
        ibVoltar.setOnClickListener(v -> finish());
        ibConfiguracao.setOnClickListener(v -> finish());

        // Configura o botão "Voltar ao Início" (Etapa 79)
        btnVoltarInicio.setOnClickListener(v -> {
            Intent intent = new Intent(ProgressoActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void configurarObservadores() {

        // Observador do carregamento do Spinner
        viewModel.getEstaCarregandoSpinner().observe(this, isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                llConteudoProgresso.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                llConteudoProgresso.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getErroSpinner().observe(this, erro -> {
            Toast.makeText(this, erro, Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
        });

        // Observador da lista de exercícios (para o Spinner)
        viewModel.getListaExercicios().observe(this, exercicios -> {
            if (exercicios != null && !exercicios.isEmpty()) {
                listaDeExerciciosLocal.clear();
                listaDeExerciciosLocal.addAll(exercicios);

                List<String> nomesExercicios = exercicios.stream()
                        .map(Exercicio::getNome)
                        .collect(Collectors.toList());

                popularSpinnerComDados(nomesExercicios);

                // Carrega o gráfico para o primeiro item (se o listener não o fizer)
                if (spinnerExercicios.getAdapter().getCount() > 0) {
                    Exercicio primeiroExercicio = listaDeExerciciosLocal.get(0);
                    viewModel.carregarDadosDoGrafico(tokenDeAutenticacao, primeiroExercicio.getId());
                }
            }
        });

        // Observador do carregamento do Gráfico
        viewModel.getEstaCarregandoGrafico().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            lineChart.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        });

        viewModel.getErroGrafico().observe(this, erro -> {
            Toast.makeText(this, erro, Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });

        // --- OBSERVADOR PRINCIPAL (DADOS DO GRÁFICO) ---
        viewModel.getHistoricoGrafico().observe(this, historico -> {
            // A UI é atualizada aqui (Etapa 76)
            if (historico != null && !historico.isEmpty()) {
                // DESENHA O GRÁFICO
                desenharGrafico(historico);

            } else if (historico != null) {
                // (historico.isEmpty())
                Toast.makeText(this, "Nenhum histórico salvo para este exercício.", Toast.LENGTH_SHORT).show();
                lineChart.clear(); // Limpa o gráfico anterior
                lineChart.invalidate(); // Atualiza a tela do gráfico
            }
        });
    }

    private void popularSpinnerComDados(List<String> nomesExercicios) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nomesExercicios
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExercicios.setAdapter(adapter);

        // Listener de seleção do Spinner
        spinnerExercicios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (listaDeExerciciosLocal != null && position < listaDeExerciciosLocal.size()) {
                    Exercicio exercicioSelecionado = listaDeExerciciosLocal.get(position);

                    // Chama o ViewModel (Etapa 88)
                    viewModel.carregarDadosDoGrafico(tokenDeAutenticacao, exercicioSelecionado.getId());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // Método de Estilização (Etapa 76)
    private void configurarEstiloGrafico() {
        lineChart.setNoDataText("Selecione um exercício para ver o progresso.");
        lineChart.setNoDataTextColor(Color.BLACK);
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getLegend().setTextColor(Color.BLACK);
        lineChart.getAxisLeft().setTextColor(Color.BLACK);
        lineChart.getXAxis().setTextColor(Color.BLACK);
    }

    // Método de Desenho (Etapa 76)
    private void desenharGrafico(List<HistoricoResponseDTO> historico) {
        List<Entry> entries = new ArrayList<>();
        final List<String> labelsX = new ArrayList<>();

        for (int i = 0; i < historico.size(); i++) {
            HistoricoResponseDTO item = historico.get(i);
            // Valor Y: A carga usada
            entries.add(new Entry(i, (float) item.getCarga()));
            // Valor X (Label): A data formatada
            labelsX.add(formatarData(item.getDataExecucao()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Carga (KG)");

        int corAzul = ContextCompat.getColor(this, R.color.Ford_Blue);
        int corAzulEscuro = ContextCompat.getColor(this, R.color.blue_dark);

        dataSet.setColor(corAzul);
        dataSet.setCircleColor(corAzulEscuro);
        dataSet.setHighLightColor(Color.RED);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(corAzulEscuro);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelsX));

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    // Formatador de Data (Etapa 76)
    private String formatarData(String dataApi) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                LocalDate date = LocalDate.parse(dataApi);
                return date.format(DateTimeFormatter.ofPattern("dd/MM"));
            } catch (Exception e) {
                return dataApi;
            }
        } else {
            if (dataApi.length() > 7) {
                return dataApi.substring(8) + "/" + dataApi.substring(5, 7);
            }
            return dataApi;
        }
    }

    // Configuração do Menu Inferior (Etapa 55.1)
    private void configurarBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_ficha_treinos) {
                startActivity(new Intent(this, MeusTreinosActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_perfil) {
                startActivity(new Intent(this, PerfilActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_treino_do_dia) {
                startActivity(new Intent(this, TreinoDoDiaRouterActivity.class));
                return true;
            } else if (itemId == R.id.nav_chat_suporte) {
                startActivity(new Intent(this, AjudaActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}