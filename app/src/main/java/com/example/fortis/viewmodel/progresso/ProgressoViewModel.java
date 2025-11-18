package com.example.fortis.viewmodel.progresso;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fortis.data.api.ApiService;
import com.example.fortis.data.api.RetrofitClient;
import com.example.fortis.data.model.Exercicio;
import com.example.fortis.data.model.ExercicioDTO;
// --- 1. IMPORTAR O DTO DE RESPOSTA ---
import com.example.fortis.data.model.HistoricoResponseDTO;
import com.example.fortis.data.model.Treino;
import com.example.fortis.data.model.TreinoDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgressoViewModel extends AndroidViewModel {

    private final ApiService apiService;

    // LiveData para o SPINNER (Lista de exercícios)
    private final MutableLiveData<Boolean> _estaCarregandoSpinner = new MutableLiveData<>(true);
    public LiveData<Boolean> getEstaCarregandoSpinner() { return _estaCarregandoSpinner; }

    private final MutableLiveData<String> _erroSpinner = new MutableLiveData<>();
    public LiveData<String> getErroSpinner() { return _erroSpinner; }

    private final MutableLiveData<List<Exercicio>> _listaExercicios = new MutableLiveData<>();
    public LiveData<List<Exercicio>> getListaExercicios() { return _listaExercicios; }

    // --- 2. NOVOS LIVEDATA (Para o Gráfico) ---
    private final MutableLiveData<Boolean> _estaCarregandoGrafico = new MutableLiveData<>(false);
    public LiveData<Boolean> getEstaCarregandoGrafico() { return _estaCarregandoGrafico; }

    private final MutableLiveData<String> _erroGrafico = new MutableLiveData<>();
    public LiveData<String> getErroGrafico() { return _erroGrafico; }

    // (Este é o LiveData que a Etapa 72/76 irá consumir)
    private final MutableLiveData<List<HistoricoResponseDTO>> _historicoGrafico = new MutableLiveData<>();
    public LiveData<List<HistoricoResponseDTO>> getHistoricoGrafico() { return _historicoGrafico; }


    public ProgressoViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getApiService();
    }

    /**
     * Etapa 1: Busca o treino completo e extrai a lista de exercícios para o Spinner.
     */
    public void carregarExerciciosDoTreino(String token, long treinoId) {
        _estaCarregandoSpinner.setValue(true);

        apiService.getTreinoCompleto("Bearer " + token, treinoId).enqueue(new Callback<TreinoDTO>() {
            @Override
            public void onResponse(Call<TreinoDTO> call, Response<TreinoDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Treino treino = converterParaTreino(response.body());
                    if (treino != null && !treino.getExercicios().isEmpty()) {
                        _listaExercicios.setValue(treino.getExercicios());
                    } else {
                        _erroSpinner.setValue("Treino não encontrado ou sem exercícios.");
                    }
                } else {
                    _erroSpinner.setValue("Falha ao carregar exercícios: " + response.message());
                }
                _estaCarregandoSpinner.setValue(false);
            }

            @Override
            public void onFailure(Call<TreinoDTO> call, Throwable t) {
                _erroSpinner.setValue("Erro de rede: " + t.getMessage());
                _estaCarregandoSpinner.setValue(false);
            }
        });
    }

    /**
     * Etapa 2 (Esta Etapa): Busca o histórico de um exercício específico para o gráfico.
     */
    public void carregarDadosDoGrafico(String token, Long idExercicio) {
        if (idExercicio == null || idExercicio <= 0) {
            _erroGrafico.setValue("ID de exercício inválido.");
            return;
        }

        _estaCarregandoGrafico.setValue(true);
        _historicoGrafico.setValue(new ArrayList<>()); // Limpa o gráfico anterior

        // --- 3. CHAMADA REAL À API (Etapa 86) ---
        apiService.getHistoricoExercicio("Bearer " + token, idExercicio).enqueue(new Callback<List<HistoricoResponseDTO>>() {
            @Override
            public void onResponse(Call<List<HistoricoResponseDTO>> call, Response<List<HistoricoResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Envia os dados reais para a Activity
                    _historicoGrafico.setValue(response.body());
                } else {
                    _erroGrafico.setValue("Falha ao carregar dados do gráfico.");
                }
                _estaCarregandoGrafico.setValue(false);
            }

            @Override
            public void onFailure(Call<List<HistoricoResponseDTO>> call, Throwable t) {
                _erroGrafico.setValue("Erro de rede (gráfico): " + t.getMessage());
                _estaCarregandoGrafico.setValue(false);
            }
        });
    }

    // --- Métodos de Conversão (DTO -> Modelo) ---
    // (Necessários para a Etapa 1: carregarExerciciosDoTreino)

    private Treino converterParaTreino(TreinoDTO dto) {
        if (dto == null) return null;
        Treino treino = new Treino();
        treino.setId(dto.getId());
        treino.setNome(dto.getNome());
        treino.setDiaSemana(dto.getDiaSemana());

        if (dto.getExercicios() != null) {
            treino.setExercicios(dto.getExercicios().stream()
                    .map(this::converterParaExercicio)
                    .collect(Collectors.toList()));
        } else {
            treino.setExercicios(new ArrayList<>());
        }
        return treino;
    }

    private Exercicio converterParaExercicio(ExercicioDTO dto) {
        if (dto == null) return null;
        Exercicio exercicio = new Exercicio();
        exercicio.setId(dto.getId());
        exercicio.setNome(dto.getNome());
        exercicio.setSeries(dto.getSeries());
        exercicio.setRepeticoes(dto.getRepeticoes());
        return exercicio;
    }
}