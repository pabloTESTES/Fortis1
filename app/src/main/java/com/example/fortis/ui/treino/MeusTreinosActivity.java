package com.example.fortis.ui.treino;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fortis.R;
import com.example.fortis.data.SessionManager;
import com.example.fortis.data.model.Treino;
import com.example.fortis.ui.configuracoes.ConfiguracoesActivity;
import com.example.fortis.ui.editar_treino.EditarTreinoActivity;
import com.example.fortis.ui.home.HomeActivity;
import com.example.fortis.ui.login.LoginActivity;
import com.example.fortis.ui.perfil.PerfilActivity;
import com.example.fortis.ui.suporte.AjudaActivity;
import com.example.fortis.viewmodel.treino.MeusTreinosViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

/**
 * Activity que exibe a lista de "Meus Treinos" (Ficha de Treinos)
 * e implementa a interface de clique do Adapter.
 */
public class MeusTreinosActivity extends AppCompatActivity implements TreinoAdapter.OnTreinoClickListener {

    private static final int EDITAR_TREINO_REQUEST = 101;
    private static final int CONFIGURACOES_REQUEST = 103;

    private MeusTreinosViewModel viewModel;
    private SessionManager sessionManager;

    // Componentes da UI (do seu XML)
    private RecyclerView rvTreinos;
    private TreinoAdapter adapter;
    private ProgressBar progressBarMeusTreinos;
    private ImageButton ibSairSistema;
    private ImageButton ibConfiguracao;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_treinos); // Layout da Ação 2

        sessionManager = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(MeusTreinosViewModel.class);

        associarViews();
        configurarBotoesHeader();
        configurarRecyclerView();
        configurarObservadores();
        configurarBottomNavigation();

        // Pede ao ViewModel para carregar a lista de treinos
        viewModel.buscarTreinos();
    }

    private void associarViews() {
        // IDs do 'activity_meus_treinos.xml'
        rvTreinos = findViewById(R.id.rvTreinos);
        progressBarMeusTreinos = findViewById(R.id.progressBarMeusTreinos);
        ibSairSistema = findViewById(R.id.ibSairSistema);
        ibConfiguracao = findViewById(R.id.ibConfiguracao);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void configurarRecyclerView() {
        // Inicia o Adapter com uma lista vazia
        // 'this' (a própria Activity) é o listener de cliques
        adapter = new TreinoAdapter(new ArrayList<>(), this);
        rvTreinos.setLayoutManager(new LinearLayoutManager(this));
        rvTreinos.setAdapter(adapter);
    }

    private void configurarBotoesHeader() {
        ibConfiguracao.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConfiguracoesActivity.class);
            startActivityForResult(intent, CONFIGURACOES_REQUEST);
        });

        ibSairSistema.setOnClickListener(v -> {
            mostrarDialogoLogout();
        });
    }

    private void configurarObservadores() {
        viewModel.getEstaCarregando().observe(this, isLoading -> {
            progressBarMeusTreinos.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErro().observe(this, erro -> {
            if (erro != null) {
                Toast.makeText(this, erro, Toast.LENGTH_LONG).show();
            }
        });

        // Observador principal: recebe a lista de treinos
        viewModel.getTreinos().observe(this, treinos -> {
            if (treinos != null) {
                // Envia a lista para o Adapter
                adapter.setTreinos(treinos);
            }
        });
    }

    // --- Implementação dos Cliques do Adapter ---

    /**
     * Ocorre quando o utilizador clica no corpo do item (para EDITAR).
     * (Implementação da interface 'TreinoAdapter.OnTreinoClickListener')
     */
    @Override
    public void onItemClick(Treino treino) {
        Intent intent = new Intent(this, EditarTreinoActivity.class);
        intent.putExtra("TREINO_ID", treino.getId());
        startActivityForResult(intent, EDITAR_TREINO_REQUEST);
    }

    /**
     * Ocorre quando o utilizador clica no botão "VER TREINO" (Play).
     * (Implementação da interface 'TreinoAdapter.OnTreinoClickListener')
     */
    @Override
    public void onVerTreinoClick(Treino treino) {
        Intent intent = new Intent(this, TreinoActivity.class);
        intent.putExtra("TREINO_ID", treino.getId());
        startActivity(intent);
    }

    // --- Navegação ---

    /**
     * Ouve o resultado das Activities que iniciámos (EditarTreino e Configurações)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Se o utilizador salvou algo na tela de Edição de Treino OU na Edição de Perfil
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == EDITAR_TREINO_REQUEST || requestCode == CONFIGURACOES_REQUEST) {
                // Recarrega os dados para refletir as alterações
                Toast.makeText(this, "Atualizando dados...", Toast.LENGTH_SHORT).show();
                viewModel.buscarTreinos();
            }
        }
    }

    private void configurarBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_ficha_treinos);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_ficha_treinos) {
                return true;
            } else if (itemId == R.id.nav_perfil) {
                Intent intent = new Intent(this, PerfilActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.navigation_treino_do_dia) {
                Intent intent = new Intent(this, TreinoDoDiaRouterActivity.class);
                startActivity(intent);
                return true;

                // --- CORREÇÃO APLICADA AQUI ---
            } else if (itemId == R.id.nav_chat_suporte) {
                Intent intent = new Intent(this, AjudaActivity.class);
                startActivity(intent);
                finish(); // Fecha "Meus Treinos"
                return true;
                // --- FIM DA CORREÇÃO ---
            }

            return false;
        });
    }

    private void mostrarDialogoLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Sair da Conta")
                .setMessage("Tem certeza que deseja se desconectar?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    sessionManager.clearSession();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Não", null)
                .show();
    }
}