package com.example.fortis.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * DTO (Data Transfer Object) usado para RECEBER os dados do histórico
 * da API (GET /historico/progresso/{id}).
 */
public class HistoricoResponseDTO {

    @SerializedName("idHistorico")
    private Long idHistorico;

    @SerializedName("dataExecucao")
    private String dataExecucao; // Recebemos como String (ex: "2025-11-15")

    @SerializedName("carga")
    private double carga;

    @SerializedName("repeticoesFeitas")
    private int repeticoesFeitas;

    @SerializedName("serieNumero")
    private int serieNumero;

    // Getters e Setters (Necessários para o Gson e para o ViewModel)
    public Long getIdHistorico() {
        return idHistorico;
    }

    public void setIdHistorico(Long idHistorico) {
        this.idHistorico = idHistorico;
    }

    public String getDataExecucao() {
        return dataExecucao;
    }

    public void setDataExecucao(String dataExecucao) {
        this.dataExecucao = dataExecucao;
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