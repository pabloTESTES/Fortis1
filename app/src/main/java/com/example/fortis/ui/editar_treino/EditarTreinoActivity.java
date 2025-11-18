package com.example.fortis.ui.editar_treino;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton; // <-- 1. IMPORTAR IMAGEBUTTON
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
// import androidx.appcompat.widget.Toolbar; // <-- 2. REMOVER TOOLBAR
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fortis.R;
import com.example.fortis.data.SessionManager;
import com.example.fortis.data.model.Exercicio;
import com.example.fortis.data.model.Treino;
import com.example.fortis.ui.treino.TreinoActivity;
import com.example.fortis.viewmodel.treino.EditarTreinoViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class EditarTreinoActivity extends AppCompatActivity {

    private EditarTreinoViewModel viewModel;
    private SessionManager gerenciadorDeSessao;
    private long idDoTreino;
    private String tokenDeAutenticacao;
    private boolean modoVisualizacao = false;
    private ExercicioEditAdapter exercicioAdapter;

    // --- 3. VIEWS DO CABEÇALHO ATUALIZADAS ---
    private ProgressBar barraDeProgresso;
    // private Toolbar toolbar; // (Removido)
    private ImageButton ibVoltar;
    private ImageButton ibConfiguracao;

    private TextInputEditText etNomeTreino;
    private AutoCompleteTextView actvDiaSemana;
    private Button btnExcluirTreino;
    private Button btnAdicionarExercicio;
    private RecyclerView rvExerciciosEdit;
    private Button btnSalvarTreino;
    private MaterialButton btnIniciarTreinoPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_treino);

        gerenciadorDeSessao = new SessionManager(this);
        tokenDeAutenticacao = gerenciadorDeSessao.getAuthToken();
        idDoTreino = getIntent().getLongExtra("TREINO_ID", -1);
        modoVisualizacao = getIntent().getBooleanExtra("MODO_VISUALIZACAO", false);

        if (idDoTreino == -1 || tokenDeAutenticacao == null) {
            Toast.makeText(this, "Erro: Treino ou Autenticação inválida.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(EditarTreinoViewModel.class);

        associarViews();
        // configurarToolbar(); // (Removido)
        configurarHeaderBotoes(); // <-- 4. NOVO MÉTODO CHAMADO
        configurarBotoes();
        configurarRecyclerView();
        configurarObservadores();
        preencherDropdownDias();

        atualizarModoUI();

        viewModel.buscarTreinoPorId(tokenDeAutenticacao, idDoTreino);
    }

    private void associarViews() {
        // --- 5. ASSOCIAR NOVOS BOTÕES DO CABEÇALHO ---
        // toolbar = findViewById(R.id.toolbarEditarTreino); // (Removido)
        ibVoltar = findViewById(R.id.ibVoltar);
        ibConfiguracao = findViewById(R.id.ibConfiguracao);

        etNomeTreino = findViewById(R.id.etNomeTreino);
        actvDiaSemana = findViewById(R.id.actvDiaSemana);
        rvExerciciosEdit = findViewById(R.id.rvExerciciosEdit);
        btnAdicionarExercicio = findViewById(R.id.btnAdicionarExercicio);
        btnSalvarTreino = findViewById(R.id.btnSalvarTreino);
        btnIniciarTreinoPreview = findViewById(R.id.btnIniciarTreinoPreview);

        try {
            barraDeProgresso = findViewById(R.id.progressBarEdit);
        } catch (Exception e) { }
        try {
            btnExcluirTreino = findViewById(R.id.btnExcluirTreino);
        } catch (Exception e) { }
    }

    // --- 6. MÉTODO ANTIGO DA TOOLBAR REMOVIDO ---
    /*
    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }
    */

    // --- 7. NOVO MÉTODO (padrão FAQ/Termos) ---
    private void configurarHeaderBotoes() {
        ibVoltar.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish(); // Ação de "Voltar"
        });

        ibConfiguracao.setOnClickListener(v -> {
            // Se precisar que ele faça algo. Por enquanto, "Voltar".
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }


    private void preencherDropdownDias() {
        String[] dias = new String[]{"segunda", "terca", "quarta", "quinta", "sexta", "sabado", "domingo"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, dias);
        actvDiaSemana.setAdapter(adapter);
    }

    private void configurarRecyclerView() {
        exercicioAdapter = new ExercicioEditAdapter(new ArrayList<>(), modoVisualizacao,
                exercicio -> mostrarDialogoEditarExercicio(exercicio),
                exercicio -> {
                    new AlertDialog.Builder(EditarTreinoActivity.this)
                            .setTitle("Deletar Exercício")
                            .setMessage("Tem certeza que deseja deletar o exercício '" + exercicio.getNome() + "'?")
                            .setPositiveButton("Sim", (dialog, which) -> {
                                viewModel.deletarExercicio(tokenDeAutenticacao, exercicio.getId());
                            })
                            .setNegativeButton("Não", null)
                            .show();
                });

        rvExerciciosEdit.setLayoutManager(new LinearLayoutManager(this));
        rvExerciciosEdit.setAdapter(exercicioAdapter);
    }

    // --- 8. LÓGICA DE UI (REMOVENDO O 'getSupportActionBar()') ---
    private void atualizarModoUI() {
        btnIniciarTreinoPreview.setVisibility(View.VISIBLE);

        if (modoVisualizacao) {
            // MODO "TREINO DO DIA"
            // getSupportActionBar().setTitle("Treino do Dia"); // (Removido)

            btnSalvarTreino.setVisibility(View.GONE);
            btnAdicionarExercicio.setVisibility(View.GONE);
            if (btnExcluirTreino != null) {
                btnExcluirTreino.setVisibility(View.GONE);
            }

            etNomeTreino.setEnabled(false);
            actvDiaSemana.setEnabled(false);
            if (exercicioAdapter != null) {
                exercicioAdapter.setModoEdicao(false);
            }
        } else {
            // MODO "GERENCIAMENTO"
            // getSupportActionBar().setTitle("Detalhes do Treino"); // (Removido)

            btnSalvarTreino.setVisibility(View.VISIBLE);
            btnAdicionarExercicio.setVisibility(View.VISIBLE);
            if (btnExcluirTreino != null) {
                btnExcluirTreino.setVisibility(View.VISIBLE);
            }

            etNomeTreino.setEnabled(true);
            actvDiaSemana.setEnabled(true);
            if (exercicioAdapter != null) {
                exercicioAdapter.setModoEdicao(true);
            }
        }
    }


    private void configurarBotoes() {
        if (btnExcluirTreino != null) {
            btnExcluirTreino.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Excluir Treino")
                        .setMessage("Tem certeza que deseja excluir este treino permanentemente?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            viewModel.deletarTreino(tokenDeAutenticacao, idDoTreino);
                            setResult(Activity.RESULT_OK);
                        })
                        .setNegativeButton("Não", null)
                        .show();
            });
        }

        btnAdicionarExercicio.setOnClickListener(v -> {
            mostrarDialogoAdicionarExercicio();
        });

        btnSalvarTreino.setOnClickListener(v -> {
            // TODO: Lógica de salvar
            Toast.makeText(this, "Alterações salvas!", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
            finish();
        });

        btnIniciarTreinoPreview.setOnClickListener(v -> {
            Intent intent = new Intent(EditarTreinoActivity.this, TreinoActivity.class);
            intent.putExtra("TREINO_ID", idDoTreino);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    // ... (O restante dos métodos: mostrarDialogoAdicionarExercicio,
    // mostrarDialogoEditarExercicio, configurarObservadores, etc.
    // permanecem OS MESMOS) ...
    private void mostrarDialogoAdicionarExercicio() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_adicionar_exercicio, null);
        EditText etNomeExercicio = dialogView.findViewById(R.id.etNomeExercicio);
        EditText etSeries = dialogView.findViewById(R.id.etSeries);
        EditText etRepeticoes = dialogView.findViewById(R.id.etRepeticoes);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String nome = etNomeExercicio.getText().toString().trim();
                    String seriesStr = etSeries.getText().toString().trim();
                    String repeticoesStr = etRepeticoes.getText().toString().trim();

                    if (nome.isEmpty() || seriesStr.isEmpty() || repeticoesStr.isEmpty()) {
                        Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        int series = Integer.parseInt(seriesStr);
                        int repeticoes = Integer.parseInt(repeticoesStr);
                        viewModel.adicionarExercicio(tokenDeAutenticacao, idDoTreino, nome, series, repeticoes);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Séries e repetições devem ser números", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditarExercicio(Exercicio exercicioParaEditar) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_adicionar_exercicio, null);
        TextView tvTituloDialog = dialogView.findViewById(R.id.tvTituloDialog);
        EditText etNomeExercicio = dialogView.findViewById(R.id.etNomeExercicio);
        EditText etSeries = dialogView.findViewById(R.id.etSeries);
        EditText etRepeticoes = dialogView.findViewById(R.id.etRepeticoes);

        tvTituloDialog.setText("Editar Exercício");
        etNomeExercicio.setText(exercicioParaEditar.getNome());
        etSeries.setText(String.valueOf(exercicioParaEditar.getSeries()));
        etRepeticoes.setText(String.valueOf(exercicioParaEditar.getRepeticoes()));

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String nome = etNomeExercicio.getText().toString().trim();
                    String seriesStr = etSeries.getText().toString().trim();
                    String repeticoesStr = etRepeticoes.getText().toString().trim();

                    if (nome.isEmpty() || seriesStr.isEmpty() || repeticoesStr.isEmpty()) {
                        Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        int series = Integer.parseInt(seriesStr);
                        int repeticoes = Integer.parseInt(repeticoesStr);
                        viewModel.editarExercicio(tokenDeAutenticacao, exercicioParaEditar.getId(), nome, series, repeticoes);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Séries e repetições devem ser números", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void configurarObservadores() {
        viewModel.getTreino().observe(this, new Observer<Treino>() {
            @Override
            public void onChanged(Treino treino) {
                if (treino != null) {
                    etNomeTreino.setText(treino.getNome());
                    actvDiaSemana.setText(treino.getDiaSemana(), false);
                    exercicioAdapter.atualizarLista(treino.getExercicios());
                }
            }
        });

        viewModel.getEstaCarregando().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean estaCarregando) {
                if (barraDeProgresso != null) {
                    barraDeProgresso.setVisibility(estaCarregando ? View.VISIBLE : View.GONE);
                }
            }
        });

        viewModel.getErro().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String erro) {
                Toast.makeText(EditarTreinoActivity.this, erro, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getExercicioAdicionado().observe(this, new Observer<Exercicio>() {
            @Override
            public void onChanged(Exercicio exercicio) {
                Toast.makeText(EditarTreinoActivity.this, "Exercício '" + exercicio.getNome() + "' salvo!", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIdExercicioDeletado().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long exercicioId) {
                Toast.makeText(EditarTreinoActivity.this, "Exercício deletado", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getExercicioAtualizado().observe(this, new Observer<Exercicio>() {
            @Override
            public void onChanged(Exercicio exercicio) {
                Toast.makeText(EditarTreinoActivity.this, "Exercício '" + exercicio.getNome() + "' atualizado!", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getTreinoDeletado().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean deletado) {
                if (deletado) {
                    Toast.makeText(EditarTreinoActivity.this, "Treino deletado com sucesso", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        });
    }
}