package com.biblioteca.modelos;

import java.math.BigDecimal;
import java.sql.Date;

public class Devolucion {
    private int id;
    private int idPrestamo;
    private String idUsuario;
    private String idDocumento;
    private Date fechaDevolucionReal;
    private int diasMora;
    private BigDecimal montoMora;

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(String idDocumento) {
        this.idDocumento = idDocumento;
    }

    public Date getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }

    public void setFechaDevolucionReal(Date fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
    }

    public int getDiasMora() {
        return diasMora;
    }

    public void setDiasMora(int diasMora) {
        this.diasMora = diasMora;
    }

    public BigDecimal getMontoMora() {
        return montoMora;
    }

    public void setMontoMora(BigDecimal montoMora) {
        this.montoMora = montoMora;
    }
}
