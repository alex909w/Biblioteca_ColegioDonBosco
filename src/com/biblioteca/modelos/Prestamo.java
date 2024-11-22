package com.biblioteca.modelos;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * Modelo para la entidad Prestamo.
 */
public class Prestamo {
    private int id;
    private String idUsuario;
    private String idDocumento;
    private double moraDiaria;
    private Date fechaPrestamo;
    private Date fechaDevolucion;
    private Date fechaDevolucionProgramada;
    private String estado;
    private int diasMora;
    private BigDecimal montoMora;

    // Constructor vacío
    public Prestamo() {
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public double getMoraDiaria() {
        return moraDiaria;
    }

    public void setMoraDiaria(double moraDiaria) {
        this.moraDiaria = moraDiaria;
    }

    public Date getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(Date fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public Date getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(Date fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public Date getFechaDevolucionProgramada() {
        return fechaDevolucionProgramada;
    }

    public void setFechaDevolucionProgramada(Date fechaDevolucionProgramada) {
        this.fechaDevolucionProgramada = fechaDevolucionProgramada;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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
