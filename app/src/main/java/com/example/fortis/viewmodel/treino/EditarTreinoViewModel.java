package com.example.fortis.viewmodel.treino;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fortis.data.SessionManager;
import com.example.fortis.data.api.ApiService;
import com.example.fortis.data.api.RetrofitClient;
import com.example.fortis.data.model.Exercicio;
import com.example.fortis.data.model.ExercicioDTO;
import com.example.fortis.data.model.Treino;
import com.example.fortis.data.model.TreinoDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarTreinoViewModel extends AndroidViewModel {

    private final ApiService apiService;

    // LiveData
    private final MutableLiveData<Boolean> _estaCarregando = new MutableLiveData<>();
    public LiveData<Boolean> getEstaCarregando() { return _estaCarregando; }

    private final MutableLiveData<String> _erro = new MutableLiveData<>();
    public LiveData<String> getErro() { return _erro; }

    private final MutableLiveData<Treino> _treino = new MutableLiveData<>();
    public LiveData<Treino> getTreino() { return _treino; }

    private final MutableLiveData<Boolean> _treinoDeletado = new MutableLiveData<>(false);
    public LiveData<Boolean> getTreinoDeletado() { return _treinoDeletado; }

    private final MutableLiveData<Exercicio> _exercicioAdicionado = new MutableLiveData<>();
    public LiveData<Exercicio> getExercicioAdicionado() { return _exercicioAdicionado; }

    private final MutableLiveData<Long> _idExercicioDeletado = new MutableLiveData<>();
    public LiveData<Long> getIdExercicioDeletado() { return _idExercicioDeletado; }

    private final MutableLiveData<Exercicio> _exercicioAtualizado = new MutableLiveData<>();
    public LiveData<Exercicio> getExercicioAtualizado() { return _exercicioAtualizado; }

    public EditarTreinoViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getApiService();
    }

    // Método usa 'getTreinoCompleto' (Corrigido na Etapa 31)
    public void buscarTreinoPorId(String token, long treinoId) {
        _estaCarregando.setValue(true);
        apiService.getTreinoCompleto("Bearer " + token, treinoId).enqueue(new Callback<TreinoDTO>() {
            @Override
            public void onResponse(Call<TreinoDTO> call, Response<TreinoDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _treino.setValue(converterParaTreino(response.body()));
                } else {
                    _erro.setValue("Falha ao buscar treino: " + response.code());
                }
                _estaCarregando.setValue(false);
            }

            @Override
            public void onFailure(Call<TreinoDTO> call, Throwable t) {
                _erro.setValue("Erro de rede: " + t.getMessage());
                _estaCarregando.setValue(false);
            }
        });
    }

    public void deletarTreino(String token, long treinoId) {
        _estaCarregando.setValue(true);
        apiService.deletarTreino("Bearer " + token, treinoId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    _treinoDeletado.setValue(true);
                } else {
                    _erro.setValue("Falha ao deletar treino: " + response.code());
                }
                _estaCarregando.setValue(false);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                _erro.setValue("Erro de rede: " + t.getMessage());
                _estaCarregando.setValue(false);
            }
        });
    }

    /**
     * Adiciona um novo exercício a este treino.
     */
    public void adicionarExercicio(String token, long treinoId, String nome, int series, int repeticoes) {

        ExercicioDTO dto = new ExercicioDTO();
        dto.setId(0L); // O ID é 0 para criação
        dto.setNome(nome);
        dto.setSeries(series);
        dto.setRepeticoes(repeticoes);
        // --- FIM DA CORREÇÃO ---

        apiService.adicionarExercicio("Bearer " + token, treinoId, dto).enqueue(new Callback<ExercicioDTO>() {
            @Override
            public void onResponse(Call<ExercicioDTO> call, Response<ExercicioDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Treino treinoAtual = _treino.getValue();
                    if (treinoAtual != null) {
                        treinoAtual.getExercicios().add(converterParaExercicio(response.body()));
                        _treino.setValue(treinoAtual);
                        _exercicioAdicionado.setValue(converterParaExercicio(response.body()));
                    }
                } else {
                    _erro.setValue("Falha ao adicionar exercício.");
                }
            }
            @Override
            public void onFailure(Call<ExercicioDTO> call, Throwable t) {
                _erro.setValue("Erro de rede: " + t.getMessage());
            }
        });
    }

    public void deletarExercicio(String token, long exercicioId) {
        apiService.deletarExercicio("Bearer " + token, exercicioId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Treino treinoAtual = _treino.getValue();
                    if (treinoAtual != null) {
                        treinoAtual.getExercicios().removeIf(ex -> ex.getId() == exercicioId);
                        _treino.setValue(treinoAtual);
                        _idExercicioDeletado.setValue(exercicioId);
                    }
                } else {
                    _erro.setValue("Falha ao deletar exercício.");
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                _erro.setValue("Erro de rede: " + t.getMessage());
            }
        });
    }

    /**
     * Atualiza um exercício existente.
     */
    public void editarExercicio(String token, long exercicioId, String nome, int series, int repeticoes) {

        // --- CORREÇÃO AQUI (Método editarExercicio) ---
        // Usando o construtor vazio e setters
        ExercicioDTO dto = new ExercicioDTO();
        dto.setId(exercicioId);
        dto.setNome(nome);
        dto.setSeries(series);
        dto.setRepeticoes(repeticoes);
        // --- FIM DA CORREÇÃO ---

        apiService.atualizarExercicio("Bearer " + token, exercicioId, dto).enqueue(new Callback<ExercicioDTO>() {
            @Override
            public void onResponse(Call<ExercicioDTO> call, Response<ExercicioDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Treino treinoAtual = _treino.getValue();
                    Exercicio exercicioAtualizado = converterParaExercicio(response.body());
                    if (treinoAtual != null) {
                        for (int i = 0; i < treinoAtual.getExercicios().size(); i++) {
                            if (treinoAtual.getExercicios().get(i).getId() == exercicioId) {
                                treinoAtual.getExercicios().set(i, exercicioAtualizado);
                                break;
                            }
                        }
                        _treino.setValue(treinoAtual);
                        _exercicioAtualizado.setValue(exercicioAtualizado);
                    }
                } else {
                    _erro.setValue("Falha ao atualizar exercício.");
                }
            }
            @Override
            public void onFailure(Call<ExercicioDTO> call, Throwable t) {
                _erro.setValue("Erro de rede: " + t.getMessage());
            }
        });
    }

    // --- Métodos de Conversão (DTO -> Modelo) ---
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