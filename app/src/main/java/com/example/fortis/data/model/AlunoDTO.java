package com.example.fortis.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * POJO (Modelo) que espelha o AlunoDTO do backend.
 * Usado pelo Retrofit para desserializar a resposta do endpoint /aluno/perfil.
 */
public class AlunoDTO {

    // --- Dados do Aluno ---
    @SerializedName("idAluno")
    private Long idAluno;

    @SerializedName("nome")
    private String nome;

    @SerializedName("cpf")
    private String cpf;

    @SerializedName("dataNascimento")
    private Date dataNascimento;

    @SerializedName("telefone")
    private String telefone;

    @SerializedName("email")
    private String email;

    // --- 1. DADOS DE ENDEREÇO (ADICIONADOS) ---
    @SerializedName("cep")
    private String cep;

    @SerializedName("rua")
    private String rua;

    @SerializedName("cidade")
    private String cidade;

    @SerializedName("estado")
    private String estado;

    // --- Dados da Matrícula e Plano ---
    @SerializedName("nomePlano")
    private String nomePlano;

    @SerializedName("valorPlano")
    private Double valorPlano;

    @SerializedName("statusMatricula")
    private String statusMatricula;

    @SerializedName("dataVencimentoMatricula")
    private Date dataVencimentoMatricula;

    // --- Getters e Setters ---

    public Long getIdAluno() { return idAluno; }
    public void setIdAluno(Long idAluno) { this.idAluno = idAluno; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public Date getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(Date dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // --- 2. GETTERS E SETTERS DE ENDEREÇO (ADICIONADOS) ---
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getRua() { return rua; }
    public void setRua(String rua) { this.rua = rua; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // --- Getters/Setters do Plano ---
    public String getNomePlano() { return nomePlano; }
    public void setNomePlano(String nomePlano) { this.nomePlano = nomePlano; }

    public Double getValorPlano() { return valorPlano; }
    public void setValorPlano(Double valorPlano) { this.valorPlano = valorPlano; }

    public String getStatusMatricula() { return statusMatricula; }
    public void setStatusMatricula(String statusMatricula) { this.statusMatricula = statusMatricula; }

    public Date getDataVencimentoMatricula() { return dataVencimentoMatricula; }
    public void setDataVencimentoMatricula(Date dataVencimentoMatricula) { this.dataVencimentoMatricula = dataVencimentoMatricula; }
}