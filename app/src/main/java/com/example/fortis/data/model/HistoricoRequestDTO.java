package com.example.fortis.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * DTO (Data Transfer Object) usado para ENVIAR os dados de uma série concluída
 * para a API (POST /historico/salvar).
 */
public class HistoricoRequestDTO {

    @SerializedName("idExercicio")
    private Long idExercicio;

    @SerializedName("carga")
    private double carga;

    @SerializedName("repeticoesFeitas")
    private int repeticoesFeitas;

    @SerializedName("serieNumero")
    private int serieNumero;

    // Construtor
    public HistoricoRequestDTO(Long idExercicio, double carga, int repeticoesFeitas, int serieNumero) {
        this.idExercicio = idExercicio;
        this.carga = carga;
        this.repeticoesFeitas = repeticoesFeitas;
        this.serieNumero = serieNumero;
    }

    // Getters e Setters (Necessários para o Gson)
    public Long getIdExercicio() {
        return idExercicio;
    }

    public void setIdExercicio(Long idExercicio) {
        this.idExercicio = idExercicio;
    }

    public double getCarga() {
        return carga;
    }

    public void setCarga(double carga) {
        this.carga = carga;
    }

    public int getRepeticoesFeitas() {
        return repeticoesFeitas;
    }

    public void setRepeticoesFeitas(int repeticoesFeitas) {
        this.repeticoesFeitas = repeticoesFeitas;
    }

    public int getSerieNumero() {
        return serieNumero;
    }

    public void setSerieNumero(int serieNumero) {
        this.serieNumero = serieNumero;
    }
}